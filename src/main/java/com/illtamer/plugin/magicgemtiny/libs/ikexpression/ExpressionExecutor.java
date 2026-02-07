/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.BaseDataMeta;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Reference;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionParser;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.function.FunctionExecution;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.Operator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ExpressionExecutor {
    public List<ExpressionToken> analyze(String expression) throws IllegalExpressionException {
        ExpressionParser expParser = new ExpressionParser();
        List<ExpressionToken> list = null;
        try {
            list = expParser.getExpressionTokens(expression);
            return list;
        }
        catch (FormatException e) {
            e.printStackTrace();
            throw new IllegalExpressionException(e.getMessage());
        }
    }

    public List<ExpressionToken> compile(List<ExpressionToken> expTokens) throws IllegalExpressionException {
        if (expTokens == null || expTokens.isEmpty()) {
            throw new IllegalArgumentException("\u65e0\u6cd5\u8f6c\u5316\u7a7a\u7684\u8868\u8fbe\u5f0f");
        }
        ArrayList<ExpressionToken> _RPNExpList = new ArrayList<ExpressionToken>();
        Stack<ExpressionToken> opStack = new Stack<ExpressionToken>();
        Stack<ExpressionToken> verifyStack = new Stack<ExpressionToken>();
        ExpressionToken _function = null;
        for (ExpressionToken expToken : expTokens) {
            ExpressionToken result;
            ExpressionToken onTopOp;
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_CONSTANT == expToken.getTokenType()) {
                _RPNExpList.add(expToken);
                verifyStack.push(expToken);
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_VARIABLE == expToken.getTokenType()) {
                Variable var = VariableContainer.getVariable(expToken.getVariable().getVariableName());
                if (var == null) {
                    expToken.getVariable().setDataType(BaseDataMeta.DataType.DATATYPE_NULL);
                } else {
                    if (var.getDataType() == null) {
                        throw new IllegalExpressionException("\u8868\u8fbe\u5f0f\u4e0d\u5408\u6cd5\uff0c\u53d8\u91cf\"" + expToken.toString() + "\"\u7f3a\u5c11\u5b9a\u4e49;\u4f4d\u7f6e:" + expToken.getStartPosition(), expToken.toString(), expToken.getStartPosition());
                    }
                    expToken.getVariable().setDataType(var.getDataType());
                }
                _RPNExpList.add(expToken);
                verifyStack.push(expToken);
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR == expToken.getTokenType()) {
                if (opStack.empty()) {
                    if (Operator.COLON == expToken.getOperator()) {
                        throw new IllegalExpressionException("\u5728\u8bfb\u5165\"\uff1a\"\u65f6\uff0c\u64cd\u4f5c\u6808\u4e2d\u627e\u4e0d\u5230\u5bf9\u5e94\u7684\"\uff1f\" ", expToken.toString(), expToken.getStartPosition());
                    }
                    opStack.push(expToken);
                    continue;
                }
                boolean doPeek = true;
                while (!opStack.empty() && doPeek) {
                    onTopOp = (ExpressionToken)opStack.peek();
                    if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == onTopOp.getTokenType()) {
                        if (Operator.COLON == expToken.getOperator()) {
                            throw new IllegalExpressionException("\u5728\u8bfb\u5165\"\uff1a\"\u65f6\uff0c\u64cd\u4f5c\u6808\u4e2d\u627e\u4e0d\u5230\u5bf9\u5e94\u7684\"\uff1f\"", expToken.toString(), expToken.getStartPosition());
                        }
                        opStack.push(expToken);
                        doPeek = false;
                        continue;
                    }
                    if (ExpressionToken.ETokenType.ETOKEN_TYPE_SPLITOR == onTopOp.getTokenType() && "(".equals(onTopOp.getSplitor())) {
                        if (Operator.COLON == expToken.getOperator()) {
                            throw new IllegalExpressionException("\u5728\u8bfb\u5165\"\uff1a\"\u65f6\uff0c\u64cd\u4f5c\u6808\u4e2d\u627e\u4e0d\u5230\u5bf9\u5e94\u7684\"\uff1f\"", expToken.toString(), expToken.getStartPosition());
                        }
                        opStack.push(expToken);
                        doPeek = false;
                        continue;
                    }
                    if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR != onTopOp.getTokenType()) continue;
                    if (expToken.getOperator().getPiority() > onTopOp.getOperator().getPiority()) {
                        if (Operator.COLON == expToken.getOperator()) continue;
                        opStack.push(expToken);
                        doPeek = false;
                        continue;
                    }
                    if (expToken.getOperator().getPiority() == onTopOp.getOperator().getPiority()) {
                        if (Operator.QUES == expToken.getOperator()) {
                            opStack.push(expToken);
                            doPeek = false;
                            continue;
                        }
                        if (Operator.COLON == expToken.getOperator()) {
                            if (Operator.QUES == onTopOp.getOperator()) {
                                opStack.pop();
                                ExpressionToken opSelectToken = ExpressionToken.createOperatorToken(Operator.SELECT);
                                opSelectToken.setStartPosition(onTopOp.getStartPosition());
                                opStack.push(opSelectToken);
                                doPeek = false;
                                continue;
                            }
                            if (Operator.SELECT != onTopOp.getOperator()) continue;
                            result = this.verifyOperator(onTopOp, verifyStack);
                            verifyStack.push(result);
                            opStack.pop();
                            _RPNExpList.add(onTopOp);
                            continue;
                        }
                        result = this.verifyOperator(onTopOp, verifyStack);
                        verifyStack.push(result);
                        opStack.pop();
                        _RPNExpList.add(onTopOp);
                        continue;
                    }
                    result = this.verifyOperator(onTopOp, verifyStack);
                    verifyStack.push(result);
                    opStack.pop();
                    _RPNExpList.add(onTopOp);
                }
                if (!doPeek || !opStack.empty()) continue;
                if (Operator.COLON == expToken.getOperator()) {
                    throw new IllegalExpressionException("\u5728\u8bfb\u5165\"\uff1a\"\u65f6\uff0c\u64cd\u4f5c\u6808\u4e2d\u627e\u4e0d\u5230\u5bf9\u5e94\u7684\"\uff1f\"", expToken.toString(), expToken.getStartPosition());
                }
                opStack.push(expToken);
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == expToken.getTokenType()) {
                _function = expToken;
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_SPLITOR != expToken.getTokenType()) continue;
            if ("(".equals(expToken.getSplitor())) {
                if (_function != null) {
                    _RPNExpList.add(expToken);
                    verifyStack.push(expToken);
                    opStack.push(expToken);
                    opStack.push(_function);
                    _function = null;
                    continue;
                }
                opStack.push(expToken);
                continue;
            }
            if (")".equals(expToken.getSplitor())) {
                boolean doPop = true;
                while (doPop && !opStack.empty()) {
                    onTopOp = (ExpressionToken)opStack.pop();
                    if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR == onTopOp.getTokenType()) {
                        if (Operator.QUES == onTopOp.getOperator()) {
                            throw new IllegalExpressionException("\u5728\u8bfb\u5165\")\"\u65f6\uff0c\u64cd\u4f5c\u6808\u4e2d\u9047\u5230\"\uff1f\" ,\u7f3a\u5c11\":\"\u53f7", onTopOp.toString(), onTopOp.getStartPosition());
                        }
                        result = this.verifyOperator(onTopOp, verifyStack);
                        verifyStack.push(result);
                        _RPNExpList.add(onTopOp);
                        continue;
                    }
                    if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == onTopOp.getTokenType()) {
                        result = this.verifyFunction(onTopOp, verifyStack);
                        verifyStack.push(result);
                        _RPNExpList.add(expToken);
                        _RPNExpList.add(onTopOp);
                        continue;
                    }
                    if (!"(".equals(onTopOp.getSplitor())) continue;
                    doPop = false;
                }
                if (!doPop || !opStack.empty()) continue;
                throw new IllegalExpressionException("\u5728\u8bfb\u5165\")\"\u65f6\uff0c\u64cd\u4f5c\u6808\u4e2d\u627e\u4e0d\u5230\u5bf9\u5e94\u7684\"(\" ", expToken.getSplitor(), expToken.getStartPosition());
            }
            if (!",".equals(expToken.getSplitor())) continue;
            boolean doPeek = true;
            while (!opStack.empty() && doPeek) {
                onTopOp = (ExpressionToken)opStack.peek();
                if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR == onTopOp.getTokenType()) {
                    if (Operator.QUES == onTopOp.getOperator()) {
                        throw new IllegalExpressionException("\u5728\u8bfb\u5165\",\"\u65f6\uff0c\u64cd\u4f5c\u6808\u4e2d\u9047\u5230\"\uff1f\" ,\u7f3a\u5c11\":\"\u53f7", onTopOp.toString(), onTopOp.getStartPosition());
                    }
                    opStack.pop();
                    result = this.verifyOperator(onTopOp, verifyStack);
                    verifyStack.push(result);
                    _RPNExpList.add(onTopOp);
                    continue;
                }
                if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == onTopOp.getTokenType()) {
                    doPeek = false;
                    continue;
                }
                if (ExpressionToken.ETokenType.ETOKEN_TYPE_SPLITOR != onTopOp.getTokenType() || !"(".equals(onTopOp.getSplitor())) continue;
                throw new IllegalExpressionException("\u5728\u8bfb\u5165\",\"\u65f6\uff0c\u64cd\u4f5c\u7b26\u6808\u9876\u4e3a\"(\",,(\u51fd\u6570\u4e22\u5931) \u4f4d\u7f6e\uff1a" + onTopOp.getStartPosition(), expToken.getSplitor(), expToken.getStartPosition());
            }
            if (!doPeek || !opStack.empty()) continue;
            throw new IllegalExpressionException("\u5728\u8bfb\u5165\",\"\u65f6\uff0c\u64cd\u4f5c\u7b26\u6808\u5f39\u7a7a\uff0c\u6ca1\u6709\u627e\u5230\u76f8\u5e94\u7684\u51fd\u6570\u8bcd\u5143 ", expToken.getSplitor(), expToken.getStartPosition());
        }
        while (!opStack.empty()) {
            ExpressionToken onTopOp = (ExpressionToken)opStack.pop();
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR == onTopOp.getTokenType()) {
                if (Operator.QUES == onTopOp.getOperator()) {
                    throw new IllegalExpressionException("\u64cd\u4f5c\u6808\u4e2d\u9047\u5230\u5269\u4f59\u7684\"\uff1f\" ,\u7f3a\u5c11\":\"\u53f7", onTopOp.toString(), onTopOp.getStartPosition());
                }
                ExpressionToken result = this.verifyOperator(onTopOp, verifyStack);
                verifyStack.push(result);
                _RPNExpList.add(onTopOp);
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == onTopOp.getTokenType()) {
                throw new IllegalExpressionException("\u51fd\u6570" + onTopOp.getFunctionName() + "\u7f3a\u5c11\")\"", onTopOp.getFunctionName(), onTopOp.getStartPosition());
            }
            if (!"(".equals(onTopOp.getSplitor())) continue;
            throw new IllegalExpressionException("\u5de6\u62ec\u53f7\"(\"\u7f3a\u5c11\u914d\u5957\u7684\u53f3\u62ec\u53f7\")\"", onTopOp.getFunctionName(), onTopOp.getStartPosition());
        }
        if (verifyStack.size() != 1) {
            StringBuffer errorBuffer = new StringBuffer("\r\n");
            while (!verifyStack.empty()) {
                ExpressionToken onTop = (ExpressionToken)verifyStack.pop();
                errorBuffer.append("\t").append(onTop.toString()).append("\r\n");
            }
            throw new IllegalExpressionException("\u8868\u8fbe\u5f0f\u4e0d\u5b8c\u6574.\r\n \u6821\u9a8c\u6808\u72b6\u6001\u5f02\u5e38:" + errorBuffer);
        }
        return _RPNExpList;
    }

    public Constant execute(List<ExpressionToken> _RPNExpList) throws IllegalExpressionException {
        if (_RPNExpList == null || _RPNExpList.isEmpty()) {
            throw new IllegalArgumentException("\u65e0\u6cd5\u6267\u884c\u7a7a\u7684\u9006\u6ce2\u5170\u5f0f\u961f\u5217");
        }
        Stack<ExpressionToken> compileStack = new Stack<ExpressionToken>();
        for (ExpressionToken expToken : _RPNExpList) {
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_CONSTANT == expToken.getTokenType()) {
                compileStack.push(expToken);
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_VARIABLE == expToken.getTokenType()) {
                ExpressionToken constantToken;
                Variable varWithValue = VariableContainer.getVariable(expToken.getVariable().getVariableName());
                if (varWithValue != null) {
                    constantToken = ExpressionToken.createConstantToken(varWithValue.getDataType(), varWithValue.getDataValue());
                    compileStack.push(constantToken);
                    continue;
                }
                constantToken = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_NULL, null);
                compileStack.push(constantToken);
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR == expToken.getTokenType()) {
                Operator operator = expToken.getOperator();
                int opType = operator.getOpType();
                Constant[] args = new Constant[opType];
                ExpressionToken argToken = null;
                for (int i = 0; i < opType; ++i) {
                    if (!compileStack.empty()) {
                        argToken = (ExpressionToken)compileStack.pop();
                        if (ExpressionToken.ETokenType.ETOKEN_TYPE_CONSTANT != argToken.getTokenType()) {
                            throw new IllegalStateException("\u64cd\u4f5c\u7b26" + operator.getToken() + "\u627e\u4e0d\u5230\u76f8\u5e94\u7684\u53c2\u6570\uff0c\u6216\u53c2\u6570\u4e2a\u6570\u4e0d\u8db3;\u4f4d\u7f6e\uff1a" + expToken.getStartPosition());
                        }
                    } else {
                        throw new IllegalStateException("\u64cd\u4f5c\u7b26" + operator.getToken() + "\u627e\u4e0d\u5230\u76f8\u5e94\u7684\u53c2\u6570\uff0c\u6216\u53c2\u6570\u4e2a\u6570\u4e0d\u8db3;\u4f4d\u7f6e\uff1a" + expToken.getStartPosition());
                    }
                    args[i] = argToken.getConstant();
                }
                Reference ref = new Reference(expToken, args);
                ExpressionToken resultToken = ExpressionToken.createReference(ref);
                compileStack.push(resultToken);
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == expToken.getTokenType()) {
                if (!compileStack.empty()) {
                    ExpressionToken onTop = (ExpressionToken)compileStack.pop();
                    if (")".equals(onTop.getSplitor())) {
                        boolean doPop = true;
                        ArrayList<Constant> argsList = new ArrayList<Constant>();
                        ExpressionToken parameter = null;
                        while (doPop && !compileStack.empty()) {
                            parameter = (ExpressionToken)compileStack.pop();
                            if (ExpressionToken.ETokenType.ETOKEN_TYPE_CONSTANT == parameter.getTokenType()) {
                                argsList.add(parameter.getConstant());
                                continue;
                            }
                            if ("(".equals(parameter.getSplitor())) {
                                doPop = false;
                                continue;
                            }
                            throw new IllegalStateException("\u51fd\u6570" + expToken.getFunctionName() + "\u6267\u884c\u65f6\u9047\u5230\u975e\u6cd5\u53c2\u6570" + parameter.toString());
                        }
                        if (doPop && compileStack.empty()) {
                            throw new IllegalStateException("\u51fd\u6570" + expToken.getFunctionName() + "\u6267\u884c\u65f6\u6ca1\u6709\u627e\u5230\u5e94\u6709\u7684\"(\"");
                        }
                        Constant[] arguments = new Constant[argsList.size()];
                        arguments = argsList.toArray(arguments);
                        Reference ref = new Reference(expToken, arguments);
                        ExpressionToken resultToken = ExpressionToken.createReference(ref);
                        compileStack.push(resultToken);
                        continue;
                    }
                    throw new IllegalStateException("\u51fd\u6570" + expToken.getFunctionName() + "\u6267\u884c\u65f6\u6ca1\u6709\u627e\u5230\u5e94\u6709\u7684\")\"");
                }
                throw new IllegalStateException("\u51fd\u6570" + expToken.getFunctionName() + "\u6267\u884c\u65f6\u6ca1\u6709\u627e\u5230\u5e94\u6709\u7684\")\"");
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_SPLITOR != expToken.getTokenType()) continue;
            compileStack.push(expToken);
        }
        if (compileStack.size() == 1) {
            ExpressionToken token = (ExpressionToken)compileStack.pop();
            Constant result = token.getConstant();
            if (result.isReference()) {
                Reference resultRef = (Reference)result.getDataValue();
                return resultRef.execute();
            }
            return result;
        }
        StringBuffer errorBuffer = new StringBuffer("\r\n");
        while (!compileStack.empty()) {
            ExpressionToken onTop = (ExpressionToken)compileStack.pop();
            errorBuffer.append("\t").append(onTop.toString()).append("\r\n");
        }
        throw new IllegalStateException("\u8868\u8fbe\u5f0f\u4e0d\u5b8c\u6574.\r\n \u7ed3\u679c\u72b6\u6001\u5f02\u5e38:" + errorBuffer);
    }

    public String tokensToString(List<ExpressionToken> tokenList) {
        if (tokenList == null) {
            throw new IllegalArgumentException("\u53c2\u6570tokenList\u4e3a\u7a7a");
        }
        StringBuffer expressionText = new StringBuffer();
        for (ExpressionToken token : tokenList) {
            ExpressionToken.ETokenType tokenType = token.getTokenType();
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_CONSTANT == tokenType) {
                Constant c = token.getConstant();
                if (BaseDataMeta.DataType.DATATYPE_BOOLEAN == c.getDataType()) {
                    expressionText.append(c.getDataValueText()).append(" ");
                    continue;
                }
                if (BaseDataMeta.DataType.DATATYPE_DATE == c.getDataType()) {
                    expressionText.append("[").append(c.getDataValueText()).append("] ");
                    continue;
                }
                if (BaseDataMeta.DataType.DATATYPE_DOUBLE == c.getDataType()) {
                    expressionText.append(c.getDataValueText()).append(" ");
                    continue;
                }
                if (BaseDataMeta.DataType.DATATYPE_FLOAT == c.getDataType()) {
                    expressionText.append(c.getDataValueText()).append("F ");
                    continue;
                }
                if (BaseDataMeta.DataType.DATATYPE_INT == c.getDataType()) {
                    expressionText.append(c.getDataValueText()).append(" ");
                    continue;
                }
                if (BaseDataMeta.DataType.DATATYPE_LONG == c.getDataType()) {
                    expressionText.append(c.getDataValueText()).append("L ");
                    continue;
                }
                if (BaseDataMeta.DataType.DATATYPE_NULL == c.getDataType()) {
                    expressionText.append(c.getDataValueText()).append(" ");
                    continue;
                }
                if (BaseDataMeta.DataType.DATATYPE_STRING != c.getDataType()) continue;
                expressionText.append("\"").append(c.getDataValueText()).append("\" ");
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_VARIABLE == tokenType) {
                expressionText.append(token.getVariable().getVariableName()).append(" ");
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_FUNCTION == tokenType) {
                expressionText.append('$').append(token.getFunctionName()).append(" ");
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR == tokenType) {
                expressionText.append(token.getOperator().toString()).append(" ");
                continue;
            }
            if (ExpressionToken.ETokenType.ETOKEN_TYPE_SPLITOR != tokenType) continue;
            expressionText.append(token.getSplitor()).append(" ");
        }
        return expressionText.toString();
    }

    public List<ExpressionToken> stringToTokens(String tokenExpression) throws IllegalExpressionException {
        if (tokenExpression == null) {
            throw new IllegalArgumentException("\u53c2\u6570tokenExpression\u4e3a\u7a7a");
        }
        ArrayList<ExpressionToken> tokens = new ArrayList<ExpressionToken>();
        char[] expChars = tokenExpression.toCharArray();
        int status = 0;
        StringBuffer tokenBuffer = new StringBuffer();
        for (int i = 0; i < expChars.length; ++i) {
            if (' ' == expChars[i]) {
                if (status == 0) {
                    this.addToken(tokenBuffer.toString(), tokens);
                    tokenBuffer = new StringBuffer();
                    continue;
                }
                if (status == 1 || status == 2) {
                    tokenBuffer.append(expChars[i]);
                    continue;
                }
                throw new IllegalExpressionException("\u975e\u6cd5\u7684\u8f6c\u4e49\u7b26\"" + expChars[i] + "\" \uff0c\u4f4d\u7f6e\uff1a" + i);
            }
            if ('[' == expChars[i]) {
                if (status == 0) {
                    status = 1;
                    tokenBuffer.append(expChars[i]);
                    continue;
                }
                if (status == 1) {
                    throw new IllegalExpressionException("\u975e\u6cd5\u7684\u65e5\u671f\u5f00\u59cb\u5b57\u7b26\uff0c\u4f4d\u7f6e\uff1a" + i);
                }
                if (status == 2) {
                    tokenBuffer.append(expChars[i]);
                    continue;
                }
                throw new IllegalExpressionException("\u975e\u6cd5\u7684\u8f6c\u4e49\u7b26\"" + expChars[i] + "\" \uff0c\u4f4d\u7f6e\uff1a" + i);
            }
            if (']' == expChars[i]) {
                if (status == 0) {
                    throw new IllegalExpressionException("\u975e\u6cd5\u7684\u65e5\u671f\u7ed3\u675f\u5b57\u7b26\uff0c\u4f4d\u7f6e\uff1a" + i);
                }
                if (status == 1) {
                    status = 0;
                    tokenBuffer.append(expChars[i]);
                    continue;
                }
                if (status == 2) {
                    tokenBuffer.append(expChars[i]);
                    continue;
                }
                throw new IllegalExpressionException("\u975e\u6cd5\u7684\u8f6c\u4e49\u7b26\"" + expChars[i] + "\" \uff0c\u4f4d\u7f6e\uff1a" + i);
            }
            if ('\"' == expChars[i]) {
                if (status == 0) {
                    status = 2;
                    tokenBuffer.append(expChars[i]);
                    continue;
                }
                if (status == 1) {
                    throw new IllegalExpressionException("\u975e\u6cd5\u7684\u65e5\u671f\u5b57\u7b26\"" + expChars[i] + "\" \uff0c\u4f4d\u7f6e\uff1a" + i);
                }
                if (status == 2) {
                    status = 0;
                    tokenBuffer.append(expChars[i]);
                    continue;
                }
                status = 2;
                tokenBuffer.append(expChars[i]);
                continue;
            }
            if ('\\' == expChars[i]) {
                if (status == 0) {
                    throw new IllegalExpressionException("\u975e\u6cd5\u7684\u5b57\u7b26\"" + expChars[i] + "\" \uff0c\u4f4d\u7f6e\uff1a" + i);
                }
                if (status == 1) {
                    throw new IllegalExpressionException("\u975e\u6cd5\u7684\u65e5\u671f\u5b57\u7b26\"" + expChars[i] + "\" \uff0c\u4f4d\u7f6e\uff1a" + i);
                }
                if (status == 2) {
                    status = 3;
                    tokenBuffer.append(expChars[i]);
                    continue;
                }
                status = 2;
                tokenBuffer.append(expChars[i]);
                continue;
            }
            if (status == 0 || status == 1 || status == 2) {
                tokenBuffer.append(expChars[i]);
                continue;
            }
            throw new IllegalExpressionException("\u975e\u6cd5\u7684\u8f6c\u4e49\u7b26\"" + expChars[i] + "\" \uff0c\u4f4d\u7f6e\uff1a" + i);
        }
        tokenBuffer.trimToSize();
        if (tokenBuffer.length() > 0) {
            this.addToken(tokenBuffer.toString(), tokens);
        }
        return tokens;
    }

    private void addToken(String tokenString, List<ExpressionToken> tokens) throws IllegalExpressionException {
        ExpressionToken token = null;
        if (ExpressionTokenHelper.isNull(tokenString)) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_NULL, null);
            tokens.add(token);
        } else if (ExpressionTokenHelper.isBoolean(tokenString)) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.valueOf(tokenString));
            tokens.add(token);
        } else if (ExpressionTokenHelper.isInteger(tokenString)) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_INT, Integer.valueOf(tokenString));
            tokens.add(token);
        } else if (ExpressionTokenHelper.isLong(tokenString)) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_LONG, Long.valueOf(tokenString.substring(0, tokenString.length() - 1)));
            tokens.add(token);
        } else if (ExpressionTokenHelper.isFloat(tokenString)) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_FLOAT, Float.valueOf(tokenString.substring(0, tokenString.length() - 1)));
            tokens.add(token);
        } else if (ExpressionTokenHelper.isDouble(tokenString)) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_DOUBLE, Double.valueOf(tokenString));
            tokens.add(token);
        } else if (ExpressionTokenHelper.isDateTime(tokenString)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_DATE, sdf.parse(tokenString.substring(1, tokenString.length() - 1)));
            }
            catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalExpressionException("\u65e5\u671f\u53c2\u6570\u683c\u5f0f\u9519\u8bef");
            }
            tokens.add(token);
        } else if (ExpressionTokenHelper.isString(tokenString)) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_STRING, tokenString.substring(1, tokenString.length() - 1));
            tokens.add(token);
        } else if (ExpressionTokenHelper.isSplitor(tokenString)) {
            token = ExpressionToken.createSplitorToken(tokenString);
            tokens.add(token);
        } else if (ExpressionTokenHelper.isFunction(tokenString)) {
            token = ExpressionToken.createFunctionToken(tokenString.substring(1));
            tokens.add(token);
        } else if (ExpressionTokenHelper.isOperator(tokenString)) {
            Operator operator = Operator.valueOf(tokenString);
            token = ExpressionToken.createOperatorToken(operator);
            tokens.add(token);
        } else {
            token = ExpressionToken.createVariableToken(tokenString);
            tokens.add(token);
        }
    }

    private ExpressionToken verifyOperator(ExpressionToken opToken, Stack<ExpressionToken> verifyStack) throws IllegalExpressionException {
        Operator op = opToken.getOperator();
        int opType = op.getOpType();
        BaseDataMeta[] args = new BaseDataMeta[opType];
        ExpressionToken argToken = null;
        for (int i = 0; i < opType; ++i) {
            if (!verifyStack.empty()) {
                argToken = verifyStack.pop();
                if (ExpressionToken.ETokenType.ETOKEN_TYPE_CONSTANT == argToken.getTokenType()) {
                    args[i] = argToken.getConstant();
                    continue;
                }
                if (ExpressionToken.ETokenType.ETOKEN_TYPE_VARIABLE == argToken.getTokenType()) {
                    args[i] = argToken.getVariable();
                    continue;
                }
                throw new IllegalExpressionException("\u8868\u8fbe\u5f0f\u4e0d\u5408\u6cd5\uff0c\u64cd\u4f5c\u7b26\"" + op.getToken() + "\"\u53c2\u6570\u9519\u8bef;\u4f4d\u7f6e\uff1a" + argToken.getStartPosition(), opToken.toString(), opToken.getStartPosition());
            }
            throw new IllegalExpressionException("\u8868\u8fbe\u5f0f\u4e0d\u5408\u6cd5\uff0c\u64cd\u4f5c\u7b26\"" + op.getToken() + "\"\u627e\u4e0d\u5230\u76f8\u5e94\u7684\u53c2\u6570\uff0c\u6216\u53c2\u6570\u4e2a\u6570\u4e0d\u8db3;", opToken.toString(), opToken.getStartPosition());
        }
        Constant result = op.verify(opToken.getStartPosition(), args);
        return ExpressionToken.createConstantToken(result);
    }

    private ExpressionToken verifyFunction(ExpressionToken funtionToken, Stack<ExpressionToken> verifyStack) throws IllegalExpressionException {
        if (!verifyStack.empty()) {
            boolean doPop = true;
            ArrayList<BaseDataMeta> args = new ArrayList<BaseDataMeta>();
            ExpressionToken parameter = null;
            while (doPop && !verifyStack.empty()) {
                parameter = verifyStack.pop();
                if (ExpressionToken.ETokenType.ETOKEN_TYPE_CONSTANT == parameter.getTokenType()) {
                    args.add(parameter.getConstant());
                    continue;
                }
                if (ExpressionToken.ETokenType.ETOKEN_TYPE_VARIABLE == parameter.getTokenType()) {
                    args.add(parameter.getVariable());
                    continue;
                }
                if ("(".equals(parameter.getSplitor())) {
                    doPop = false;
                    continue;
                }
                throw new IllegalExpressionException("\u8868\u8fbe\u5f0f\u4e0d\u5408\u6cd5\uff0c\u51fd\u6570\"" + funtionToken.getFunctionName() + "\"\u9047\u5230\u975e\u6cd5\u53c2\u6570" + parameter.toString() + ";\u4f4d\u7f6e:" + parameter.getStartPosition(), funtionToken.toString(), funtionToken.getStartPosition());
            }
            if (doPop && verifyStack.empty()) {
                throw new IllegalExpressionException("\u8868\u8fbe\u5f0f\u4e0d\u5408\u6cd5\uff0c\u51fd\u6570\"" + funtionToken.getFunctionName() + "\"\u7f3a\u5c11\"(\"\uff1b\u4f4d\u7f6e:" + (funtionToken.getStartPosition() + funtionToken.toString().length()), funtionToken.toString(), funtionToken.getStartPosition());
            }
            BaseDataMeta[] arguments = new BaseDataMeta[args.size()];
            arguments = args.toArray(arguments);
            Constant result = FunctionExecution.varify(funtionToken.getFunctionName(), funtionToken.getStartPosition(), arguments);
            return ExpressionToken.createConstantToken(result);
        }
        throw new IllegalExpressionException("\u8868\u8fbe\u5f0f\u4e0d\u5408\u6cd5\uff0c\u51fd\u6570\"" + funtionToken.getFunctionName() + "\"\u4e0d\u5b8c\u6574", funtionToken.toString(), funtionToken.getStartPosition());
    }
}
