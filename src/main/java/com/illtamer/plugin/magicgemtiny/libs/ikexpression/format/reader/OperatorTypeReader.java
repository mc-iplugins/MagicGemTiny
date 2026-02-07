/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.Element;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class OperatorTypeReader
implements ElementReader {
    private static final Set<String> OPERATOR_WORDS = new HashSet<String>();

    public static boolean isOperatorWord(String tokenText) {
        return OPERATOR_WORDS.contains(tokenText);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isOperatorStart(ExpressionReader sr) throws IOException {
        sr.mark(0);
        try {
            StringBuffer sb = new StringBuffer();
            int b = sr.read();
            if (b == -1) {
                boolean bl = false;
                return bl;
            }
            char c = (char)b;
            sb.append(c);
            if (OperatorTypeReader.isOperatorWord(sb.toString())) {
                boolean bl = true;
                return bl;
            }
            while ((b = sr.read()) != -1) {
                c = (char)b;
                sb.append(c);
                if (OperatorTypeReader.isOperatorWord(sb.toString())) {
                    boolean bl = true;
                    return bl;
                }
                if ("+-*/%^<>=&|!?:#$(),[]'\" \r\n\t".indexOf(c) < 0) continue;
                boolean bl = false;
                return bl;
            }
            boolean bl = false;
            return bl;
        }
        finally {
            sr.reset();
        }
    }

    @Override
    public Element read(ExpressionReader sr) throws FormatException, IOException {
        int index = sr.getCruuentIndex();
        StringBuffer sb = new StringBuffer();
        int b = sr.read();
        if (b == -1) {
            throw new FormatException("\u8868\u8fbe\u5f0f\u5df2\u7ed3\u675f");
        }
        char c = (char)b;
        sb.append(c);
        if (OperatorTypeReader.isOperatorWord(sb.toString())) {
            if (sb.length() == 1) {
                sr.mark(0);
                b = sr.read();
                if (b != -1 && OperatorTypeReader.isOperatorWord(sb.toString() + (char)b)) {
                    return new Element(sb.toString() + (char)b, index, Element.ElementType.OPERATOR);
                }
                sr.reset();
            }
            return new Element(sb.toString(), index, Element.ElementType.OPERATOR);
        }
        while ((b = sr.read()) != -1) {
            c = (char)b;
            sb.append(c);
            if (OperatorTypeReader.isOperatorWord(sb.toString())) {
                return new Element(sb.toString(), index, Element.ElementType.OPERATOR);
            }
            if ("+-*/%^<>=&|!?:#$(),[]'\" \r\n\t".indexOf(c) < 0) continue;
            throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u8fd0\u7b97\u7b26\uff1a" + sb.toString());
        }
        throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u8fd0\u7b97\u7b26\u7ed3\u675f");
    }

    static {
        OPERATOR_WORDS.add("+");
        OPERATOR_WORDS.add("-");
        OPERATOR_WORDS.add(">");
        OPERATOR_WORDS.add("<");
        OPERATOR_WORDS.add(">=");
        OPERATOR_WORDS.add("<=");
        OPERATOR_WORDS.add("==");
        OPERATOR_WORDS.add("!=");
        OPERATOR_WORDS.add("*");
        OPERATOR_WORDS.add("/");
        OPERATOR_WORDS.add("%");
        OPERATOR_WORDS.add("&&");
        OPERATOR_WORDS.add("||");
        OPERATOR_WORDS.add("!");
        OPERATOR_WORDS.add("#");
        OPERATOR_WORDS.add("?:");
        OPERATOR_WORDS.add("?");
        OPERATOR_WORDS.add(":");
    }
}
