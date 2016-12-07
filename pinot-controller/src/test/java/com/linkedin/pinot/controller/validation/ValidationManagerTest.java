/**
 * Copyright (C) 2014-2016 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.controller.validation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.helix.AccessOption;
import org.apache.helix.HelixAdmin;
import org.apache.helix.ZNRecord;
import org.apache.helix.manager.zk.ZkClient;
import org.apache.helix.model.IdealState;
import org.apache.helix.store.HelixPropertyStore;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.linkedin.pinot.common.config.AbstractTableConfig;
import com.linkedin.pinot.common.config.TableNameBuilder;
import com.linkedin.pinot.common.data.MetricFieldSpec;
import com.linkedin.pinot.common.data.Schema;
import com.linkedin.pinot.common.metadata.ZKMetadataProvider;
import com.linkedin.pinot.common.metadata.segment.OfflineSegmentZKMetadata;
import com.linkedin.pinot.common.metrics.ControllerMetrics;
import com.linkedin.pinot.common.metrics.ValidationMetrics;
import com.linkedin.pinot.common.segment.SegmentMetadata;
import com.linkedin.pinot.common.segment.StarTreeMetadata;
import com.linkedin.pinot.common.utils.CommonConstants;
import com.linkedin.pinot.common.utils.HLCSegmentName;
import com.linkedin.pinot.common.utils.LLCSegmentName;
import com.linkedin.pinot.common.utils.StringUtil;
import com.linkedin.pinot.common.utils.ZkStarter;
import com.linkedin.pinot.controller.ControllerConf;
import com.linkedin.pinot.controller.helix.ControllerRequestBuilderUtil;
import com.linkedin.pinot.controller.helix.core.PinotHelixResourceManager;
import com.linkedin.pinot.controller.helix.core.PinotHelixSegmentOnlineOfflineStateModelGenerator;
import com.linkedin.pinot.controller.helix.core.PinotTableIdealStateBuilder;
import com.linkedin.pinot.controller.helix.core.realtime.PinotLLCRealtimeSegmentManager;
import com.linkedin.pinot.core.segment.creator.impl.V1Constants;
import com.linkedin.pinot.core.segment.index.SegmentMetadataImpl;
import com.linkedin.pinot.core.startree.hll.HllConstants;
import com.yammer.metrics.core.MetricsRegistry;
import javax.annotation.Nullable;


/**
 * Tests for the ValidationManager.
 *
 */
public class ValidationManagerTest {

  private String HELIX_CLUSTER_NAME = "TestValidationManager";

  private String ZK_STR = ZkStarter.DEFAULT_ZK_STR;
  private String CONTROLLER_INSTANCE_NAME = "localhost_11984";

  private ZkClient _zkClient;

  private PinotHelixResourceManager _pinotHelixResourceManager;
  private ZkStarter.ZookeeperInstance _zookeeperInstance;

  @BeforeTest
  public void setUp() throws Exception {
    _zookeeperInstance = ZkStarter.startLocalZkServer();
    _zkClient = new ZkClient(ZK_STR);

    _pinotHelixResourceManager =
        new PinotHelixResourceManager(ZK_STR, HELIX_CLUSTER_NAME, CONTROLLER_INSTANCE_NAME, null, 1000L, true, /*isUpdateStateModel=*/false);
    _pinotHelixResourceManager.start();
    PinotLLCRealtimeSegmentManager.create(_pinotHelixResourceManager, new ControllerConf(),
        new ControllerMetrics(new MetricsRegistry()));
  }

  @Test
  public void testPushTimePersistence() throws Exception {

    String testTableName = "testTable";

    Thread.sleep(1000);

    ControllerRequestBuilderUtil.addFakeDataInstancesToAutoJoinHelixCluster(HELIX_CLUSTER_NAME, ZK_STR, 2, true);
    ControllerRequestBuilderUtil.addFakeBrokerInstancesToAutoJoinHelixCluster(HELIX_CLUSTER_NAME, ZK_STR, 2, true);

    String OfflineTableConfigJson = ControllerRequestBuilderUtil.buildCreateOfflineTableJSON(testTableName, null, null,
        "timestamp", "millsSinceEpoch", "DAYS", "5", 2, "BalanceNumSegmentAssignmentStrategy").toString();
    AbstractTableConfig offlineTableConfig = AbstractTableConfig.init(OfflineTableConfigJson);
    _pinotHelixResourceManager.addTable(offlineTableConfig);

    DummyMetadata metadata = new DummyMetadata(testTableName);

    _pinotHelixResourceManager.addSegment(metadata, "http://dummy/");

    Thread.sleep(1000);

    OfflineSegmentZKMetadata offlineSegmentZKMetadata = ZKMetadataProvider.getOfflineSegmentZKMetadata(
        _pinotHelixResourceManager.getPropertyStore(), metadata.getTableName(), metadata.getName());

    SegmentMetadata fetchedMetadata = new SegmentMetadataImpl(offlineSegmentZKMetadata);
    long pushTime = fetchedMetadata.getPushTime();

    // Check that the segment has been pushed in the last 30 seconds
    Assert.assertTrue(System.currentTimeMillis() - pushTime < 30000);

    // Check that there is no refresh time
    Assert.assertEquals(fetchedMetadata.getRefreshTime(), Long.MIN_VALUE);

    // Refresh the segment
    metadata.setCrc("anotherfakecrc");
    _pinotHelixResourceManager.addSegment(metadata, "http://dummy/");

    Thread.sleep(1000);

    offlineSegmentZKMetadata = ZKMetadataProvider.getOfflineSegmentZKMetadata(
        _pinotHelixResourceManager.getPropertyStore(), metadata.getTableName(), metadata.getName());
    fetchedMetadata = new SegmentMetadataImpl(offlineSegmentZKMetadata);

    // Check that the segment still has the same push time
    Assert.assertEquals(fetchedMetadata.getPushTime(), pushTime);

    // Check that the refresh time is in the last 30 seconds
    Assert.assertTrue(System.currentTimeMillis() - fetchedMetadata.getRefreshTime() < 30000);

  }

  @Test
  public void testTotalDocumentCountOffline() throws Exception {

    // Create a bunch of dummy segments
    String testTableName = "TestTableTotalDocCountTest";
    DummyMetadata metadata1 = new DummyMetadata(testTableName, 10);
    DummyMetadata metadata2 = new DummyMetadata(testTableName, 20);
    DummyMetadata metadata3 = new DummyMetadata(testTableName, 30);

    // Add them to a list
    List<SegmentMetadata> segmentMetadataList = new ArrayList<SegmentMetadata>();
    segmentMetadataList.add(metadata1);
    segmentMetadataList.add(metadata2);
    segmentMetadataList.add(metadata3);

    Assert.assertEquals(ValidationManager.computeOfflineTotalDocumentInSegments(segmentMetadataList), 60);

  }

  @Test
  public void testTotalDocumentCountRealTime() throws Exception {

    // Create a bunch of dummy segments
    String testTableName = "TestTableTotalDocCountTest";
    final String group1 = testTableName + "_REALTIME_1466446700000_34";
    final String group2 = testTableName + "_REALTIME_1466446700000_17";
    String segmentName1 = new HLCSegmentName(group1, "0", "1").getSegmentName();
    String segmentName2 = new HLCSegmentName(group1, "0", "2").getSegmentName();
    String segmentName3 = new HLCSegmentName(group1, "0", "3").getSegmentName();
    String segmentName4 = new HLCSegmentName(group2, "0", "3").getSegmentName();

    DummyMetadata metadata1 = new DummyMetadata(testTableName, segmentName1, 10);
    DummyMetadata metadata2 = new DummyMetadata(testTableName, segmentName2, 20);
    DummyMetadata metadata3 = new DummyMetadata(testTableName, segmentName3, 30);

    // This should get ignored in the count as it belongs to a different group id
    DummyMetadata metadata4 = new DummyMetadata(testTableName, segmentName4, 20);

    // Add them to a list
    List<SegmentMetadata> segmentMetadataList = new ArrayList<SegmentMetadata>();
    segmentMetadataList.add(metadata1);
    segmentMetadataList.add(metadata2);
    segmentMetadataList.add(metadata3);
    segmentMetadataList.add(metadata4);

    Assert.assertEquals(ValidationManager.computeRealtimeTotalDocumentInSegments(segmentMetadataList, true), 60);

    // Now add some low level segment names
    String segmentName5 = new LLCSegmentName(testTableName, 1, 0, 1000).getSegmentName();
    String segmentName6 = new LLCSegmentName(testTableName, 2, 27, 10000).getSegmentName();
    DummyMetadata metadata5 = new DummyMetadata(testTableName, segmentName5, 10);
    DummyMetadata metadata6 = new DummyMetadata(testTableName, segmentName6, 5);
    segmentMetadataList.add(metadata5);
    segmentMetadataList.add(metadata6);

    // Only the LLC segments should get counted.
    Assert.assertEquals(ValidationManager.computeRealtimeTotalDocumentInSegments(segmentMetadataList, false), 15);
  }

  @AfterTest
  public void shutDown() {
    _pinotHelixResourceManager.stop();
    _zkClient.close();
    ZkStarter.stopLocalZkServer(_zookeeperInstance);
  }

  @Test
  public void testCountMissingSegments() {
    // Should not crash with an empty or one element arrays
    ValidationManager.countMissingSegments(new long[0], TimeUnit.DAYS);
    ValidationManager.countMissingSegments(new long[1], TimeUnit.DAYS);

    // Should be no missing segments on two consecutive days
    Assert.assertEquals(ValidationManager.countMissingSegments(new long[] { new DateTime(2014, 1, 1, 22, 0).toInstant()
        .getMillis(), new DateTime(2014, 1, 2, 22, 0).toInstant().getMillis(), }, TimeUnit.DAYS), 0);

    // Should be no missing segments on five consecutive days
    Assert
        .assertEquals(ValidationManager.countMissingSegments(
            new long[] { new DateTime(2014, 1, 1, 22, 0).toInstant().getMillis(), new DateTime(2014, 1, 2, 22, 0)
                .toInstant()
                .getMillis(), new DateTime(2014, 1, 3, 22, 0).toInstant().getMillis(), new DateTime(2014, 1, 4, 22, 0)
                    .toInstant().getMillis(), new DateTime(2014, 1, 5, 22, 0).toInstant().getMillis(), },
        TimeUnit.DAYS), 0);

    // Should be no missing segments on five consecutive days, even if the interval between them isn't exactly 24 hours
    Assert
        .assertEquals(ValidationManager.countMissingSegments(
            new long[] { new DateTime(2014, 1, 1, 22, 0).toInstant().getMillis(), new DateTime(2014, 1, 2, 21, 0)
                .toInstant()
                .getMillis(), new DateTime(2014, 1, 3, 23, 0).toInstant().getMillis(), new DateTime(2014, 1, 4, 21, 5)
                    .toInstant().getMillis(), new DateTime(2014, 1, 5, 22, 15).toInstant().getMillis(), },
        TimeUnit.DAYS), 0);

    // Should be no missing segments on five consecutive days, even if there is a duplicate segment
    Assert.assertEquals(ValidationManager.countMissingSegments(
        new long[] { new DateTime(2014, 1, 1, 22, 0).toInstant().getMillis(), new DateTime(2014, 1, 2, 21, 0)
            .toInstant().getMillis(), new DateTime(2014, 1, 3, 22, 0).toInstant()
                .getMillis(), new DateTime(2014, 1, 3, 23, 0).toInstant().getMillis(), new DateTime(2014, 1, 4, 21, 5)
                    .toInstant().getMillis(), new DateTime(2014, 1, 5, 22, 15).toInstant().getMillis(), },
        TimeUnit.DAYS), 0);

    // Should be exactly one missing segment
    Assert.assertEquals(
        ValidationManager.countMissingSegments(new long[] { new DateTime(2014, 1, 1, 22, 0).toInstant()
            .getMillis(), new DateTime(2014, 1, 2, 21, 0).toInstant().getMillis(), new DateTime(2014, 1, 4, 21, 5)
                .toInstant().getMillis(), new DateTime(2014, 1, 5, 22, 15).toInstant().getMillis(), },
            TimeUnit.DAYS),
        1);

    // Should be one missing segment, even if there is a duplicate segment
    Assert
        .assertEquals(ValidationManager.countMissingSegments(
            new long[] { new DateTime(2014, 1, 1, 22, 0).toInstant().getMillis(), new DateTime(2014, 1, 3, 22, 0)
                .toInstant()
                .getMillis(), new DateTime(2014, 1, 3, 23, 0).toInstant().getMillis(), new DateTime(2014, 1, 4, 21, 5)
                    .toInstant().getMillis(), new DateTime(2014, 1, 5, 22, 15).toInstant().getMillis(), },
        TimeUnit.DAYS), 1);

    // Should be two missing segments
    Assert
        .assertEquals(
            ValidationManager.countMissingSegments(
                new long[] { new DateTime(2014, 1, 1, 22, 0).toInstant().getMillis(), new DateTime(2014, 1, 3, 23, 0)
                    .toInstant().getMillis(), new DateTime(2014, 1, 5, 22, 15).toInstant().getMillis(), },
        TimeUnit.DAYS), 2);

    // Should be three missing segments
    Assert.assertEquals(ValidationManager.countMissingSegments(new long[] { new DateTime(2014, 1, 1, 22, 0).toInstant()
        .getMillis(), new DateTime(2014, 1, 5, 22, 15).toInstant().getMillis(), }, TimeUnit.DAYS), 3);

    // Should be three missing segments
    Assert.assertEquals(ValidationManager.countMissingSegments(new long[] { new DateTime(2014, 1, 1, 22, 25).toInstant()
        .getMillis(), new DateTime(2014, 1, 5, 22, 15).toInstant().getMillis(), }, TimeUnit.DAYS), 3);
  }

  @Test
  public void testComputeMissingIntervals() {
    Interval jan1st = new Interval(new DateTime(2015, 1, 1, 0, 0, 0), new DateTime(2015, 1, 1, 23, 59, 59));
    Interval jan2nd = new Interval(new DateTime(2015, 1, 2, 0, 0, 0), new DateTime(2015, 1, 2, 23, 59, 59));
    Interval jan3rd = new Interval(new DateTime(2015, 1, 3, 0, 0, 0), new DateTime(2015, 1, 3, 23, 59, 59));
    Interval jan4th = new Interval(new DateTime(2015, 1, 4, 0, 0, 0), new DateTime(2015, 1, 4, 23, 59, 59));
    Interval jan5th = new Interval(new DateTime(2015, 1, 5, 0, 0, 0), new DateTime(2015, 1, 5, 23, 59, 59));

    ArrayList<Interval> jan1st2nd3rd = new ArrayList<Interval>();
    jan1st2nd3rd.add(jan1st);
    jan1st2nd3rd.add(jan2nd);
    jan1st2nd3rd.add(jan3rd);
    List<Interval> missingIntervalsForJan1st2nd3rd =
        ValidationManager.computeMissingIntervals(jan1st2nd3rd, Duration.standardDays(1));

    Assert.assertTrue(missingIntervalsForJan1st2nd3rd.isEmpty());

    ArrayList<Interval> jan1st2nd3rd5th = new ArrayList<Interval>(jan1st2nd3rd);
    jan1st2nd3rd5th.add(jan5th);
    List<Interval> missingIntervalsForJan1st2nd3rd5th =
        ValidationManager.computeMissingIntervals(jan1st2nd3rd5th, Duration.standardDays(1));

    Assert.assertEquals(missingIntervalsForJan1st2nd3rd5th.size(), 1);

    // Should also work if the intervals are in random order
    ArrayList<Interval> jan5th2nd1st = new ArrayList<Interval>();
    jan5th2nd1st.add(jan5th);
    jan5th2nd1st.add(jan2nd);
    jan5th2nd1st.add(jan1st);
    List<Interval> missingIntervalsForJan5th2nd1st =
        ValidationManager.computeMissingIntervals(jan5th2nd1st, Duration.standardDays(1));
    Assert.assertEquals(missingIntervalsForJan5th2nd1st.size(), 2);

    // Should also work if the intervals are of different sizes
    Interval jan1stAnd2nd = new Interval(new DateTime(2015, 1, 1, 0, 0, 0), new DateTime(2015, 1, 2, 23, 59, 59));
    ArrayList<Interval> jan1st2nd4th5th = new ArrayList<Interval>();
    jan1st2nd4th5th.add(jan1stAnd2nd);
    jan1st2nd4th5th.add(jan4th);
    jan1st2nd4th5th.add(jan5th);
    List<Interval> missingIntervalsForJan1st2nd4th5th =
        ValidationManager.computeMissingIntervals(jan1st2nd4th5th, Duration.standardDays(1));
    Assert.assertEquals(missingIntervalsForJan1st2nd4th5th.size(), 1);
  }

  @Test
  public void testExtremeSenario() {
    List<Interval> intervals = new ArrayList<Interval>();
    intervals.add(new Interval(1, 2));
    intervals.add(new Interval(Integer.MAX_VALUE - 5, Integer.MAX_VALUE));
    intervals.add(new Interval(Integer.MAX_VALUE / 2 - 5, Integer.MAX_VALUE / 2));
    Duration frequency = new Duration(1);

    List<Interval> computeMissingIntervals = ValidationManager.computeMissingIntervals(intervals, frequency);
    Assert.assertEquals(computeMissingIntervals.size(), 22);
  }

  @Test
  public void testLLCValidation() throws Exception {
    final String topicName = "topic";
    final int kafkaPartitionCount = 2;
    final String realtimeTableName = "table_REALTIME";
    final String tableName = TableNameBuilder.extractRawTableName(realtimeTableName);
    final String S1 = "S1"; // Server 1
    final String S2 = "S2"; // Server 2
    final String S3 = "S3"; // Server 3
    final List<String> hosts = Arrays.asList(new String[]{S1, S2, S3});
    final HelixPropertyStore propertyStore = _pinotHelixResourceManager.getPropertyStore();
    final HelixAdmin helixAdmin = _pinotHelixResourceManager.getHelixAdmin();

    ZNRecord znRecord = new ZNRecord(topicName);
    for (int i = 0; i < kafkaPartitionCount; i++) {
      znRecord.setListField(Integer.toString(i), hosts);
    }
    final String path = ZKMetadataProvider.constructPropertyStorePathForKafkaPartitions(realtimeTableName);
    propertyStore.set(path, znRecord, AccessOption.PERSISTENT);

    long msSinceEpoch = 1540;

    LLCSegmentName p0s0 = new LLCSegmentName(tableName, 0, 0, msSinceEpoch);
    LLCSegmentName p0s1 = new LLCSegmentName(tableName, 0, 1, msSinceEpoch);
    LLCSegmentName p1s0 = new LLCSegmentName(tableName, 1, 0, msSinceEpoch);
    LLCSegmentName p1s1 = new LLCSegmentName(tableName, 1, 1, msSinceEpoch);

    IdealState idealstate = PinotTableIdealStateBuilder.buildEmptyIdealStateFor(realtimeTableName, 3, helixAdmin, HELIX_CLUSTER_NAME);
    idealstate.setPartitionState(p0s0.getSegmentName(), S1, PinotHelixSegmentOnlineOfflineStateModelGenerator.ONLINE_STATE);
    idealstate.setPartitionState(p0s0.getSegmentName(), S2, PinotHelixSegmentOnlineOfflineStateModelGenerator.ONLINE_STATE);
    idealstate.setPartitionState(p0s0.getSegmentName(), S3, PinotHelixSegmentOnlineOfflineStateModelGenerator.ONLINE_STATE);
//    idealstate.setPartitionState(p0s1.getSegmentName(), S1, PinotHelixSegmentOnlineOfflineStateModelGenerator.CONSUMING_STATE);
//    idealstate.setPartitionState(p0s1.getSegmentName(), S2, PinotHelixSegmentOnlineOfflineStateModelGenerator.CONSUMING_STATE);
//    idealstate.setPartitionState(p0s1.getSegmentName(), S3, PinotHelixSegmentOnlineOfflineStateModelGenerator.CONSUMING_STATE);
    idealstate.setPartitionState(p1s0.getSegmentName(), S1, PinotHelixSegmentOnlineOfflineStateModelGenerator.ONLINE_STATE);
    idealstate.setPartitionState(p1s0.getSegmentName(), S2, PinotHelixSegmentOnlineOfflineStateModelGenerator.ONLINE_STATE);
    idealstate.setPartitionState(p1s0.getSegmentName(), S3, PinotHelixSegmentOnlineOfflineStateModelGenerator.ONLINE_STATE);
    idealstate.setPartitionState(p1s1.getSegmentName(), S1, PinotHelixSegmentOnlineOfflineStateModelGenerator.CONSUMING_STATE);
    idealstate.setPartitionState(p1s1.getSegmentName(), S2, PinotHelixSegmentOnlineOfflineStateModelGenerator.CONSUMING_STATE);
    idealstate.setPartitionState(p1s1.getSegmentName(), S3, PinotHelixSegmentOnlineOfflineStateModelGenerator.CONSUMING_STATE);
    helixAdmin.addResource(HELIX_CLUSTER_NAME, realtimeTableName, idealstate);

    FakeValidationMetrics validationMetrics = new FakeValidationMetrics();

    ValidationManager validationManager = new ValidationManager(validationMetrics, _pinotHelixResourceManager, new ControllerConf());
    Map<String, String> streamConfigs = new HashMap<String, String>(4);
    streamConfigs.put(StringUtil.join(".", CommonConstants.Helix.DataSource.STREAM_PREFIX,
        CommonConstants.Helix.DataSource.Realtime.Kafka.CONSUMER_TYPE), "highLevel,simple");
//    streamConfigs.put(StringUtil.join(".", CommonConstants.Helix.DataSource.STREAM_PREFIX,
//        CommonConstants.Helix.DataSource.Realtime.Kafka.KAFKA_CONSUMER_PROPS_PREFIX,
//        "auto.offset.reset"), "smallest");
    Field autoCreateOnError = ValidationManager.class.getDeclaredField("_autoCreateOnError");
    autoCreateOnError.setAccessible(true);
    autoCreateOnError.setBoolean(validationManager, false);
    validationManager.validateLLCSegments(realtimeTableName, streamConfigs);

    Assert.assertEquals(validationMetrics.partitionCount, 1);

    // Set partition 0 to have one instance in CONSUMING state, and others in OFFLINE.
    // we should not flag any partitions to correct.
    helixAdmin.dropResource(HELIX_CLUSTER_NAME, realtimeTableName);
    idealstate.setPartitionState(p0s1.getSegmentName(), S1,
        PinotHelixSegmentOnlineOfflineStateModelGenerator.CONSUMING_STATE);
    idealstate.setPartitionState(p0s1.getSegmentName(), S2,
        PinotHelixSegmentOnlineOfflineStateModelGenerator.OFFLINE_STATE);
    idealstate.setPartitionState(p0s1.getSegmentName(), S3,
        PinotHelixSegmentOnlineOfflineStateModelGenerator.OFFLINE_STATE);
    helixAdmin.addResource(HELIX_CLUSTER_NAME, realtimeTableName, idealstate);
    validationManager.validateLLCSegments(realtimeTableName, streamConfigs);
    Assert.assertEquals(validationMetrics.partitionCount, 0);

    helixAdmin.dropResource(HELIX_CLUSTER_NAME, realtimeTableName);
  }

  private class FakeValidationMetrics extends ValidationMetrics {
    public int partitionCount = -1;

    public FakeValidationMetrics() {
      super(null);
    }
    @Override
    public void updateNumNonConsumingPartitionsMetric(final String tableName, final int count) {
      partitionCount = count;
    }
  }

  private class DummyMetadata implements SegmentMetadata {
    private String _resourceName;
    private String _indexType;
    private Duration _timeGranularity;
    private Interval _interval;
    private String _crc;
    private String _version;
    private Schema _schema;
    private String _shardingKey;
    private String _segmentName;
    private long _indexCreationTime;
    private long _pushTime;
    private long _refreshTime;
    private int _totalDocs;
    private String _indexDir;

    public DummyMetadata(String resourceName) {
      _resourceName = resourceName;
      _segmentName = resourceName + "_" + System.currentTimeMillis();
      _crc = System.currentTimeMillis() + "";
    }

    public DummyMetadata(String resourceName, int totalDocsInSegment) {
      _resourceName = resourceName;
      _segmentName = resourceName + "_" + System.currentTimeMillis();
      _crc = System.currentTimeMillis() + "";
      _totalDocs = totalDocsInSegment;
    }

    public DummyMetadata(String resourceName, String segmentName, int totalDocsInSegment) {
      _resourceName = resourceName;
      _segmentName = segmentName;
      _crc = System.currentTimeMillis() + "";
      _totalDocs = totalDocsInSegment;
    }

    @Override
    public String getIndexDir() {
      return _indexDir;
    }

    @Override
    public String getName() {
      return _segmentName;
    }

    @Override
    public Map<String, String> toMap() {
      return new HashMap<String, String>();
    }

    @Override
    public int getTotalDocs() {
      return _totalDocs;
    }

    @Override
    public int getTotalRawDocs() {
      return _totalDocs;
    }

    @Override
    public long getIndexCreationTime() {
      return _indexCreationTime;
    }

    @Override
    public long getPushTime() {
      return _pushTime;
    }

    @Override
    public long getRefreshTime() {
      return _refreshTime;
    }

    @Override
    public String getTableName() {
      return _resourceName;
    }

    @Override
    public String getIndexType() {
      return _indexType;
    }

    @Override
    public Duration getTimeGranularity() {
      return _timeGranularity;
    }

    @Override
    public Interval getTimeInterval() {
      return _interval;
    }

    @Override
    public String getCrc() {
      return _crc;
    }

    public void setCrc(String crc) {
      _crc = crc;
    }

    @Override
    public String getVersion() {
      return _version;
    }

    @Override
    public Schema getSchema() {
      return _schema;
    }

    @Override
    public String getShardingKey() {
      return _shardingKey;
    }

    @Override
    public boolean hasDictionary(String columnName) {
      return false;
    }

    @Override
    public boolean close() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean hasStarTree() {
      return false;
    }

    @Nullable
    @Override
    public StarTreeMetadata getStarTreeMetadata() {
      return null;
    }

    @Override
    public String getForwardIndexFileName(String column, String segmentVersion) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getDictionaryFileName(String column, String segmentVersion) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getBitmapInvertedIndexFileName(String column, String segmentVersion) {
      // TODO Auto-generated method stub
      return null;
    }

    @Nullable
    @Override
    public String getCreatorName() {
      return null;
    }

    @Override
    public char getPaddingCharacter() {
      return V1Constants.Str.DEFAULT_STRING_PAD_CHAR;
    }

    @Override
    public int getHllLog2m() {
      return HllConstants.DEFAULT_LOG2M;
    }

    @Nullable
    @Override
    public String getDerivedColumn(String column, MetricFieldSpec.DerivedMetricType derivedMetricType) {
      return null;
    }
  }
}
