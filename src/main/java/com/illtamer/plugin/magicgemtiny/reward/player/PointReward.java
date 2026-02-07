package com.illtamer.plugin.magicgemtiny.reward.player;

import com.illtamer.plugin.magicgemtiny.entity.DynamicValue;
import com.illtamer.plugin.magicgemtiny.hook.PlayerPointsHook;
import com.illtamer.plugin.magicgemtiny.reward.PlayerReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import org.bukkit.entity.Player;

/**
 * 点券奖励
 * */
public class PointReward extends PlayerReward {

    private DynamicValue amount;

    @Override
    protected void init() {
        String amountStr = getParamString("amount", null);
        if (StringUtil.isNotBlank(amountStr)) {
            amount = new DynamicValue(amountStr);
        }
    }

    @Override
    public void execute(Player player) {
        if (amount == null) {
            return;
        }
        double amountDouble = amount.getDouble(player, null);
        if (amountDouble > 0) {
            PlayerPointsHook.give((int) amountDouble, player);
        } else {
            PlayerPointsHook.take((int) amountDouble, player);
        }
    }

    @Override
    public boolean tryTest(Player player) {
        if (amount == null) {
            return true;
        }
        double amountDouble = amount.getDouble(player, null);
        if (amountDouble < 0) {
            boolean check = PlayerPointsHook.check((int) amountDouble, player);
            if (!check) {
                player.sendMessage("§c您没有足够的点券可供消耗");
            }
            return check;
        }
        return true;
    }

}
