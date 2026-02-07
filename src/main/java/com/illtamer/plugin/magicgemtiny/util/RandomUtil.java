package com.illtamer.plugin.magicgemtiny.util;

import java.util.Random;

public class RandomUtil {

    public static boolean success(double successValue) {
        if (successValue == 0) {
            return false;
        }
        return successValue >= new Random().nextDouble() * 100;
    }

}
