package com.illtamer.plugin.magicgemtiny.condition;

import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import de.tr7zw.nbtapi.NBTItem;

@FunctionalInterface
public interface ItemCondition {

    /**
     * 判断物品是否满足该条件
     * @throws ConditionException e
     */
    boolean test(NBTItem nbtItem);

}
