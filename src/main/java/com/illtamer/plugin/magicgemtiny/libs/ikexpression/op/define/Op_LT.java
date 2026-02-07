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

public class Op_LT
implements IOperatorExecution {
    public static final Operator THIS_OPERATOR = Operator.LT;

    @Override
    public Constant execute(Constant[] args) throws IllegalExpressionException {
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\u53c2\u6570\u4e2a\u6570\u4e0d\u5339\u914d");
        }
        Constant first = args[1];
        if (null == first || null == first.getDataValue()) {
            throw new NullPointerException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e3a\u7a7a");
        }
        Constant second = args[0];
        if (null == second || null == second.getDataValue()) {
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
        if (BaseDataMeta.DataType.DATATYPE_DATE == first.getDataType() && BaseDataMeta.DataType.DATATYPE_DATE == second.getDataType()) {
            int result = first.getDateValue().compareTo(second.getDateValue());
            if (result < 0) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        if (BaseDataMeta.DataType.DATATYPE_STRING == first.getDataType() && BaseDataMeta.DataType.DATATYPE_STRING == second.getDataType()) {
            int result = first.getStringValue().compareTo(second.getStringValue());
            if (result < 0) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        if (!(BaseDataMeta.DataType.DATATYPE_DOUBLE != first.getDataType() && BaseDataMeta.DataType.DATATYPE_FLOAT != first.getDataType() && BaseDataMeta.DataType.DATATYPE_LONG != first.getDataType() && BaseDataMeta.DataType.DATATYPE_INT != first.getDataType() || BaseDataMeta.DataType.DATATYPE_DOUBLE != second.getDataType() && BaseDataMeta.DataType.DATATYPE_FLOAT != second.getDataType() && BaseDataMeta.DataType.DATATYPE_LONG != second.getDataType() && BaseDataMeta.DataType.DATATYPE_INT != second.getDataType())) {
            int result = Double.compare(first.getDoubleValue(), second.getDoubleValue());
            if (result < 0) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        throw new IllegalArgumentException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u7c7b\u578b\u9519\u8bef");
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
        if (BaseDataMeta.DataType.DATATYPE_DATE == first.getDataType() && BaseDataMeta.DataType.DATATYPE_DATE == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        if (BaseDataMeta.DataType.DATATYPE_STRING == first.getDataType() && BaseDataMeta.DataType.DATATYPE_STRING == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        if (!(BaseDataMeta.DataType.DATATYPE_DOUBLE != first.getDataType() && BaseDataMeta.DataType.DATATYPE_FLOAT != first.getDataType() && BaseDataMeta.DataType.DATATYPE_LONG != first.getDataType() && BaseDataMeta.DataType.DATATYPE_INT != first.getDataType() || BaseDataMeta.DataType.DATATYPE_DOUBLE != second.getDataType() && BaseDataMeta.DataType.DATATYPE_FLOAT != second.getDataType() && BaseDataMeta.DataType.DATATYPE_LONG != second.getDataType() && BaseDataMeta.DataType.DATATYPE_INT != second.getDataType())) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        throw new IllegalExpressionException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u7c7b\u578b\u9519\u8bef");
    }
}
