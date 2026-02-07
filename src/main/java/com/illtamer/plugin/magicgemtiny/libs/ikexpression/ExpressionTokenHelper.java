/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.Operator;

public class ExpressionTokenHelper {
    public static boolean isNull(String s) {
        return "null".equals(s);
    }

    public static boolean isBoolean(String s) {
        return "true".equals(s) || "false".equals(s);
    }

    public static boolean isInteger(String s) {
        if (s != null && s.length() > 0) {
            if (s.length() == 1) {
                return ExpressionTokenHelper.isNumber(s.charAt(0)) && '.' != s.charAt(0);
            }
            return ExpressionTokenHelper.isNumber(s.charAt(0)) && ExpressionTokenHelper.isNumber(s.charAt(s.length() - 1)) && s.indexOf(46) < 0;
        }
        return false;
    }

    public static boolean isDouble(String s) {
        if (s != null && s.length() > 1) {
            return ExpressionTokenHelper.isNumber(s.charAt(0)) && ExpressionTokenHelper.isNumber(s.charAt(s.length() - 1)) && s.indexOf(46) >= 0;
        }
        return false;
    }

    public static boolean isLong(String s) {
        if (s != null && s.length() > 1) {
            return ExpressionTokenHelper.isNumber(s.charAt(0)) && s.endsWith("L");
        }
        return false;
    }

    public static boolean isFloat(String s) {
        if (s != null && s.length() > 1) {
            return ExpressionTokenHelper.isNumber(s.charAt(0)) && s.endsWith("F");
        }
        return false;
    }

    public static boolean isString(String s) {
        if (s != null && s.length() > 1) {
            return s.charAt(0) == '\"';
        }
        return false;
    }

    public static boolean isDateTime(String s) {
        if (s != null && s.length() > 1) {
            return s.charAt(0) == '[';
        }
        return false;
    }

    public static boolean isSplitor(String s) {
        return ",".equals(s) || "(".equals(s) || ")".equals(s);
    }

    public static boolean isFunction(String s) {
        if (s != null && s.length() > 1) {
            return s.charAt(0) == '$';
        }
        return false;
    }

    public static boolean isOperator(String s) {
        if (s != null) {
            try {
                Operator.valueOf(s);
                return true;
            }
            catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9' || c == '.';
    }
}
