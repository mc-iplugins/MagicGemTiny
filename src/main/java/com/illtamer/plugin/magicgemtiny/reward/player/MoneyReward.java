package com.illtamer.plugin.magicgemtiny.reward.player;

import com.illtamer.plugin.magicgemtiny.entity.DynamicValue;
import com.illtamer.plugin.magicgemtiny.hook.VaultHook;
import com.illtamer.plugin.magicgemtiny.reward.PlayerReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import org.bukkit.entity.Player;

/**
 * 金币奖励
 * */
public class MoneyReward extends PlayerReward {

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
            VaultHook.give(amountDouble, player);
        } else {
            VaultHook.take(amountDouble, player);
        }
    }

    @Override
    public boolean tryTest(Player player) {
        if (amount == null) {
            return true;
        }
        double amountDouble = amount.getDouble(player, null);
        if (amountDouble < 0) {
            boolean check = VaultHook.check(amountDouble, player);
            if (!check) {
                player.sendMessage("§c您没有足够的金币可供消耗");
            }
            return check;
        }
        return true;
    }

}
