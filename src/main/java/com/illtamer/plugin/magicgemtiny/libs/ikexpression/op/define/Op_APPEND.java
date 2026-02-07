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

import java.text.ParseException;
import java.util.ArrayList;

public class Op_APPEND
implements IOperatorExecution {
    public static final Operator THIS_OPERATOR = Operator.APPEND;

    @Override
    public Constant execute(Constant[] args) throws IllegalExpressionException {
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
        return this.append(first, second);
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
        return new Constant(BaseDataMeta.DataType.DATATYPE_LIST, null);
    }

    private Constant append(Constant arg1, Constant arg2) {
        Object object;
        if (arg1 == null || arg2 == null) {
            throw new IllegalArgumentException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\"\u53c2\u6570\u4e22\u5931");
        }
        ArrayList<Object> resultCollection = new ArrayList<Object>();
        if (BaseDataMeta.DataType.DATATYPE_LIST == arg1.getDataType()) {
            if (arg1.getCollection() != null) {
                resultCollection.addAll(arg1.getCollection());
            }
        } else {
            try {
                object = arg1.toJavaObject();
                resultCollection.add(object);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (BaseDataMeta.DataType.DATATYPE_LIST == arg2.getDataType()) {
            if (arg2.getCollection() != null) {
                resultCollection.addAll(arg2.getCollection());
            }
        } else {
            try {
                object = arg2.toJavaObject();
                resultCollection.add(object);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Constant result = new Constant(BaseDataMeta.DataType.DATATYPE_LIST, resultCollection);
        return result;
    }
}
