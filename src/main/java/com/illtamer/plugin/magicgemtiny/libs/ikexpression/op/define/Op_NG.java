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

public class Op_NG
implements IOperatorExecution {
    public static final Operator THIS_OPERATOR = Operator.NG;

    @Override
    public Constant execute(Constant[] args) throws IllegalExpressionException {
        Number result;
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\u53c2\u6570\u4e2a\u6570\u4e0d\u5339\u914d");
        }
        Constant first = args[0];
        if (null == first || null == first.getDataValue()) {
            throw new NullPointerException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e3a\u7a7a");
        }
        if (first.isReference()) {
            Reference firstRef = (Reference)first.getDataValue();
            first = firstRef.execute();
        }
        if (BaseDataMeta.DataType.DATATYPE_DOUBLE == first.getDataType()) {
            result = 0.0 - first.getDoubleValue();
            return new Constant(BaseDataMeta.DataType.DATATYPE_DOUBLE, result);
        }
        if (BaseDataMeta.DataType.DATATYPE_FLOAT == first.getDataType()) {
            result = Float.valueOf(0.0f - first.getFloatValue().floatValue());
            return new Constant(BaseDataMeta.DataType.DATATYPE_FLOAT, result);
        }
        if (BaseDataMeta.DataType.DATATYPE_LONG == first.getDataType()) {
            result = 0L - first.getLongValue();
            return new Constant(BaseDataMeta.DataType.DATATYPE_LONG, result);
        }
        if (BaseDataMeta.DataType.DATATYPE_INT == first.getDataType()) {
            result = 0 - first.getIntegerValue();
            return new Constant(BaseDataMeta.DataType.DATATYPE_INT, result);
        }
        throw new IllegalArgumentException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u7c7b\u578b\u9519\u8bef");
    }

    @Override
    public Constant verify(int opPositin, BaseDataMeta[] args) throws IllegalExpressionException {
        if (args == null) {
            throw new IllegalArgumentException("\u8fd0\u7b97\u64cd\u4f5c\u7b26\u53c2\u6570\u4e3a\u7a7a");
        }
        if (args.length != 1) {
            throw new IllegalExpressionException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e2a\u6570\u4e0d\u5339\u914d", THIS_OPERATOR.getToken(), opPositin);
        }
        BaseDataMeta first = args[0];
        if (first == null) {
            throw new NullPointerException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e3a\u7a7a");
        }
        if (BaseDataMeta.DataType.DATATYPE_DOUBLE == first.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_DOUBLE, 0.0);
        }
        if (BaseDataMeta.DataType.DATATYPE_FLOAT == first.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_FLOAT, Float.valueOf(0.0f));
        }
        if (BaseDataMeta.DataType.DATATYPE_LONG == first.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_LONG, 0L);
        }
        if (BaseDataMeta.DataType.DATATYPE_INT == first.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_INT, 0);
        }
        throw new IllegalExpressionException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u7c7b\u578b\u9519\u8bef", THIS_OPERATOR.getToken(), opPositin);
    }
}
