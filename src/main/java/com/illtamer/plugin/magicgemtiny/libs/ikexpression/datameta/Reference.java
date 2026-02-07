/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.ExpressionToken;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.IllegalExpressionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.function.FunctionExecution;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.Operator;

public class Reference {
    private ExpressionToken token;
    private Constant[] arguments;
    private BaseDataMeta.DataType dataType;

    public Reference(ExpressionToken token, Constant[] args) throws IllegalExpressionException {
        this.token = token;
        this.arguments = args;
        if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == token.getTokenType()) {
            Constant result = FunctionExecution.varify(token.getFunctionName(), token.getStartPosition(), args);
            this.dataType = result.getDataType();
        } else if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR == token.getTokenType()) {
            Operator op = token.getOperator();
            Constant result = op.verify(token.getStartPosition(), args);
            this.dataType = result.getDataType();
        }
    }

    public BaseDataMeta.DataType getDataType() {
        return this.dataType;
    }

    public Constant[] getArgs() {
        return this.arguments;
    }

    public void setArgs(Constant[] args) {
        this.arguments = args;
    }

    public ExpressionToken getToken() {
        return this.token;
    }

    public void setToken(ExpressionToken token) {
        this.token = token;
    }

    public Constant execute() throws IllegalExpressionException {
        if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR == this.token.getTokenType()) {
            Operator op = this.token.getOperator();
            return op.execute(this.arguments);
        }
        if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == this.token.getTokenType()) {
            return FunctionExecution.execute(this.token.getFunctionName(), this.token.getStartPosition(), this.arguments);
        }
        throw new IllegalExpressionException("\u4e0d\u652f\u6301\u7684Reference\u6267\u884c\u5f02\u5e38");
    }
}
