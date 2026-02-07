/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.define;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.IllegalExpressionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.BaseDataMeta;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.IOperatorExecution;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.Operator;

public class Op_QUES
implements IOperatorExecution {
    public static final Operator THIS_OPERATOR = Operator.QUES;

    @Override
    public Constant execute(Constant[] args) {
        throw new UnsupportedOperationException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\u4e0d\u652f\u6301\u8be5\u65b9\u6cd5");
    }

    @Override
    public Constant verify(int opPositin, BaseDataMeta[] args) throws IllegalExpressionException {
        throw new UnsupportedOperationException("\u64cd\u4f5c\u7b26\"" + THIS_OPERATOR.getToken() + "\u4e0d\u652f\u6301\u8be5\u65b9\u6cd5");
    }
}
