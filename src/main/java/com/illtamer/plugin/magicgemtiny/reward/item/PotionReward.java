package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.EnumUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

/**
 * 药水宝石奖励
 * <p>
 * 配置格式：{@code Potion{name=药水名;level=等级;duration=秒数}}
 * <ul>
 *   <li>{@code level} 为空时默认 1 级（amplifier = level - 1）</li>
 *   <li>{@code duration} 为空或 -1 时持续无限；否则为持续秒数</li>
 * </ul>
 * <p>
 * 镶嵌后由 {@link com.illtamer.plugin.magicgemtiny.task.EquipmentPotionEffectTask}
 * 每 40 tick 扫描玩家的主手、副手、盔甲槽，对携带该宝石物品的玩家持续施加对应药水效果。
 * <p>
 * 该奖励不直接修改物品，仅在 changelog 写入记录（用于镶嵌计数和拆卸撤销）。
 * 拆卸时 changelog 记录被删除，任务器下一周期停止续期效果，并主动清除。
 */
public class PotionReward extends ItemReward {

    /** 配置中的原始药水名 */
    private String name;
    /** 解析后的药水效果类型 */
    private @Nullable PotionEffectType effect;
    /** 药水等级，默认 1（对应 amplifier=0） */
    private int level;
    /** 持续时间（秒），-1 表示无限 */
    private int duration;

    @Override
    protected void init() {
        name = getParamString("name", null);
        effect = EnumUtil.getPotionEffectType(name);

        Integer lv = getParamInteger("level", null);
        level = (lv == null || lv <= 0) ? 1 : lv;

        Integer dur = getParamInteger("duration", null);
        duration = (dur == null || dur == -1) ? -1 : Math.max(1, dur);
    }

    /**
     * 执行镶嵌：不修改物品本体，只在 changelog 中写入药水参数。
     * 写入后 {@link com.illtamer.plugin.magicgemtiny.util.GemUtil} 判定为"成功记录"，
     * 允许后续拆卸。
     */
    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("level", level);
        obj.addProperty("duration", duration);
        json.add("Potion", obj);
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) throws ConditionException {
        if (StringUtil.isBlank(name)) {
            throw new ConditionException("药水名不能为空，请联系管理员检查宝石配置");
        }
        if (effect == null) {
            throw new ConditionException("找不到对应的药水效果: " + name + "，请联系管理员");
        }
        return true;
    }

    /**
     * 拆卸还原：changelog 记录删除后任务器自动停止续期，无需修改物品。
     *
     * @return 始终返回 {@code true}，允许拆卸提交
     */
    @Override
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        // 不需要回滚物品；任务器检测到 changelog 无此记录后会在下一 tick 主动 removePotionEffect
        return true;
    }

    /**
     * 药水宝石始终支持拆卸
     */
    @Override
    public boolean disassemble() {
        return true;
    }

    // ---- 供 EquipmentPotionEffectTask 读取 ----

    @Nullable
    public PotionEffectType getEffect() {
        return effect;
    }

    public int getLevel() {
        return level;
    }

    public int getDuration() {
        return duration;
    }

}
