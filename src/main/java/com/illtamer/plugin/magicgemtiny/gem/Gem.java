package com.illtamer.plugin.magicgemtiny.gem;

import com.illtamer.plugin.magicgemtiny.entity.DynamicValue;
import com.illtamer.plugin.magicgemtiny.reward.Reward;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public abstract class Gem {

    protected final NBTItem gemNbt;

    protected final String name;
    protected final Type type;
    protected final ItemStack displayItem;

    protected final DynamicValue success;
    protected final String successTip;
    // 宝石使用失败的提示信息，如果没有则使用默认提示
    private @Nullable String failTip;
    protected final List<Reward> rewards;

    public Gem(
            String name,
            Type type,
            ItemStack displayItem,
            DynamicValue success,
            String successTip,
            List<Reward> rewards
    ) {
        this.name = name;
        this.type = type;
        this.displayItem = displayItem;
        this.success = success;
        this.successTip = successTip;
        this.rewards = rewards;
        this.gemNbt = new NBTItem(displayItem);
        gemNbt.setString("MAGICGEM_NAME", name);
    }

    public ItemStack getItem() {
        return getItem(1);
    }

    public ItemStack getItem(int amount) {
        ItemStack item = gemNbt.getItem();
        item.setAmount(amount);
        return item;
    }

    // 返回支持的配置key
    public static Set<String> supportKeys() {
        return new HashSet<>(Arrays.asList(
                "Name", "Type", "Texture", "Material", "Display", "Tips", "CustomModelData",
                "Success", "SuccessTip", "FailTip", "Rewards", "Color"
        ));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        ITEM("ItemGem", "物品宝石"),
        PLAYER("PlayerGem", "玩家宝石"),
        RANDOM("RandomGem", "随机宝石")
        ;

        private final String name;
        private final String description;

        public static Type parse(String name) {
            for (Type type : Type.values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }

}
