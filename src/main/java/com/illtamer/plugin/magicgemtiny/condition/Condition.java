package com.illtamer.plugin.magicgemtiny.condition;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface Condition {

    /**
     * 判断物品是否满足默认条件
     * @param andMode true-全部条件满足、false-任意条件满足
     * */
    default boolean checkWithDefaultRequires(ItemStack item, boolean andMode) {
        return check(item, andMode, null);
    }

    /**
     * 判断物品是否满足指定条件
     * @apiNote 满足任意条件即可
     * */
    default boolean check(ItemStack item, @Nullable List<String> requires) {
        return check(item, false, requires);
    }

    /**
     * 判断物品是否满足指定条件
     * @param andMode true-全部条件满足、false-任意条件满足
     * */
    boolean check(ItemStack item, boolean andMode, @Nullable List<String> requires);

}
