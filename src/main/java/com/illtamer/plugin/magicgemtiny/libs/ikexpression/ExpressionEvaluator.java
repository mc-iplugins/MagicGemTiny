/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

public class ExpressionEvaluator {
    public static String compile(String expression) {
        return ExpressionEvaluator.compile(expression, null);
    }

    public static String compile(String expression, Collection<Variable> variables) {
        if (expression == null) {
            throw new RuntimeException("\u8868\u8fbe\u5f0f\u4e3a\u7a7a");
        }
        ExpressionExecutor ee = new ExpressionExecutor();
        try {
            if (variables != null && variables.size() > 0) {
                for (Variable var : variables) {
                    VariableContainer.addVariable(var);
                }
            }
            List<ExpressionToken> expTokens = ee.analyze(expression);
            expTokens = ee.compile(expTokens);
            String string = ee.tokensToString(expTokens);
            return string;
        }
        catch (IllegalExpressionException e) {
            e.printStackTrace();
            throw new RuntimeException("\u8868\u8fbe\u5f0f\uff1a\"" + expression + "\" \u7f16\u8bd1\u671f\u68c0\u67e5\u5f02\u5e38");
        }
        finally {
            VariableContainer.removeVariableMap();
        }
    }

    public static PreparedExpression preparedCompile(String expression, Collection<Variable> variables) {
        if (expression == null) {
            throw new RuntimeException("\u8868\u8fbe\u5f0f\u4e3a\u7a7a");
        }
        ExpressionExecutor ee = new ExpressionExecutor();
        try {
            PreparedExpression pe;
            if (variables != null && variables.size() > 0) {
                for (Variable var : variables) {
                    VariableContainer.addVariable(var);
                }
            }
            List<ExpressionToken> expTokens = ee.analyze(expression);
            expTokens = ee.compile(expTokens);
            PreparedExpression preparedExpression = pe = new PreparedExpression(expression, expTokens, VariableContainer.getVariableMap());
            return preparedExpression;
        }
        catch (IllegalExpressionException e) {
            e.printStackTrace();
            throw new RuntimeException("\u8868\u8fbe\u5f0f\uff1a\"" + expression + "\" \u9884\u7f16\u8bd1\u5f02\u5e38");
        }
        finally {
            VariableContainer.removeVariableMap();
        }
    }

    public static Object evaluate(String expression) {
        return ExpressionEvaluator.evaluate(expression, null);
    }

    public static Object evaluate(String expression, Collection<Variable> variables) {
        if (expression == null) {
            return null;
        }
        ExpressionExecutor ee = new ExpressionExecutor();
        try {
            if (variables != null && variables.size() > 0) {
                for (Variable var : variables) {
                    VariableContainer.addVariable(var);
                }
            }
            List<ExpressionToken> expTokens = ee.analyze(expression);
            expTokens = ee.compile(expTokens);
            Constant constant = ee.execute(expTokens);
            Object object = constant.toJavaObject();
            return object;
        }
        catch (ParseException | IllegalExpressionException e) {
            e.printStackTrace();
            throw new RuntimeException("\u8868\u8fbe\u5f0f\uff1a\"" + expression + "\" \u6267\u884c\u5f02\u5e38");
        }
        finally {
            VariableContainer.removeVariableMap();
        }
    }

    public static void addVarible(Variable variable) {
        VariableContainer.addVariable(variable);
    }

    public static void addVaribles(Collection<Variable> variables) {
        if (variables != null && variables.size() > 0) {
            for (Variable var : variables) {
                VariableContainer.addVariable(var);
            }
        }
    }
}
