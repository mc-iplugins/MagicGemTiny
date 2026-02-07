package com.illtamer.plugin.magicgemtiny.gem;

import com.illtamer.plugin.magicgemtiny.entity.DynamicValue;
import com.illtamer.plugin.magicgemtiny.reward.Reward;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 玩家宝石
 * @apiNote 玩家右键直接使用的宝石
 * */
@Setter
@Getter
public class PlayerGem extends Gem {

    // 是否是食用宝石，如果为true，则宝石材料必须是原版就可以吃的东西
    private boolean eat;
    // 是否需要蹲着才能使用
    private boolean shift;

    public PlayerGem(
            String name,
            ItemStack displayItem,
            DynamicValue success,
            String successTip,
            List<Reward> rewards
    ) {
        super(name, Type.PLAYER, displayItem, success, successTip, rewards);
    }

    public static Set<String> supportKeys() {
        Set<String> supportKeys = Gem.supportKeys();
        supportKeys.addAll(Arrays.asList(
                "Eat", "Shift"
        ));
        return supportKeys;
    }

}
