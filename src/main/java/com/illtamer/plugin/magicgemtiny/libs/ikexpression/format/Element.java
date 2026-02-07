/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format;

public class Element {
    private String text;
    private ElementType type;
    private int index;

    public Element(String text, int index, ElementType type) {
        this.text = text;
        this.index = index;
        this.type = type;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ElementType getType() {
        return this.type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public static enum ElementType {
        NULL,
        STRING,
        BOOLEAN,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        DATE,
        VARIABLE,
        OPERATOR,
        FUNCTION,
        SPLITOR;

    }
}
