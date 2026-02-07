/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.ExpressionToken;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.BaseDataMeta;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.Operator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExpressionParser {
    private static final Map<String, Operator> operators = new HashMap<String, Operator>();
    private final Stack<String> parenthesis = new Stack();

    public static void main(String[] s) {
        String expression = "$CALCDATE($SYSDATE() ,0,0 , 7,0 ,0,aa ) > [2008-10-01]";
        ExpressionParser ep = new ExpressionParser();
        try {
            List<ExpressionToken> list = ep.getExpressionTokens(expression);
            for (ExpressionToken et : list) {
                System.out.println(et.getTokenType() + " : " + et);
            }
        }
        catch (FormatException e) {
            e.printStackTrace();
        }
    }

    public Operator getOperator(String name) {
        return operators.get(name);
    }

    public List<ExpressionToken> getExpressionTokens(String expression) throws FormatException {
        ExpressionReader eReader = new ExpressionReader(expression);
        ArrayList<ExpressionToken> list = new ArrayList<ExpressionToken>();
        ExpressionToken expressionToken = null;
        Element ele = null;
        try {
            while ((ele = eReader.readToken()) != null) {
                expressionToken = this.changeToToken(expressionToken, ele);
                this.pushParenthesis(ele);
                list.add(expressionToken);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
            throw new FormatException("\u8868\u8fbe\u5f0f\u8bcd\u5143\u683c\u5f0f\u5f02\u5e38");
        }
        if (!this.parenthesis.isEmpty()) {
            throw new FormatException("\u62ec\u53f7\u5339\u914d\u51fa\u9519");
        }
        return list;
    }

    public void pushParenthesis(Element ele) throws FormatException {
        if (Element.ElementType.SPLITOR == ele.getType()) {
            if ("(".equals(ele.getText())) {
                this.parenthesis.push("(");
            } else if (")".equals(ele.getText())) {
                if (this.parenthesis.isEmpty() || !"(".equals(this.parenthesis.peek())) {
                    throw new FormatException("\u62ec\u53f7\u5339\u914d\u51fa\u9519");
                }
                this.parenthesis.pop();
            }
        }
    }

    public ExpressionToken changeToToken(ExpressionToken previousToken, Element ele) throws ParseException {
        if (ele == null) {
            throw new IllegalArgumentException();
        }
        ExpressionToken token = null;
        if (Element.ElementType.NULL == ele.getType()) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_NULL, null);
        } else if (Element.ElementType.STRING == ele.getType()) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_STRING, ele.getText());
        } else if (Element.ElementType.BOOLEAN == ele.getType()) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.valueOf(ele.getText()));
        } else if (Element.ElementType.INT == ele.getType()) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_INT, Integer.valueOf(ele.getText()));
        } else if (Element.ElementType.LONG == ele.getType()) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_LONG, Long.valueOf(ele.getText()));
        } else if (Element.ElementType.FLOAT == ele.getType()) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_FLOAT, Float.valueOf(ele.getText()));
        } else if (Element.ElementType.DOUBLE == ele.getType()) {
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_DOUBLE, Double.valueOf(ele.getText()));
        } else if (Element.ElementType.DATE == ele.getType()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            token = ExpressionToken.createConstantToken(BaseDataMeta.DataType.DATATYPE_DATE, sdf.parse(ele.getText()));
        } else if (Element.ElementType.VARIABLE == ele.getType()) {
            token = ExpressionToken.createVariableToken(ele.getText());
        } else if (Element.ElementType.OPERATOR == ele.getType()) {
            token = "-".equals(ele.getText()) && (previousToken == null || previousToken.getTokenType() == ExpressionToken.ETokenType.ETOKEN_TYPE_OPERATOR || previousToken.getTokenType() == ExpressionToken.ETokenType.ETOKEN_TYPE_SPLITOR && !")".equals(previousToken.getSplitor())) ? ExpressionToken.createOperatorToken(Operator.NG) : ExpressionToken.createOperatorToken(this.getOperator(ele.getText()));
        } else if (Element.ElementType.FUNCTION == ele.getType()) {
            token = ExpressionToken.createFunctionToken(ele.getText());
        } else if (Element.ElementType.SPLITOR == ele.getType()) {
            token = ExpressionToken.createSplitorToken(ele.getText());
        }
        token.setStartPosition(ele.getIndex());
        return token;
    }

    static {
        operators.put(Operator.NOT.getToken(), Operator.NOT);
        operators.put(Operator.MUTI.getToken(), Operator.MUTI);
        operators.put(Operator.DIV.getToken(), Operator.DIV);
        operators.put(Operator.MOD.getToken(), Operator.MOD);
        operators.put(Operator.PLUS.getToken(), Operator.PLUS);
        operators.put(Operator.MINUS.getToken(), Operator.MINUS);
        operators.put(Operator.LT.getToken(), Operator.LT);
        operators.put(Operator.LE.getToken(), Operator.LE);
        operators.put(Operator.GT.getToken(), Operator.GT);
        operators.put(Operator.GE.getToken(), Operator.GE);
        operators.put(Operator.EQ.getToken(), Operator.EQ);
        operators.put(Operator.NEQ.getToken(), Operator.NEQ);
        operators.put(Operator.AND.getToken(), Operator.AND);
        operators.put(Operator.OR.getToken(), Operator.OR);
        operators.put(Operator.APPEND.getToken(), Operator.APPEND);
        operators.put(Operator.SELECT.getToken(), Operator.SELECT);
        operators.put(Operator.QUES.getToken(), Operator.QUES);
        operators.put(Operator.COLON.getToken(), Operator.COLON);
    }
}
