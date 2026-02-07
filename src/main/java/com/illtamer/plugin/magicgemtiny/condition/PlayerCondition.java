package com.illtamer.plugin.magicgemtiny.condition;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PlayerCondition {

    /**
     * 判断玩家是否满足该条件
     */
    boolean test(Player player);

}
