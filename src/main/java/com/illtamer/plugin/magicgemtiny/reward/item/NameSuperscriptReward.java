package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import com.illtamer.plugin.magicgemtiny.util.SuperscriptUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 物品名末尾上标升阶
 * @apiNote 名称末尾无上标时追加 ⁺¹, 已有上标时在原值基础上累加(⁺¹ -> ⁺²)。
 *      可选参数 amount(a), 默认 1; prefix(p), 上标数字前的前缀(如 "§c"),
 *      已存在则复用, 缺失则在写入上标前补上, 支持 & 色码。
 *      仅对带自定义显示名的物品生效。拆卸时按记录的增量回退上标。
 * */
public class NameSuperscriptReward extends ItemReward {

    private static final String LOG_KEY = "NameSuperscript";

    private int amount;
    private String prefix;

    @Override
    protected void init() {
        // 不使用 alias 重载: 别名缺省时其内部会对 null 调用 trim
        Integer value = getParamInteger("amount", null);
        if (value == null) {
            value = getParamInteger("a", null);
        }
        amount = value == null ? 1 : value;
        String prefixValue = getParamString("prefix", null);
        if (prefixValue == null) {
            prefixValue = getParamString("p", null);
        }
        prefix = StringUtil.isBlank(prefixValue) ? null : StringUtil.c(prefixValue);
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return; // tryTest 已校验, 此处兜底
        }
        meta.setDisplayName(SuperscriptUtil.increase(meta.getDisplayName(), amount, prefix));
        item.setItemMeta(meta);
        json.addProperty(LOG_KEY, amount);
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) throws ConditionException {
        if (amount < 1) {
            throw new ConditionException("上标增量 amount 必须大于 0");
        }
        ItemMeta meta = nbtItem.getItem().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            throw new ConditionException("该物品没有自定义名称, 无法添加上标");
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return true;
    }

    /**
     * 按记录的增量回退上标
     * @return 当前上标小于记录增量(名称已被篡改)时返回 false 中止拆卸
     * */
    @Override
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        JsonElement element = log.get(LOG_KEY);
        if (element == null || element.isJsonNull()) {
            return true; // 本奖励未产生记录, 无需还原
        }
        int delta = element.getAsInt();
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        String name = meta.getDisplayName();
        if (SuperscriptUtil.getLevel(name) < delta) {
            return false;
        }
        meta.setDisplayName(SuperscriptUtil.increase(name, -delta, prefix));
        item.setItemMeta(meta);
        return true;
    }

}
