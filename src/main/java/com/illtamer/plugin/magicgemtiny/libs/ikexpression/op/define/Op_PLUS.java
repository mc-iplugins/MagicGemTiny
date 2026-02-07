/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.define;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.IllegalExpressionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.BaseDataMeta;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Reference;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.IOperatorExecution;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.Operator;

public class Op_PLUS
implements IOperatorExecution {
    public static final Operator THIS_OPERATOR = Operator.PLUS;

    @Override
    public Constant execute(Constant[] args) throws IllegalExpressionException {
        Number result;
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\u53c2\u6570\u4e2a\u6570\u4e0d\u5339\u914d");
        }
        Constant first = args[1];
        Constant second = args[0];
        if (first == null || second == null) {
            throw new NullPointerException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e3a\u7a7a");
        }
        if (first.isReference()) {
            Reference firstRef = (Reference)first.getDataValue();
            first = firstRef.execute();
        }
        if (second.isReference()) {
            Reference secondRef = (Reference)second.getDataValue();
            second = secondRef.execute();
        }
        if (BaseDataMeta.DataType.DATATYPE_LIST == first.getDataType() || BaseDataMeta.DataType.DATATYPE_LIST == second.getDataType()) {
            throw new IllegalArgumentException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u7c7b\u578b\u9519\u8bef");
        }
        if (BaseDataMeta.DataType.DATATYPE_STRING == first.getDataType() || BaseDataMeta.DataType.DATATYPE_STRING == second.getDataType() || BaseDataMeta.DataType.DATATYPE_NULL == first.getDataType() || BaseDataMeta.DataType.DATATYPE_NULL == second.getDataType() || BaseDataMeta.DataType.DATATYPE_BOOLEAN == first.getDataType() || BaseDataMeta.DataType.DATATYPE_BOOLEAN == second.getDataType() || BaseDataMeta.DataType.DATATYPE_DATE == first.getDataType() || BaseDataMeta.DataType.DATATYPE_DATE == second.getDataType()) {
            String firstString = "";
            String secondString = "";
            if (null != first.getStringValue()) {
                firstString = first.getStringValue();
            }
            if (null != second.getStringValue()) {
                secondString = second.getStringValue();
            }
            String result2 = firstString + secondString;
            return new Constant(BaseDataMeta.DataType.DATATYPE_STRING, result2);
        }
        if (null == first.getDataValue() || null == second.getDataValue()) {
            throw new NullPointerException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e3a\u7a7a");
        }
        if (BaseDataMeta.DataType.DATATYPE_DOUBLE == first.getDataType() || BaseDataMeta.DataType.DATATYPE_DOUBLE == second.getDataType()) {
            result = first.getDoubleValue() + second.getDoubleValue();
            return new Constant(BaseDataMeta.DataType.DATATYPE_DOUBLE, result);
        }
        if (BaseDataMeta.DataType.DATATYPE_FLOAT == first.getDataType() || BaseDataMeta.DataType.DATATYPE_FLOAT == second.getDataType()) {
            result = Float.valueOf(first.getFloatValue().floatValue() + second.getFloatValue().floatValue());
            return new Constant(BaseDataMeta.DataType.DATATYPE_FLOAT, result);
        }
        if (BaseDataMeta.DataType.DATATYPE_LONG == first.getDataType() || BaseDataMeta.DataType.DATATYPE_LONG == second.getDataType()) {
            result = first.getLongValue() + second.getLongValue();
            return new Constant(BaseDataMeta.DataType.DATATYPE_LONG, result);
        }
        result = first.getIntegerValue() + second.getIntegerValue();
        return new Constant(BaseDataMeta.DataType.DATATYPE_INT, result);
    }

    @Override
    public Constant verify(int opPositin, BaseDataMeta[] args) throws IllegalExpressionException {
        if (args == null) {
            throw new IllegalArgumentException("\u8fd0\u7b97\u64cd\u4f5c\u7b26\u53c2\u6570\u4e3a\u7a7a");
        }
        if (args.length != 2) {
            throw new IllegalExpressionException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e2a\u6570\u4e0d\u5339\u914d", THIS_OPERATOR.getToken(), opPositin);
        }
        BaseDataMeta first = args[1];
        BaseDataMeta second = args[0];
        if (first == null || second == null) {
            throw new NullPointerException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e3a\u7a7a");
        }
        if (BaseDataMeta.DataType.DATATYPE_LIST == first.getDataType() || BaseDataMeta.DataType.DATATYPE_LIST == second.getDataType()) {
            throw new IllegalExpressionException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u7c7b\u578b\u9519\u8bef", THIS_OPERATOR.getToken(), opPositin);
        }
        if (BaseDataMeta.DataType.DATATYPE_STRING == first.getDataType() || BaseDataMeta.DataType.DATATYPE_STRING == second.getDataType() || BaseDataMeta.DataType.DATATYPE_NULL == first.getDataType() || BaseDataMeta.DataType.DATATYPE_NULL == second.getDataType() || BaseDataMeta.DataType.DATATYPE_BOOLEAN == first.getDataType() || BaseDataMeta.DataType.DATATYPE_BOOLEAN == second.getDataType() || BaseDataMeta.DataType.DATATYPE_DATE == first.getDataType() || BaseDataMeta.DataType.DATATYPE_DATE == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_STRING, null);
        }
        if (BaseDataMeta.DataType.DATATYPE_DOUBLE == first.getDataType() || BaseDataMeta.DataType.DATATYPE_DOUBLE == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_DOUBLE, 0.0);
        }
        if (BaseDataMeta.DataType.DATATYPE_FLOAT == first.getDataType() || BaseDataMeta.DataType.DATATYPE_FLOAT == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_FLOAT, Float.valueOf(0.0f));
        }
        if (BaseDataMeta.DataType.DATATYPE_LONG == first.getDataType() || BaseDataMeta.DataType.DATATYPE_LONG == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_LONG, 0L);
        }
        return new Constant(BaseDataMeta.DataType.DATATYPE_INT, 0);
    }
}
