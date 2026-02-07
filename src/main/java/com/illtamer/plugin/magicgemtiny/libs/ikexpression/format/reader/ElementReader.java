/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.Element;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;

public interface ElementReader {
    public Element read(ExpressionReader var1) throws FormatException, IOException;
}
