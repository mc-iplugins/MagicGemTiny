/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;

public class ElementReaderFactory {
    public static ElementReader createElementReader(ExpressionReader reader) throws IOException, FormatException {
        reader.mark(0);
        int b = reader.read();
        reader.reset();
        if (b != -1) {
            char c = (char)b;
            try {
                if (c == '\"') {
                    return (ElementReader)StringTypeReader.class.newInstance();
                }
                if (c == '^') {
                    return (ElementReader)DateTypeReader.class.newInstance();
                }
                if (c == '$') {
                    return (ElementReader)FunctionTypeReader.class.newInstance();
                }
                if ("(),".indexOf(c) >= 0) {
                    return (ElementReader)SplitorTypeReader.class.newInstance();
                }
                if ("01234567890.".indexOf(c) >= 0) {
                    return (ElementReader)NumberTypeReader.class.newInstance();
                }
                if (OperatorTypeReader.isOperatorStart(reader)) {
                    return (ElementReader)OperatorTypeReader.class.newInstance();
                }
                return (ElementReader)VariableTypeReader.class.newInstance();
            }
            catch (Exception e) {
                throw new FormatException(e);
            }
        }
        throw new FormatException("\u6d41\u5df2\u7ed3\u675f");
    }
}
