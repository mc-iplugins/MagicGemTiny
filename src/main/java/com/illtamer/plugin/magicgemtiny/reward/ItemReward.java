package com.illtamer.plugin.magicgemtiny.reward;

import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.condition.ItemCondition;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;

/**
 * 物品奖励只对物品宝石有效
 * */
public abstract class ItemReward extends Reward implements ItemCondition {

    private boolean init = false;

    abstract public void execute(NBTItem nbtItem, Player player, JsonObject json);

    /**
     * 降级操作
     * */
    public void downgrade(NBTItem nbtItem, Player player, JsonObject json) {}

    // TODO 拆卸 仅Embed>=1的允许拆卸

    abstract protected boolean tryTest(NBTItem nbtItem) throws ConditionException;

    @Override
    public boolean test(NBTItem nbtItem) throws ConditionException {
        if (!init) {
            init();
            init = true;
        }
        return tryTest(nbtItem);
    }

    /**
     * 是否可拆卸
     * */
    public boolean disassemble() {
        return false;
    }

}
