/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;

import java.util.HashMap;
import java.util.Map;

public class VariableContainer {
    private static final ThreadLocal<Map<String, Variable>> variableMapThreadLocal = new ThreadLocal();

    public static Map<String, Variable> getVariableMap() {
        Map<String, Variable> variableMap = variableMapThreadLocal.get();
        if (variableMap == null) {
            variableMap = new HashMap<String, Variable>();
            variableMapThreadLocal.set(variableMap);
        }
        return variableMap;
    }

    public static void setVariableMap(Map<String, Variable> variableMap) {
        VariableContainer.removeVariableMap();
        if (variableMap != null) {
            variableMapThreadLocal.set(variableMap);
        }
    }

    public static void removeVariableMap() {
        Map<String, Variable> variableMap = variableMapThreadLocal.get();
        if (variableMap != null) {
            variableMap.clear();
        }
        variableMapThreadLocal.remove();
    }

    public static void addVariable(Variable variable) {
        if (variable != null) {
            VariableContainer.getVariableMap().put(variable.getVariableName(), variable);
        }
    }

    public static Variable getVariable(String variableName) {
        if (variableName != null) {
            return VariableContainer.getVariableMap().get(variableName);
        }
        return null;
    }

    public static Variable removeVariable(String variableName) {
        return VariableContainer.getVariableMap().remove(variableName);
    }

    public static Variable removeVariable(Variable variable) {
        if (variable != null) {
            return VariableContainer.getVariableMap().remove(variable.getVariableName());
        }
        return null;
    }
}
