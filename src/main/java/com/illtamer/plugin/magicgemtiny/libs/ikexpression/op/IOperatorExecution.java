/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.op;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.IllegalExpressionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.BaseDataMeta;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;

public interface IOperatorExecution {
    public Constant execute(Constant[] var1) throws IllegalExpressionException;

    public Constant verify(int var1, BaseDataMeta[] var2) throws IllegalExpressionException;
}
