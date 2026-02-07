/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class BaseDataMeta {
    DataType dataType;
    Object dataValue;
    private boolean isReference;

    public BaseDataMeta(DataType dataType, Object dataValue) {
        this.dataType = dataType;
        this.dataValue = dataValue;
        this.verifyDataMeta();
    }

    public DataType getDataType() {
        if (this.isReference) {
            return this.getReference().getDataType();
        }
        return this.dataType;
    }

    public Object getDataValue() {
        return this.dataValue;
    }

    public String getDataValueText() {
        if (this.dataValue == null) {
            return null;
        }
        if (DataType.DATATYPE_DATE == this.dataType) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date)this.dataValue);
        }
        if (DataType.DATATYPE_LIST == this.dataType) {
            StringBuffer buff = new StringBuffer("[");
            List col = (List)this.dataValue;
            for (Object o : col) {
                if (o == null) {
                    buff.append("null, ");
                    continue;
                }
                if (o instanceof Date) {
                    buff.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date)o)).append(", ");
                    continue;
                }
                buff.append(o.toString()).append(", ");
            }
            buff.append("]");
            if (buff.length() > 2) {
                buff.delete(buff.length() - 3, buff.length() - 1);
            }
            return buff.toString();
        }
        return this.dataValue.toString();
    }

    public String getStringValue() {
        return this.getDataValueText();
    }

    public Boolean getBooleanValue() {
        if (DataType.DATATYPE_BOOLEAN != this.dataType) {
            throw new UnsupportedOperationException("\u5f53\u524d\u5e38\u91cf\u7c7b\u578b\u4e0d\u652f\u6301\u6b64\u64cd\u4f5c");
        }
        return (Boolean)this.dataValue;
    }

    public Integer getIntegerValue() {
        if (DataType.DATATYPE_INT != this.dataType) {
            throw new UnsupportedOperationException("\u5f53\u524d\u5e38\u91cf\u7c7b\u578b\u4e0d\u652f\u6301\u6b64\u64cd\u4f5c");
        }
        return (Integer)this.dataValue;
    }

    public Long getLongValue() {
        if (DataType.DATATYPE_INT != this.dataType && DataType.DATATYPE_LONG != this.dataType) {
            throw new UnsupportedOperationException("\u5f53\u524d\u5e38\u91cf\u7c7b\u578b\u4e0d\u652f\u6301\u6b64\u64cd\u4f5c");
        }
        if (this.dataValue == null) {
            return null;
        }
        return Long.valueOf(this.dataValue.toString());
    }

    public Float getFloatValue() {
        if (DataType.DATATYPE_INT != this.dataType && DataType.DATATYPE_FLOAT != this.dataType && DataType.DATATYPE_LONG != this.dataType) {
            throw new UnsupportedOperationException("\u5f53\u524d\u5e38\u91cf\u7c7b\u578b\u4e0d\u652f\u6301\u6b64\u64cd\u4f5c");
        }
        if (this.dataValue == null) {
            return null;
        }
        return Float.valueOf(this.dataValue.toString());
    }

    public Double getDoubleValue() {
        if (DataType.DATATYPE_INT != this.dataType && DataType.DATATYPE_LONG != this.dataType && DataType.DATATYPE_FLOAT != this.dataType && DataType.DATATYPE_DOUBLE != this.dataType) {
            throw new UnsupportedOperationException("\u5f53\u524d\u5e38\u91cf\u7c7b\u578b\u4e0d\u652f\u6301\u6b64\u64cd\u4f5c");
        }
        if (this.dataValue == null) {
            return null;
        }
        return Double.valueOf(this.dataValue.toString());
    }

    public Date getDateValue() {
        if (DataType.DATATYPE_DATE != this.dataType) {
            throw new UnsupportedOperationException("\u5f53\u524d\u5e38\u91cf\u7c7b\u578b\u4e0d\u652f\u6301\u6b64\u64cd\u4f5c");
        }
        return (Date)this.dataValue;
    }

    public List<Object> getCollection() {
        if (DataType.DATATYPE_LIST != this.dataType) {
            throw new UnsupportedOperationException("\u5f53\u524d\u5e38\u91cf\u7c7b\u578b\u4e0d\u652f\u6301\u6b64\u64cd\u4f5c");
        }
        return (List)this.dataValue;
    }

    public Reference getReference() {
        if (!this.isReference) {
            throw new UnsupportedOperationException("\u5f53\u524d\u5e38\u91cf\u7c7b\u578b\u4e0d\u652f\u6301\u6b64\u64cd\u4f5c");
        }
        return (Reference)this.dataValue;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof BaseDataMeta) {
            BaseDataMeta bdo = (BaseDataMeta)o;
            if (this.isReference() && bdo.isReference) {
                return this.getReference() == bdo.getReference();
            }
            if (bdo.dataType == this.dataType) {
                if (bdo.dataValue != null && bdo.dataValue.equals(this.dataValue)) {
                    return true;
                }
                return bdo.dataValue == null && this.dataValue == null;
            }
            return false;
        }
        return false;
    }

    protected void verifyDataMeta() {
        if (this.dataType != null && this.dataValue != null) {
            if (DataType.DATATYPE_NULL == this.dataType && this.dataValue != null) {
                throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c\u4e0d\u4e3a\u7a7a");
            }
            if (DataType.DATATYPE_BOOLEAN == this.dataType) {
                try {
                    this.getBooleanValue();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (DataType.DATATYPE_DATE == this.dataType) {
                try {
                    this.getDateValue();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (DataType.DATATYPE_DOUBLE == this.dataType) {
                try {
                    this.getDoubleValue();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (DataType.DATATYPE_FLOAT == this.dataType) {
                try {
                    this.getFloatValue();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (DataType.DATATYPE_INT == this.dataType) {
                try {
                    this.getIntegerValue();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (DataType.DATATYPE_LONG == this.dataType) {
                try {
                    this.getLongValue();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (DataType.DATATYPE_STRING == this.dataType) {
                try {
                    this.getStringValue();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (DataType.DATATYPE_LIST == this.dataType) {
                try {
                    this.getCollection();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (this.isReference) {
                try {
                    this.getReference();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
            if (DataType.DATATYPE_OBJECT == this.dataType) {
                try {
                    this.getDataValue();
                }
                catch (UnsupportedOperationException e) {
                    throw new IllegalArgumentException("\u6570\u636e\u7c7b\u578b\u4e0d\u5339\u914d; \u7c7b\u578b\uff1a" + (Object)((Object)this.dataType) + ",\u503c:" + this.dataValue);
                }
            }
        }
    }

    public Class<?> mapTypeToJavaClass() {
        if (DataType.DATATYPE_BOOLEAN == this.getDataType()) {
            return Boolean.TYPE;
        }
        if (DataType.DATATYPE_DATE == this.getDataType()) {
            return Date.class;
        }
        if (DataType.DATATYPE_DOUBLE == this.getDataType()) {
            return Double.TYPE;
        }
        if (DataType.DATATYPE_FLOAT == this.getDataType()) {
            return Float.TYPE;
        }
        if (DataType.DATATYPE_INT == this.getDataType()) {
            return Integer.TYPE;
        }
        if (DataType.DATATYPE_LONG == this.getDataType()) {
            return Long.TYPE;
        }
        if (DataType.DATATYPE_STRING == this.getDataType()) {
            return String.class;
        }
        if (DataType.DATATYPE_LIST == this.getDataType()) {
            return List.class;
        }
        if (DataType.DATATYPE_OBJECT == this.getDataType()) {
            return Object.class;
        }
        if (DataType.DATATYPE_NULL == this.getDataType()) {
            return null;
        }
        throw new RuntimeException("\u6620\u5c04Java\u7c7b\u578b\u5931\u8d25\uff1a\u65e0\u6cd5\u8bc6\u522b\u7684\u6570\u636e\u7c7b\u578b");
    }

    private boolean isCompatibleType(BaseDataMeta another) {
        if (DataType.DATATYPE_NULL == this.getDataType() || DataType.DATATYPE_NULL == another.getDataType()) {
            return true;
        }
        if (this.getDataType() == another.getDataType()) {
            return true;
        }
        if (DataType.DATATYPE_INT != this.getDataType() && DataType.DATATYPE_LONG != this.getDataType() && DataType.DATATYPE_FLOAT != this.getDataType() && DataType.DATATYPE_DOUBLE != this.getDataType()) {
            return false;
        }
        return DataType.DATATYPE_INT == another.getDataType() || DataType.DATATYPE_LONG == another.getDataType() || DataType.DATATYPE_FLOAT == another.getDataType() || DataType.DATATYPE_DOUBLE == another.getDataType();
    }

    public DataType getCompatibleType(BaseDataMeta another) {
        if (this.isCompatibleType(another)) {
            if (DataType.DATATYPE_NULL == this.getDataType()) {
                return another.getDataType();
            }
            if (DataType.DATATYPE_NULL == another.getDataType()) {
                return this.getDataType();
            }
            if (this.getDataType() == another.getDataType()) {
                return this.getDataType();
            }
            if (DataType.DATATYPE_DOUBLE == this.getDataType() || DataType.DATATYPE_DOUBLE == another.getDataType()) {
                return DataType.DATATYPE_DOUBLE;
            }
            if (DataType.DATATYPE_FLOAT == this.getDataType() || DataType.DATATYPE_FLOAT == another.getDataType()) {
                return DataType.DATATYPE_FLOAT;
            }
            if (DataType.DATATYPE_LONG == this.getDataType() || DataType.DATATYPE_LONG == another.getDataType()) {
                return DataType.DATATYPE_LONG;
            }
            return DataType.DATATYPE_INT;
        }
        return null;
    }

    public Object toJavaObject() throws ParseException {
        if (null == this.dataValue) {
            return null;
        }
        if (DataType.DATATYPE_BOOLEAN == this.getDataType()) {
            return this.getBooleanValue();
        }
        if (DataType.DATATYPE_DATE == this.getDataType()) {
            return this.getDateValue();
        }
        if (DataType.DATATYPE_DOUBLE == this.getDataType()) {
            return this.getDoubleValue();
        }
        if (DataType.DATATYPE_FLOAT == this.getDataType()) {
            return this.getFloatValue();
        }
        if (DataType.DATATYPE_INT == this.getDataType()) {
            return this.getIntegerValue();
        }
        if (DataType.DATATYPE_LONG == this.getDataType()) {
            return this.getLongValue();
        }
        if (DataType.DATATYPE_STRING == this.getDataType()) {
            return this.getStringValue();
        }
        if (DataType.DATATYPE_LIST == this.getDataType()) {
            return this.getCollection();
        }
        if (DataType.DATATYPE_OBJECT == this.getDataType()) {
            return this.getDataValue();
        }
        throw new RuntimeException("\u6620\u5c04Java\u7c7b\u578b\u5931\u8d25\uff1a\u65e0\u6cd5\u8bc6\u522b\u7684\u6570\u636e\u7c7b\u578b");
    }

    public boolean isReference() {
        return this.isReference;
    }

    void setReference(boolean isReference) {
        this.isReference = isReference;
    }

    public static enum DataType {
        DATATYPE_NULL,
        DATATYPE_STRING,
        DATATYPE_BOOLEAN,
        DATATYPE_INT,
        DATATYPE_LONG,
        DATATYPE_FLOAT,
        DATATYPE_DOUBLE,
        DATATYPE_DATE,
        DATATYPE_LIST,
        DATATYPE_OBJECT;

    }
}
