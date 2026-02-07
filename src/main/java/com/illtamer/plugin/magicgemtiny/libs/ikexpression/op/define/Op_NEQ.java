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

public class Op_NEQ
implements IOperatorExecution {
    public static final Operator THIS_OPERATOR = Operator.NEQ;

    @Override
    public Constant execute(Constant[] args) throws IllegalExpressionException {
        Object firstValue;
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
        if (BaseDataMeta.DataType.DATATYPE_NULL == first.getDataType()) {
            if (null != second.getDataValue()) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        if (BaseDataMeta.DataType.DATATYPE_NULL == second.getDataType()) {
            if (null != first.getDataValue()) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        if (BaseDataMeta.DataType.DATATYPE_BOOLEAN == first.getDataType() && BaseDataMeta.DataType.DATATYPE_BOOLEAN == second.getDataType()) {
            firstValue = first.getBooleanValue();
            Boolean secondValue = second.getBooleanValue();
            if (firstValue != null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, !((Boolean)firstValue).equals(secondValue));
            }
            if (secondValue == null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
        }
        if (BaseDataMeta.DataType.DATATYPE_DATE == first.getDataType() && BaseDataMeta.DataType.DATATYPE_DATE == second.getDataType()) {
            firstValue = first.getDataValueText();
            String secondValue = second.getDataValueText();
            if (firstValue != null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, !((String)firstValue).equals(secondValue));
            }
            if (secondValue == null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
        }
        if (BaseDataMeta.DataType.DATATYPE_STRING == first.getDataType() && BaseDataMeta.DataType.DATATYPE_STRING == second.getDataType()) {
            firstValue = first.getStringValue();
            String secondValue = second.getStringValue();
            if (firstValue != null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, !((String)firstValue).equals(secondValue));
            }
            if (secondValue == null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
        }
        if (!(BaseDataMeta.DataType.DATATYPE_DOUBLE != first.getDataType() && BaseDataMeta.DataType.DATATYPE_FLOAT != first.getDataType() && BaseDataMeta.DataType.DATATYPE_LONG != first.getDataType() && BaseDataMeta.DataType.DATATYPE_INT != first.getDataType() || BaseDataMeta.DataType.DATATYPE_DOUBLE != second.getDataType() && BaseDataMeta.DataType.DATATYPE_FLOAT != second.getDataType() && BaseDataMeta.DataType.DATATYPE_LONG != second.getDataType() && BaseDataMeta.DataType.DATATYPE_INT != second.getDataType())) {
            firstValue = first.getDoubleValue();
            Double secondValue = second.getDoubleValue();
            if (firstValue != null && secondValue != null) {
                int result = Double.compare((Double)firstValue, secondValue);
                if (result != 0) {
                    return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
                }
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
            }
            if (firstValue == null && secondValue == null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
        }
        if (BaseDataMeta.DataType.DATATYPE_OBJECT == first.getDataType() && BaseDataMeta.DataType.DATATYPE_OBJECT == second.getDataType()) {
            firstValue = first.getDataValue();
            Object secondValue = second.getDataValue();
            if (firstValue != null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, !firstValue.equals(secondValue));
            }
            if (secondValue == null) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.TRUE);
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
        if (BaseDataMeta.DataType.DATATYPE_LIST == first.getDataType() || BaseDataMeta.DataType.DATATYPE_LIST == second.getDataType()) {
            throw new IllegalExpressionException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u7c7b\u578b\u9519\u8bef", THIS_OPERATOR.getToken(), opPositin);
        }
        if (BaseDataMeta.DataType.DATATYPE_NULL == first.getDataType() || BaseDataMeta.DataType.DATATYPE_NULL == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        if (BaseDataMeta.DataType.DATATYPE_BOOLEAN == first.getDataType() && BaseDataMeta.DataType.DATATYPE_BOOLEAN == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
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
        if (BaseDataMeta.DataType.DATATYPE_OBJECT == first.getDataType() && BaseDataMeta.DataType.DATATYPE_OBJECT == second.getDataType()) {
            return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
        }
        throw new IllegalExpressionException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u7c7b\u578b\u9519\u8bef", THIS_OPERATOR.getToken(), opPositin);
    }
}
