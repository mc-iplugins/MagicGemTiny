/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreparedExpression {
    private final String orgExpression;
    private final List<ExpressionToken> expTokens;
    private final Map<String, Variable> variableMap;

    PreparedExpression(String orgExpression, List<ExpressionToken> expTokens, Map<String, Variable> variableMap) {
        this.orgExpression = orgExpression;
        this.expTokens = expTokens;
        this.variableMap = new HashMap<String, Variable>(variableMap);
    }

    public synchronized void setArgument(String name, Object value) {
        Variable v = this.variableMap.get(name);
        if (v != null) {
            v.setVariableValue(value);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object execute() {
        ExpressionExecutor ee = new ExpressionExecutor();
        try {
            VariableContainer.setVariableMap(new HashMap<String, Variable>(this.variableMap));
            Constant constant = ee.execute(this.expTokens);
            Object result = constant.toJavaObject();
            if (result instanceof Integer) {
                Double d = ((Integer)result).doubleValue();
                return d;
            }
            Object object = result;
            return object;
        }
        catch (ParseException | IllegalExpressionException e) {
            e.printStackTrace();
            Object var3_4 = null;
            return var3_4;
        }
        finally {
            VariableContainer.removeVariableMap();
        }
    }

    public String toString() {
        return this.orgExpression;
    }

    public PreparedExpression clone() {
        return new PreparedExpression(this.orgExpression, this.expTokens, new HashMap<String, Variable>(this.variableMap));
    }
}
