/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.Element;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;

public class FunctionTypeReader
implements ElementReader {
    public static final char START_MARK = '$';
    public static final char END_MARK = '(';

    @Override
    public Element read(ExpressionReader sr) throws FormatException, IOException {
        int index = sr.getCruuentIndex();
        StringBuffer sb = new StringBuffer();
        int b = sr.read();
        if (b == -1 || b != 36) {
            throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u51fd\u6570\u5f00\u59cb");
        }
        boolean readStart = true;
        while ((b = sr.read()) != -1) {
            char c = (char)b;
            if (c == '(') {
                if (sb.length() == 0) {
                    throw new FormatException("\u51fd\u6570\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a");
                }
                sr.reset();
                return new Element(sb.toString(), index, Element.ElementType.FUNCTION);
            }
            if (!Character.isJavaIdentifierPart(c)) {
                throw new FormatException("\u540d\u79f0\u4e0d\u80fd\u4e3a\u975e\u6cd5\u5b57\u7b26\uff1a" + c);
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
        throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u51fd\u6570\u7ed3\u675f");
    }
}
