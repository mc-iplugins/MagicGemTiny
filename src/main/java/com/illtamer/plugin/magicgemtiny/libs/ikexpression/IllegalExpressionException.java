/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression;

public class IllegalExpressionException
extends Exception {
    private static final long serialVersionUID = -382075370364295450L;
    private String errorTokenText;
    private int errorPosition = -1;

    public IllegalExpressionException() {
    }

    public IllegalExpressionException(String msg) {
        super(msg);
    }

    public IllegalExpressionException(String msg, String errorTokenText) {
        super(msg);
        this.errorTokenText = errorTokenText;
    }

    public IllegalExpressionException(String msg, String errorTokenText, int errorPosition) {
        super(msg);
        this.errorPosition = errorPosition;
        this.errorTokenText = errorTokenText;
    }

    public String getErrorTokenText() {
        return this.errorTokenText;
    }

    public void setErrorTokenText(String errorTokenText) {
        this.errorTokenText = errorTokenText;
    }

    public int getErrorPosition() {
        return this.errorPosition;
    }

    public void setErrorPosition(int errorPosition) {
        this.errorPosition = errorPosition;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.getMessage());
        sb.append("\r\n\u5904\u7406\u5bf9\u8c61\uff1a").append(this.errorTokenText);
        sb.append("\r\n\u5904\u7406\u4f4d\u7f6e\uff1a").append(this.errorPosition == -1 ? " unknow " : Integer.valueOf(this.errorPosition));
        return sb.toString();
    }
}
