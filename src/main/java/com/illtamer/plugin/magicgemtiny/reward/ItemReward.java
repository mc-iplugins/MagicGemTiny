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
     * 幂等地触发参数初始化
     * @apiNote disassemble/restore 等不经过 test 的调用需先执行本方法，
     *      以保证在服务器重启后（reward 尚未 init）字段已正确解析
     * */
    public void ensureInit() {
        if (!init) {
            init();
            init = true;
        }
    }

    /**
     * 降级操作
     * @apiNote 降级始终不支持拆卸
     * */
    public void downgrade(NBTItem nbtItem, Player player, JsonObject json) {}

    /**
     * 拆卸还原
     * @param log 镶嵌时记录在 MAGICGEM_CHANGE_LOG 中该颗宝石的变更日志对象
     * @return 是否可安全提交本次拆卸。true 表示已正确还原(或该奖励本就无需还原);
     *      false 表示检测到装备当前状态与记录不符、无法安全还原, 调用方应中止整次拆卸以防刷取。
     * @apiNote 从 log 中读取本奖励写入的还原数据，回滚对物品做的修改。
     *      默认返回 true(表示不还原的一次性奖励不阻止拆卸)。
     *      奖励会严格按照顺序执行，使用正序，拆卸逆序。
     *      调用前需确保已执行 {@link #ensureInit()}。
     * */
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        return true;
    }

    abstract protected boolean tryTest(NBTItem nbtItem) throws ConditionException;

    @Override
    public boolean test(NBTItem nbtItem) throws ConditionException {
        ensureInit();
        return tryTest(nbtItem);
    }

    /**
     * 是否可拆卸
     * */
    public boolean disassemble() {
        return false;
    }

}
