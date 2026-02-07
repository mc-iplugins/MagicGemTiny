/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.function;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.IllegalExpressionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.BaseDataMeta;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Reference;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class FunctionExecution {
    private FunctionExecution() {
    }

    public static Constant execute(String functionName, int position, Constant[] args) throws IllegalExpressionException {
        Object[] parameters;
        if (functionName == null) {
            throw new IllegalArgumentException("\u51fd\u6570\u540d\u4e3a\u7a7a");
        }
        if (args == null) {
            throw new IllegalArgumentException("\u51fd\u6570\u53c2\u6570\u5217\u8868\u4e3a\u7a7a");
        }
        for (int i = 0; i < args.length; ++i) {
            if (!args[i].isReference()) continue;
            Reference ref = (Reference)args[i].getDataValue();
            args[i] = ref.execute();
        }
        try {
            parameters = FunctionExecution.convertParameters(functionName, position, args);
        }
        catch (IllegalExpressionException e) {
            throw new IllegalArgumentException("\u51fd\u6570\"" + functionName + "\"\u8fd0\u884c\u65f6\u53c2\u6570\u7c7b\u578b\u9519\u8bef");
        }
        try {
            Object result = FunctionLoader.invokeFunction(functionName, parameters);
            if (result instanceof Boolean) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, result);
            }
            if (result instanceof Date) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_DATE, result);
            }
            if (result instanceof Double) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_DOUBLE, result);
            }
            if (result instanceof Float) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_FLOAT, result);
            }
            if (result instanceof Integer) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_INT, result);
            }
            if (result instanceof Long) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_LONG, result);
            }
            if (result instanceof String) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_STRING, result);
            }
            if (result instanceof List) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_LIST, result);
            }
            return new Constant(BaseDataMeta.DataType.DATATYPE_OBJECT, result);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalStateException("\u51fd\u6570\"" + functionName + "\"\u4e0d\u5b58\u5728\u6216\u53c2\u6570\u7c7b\u578b\u4e0d\u5339\u914d");
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IllegalStateException("\u51fd\u6570\"" + functionName + "\"\u53c2\u6570\u7c7b\u578b\u4e0d\u5339\u914d");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("\u51fd\u6570\"" + functionName + "\"\u8bbf\u95ee\u5f02\u5e38:" + e.getMessage());
        }
    }

    public static Constant varify(String functionName, int position, BaseDataMeta[] args) throws IllegalExpressionException {
        if (functionName == null) {
            throw new IllegalArgumentException("\u51fd\u6570\u540d\u4e3a\u7a7a");
        }
        try {
            Method funtion = FunctionLoader.loadFunction(functionName);
            Class<?>[] parametersType = funtion.getParameterTypes();
            if (args.length == parametersType.length) {
                for (int i = args.length - 1; i >= 0; --i) {
                    Class<?> javaType = args[i].mapTypeToJavaClass();
                    if (javaType == null || FunctionExecution.isCompatibleType(parametersType[parametersType.length - i - 1], javaType)) continue;
                    throw new IllegalExpressionException("\u51fd\u6570\"" + functionName + "\"\u53c2\u6570\u7c7b\u578b\u4e0d\u5339\u914d,\u51fd\u6570\u53c2\u6570\u5b9a\u4e49\u7c7b\u578b\u4e3a\uff1a" + parametersType[i].getName() + " \u4f20\u5165\u53c2\u6570\u5b9e\u9645\u7c7b\u578b\u4e3a\uff1a" + javaType.getName(), functionName, position);
                }
            } else {
                throw new IllegalExpressionException("\u51fd\u6570\"" + functionName + "\"\u53c2\u6570\u4e2a\u6570\u4e0d\u5339\u914d", functionName, position);
            }
            Class<?> returnType = funtion.getReturnType();
            if (Boolean.TYPE == returnType || Boolean.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_BOOLEAN, Boolean.FALSE);
            }
            if (Date.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_DATE, null);
            }
            if (Double.TYPE == returnType || Double.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_DOUBLE, 0.0);
            }
            if (Float.TYPE == returnType || Float.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_FLOAT, Float.valueOf(0.0f));
            }
            if (Integer.TYPE == returnType || Integer.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_INT, 0);
            }
            if (Long.TYPE == returnType || Long.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_LONG, 0L);
            }
            if (String.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_STRING, null);
            }
            if (List.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_LIST, null);
            }
            if (Object.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_OBJECT, null);
            }
            if (Void.TYPE == returnType || Void.class == returnType) {
                return new Constant(BaseDataMeta.DataType.DATATYPE_OBJECT, null);
            }
            throw new IllegalStateException("\u89e3\u6790\u5668\u5185\u90e8\u9519\u8bef\uff1a\u4e0d\u652f\u6301\u7684\u51fd\u6570\u8fd4\u56de\u7c7b\u578b");
        }
        catch (SecurityException e) {
            throw new IllegalExpressionException("\u51fd\u6570\"" + functionName + "\"\u4e0d\u5b58\u5728\u6216\u53c2\u6570\u7c7b\u578b\u4e0d\u5339\u914d", functionName, position);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalExpressionException("\u51fd\u6570\"" + functionName + "\"\u4e0d\u5b58\u5728\u6216\u53c2\u6570\u7c7b\u578b\u4e0d\u5339\u914d", functionName, position);
        }
    }

    private static Object[] convertParameters(String functionName, int position, Constant[] args) throws IllegalExpressionException {
        if (args == null) {
            return new Object[0];
        }
        Object[] parameters = new Object[args.length];
        for (int i = args.length - 1; i >= 0; --i) {
            try {
                parameters[args.length - 1 - i] = args[i].toJavaObject();
                continue;
            }
            catch (ParseException e1) {
                throw new IllegalExpressionException("\u51fd\u6570\"" + functionName + "\"\u53c2\u6570\u8f6c\u5316Java\u5bf9\u8c61\u9519\u8bef");
            }
        }
        return parameters;
    }

    private static boolean isCompatibleType(Class<?> parametersType, Class<?> argType) {
        if (Object.class == parametersType) {
            return true;
        }
        if (parametersType == argType) {
            return true;
        }
        if (Double.TYPE == parametersType) {
            return Float.TYPE == argType || Long.TYPE == argType || Integer.TYPE == argType;
        }
        if (Double.class == parametersType) {
            return Double.TYPE == argType;
        }
        if (Float.TYPE == parametersType) {
            return Long.TYPE == argType || Integer.TYPE == argType;
        }
        if (Float.class == parametersType) {
            return Float.TYPE == argType;
        }
        if (Long.TYPE == parametersType) {
            return Integer.TYPE == argType;
        }
        if (Long.class == parametersType) {
            return Long.TYPE == argType;
        }
        if (Integer.class == parametersType) {
            return Integer.TYPE == argType;
        }
        return false;
    }
}
