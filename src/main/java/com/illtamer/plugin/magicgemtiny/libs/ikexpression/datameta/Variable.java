/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta;

import java.util.Date;
import java.util.List;

public class Variable
extends BaseDataMeta {
    String variableName;

    public Variable(String variableName) {
        this(variableName, null, null);
    }

    public Variable(String variableName, BaseDataMeta.DataType variableDataType, Object variableValue) {
        super(variableDataType, variableValue);
        if (variableName == null) {
            throw new IllegalArgumentException("\u975e\u6cd5\u53c2\u6570\uff1a\u53d8\u91cf\u540d\u4e3a\u7a7a");
        }
        this.variableName = variableName;
    }

    public static Variable createVariable(String variableName, Object variableValue) {
        if (variableValue instanceof Boolean) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_BOOLEAN, variableValue);
        }
        if (variableValue instanceof Date) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_DATE, variableValue);
        }
        if (variableValue instanceof Double) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_DOUBLE, variableValue);
        }
        if (variableValue instanceof Float) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_FLOAT, variableValue);
        }
        if (variableValue instanceof Integer) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_INT, variableValue);
        }
        if (variableValue instanceof Long) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_LONG, variableValue);
        }
        if (variableValue instanceof String) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_STRING, variableValue);
        }
        if (variableValue instanceof List) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_LIST, variableValue);
        }
        if (variableValue instanceof Object) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_OBJECT, variableValue);
        }
        if (variableValue == null) {
            return new Variable(variableName, BaseDataMeta.DataType.DATATYPE_NULL, variableValue);
        }
        throw new IllegalArgumentException("\u975e\u6cd5\u53c2\u6570\uff1a\u65e0\u6cd5\u8bc6\u522b\u7684\u53d8\u91cf\u7c7b\u578b");
    }

    public String getVariableName() {
        return this.variableName;
    }

    public void setVariableValue(Object variableValue) {
        this.dataValue = variableValue;
        this.verifyDataMeta();
    }

    public void setDataType(BaseDataMeta.DataType dataType) {
        this.dataType = dataType;
        this.verifyDataMeta();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Variable && super.equals(o)) {
            Variable var = (Variable)o;
            return this.variableName != null && this.variableName.equals(var.variableName);
        }
        return false;
    }
}
