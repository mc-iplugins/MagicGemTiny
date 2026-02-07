package com.illtamer.plugin.magicgemtiny.reward.player;

import com.illtamer.plugin.magicgemtiny.reward.PlayerReward;
import org.bukkit.entity.Player;

/**
 * 最大生命上线奖励
 * */
public class MaxHealthReward extends PlayerReward {

    @Override
    public void execute(Player player) {
        Integer amount = getParamInteger("amount", player);
        amount = amount == null ? 0 : amount;
        Integer limit = getParamInteger("limit", player);
        limit = limit == null ? Integer.MAX_VALUE : limit;

//        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        double newMaxHealth = Math.min(amount+player.getMaxHealth(), limit);

        if (newMaxHealth != player.getMaxHealth()) {
            player.setMaxHealth(newMaxHealth);
        }
    }

}
