/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.reader;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.Element;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.ExpressionReader;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.format.FormatException;

import java.io.IOException;
import java.io.StringReader;

public class DateTypeReader
implements ElementReader {
    public static final char START_MARK = '^';
    public static final char END_MARK = '^';
    public static final String DATE_CHARS = "0123456789-:. ";

    public static String formatTime(String time) throws FormatException {
        if (time == null) {
            throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u65f6\u95f4\u8868\u8fbe\u5f0f");
        }
        StringReader sr = new StringReader(time.trim());
        StringBuffer sb = new StringBuffer();
        int b = -1;
        try {
            while ((b = sr.read()) != -1) {
                int find;
                char c = (char)b;
                if (sb.length() < 4) {
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        throw new FormatException("\u5e74\u4efd\u5fc5\u9700\u4e3a4\u4f4d\u6570\u5b57");
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 4) {
                    if (c != '-') {
                        throw new FormatException("\u65e5\u671f\u5206\u5272\u7b26\u5fc5\u9700\u4e3a\u201c\uff0d\u201d");
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 5) {
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        throw new FormatException("\u6708\u4efd\u5fc5\u9700\u4e3a2\u4f4d\u4ee5\u5185\u7684\u6570\u5b57");
                    }
                    sb.append(c);
                    sr.mark(0);
                    c = (char)sr.read();
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        sb.insert(5, '0');
                        sr.reset();
                        continue;
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 7) {
                    if (c != '-') {
                        throw new FormatException("\u65e5\u671f\u5206\u5272\u7b26\u5fc5\u9700\u4e3a\u201c\uff0d\u201d");
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 8) {
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        throw new FormatException("\u65e5\u5fc5\u9700\u4e3a2\u4f4d\u4ee5\u5185\u7684\u6570\u5b57");
                    }
                    sb.append(c);
                    sr.mark(0);
                    c = (char)sr.read();
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        sb.insert(8, '0');
                        sr.reset();
                        continue;
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 10) {
                    if (c != ' ') {
                        throw new FormatException("\u65e5\u671f\u540e\u5206\u5272\u7b26\u5fc5\u9700\u4e3a\u201c \u201d");
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 11) {
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        throw new FormatException("\u5c0f\u65f6\u5fc5\u9700\u4e3a2\u4f4d\u4ee5\u5185\u7684\u6570\u5b57");
                    }
                    sb.append(c);
                    sr.mark(0);
                    c = (char)sr.read();
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        sb.insert(11, '0');
                        sr.reset();
                        continue;
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 13) {
                    if (c != ':') {
                        throw new FormatException("\u65f6\u95f4\u5206\u5272\u7b26\u5fc5\u9700\u4e3a\u201c:\u201d");
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 14) {
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        throw new FormatException("\u5206\u949f\u5fc5\u9700\u4e3a2\u4f4d\u4ee5\u5185\u7684\u6570\u5b57");
                    }
                    sb.append(c);
                    sr.mark(0);
                    c = (char)sr.read();
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        sb.insert(14, '0');
                        sr.reset();
                        continue;
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 16) {
                    if (c != ':') {
                        throw new FormatException("\u65f6\u95f4\u5206\u5272\u7b26\u5fc5\u9700\u4e3a\u201c:\u201d");
                    }
                    sb.append(c);
                    continue;
                }
                if (sb.length() == 17) {
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        throw new FormatException("\u79d2\u5fc5\u9700\u4e3a2\u4f4d\u4ee5\u5185\u7684\u6570\u5b57");
                    }
                    sb.append(c);
                    sr.mark(0);
                    c = (char)sr.read();
                    find = DATE_CHARS.indexOf(c);
                    if (find == -1 || find > 9) {
                        sb.insert(17, '0');
                        sr.reset();
                        continue;
                    }
                    sb.append(c);
                    continue;
                }
                throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u65f6\u95f4\u8868\u8fbe\u5f0f");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u65f6\u95f4\u8868\u8fbe\u5f0f");
        }
        if (sb.length() == 10) {
            sb.append(" 00:00:00");
        } else if (sb.length() == 16) {
            sb.append(":00");
        }
        if (sb.length() != 19) {
            throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u65f6\u95f4\u8868\u8fbe\u5f0f");
        }
        return sb.toString();
    }

    @Override
    public Element read(ExpressionReader sr) throws FormatException, IOException {
        int index = sr.getCruuentIndex();
        StringBuffer sb = new StringBuffer();
        int b = sr.read();
        if (b == -1 || b != 94) {
            throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u65f6\u95f4\u5f00\u59cb");
        }
        while ((b = sr.read()) != -1) {
            char c = (char)b;
            if (c == '^') {
                return new Element(DateTypeReader.formatTime(sb.toString()), index, Element.ElementType.DATE);
            }
            if (DATE_CHARS.indexOf(c) == -1) {
                throw new FormatException("\u65f6\u95f4\u7c7b\u578b\u4e0d\u80fd\u5305\u51fd\u975e\u6cd5\u5b57\u7b26\uff1a" + c);
            }
            sb.append(c);
        }
        throw new FormatException("\u4e0d\u662f\u6709\u6548\u7684\u65f6\u95f4\u7ed3\u675f");
    }
}
