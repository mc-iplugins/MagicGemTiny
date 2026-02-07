/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.Element;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;

public class NumberTypeReader
implements ElementReader {
    public static final String NUMBER_CHARS = "01234567890.";
    public static final String LONG_MARKS = "lL";
    public static final String FLOAT_MARKS = "fF";
    public static final String DOUBLE_MARKS = "dD";

    public static void checkDecimal(StringBuffer sb) throws FormatException {
        if (sb.indexOf(".") != sb.lastIndexOf(".")) {
            throw new FormatException("\u6570\u5b57\u6700\u591a\u53ea\u80fd\u6709\u4e00\u4e2a\u5c0f\u6570\u70b9");
        }
    }

    @Override
    public Element read(ExpressionReader sr) throws FormatException, IOException {
        int index = sr.getCruuentIndex();
        StringBuffer sb = new StringBuffer();
        int b = -1;
        while ((b = sr.read()) != -1) {
            char c = (char)b;
            if (NUMBER_CHARS.indexOf(c) == -1) {
                if (LONG_MARKS.indexOf(c) >= 0) {
                    if (sb.indexOf(".") >= 0) {
                        throw new FormatException("long\u7c7b\u578b\u4e0d\u80fd\u6709\u5c0f\u6570\u70b9");
                    }
                    return new Element(sb.toString(), index, Element.ElementType.LONG);
                }
                if (FLOAT_MARKS.indexOf(c) >= 0) {
                    NumberTypeReader.checkDecimal(sb);
                    return new Element(sb.toString(), index, Element.ElementType.FLOAT);
                }
                if (DOUBLE_MARKS.indexOf(c) >= 0) {
                    NumberTypeReader.checkDecimal(sb);
                    return new Element(sb.toString(), index, Element.ElementType.DOUBLE);
                }
                sr.reset();
                if (sb.indexOf(".") >= 0) {
                    NumberTypeReader.checkDecimal(sb);
                    return new Element(sb.toString(), index, Element.ElementType.DOUBLE);
                }
                return new Element(sb.toString(), index, Element.ElementType.INT);
            }
            sb.append(c);
            sr.mark(0);
        }
        if (sb.indexOf(".") >= 0) {
            NumberTypeReader.checkDecimal(sb);
            return new Element(sb.toString(), index, Element.ElementType.DOUBLE);
        }
        return new Element(sb.toString(), index, Element.ElementType.INT);
    }
}
