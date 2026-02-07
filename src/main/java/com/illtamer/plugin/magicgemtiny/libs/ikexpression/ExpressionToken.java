/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.BaseDataMeta;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Reference;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.Operator;

public class ExpressionToken {
    private ETokenType tokenType;
    private Constant constant;
    private Variable variable;
    private Operator operator;
    private String tokenText;
    private int startPosition = -1;

    private ExpressionToken() {
    }

    public static ExpressionToken createConstantToken(BaseDataMeta.DataType dataType, Object dataValue) {
        ExpressionToken instance = new ExpressionToken();
        instance.constant = new Constant(dataType, dataValue);
        instance.tokenType = ETokenType.ETOKEN_TYPE_CONSTANT;
        if (dataValue != null) {
            instance.tokenText = instance.constant.getDataValueText();
        }
        return instance;
    }

    public static ExpressionToken createConstantToken(Constant constant) {
        if (constant == null) {
            throw new IllegalArgumentException("\u975e\u6cd5\u53c2\u6570\u5f02\u5e38\uff1a\u5e38\u91cf\u4e3anull");
        }
        ExpressionToken instance = new ExpressionToken();
        instance.constant = constant;
        instance.tokenType = ETokenType.ETOKEN_TYPE_CONSTANT;
        if (constant.getDataValue() != null) {
            instance.tokenText = constant.getDataValueText();
        }
        return instance;
    }

    public static ExpressionToken createVariableToken(String variableName) {
        ExpressionToken instance = new ExpressionToken();
        instance.variable = new Variable(variableName);
        instance.tokenType = ETokenType.ETOKEN_TYPE_VARIABLE;
        instance.tokenText = variableName;
        return instance;
    }

    public static ExpressionToken createReference(Reference ref) {
        ExpressionToken instance = new ExpressionToken();
        instance.constant = new Constant(ref);
        instance.tokenType = ETokenType.ETOKEN_TYPE_CONSTANT;
        if (ref != null) {
            instance.tokenText = instance.constant.getDataValueText();
        }
        return instance;
    }

    public static ExpressionToken createFunctionToken(String functionName) {
        if (functionName == null) {
            throw new IllegalArgumentException("\u975e\u6cd5\u53c2\u6570\uff1a\u51fd\u6570\u540d\u79f0\u4e3a\u7a7a");
        }
        ExpressionToken instance = new ExpressionToken();
        instance.tokenText = functionName;
        instance.tokenType = ETokenType.ETOKEN_TYPE_FUNCTION;
        return instance;
    }

    public static ExpressionToken createOperatorToken(Operator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("\u975e\u6cd5\u53c2\u6570\uff1a\u64cd\u4f5c\u7b26\u4e3a\u7a7a");
        }
        ExpressionToken instance = new ExpressionToken();
        instance.operator = operator;
        instance.tokenText = operator.getToken();
        instance.tokenType = ETokenType.ETOKEN_TYPE_OPERATOR;
        return instance;
    }

    public static ExpressionToken createSplitorToken(String splitorText) {
        if (splitorText == null) {
            throw new IllegalArgumentException("\u975e\u6cd5\u53c2\u6570\uff1a\u5206\u9694\u7b26\u4e3a\u7a7a");
        }
        ExpressionToken instance = new ExpressionToken();
        instance.tokenText = splitorText;
        instance.tokenType = ETokenType.ETOKEN_TYPE_SPLITOR;
        return instance;
    }

    public ETokenType getTokenType() {
        return this.tokenType;
    }

    public Constant getConstant() {
        return this.constant;
    }

    public Variable getVariable() {
        return this.variable;
    }

    public Operator getOperator() {
        return this.operator;
    }

    public String getFunctionName() {
        return this.tokenText;
    }

    public String getSplitor() {
        return this.tokenText;
    }

    public int getStartPosition() {
        return this.startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public String toString() {
        return this.tokenText;
    }

    public static enum ETokenType {
        ETOKEN_TYPE_CONSTANT,
        ETOKEN_TYPE_VARIABLE,
        ETOKEN_TYPE_OPERATOR,
        ETOKEN_TYPE_FUNCTION,
        ETOKEN_TYPE_SPLITOR;

    }
}
