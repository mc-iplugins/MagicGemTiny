/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.function;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ThreadLocalRandom;

public class SystemFunctions {
    public boolean contains(String str1, String str2) {
        if (str1 == null || str2 == null) {
            throw new NullPointerException("\u51fd\u6570\"CONTAINS\"\u53c2\u6570\u4e3a\u7a7a");
        }
        return str1.indexOf(str2) >= 0;
    }

    public double random() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public double logarithm(double input) {
        if (input <= 0.0) {
            return Double.NEGATIVE_INFINITY;
        }
        return Math.log(input);
    }

    public double sqrt(double input) {
        if (input < 0.0) {
            return 0.0;
        }
        return Math.sqrt(input);
    }

    public double pow(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    public double floor(double input) {
        return Math.floor(input);
    }

    public double randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public boolean startsWith(String str1, String str2) {
        if (str1 == null || str2 == null) {
            throw new NullPointerException("\u51fd\u6570\"STARTSWITH\"\u53c2\u6570\u4e3a\u7a7a");
        }
        return str1.startsWith(str2);
    }

    public boolean endsWith(String str1, String str2) {
        if (str1 == null || str2 == null) {
            throw new NullPointerException("\u51fd\u6570\"ENDSWITH\"\u53c2\u6570\u4e3a\u7a7a");
        }
        return str1.endsWith(str2);
    }

    public Date calcDate(Date date, int years, int months, int days, int hours, int minutes, int seconds) {
        if (date == null) {
            throw new NullPointerException("\u51fd\u6570\"CALCDATE\"\u53c2\u6570\u4e3a\u7a7a");
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(1, years);
        calendar.add(2, months);
        calendar.add(5, days);
        calendar.add(10, hours);
        calendar.add(12, minutes);
        calendar.add(13, seconds);
        return calendar.getTime();
    }

    public Date sysDate() {
        return new Date();
    }

    public boolean dayEquals(Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dayOfDate1 = sdf.format(date1);
        String dayOfDate2 = sdf.format(date2);
        return dayOfDate1.equals(dayOfDate2);
    }
}
