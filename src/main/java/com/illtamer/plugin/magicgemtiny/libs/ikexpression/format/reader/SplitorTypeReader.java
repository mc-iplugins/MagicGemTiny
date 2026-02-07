/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.Element;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;

public class SplitorTypeReader
implements ElementReader {
    public static final String SPLITOR_CHAR = "(),";

    @Override
    public Element read(ExpressionReader sr) throws FormatException, IOException {
        int index = sr.getCruuentIndex();
        int b = sr.read();
        char c = (char)b;
        if (b == -1 || SPLITOR_CHAR.indexOf(c) == -1) {
            throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u5206\u5272\u5b57\u7b26");
        }
        return new Element(Character.toString(c), index, Element.ElementType.SPLITOR);
    }
}
