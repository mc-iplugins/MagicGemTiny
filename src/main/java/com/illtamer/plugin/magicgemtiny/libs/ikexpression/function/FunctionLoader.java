/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.function;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class FunctionLoader {
    private static final String FILE_NAME = "/IKExpression.cfg.xml";
    private static final FunctionLoader single = new FunctionLoader();
    private final HashMap<String, FunctionInvoker> functionMap = new HashMap();

    private FunctionLoader() {
        try {
            this.init();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    public static void addFunction(String functionName, Object instance, Method method) {
        if (functionName == null || instance == null || method == null) {
            return;
        }
        HashMap<String, FunctionInvoker> hashMap = FunctionLoader.single.functionMap;
        FunctionLoader functionLoader = single;
        Objects.requireNonNull(functionLoader);
        hashMap.put(functionName, functionLoader.new FunctionInvoker(method, instance));
    }

    public static Method loadFunction(String functionName) throws NoSuchMethodException {
        FunctionInvoker f = FunctionLoader.single.functionMap.get(functionName);
        if (f == null) {
            throw new NoSuchMethodException();
        }
        return f.method;
    }

    public static Object invokeFunction(String functionName, Object[] parameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        FunctionInvoker f = FunctionLoader.single.functionMap.get(functionName);
        if (f == null) {
            throw new NoSuchMethodException();
        }
        return f.invoke(parameters);
    }

    private void init() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(FunctionLoader.class.getResourceAsStream(FILE_NAME));
        NodeList rootNodes = doc.getElementsByTagName("function-configuration");
        if (rootNodes.getLength() < 1) {
            return;
        }
        rootNodes = rootNodes.item(0).getChildNodes();
        for (int i = 0; i < rootNodes.getLength(); ++i) {
            Node beanNode = rootNodes.item(i);
            if (!"bean".equals(beanNode.getNodeName())) continue;
            String className = beanNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> _class = Class.forName(className);
            NodeList subNodes = beanNode.getChildNodes();
            List<Parameter> constructorArgs = null;
            HashSet<Function> functions = new HashSet<Function>();
            for (int j = 0; j < subNodes.getLength(); ++j) {
                Node subNode = subNodes.item(j);
                if ("constructor-args".equals(subNode.getNodeName()) && constructorArgs == null) {
                    constructorArgs = this.parseConstructorArgs(subNode);
                    continue;
                }
                if (!"function".equals(subNode.getNodeName()) || functions.add(this.parseFunctions(subNode))) continue;
                throw new SAXException("\u65b9\u6cd5\u540d\u4e0d\u80fd\u91cd\u590d");
            }
            if (functions.size() <= 0) continue;
            Object ins = null;
            if (constructorArgs == null || constructorArgs.size() <= 0) {
                ins = _class.newInstance();
            } else {
                Class[] cs = this.getParameterTypes(constructorArgs);
                Object[] ps = this.getParameterValues(constructorArgs);
                Constructor<?> c = _class.getConstructor(cs);
                ins = c.newInstance(ps);
            }
            for (Function f : functions) {
                Method m = _class.getMethod(f.methodName, this.getParameterTypes(f.types));
                this.functionMap.put(f.name, new FunctionInvoker(m, ins));
            }
        }
    }

    private List<Parameter> parseConstructorArgs(Node argRootNode) {
        NodeList argsNode = argRootNode.getChildNodes();
        ArrayList<Parameter> args = new ArrayList<Parameter>();
        for (int i = 0; i < argsNode.getLength(); ++i) {
            Node argNode = argsNode.item(i);
            if (!"constructor-arg".equals(argNode.getNodeName())) continue;
            String type = argNode.getAttributes().getNamedItem("type").getNodeValue();
            String value = argNode.getTextContent();
            args.add(new Parameter(type, value));
        }
        return args;
    }

    private Function parseFunctions(Node funRootNode) {
        String name = funRootNode.getAttributes().getNamedItem("name").getNodeValue();
        String methodName = funRootNode.getAttributes().getNamedItem("method").getNodeValue();
        Function f = new Function(name, methodName);
        NodeList argsNode = funRootNode.getChildNodes();
        for (int i = 0; i < argsNode.getLength(); ++i) {
            Node argNode = argsNode.item(i);
            if (!"parameter-type".equals(argNode.getNodeName())) continue;
            f.addType(argNode.getTextContent());
        }
        return f;
    }

    private Class[] getParameterTypes(List<Parameter> parameters) {
        if (parameters == null) {
            return null;
        }
        Class[] types = new Class[parameters.size()];
        for (int i = 0; i < parameters.size(); ++i) {
            types[i] = parameters.get((int)i).type;
        }
        return types;
    }

    private Object[] getParameterValues(List<Parameter> parameters) {
        if (parameters == null) {
            return null;
        }
        Object[] values = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); ++i) {
            values[i] = parameters.get((int)i).value;
        }
        return values;
    }

    private class FunctionInvoker {
        Method method;
        Object instance;

        public FunctionInvoker(Method m, Object i) {
            this.method = m;
            this.instance = i;
        }

        public Object invoke(Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            return this.method.invoke(this.instance, args);
        }
    }

    private class Function {
        String name;
        String methodName;
        List<Parameter> types;

        public Function(String _name, String _methodName) {
            if (_name == null || _methodName == null) {
                throw new IllegalArgumentException();
            }
            this.name = _name;
            this.methodName = _methodName;
            this.types = new ArrayList<Parameter>();
        }

        public void addType(String type) {
            this.types.add(new Parameter(type));
        }

        public int hashCode() {
            return this.name.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            Function other = (Function)obj;
            return this.name.equals(other.name);
        }
    }

    private class Parameter {
        Class type;
        Object value;

        public Parameter(String _type, String Value) {
            try {
                this.type = this.getTypeClass(_type);
                Constructor c = this.type.getConstructor(String.class);
                this.value = c.newInstance(Value);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Parameter(String _type) {
            try {
                this.type = this.getTypeClass(_type);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Class getTypeClass(String _type) throws ClassNotFoundException {
            if ("boolean".equals(_type)) {
                return Boolean.TYPE;
            }
            if ("byte".equals(_type)) {
                return Byte.TYPE;
            }
            if ("char".equals(_type)) {
                return Character.TYPE;
            }
            if ("double".equals(_type)) {
                return Double.TYPE;
            }
            if ("float".equals(_type)) {
                return Float.TYPE;
            }
            if ("int".equals(_type)) {
                return Integer.TYPE;
            }
            if ("long".equals(_type)) {
                return Long.TYPE;
            }
            if ("short".equals(_type)) {
                return Short.TYPE;
            }
            return Class.forName(_type);
        }
    }
}
