package com.illtamer.plugin.magicgemtiny.gem;

import com.illtamer.plugin.magicgemtiny.entity.DynamicValue;
import com.illtamer.plugin.magicgemtiny.reward.Reward;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机宝石
 * */
@Setter
@Getter
public class RandomGem extends Gem {

    // 随机宝石只能套娃物品宝石和玩家宝石中的某一种，不能两种都有
    // 为空则加载失败
    private @Nullable Type randomType;
    // gemUniqueName, weight
    private @Getter(AccessLevel.PROTECTED) ConfigurationSection gemsSection;
    private @Getter(AccessLevel.PROTECTED) Map<Gem, Integer> gemsWeightMap;
    // 随机宝石较为特殊，其成功率和奖励均没有作用，只能套娃别的宝石
    // 随机宝石只能套娃物品宝石和玩家宝石中的某一种，不能两种都有。其格式为
    // 默认情况，随机宝石包含什么宝石他自己就是什么宝石。
    // - 比如，随机宝石包含一系列的玩家宝石，那么你就只能右键使用它
    // - 又比如，随机宝石包含一系列物品宝石，那么你就只能把它打在物品上
    // 无论套娃的宝石是否成功使用，随机宝石都会被消耗
    // 特殊情况：你可以添加一行Give: true，这样随机宝石会变成抽奖宝石，玩家右键它就会随机获得一颗宝石
    // 如果为false，就按原版宝石直接使用
    private boolean give;

    public RandomGem(
            String name,
            ItemStack displayItem,
            DynamicValue success,
            String successTip,
            List<Reward> rewards
    ) {
        super(name, Type.RANDOM, displayItem, success, successTip, rewards);
    }

    public Gem randomGem() {
        if (randomType == null || gemsWeightMap.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (int weight : gemsWeightMap.values()) {
            // 防止配置错误导致负数或0，只计算正权重
            if (weight > 0) {
                totalWeight += weight;
            }
        }
        if (totalWeight <= 0) {
            return null;
        }

        // 生成一个 [0, totalWeight) 之间的随机数
        int randomValue = ThreadLocalRandom.current().nextInt(totalWeight);

        // 遍历寻找命中区间
        int currentCursor = 0;
        for (Map.Entry<Gem, Integer> entry : gemsWeightMap.entrySet()) {
            int weight = entry.getValue();
            if (weight <= 0) continue;
            currentCursor += weight;
            if (randomValue < currentCursor) {
                return entry.getKey();
            }
        }
        // 理论上永远不会运行到这里，除非多线程并发修改了集合导致不一致
        return null;
    }

    public static Set<String> supportKeys() {
        Set<String> supportKeys = Gem.supportKeys();
        supportKeys.addAll(Arrays.asList(
                "Gems", "Give"
        ));
        return supportKeys;
    }

}
