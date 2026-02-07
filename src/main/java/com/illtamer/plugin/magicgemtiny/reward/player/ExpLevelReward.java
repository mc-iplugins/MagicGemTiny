package com.illtamer.plugin.magicgemtiny.reward.player;

import com.illtamer.plugin.magicgemtiny.reward.PlayerReward;
import org.bukkit.entity.Player;

/**
 * 经验等级奖励
 * */
public class ExpLevelReward extends PlayerReward {

    @Override
    public void execute(Player player) {
        // TODO 暂不支持表达式
        // amount为表达式，你可以使用PAPI和各种函数
        // 可以使用物品变量，但仅在物品宝石中生效，在玩家宝石中无效
        Integer amount = getParamInteger("amount", player);
        if (amount == null) {
            amount = getParamInteger("level", "l", player);
        }
        if (amount == null) {
            return;
        }

        int level = player.getLevel();
        player.setLevel(level+amount);
    }

}
