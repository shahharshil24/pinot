/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.linkedin.pinot.common.request;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
/**
 * AUTO GENERATED: DO NOT EDIT
 *  Aggregation
 * 
 */
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-4-15")
public class AggregationInfo implements org.apache.thrift.TBase<AggregationInfo, AggregationInfo._Fields>, java.io.Serializable, Cloneable, Comparable<AggregationInfo> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("AggregationInfo");

  private static final org.apache.thrift.protocol.TField AGGREGATION_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("aggregationType", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField AGGREGATION_PARAMS_FIELD_DESC = new org.apache.thrift.protocol.TField("aggregationParams", org.apache.thrift.protocol.TType.MAP, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new AggregationInfoStandardSchemeFactory());
    schemes.put(TupleScheme.class, new AggregationInfoTupleSchemeFactory());
  }

  private String aggregationType; // optional
  private Map<String,String> aggregationParams; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    AGGREGATION_TYPE((short)1, "aggregationType"),
    AGGREGATION_PARAMS((short)2, "aggregationParams");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // AGGREGATION_TYPE
          return AGGREGATION_TYPE;
        case 2: // AGGREGATION_PARAMS
          return AGGREGATION_PARAMS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields.AGGREGATION_TYPE,_Fields.AGGREGATION_PARAMS};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.AGGREGATION_TYPE, new org.apache.thrift.meta_data.FieldMetaData("aggregationType", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.AGGREGATION_PARAMS, new org.apache.thrift.meta_data.FieldMetaData("aggregationParams", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(AggregationInfo.class, metaDataMap);
  }

  public AggregationInfo() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public AggregationInfo(AggregationInfo other) {
    if (other.isSetAggregationType()) {
      this.aggregationType = other.aggregationType;
    }
    if (other.isSetAggregationParams()) {
      Map<String,String> __this__aggregationParams = new HashMap<String,String>(other.aggregationParams);
      this.aggregationParams = __this__aggregationParams;
    }
  }

  public AggregationInfo deepCopy() {
    return new AggregationInfo(this);
  }

  @Override
  public void clear() {
    this.aggregationType = null;
    this.aggregationParams = null;
  }

  public String getAggregationType() {
    return this.aggregationType;
  }

  public void setAggregationType(String aggregationType) {
    this.aggregationType = aggregationType;
  }

  public void unsetAggregationType() {
    this.aggregationType = null;
  }

  /** Returns true if field aggregationType is set (has been assigned a value) and false otherwise */
  public boolean isSetAggregationType() {
    return this.aggregationType != null;
  }

  public void setAggregationTypeIsSet(boolean value) {
    if (!value) {
      this.aggregationType = null;
    }
  }

  public int getAggregationParamsSize() {
    return (this.aggregationParams == null) ? 0 : this.aggregationParams.size();
  }

  public void putToAggregationParams(String key, String val) {
    if (this.aggregationParams == null) {
      this.aggregationParams = new HashMap<String,String>();
    }
    this.aggregationParams.put(key, val);
  }

  public Map<String,String> getAggregationParams() {
    return this.aggregationParams;
  }

  public void setAggregationParams(Map<String,String> aggregationParams) {
    this.aggregationParams = aggregationParams;
  }

  public void unsetAggregationParams() {
    this.aggregationParams = null;
  }

  /** Returns true if field aggregationParams is set (has been assigned a value) and false otherwise */
  public boolean isSetAggregationParams() {
    return this.aggregationParams != null;
  }

  public void setAggregationParamsIsSet(boolean value) {
    if (!value) {
      this.aggregationParams = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case AGGREGATION_TYPE:
      if (value == null) {
        unsetAggregationType();
      } else {
        setAggregationType((String)value);
      }
      break;

    case AGGREGATION_PARAMS:
      if (value == null) {
        unsetAggregationParams();
      } else {
        setAggregationParams((Map<String,String>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case AGGREGATION_TYPE:
      return getAggregationType();

    case AGGREGATION_PARAMS:
      return getAggregationParams();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case AGGREGATION_TYPE:
      return isSetAggregationType();
    case AGGREGATION_PARAMS:
      return isSetAggregationParams();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof AggregationInfo)
      return this.equals((AggregationInfo)that);
    return false;
  }

  public boolean equals(AggregationInfo that) {
    if (that == null)
      return false;

    boolean this_present_aggregationType = true && this.isSetAggregationType();
    boolean that_present_aggregationType = true && that.isSetAggregationType();
    if (this_present_aggregationType || that_present_aggregationType) {
      if (!(this_present_aggregationType && that_present_aggregationType))
        return false;
      if (!this.aggregationType.equals(that.aggregationType))
        return false;
    }

    boolean this_present_aggregationParams = true && this.isSetAggregationParams();
    boolean that_present_aggregationParams = true && that.isSetAggregationParams();
    if (this_present_aggregationParams || that_present_aggregationParams) {
      if (!(this_present_aggregationParams && that_present_aggregationParams))
        return false;
      if (!this.aggregationParams.equals(that.aggregationParams))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_aggregationType = true && (isSetAggregationType());
    list.add(present_aggregationType);
    if (present_aggregationType)
      list.add(aggregationType);

    boolean present_aggregationParams = true && (isSetAggregationParams());
    list.add(present_aggregationParams);
    if (present_aggregationParams)
      list.add(aggregationParams);

    return list.hashCode();
  }

  @Override
  public int compareTo(AggregationInfo other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetAggregationType()).compareTo(other.isSetAggregationType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAggregationType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.aggregationType, other.aggregationType);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAggregationParams()).compareTo(other.isSetAggregationParams());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAggregationParams()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.aggregationParams, other.aggregationParams);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("AggregationInfo(");
    boolean first = true;

    if (isSetAggregationType()) {
      sb.append("aggregationType:");
      if (this.aggregationType == null) {
        sb.append("null");
      } else {
        sb.append(this.aggregationType);
      }
      first = false;
    }
    if (isSetAggregationParams()) {
      if (!first) sb.append(", ");
      sb.append("aggregationParams:");
      if (this.aggregationParams == null) {
        sb.append("null");
      } else {
        sb.append(this.aggregationParams);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class AggregationInfoStandardSchemeFactory implements SchemeFactory {
    public AggregationInfoStandardScheme getScheme() {
      return new AggregationInfoStandardScheme();
    }
  }

  private static class AggregationInfoStandardScheme extends StandardScheme<AggregationInfo> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, AggregationInfo struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // AGGREGATION_TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.aggregationType = iprot.readString();
              struct.setAggregationTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // AGGREGATION_PARAMS
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map26 = iprot.readMapBegin();
                struct.aggregationParams = new HashMap<String,String>(2*_map26.size);
                String _key27;
                String _val28;
                for (int _i29 = 0; _i29 < _map26.size; ++_i29)
                {
                  _key27 = iprot.readString();
                  _val28 = iprot.readString();
                  struct.aggregationParams.put(_key27, _val28);
                }
                iprot.readMapEnd();
              }
              struct.setAggregationParamsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, AggregationInfo struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.aggregationType != null) {
        if (struct.isSetAggregationType()) {
          oprot.writeFieldBegin(AGGREGATION_TYPE_FIELD_DESC);
          oprot.writeString(struct.aggregationType);
          oprot.writeFieldEnd();
        }
      }
      if (struct.aggregationParams != null) {
        if (struct.isSetAggregationParams()) {
          oprot.writeFieldBegin(AGGREGATION_PARAMS_FIELD_DESC);
          {
            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.aggregationParams.size()));
            for (Map.Entry<String, String> _iter30 : struct.aggregationParams.entrySet())
            {
              oprot.writeString(_iter30.getKey());
              oprot.writeString(_iter30.getValue());
            }
            oprot.writeMapEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class AggregationInfoTupleSchemeFactory implements SchemeFactory {
    public AggregationInfoTupleScheme getScheme() {
      return new AggregationInfoTupleScheme();
    }
  }

  private static class AggregationInfoTupleScheme extends TupleScheme<AggregationInfo> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, AggregationInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetAggregationType()) {
        optionals.set(0);
      }
      if (struct.isSetAggregationParams()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetAggregationType()) {
        oprot.writeString(struct.aggregationType);
      }
      if (struct.isSetAggregationParams()) {
        {
          oprot.writeI32(struct.aggregationParams.size());
          for (Map.Entry<String, String> _iter31 : struct.aggregationParams.entrySet())
          {
            oprot.writeString(_iter31.getKey());
            oprot.writeString(_iter31.getValue());
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, AggregationInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.aggregationType = iprot.readString();
        struct.setAggregationTypeIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TMap _map32 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.aggregationParams = new HashMap<String,String>(2*_map32.size);
          String _key33;
          String _val34;
          for (int _i35 = 0; _i35 < _map32.size; ++_i35)
          {
            _key33 = iprot.readString();
            _val34 = iprot.readString();
            struct.aggregationParams.put(_key33, _val34);
          }
        }
        struct.setAggregationParamsIsSet(true);
      }
    }
  }

}

