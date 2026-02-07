/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.Element;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;

public class VariableTypeReader
implements ElementReader {
    public static final String STOP_CHAR = "+-*/%^<>=&|!?:#$(),[]'\" \r\n\t";
    public static final String TRUE_WORD = "true";
    public static final String FALSE_WORD = "false";
    public static final String NULL_WORD = "null";

    private String readWord(ExpressionReader sr) throws FormatException, IOException {
        StringBuffer sb = new StringBuffer();
        boolean readStart = true;
        int b = -1;
        while ((b = sr.read()) != -1) {
            char c = (char)b;
            if (STOP_CHAR.indexOf(c) >= 0 && !readStart) {
                sr.reset();
                return sb.toString();
            }
            if (!Character.isJavaIdentifierPart(c)) {
                throw new FormatException("名称不能为非法字符：" + c);
            }
            if (readStart) {
                if (!Character.isJavaIdentifierStart(c)) {
                    throw new FormatException("\u540d\u79f0\u5f00\u5934\u4e0d\u80fd\u4e3a\u5b57\u7b26\uff1a" + c);
                }
                readStart = false;
            }
            sb.append(c);
            sr.mark(0);
        }
        return sb.toString();
    }

    @Override
    public Element read(ExpressionReader sr) throws FormatException, IOException {
        int index = sr.getCruuentIndex();
        String word = this.readWord(sr);
        if (TRUE_WORD.equals(word) || FALSE_WORD.equals(word)) {
            return new Element(word, index, Element.ElementType.BOOLEAN);
        }
        if (NULL_WORD.equals(word)) {
            return new Element(word, index, Element.ElementType.NULL);
        }
        return new Element(word, index, Element.ElementType.VARIABLE);
    }
}
