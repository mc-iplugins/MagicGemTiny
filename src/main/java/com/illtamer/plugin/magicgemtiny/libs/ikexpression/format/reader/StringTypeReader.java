/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.Element;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;

public class StringTypeReader
implements ElementReader {
    public static final char START_MARK = '\"';
    public static final char END_MARK = '\"';
    public static final char ESCAPE_MARK = '\\';

    private static char getEscapeValue(char c) throws FormatException {
        if (c == '\\' || c == '\"') {
            return c;
        }
        if (c == 'n') {
            return '\n';
        }
        if (c == 'r') {
            return '\r';
        }
        if (c == 't') {
            return '\t';
        }
        throw new FormatException("\u5b57\u7b26\u8f6c\u4e49\u51fa\u9519");
    }

    @Override
    public Element read(ExpressionReader sr) throws FormatException, IOException {
        int index = sr.getCruuentIndex();
        StringBuffer sb = new StringBuffer();
        int b = sr.read();
        if (b == -1 || b != 34) {
            throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u5b57\u7b26\u7a9c\u5f00\u59cb");
        }
        while ((b = sr.read()) != -1) {
            char c = (char)b;
            if (c == '\\') {
                c = StringTypeReader.getEscapeValue((char)sr.read());
            } else if (c == '\"') {
                return new Element(sb.toString(), index, Element.ElementType.STRING);
            }
            sb.append(c);
        }
        throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u5b57\u7b26\u7a9c\u7ed3\u675f");
    }
}
