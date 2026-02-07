/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader.ElementReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader.ElementReaderFactory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;

public class ExpressionReader
extends StringReader {
    private static final String IGNORE_CHAR = " \r\n\t";
    private int currentIndex = 0;
    private int markIndex = 0;
    private boolean prefixBlank = false;

    public ExpressionReader(String s) {
        super(s);
    }

    public static void main(String[] a) {
        ExpressionReader eReader = new ExpressionReader(" aa+\"AB\\\\CD\"!=null&&[2008-1-1 12:9]-$max(aa,bb,\"cc\")>2l3f4d1");
        Element ele = null;
        try {
            while ((ele = eReader.readToken()) != null) {
                System.out.println(ele.getType() + "……" + ele.getText() + "……" + ele.getIndex());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (FormatException e) {
            e.printStackTrace();
        }
    }

    public int getCruuentIndex() {
        return this.currentIndex;
    }

    public boolean isPrefixBlank() {
        return this.prefixBlank;
    }

    public void setPrefixBlank(boolean prefixBlank) {
        this.prefixBlank = prefixBlank;
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c != -1) {
            ++this.currentIndex;
            ++this.markIndex;
        }
        return c;
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        int c = super.read(cbuf);
        if (c > 0) {
            this.currentIndex += c;
            this.markIndex += c;
        }
        return c;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        int c = super.read(target);
        if (c > 0) {
            this.currentIndex += c;
            this.markIndex += c;
        }
        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int c = super.read(cbuf, off, len);
        if (c > 0) {
            this.currentIndex += c;
            this.markIndex += c;
        }
        return c;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this.currentIndex -= this.markIndex;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        super.mark(readAheadLimit);
        this.markIndex = 0;
    }

    public Element readToken() throws IOException, FormatException {
        this.prefixBlank = false;
        while (true) {
            this.mark(0);
            int b = this.read();
            if (b == -1) {
                return null;
            }
            char c = (char)b;
            if (IGNORE_CHAR.indexOf(c) < 0) break;
            this.prefixBlank = true;
        }
        this.reset();
        ElementReader er = ElementReaderFactory.createElementReader(this);
        return er.read(this);
    }
}
