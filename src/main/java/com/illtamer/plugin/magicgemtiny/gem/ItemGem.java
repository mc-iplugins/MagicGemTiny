package com.illtamer.plugin.magicgemtiny.gem;

import com.illtamer.plugin.magicgemtiny.entity.DynamicValue;
import com.illtamer.plugin.magicgemtiny.condition.RequireCondition;
import com.illtamer.plugin.magicgemtiny.reward.Reward;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 物品宝石
 * @apiNote 作用于物品的宝石（默认类型）
 *      仅能在工作台里使用
 * */
@Setter
@Getter
public class ItemGem extends Gem {

    // 强化失败的情况下，物品降级的概率表达式
    protected DynamicValue downgrade;
    // TODO 拆卸时宝石破碎的概率表达式
    protected DynamicValue breakValue;
    // TODO
    private @NotNull Gui gui;
    // 控制物品宝石能强化的装备类型. 这里只对结尾敏感（endWith
    // 有三种内置类型: 武器WEAPON/工具TOOL/盔甲ARMOR
    // - Weapon: 含剑/斧/弓/弩/三叉戟
    // - Armor: 包括头盔/胸甲/护腿/靴子/鞘翅。注意不包括盾牌
    // - Tool: 包括斧/镐/铲/锄
    // 特殊识别：支持显示名/Lore/NBT/NBT字符串/宝石识别。格式为关键词+冒号+识别内容。
    // - 注意关键词全为大写，冒号后没有空格
    // - 识别名称："NAME:要识别的名字"
    // - 识别Lore："LORE:要识别的Lore"
    // - 识别NBT是否存在："NBT:要检测存在的NBT名称"
    // - 识别NBT字符串的内容："NBT:要检测的NBT名称:要检测的NBT内容"
    // - 识别宝石名："GEM:宝石名"
    // - 一些例子："NAME:神奇的钓竿"，"LORE:按SHIFT+右键切换附魔"，"GEM:锋利宝石"，"NBT:MYTHIC_TYPE:振奋胸甲"
    // 注意，写在Require里的条件，并非需要全部满足，而是只要满足一条就能被识别
    private final RequireCondition requireCondition;
    // 配置了这个参数的才允许拆卸
    // 可以镶嵌的最大颗数。为0或者不写此条则为一次性宝石
    private int embed;
    // 物品降级时的提示信息
    private @Nullable String downgradeTip;
    // 拆卸宝石时的提示信息
    private @Nullable String removeTip;
    // 拆卸时宝石破碎的提示信息
    private @Nullable String breakTip;

    public ItemGem(
            String name,
            ItemStack displayItem,
            DynamicValue success,
            DynamicValue downgrade,
            DynamicValue breakValue,
            String successTip,
            List<Reward> rewards,
            List<String> requires
    ) {
        super(name, Type.ITEM, displayItem, success, successTip, rewards);
        this.downgrade = downgrade;
        this.breakValue = breakValue;
        this.requireCondition = new RequireCondition(requires);
    }

    public static Set<String> supportKeys() {
        Set<String> supportKeys = Gem.supportKeys();
        supportKeys.addAll(Arrays.asList(
                "Downgrade", "Break", "Embed", "Require", "RemoveTip", "DowngradeTip", "BreakTip"
        ));
        return supportKeys;
    }

    /**
     * 允许宝石在哪些GUI上使用
     * */
    @Getter
    @RequiredArgsConstructor
    public enum Gui {
        // 工作台
        WORKBENCH("workbench"),
        // 玩家背包
        BACKPACK("backpack"),
        ;
        private final String name;

        public static Gui parse(String name) {
            for (Gui value : Gui.values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return null;
        }

    }

}
