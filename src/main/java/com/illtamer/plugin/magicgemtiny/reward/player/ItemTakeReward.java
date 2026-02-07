package com.illtamer.plugin.magicgemtiny.reward.player;

import com.illtamer.plugin.magicgemtiny.condition.RequireCondition;
import com.illtamer.plugin.magicgemtiny.reward.PlayerReward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 从背包中收取玩家物品
 * */
public class ItemTakeReward extends PlayerReward {

    private final RequireCondition condition = new RequireCondition(null);

    @Override
    public void execute(Player player) {
        ItemStack[] contents = player.getInventory().getContents();

        for (Map.Entry<String, String> entry : args.entrySet()) {
            String require = entry.getKey();
            int needAmount = Integer.parseInt(entry.getValue());

            for (int i = 0; i < contents.length; ++i) {
                ItemStack item = contents[i];
                if (item == null || item.getType().isAir()) {
                    continue;
                }

                if (condition.check(item, Collections.singletonList(require))) {
                    int currentAmount = item.getAmount();
                    // 计算实际能从这个格子拿走多少
                    int take = Math.min(currentAmount, needAmount);
                    if (take > 0) {
                        item.setAmount(currentAmount - take);
                        needAmount -= take;

                        // 如果数量变为0或更少，Bukkit 有时不会自动移除物品，显式设为 null
                        if (item.getAmount() <= 0) {
                            player.getInventory().setItem(i, null);
                        }/* else {
                            player.getInventory().setItem(i, item);
                        }*/
                    }
                }

                if (needAmount <= 0) {
                    break;
                }
            }
        }
    }

    // 提取通用方法
    @Override
    public boolean tryTest(Player player) {
        // Key: 槽位索引, Value: 已被计入消耗的数量
        Map<Integer, Integer> reservedSlots = new HashMap<>();
        ItemStack[] contents = player.getInventory().getContents();

        for (Map.Entry<String, String> entry : args.entrySet()) {
            String require = entry.getKey();
            int needAmount = Integer.parseInt(entry.getValue());

            for (int i = 0; i < contents.length; ++i) {
                ItemStack item = contents[i];
                if (item == null || item.getType().isAir()) {
                    continue;
                }

                if (condition.check(item, Collections.singletonList(require))) {
                    // 计算该物品堆的“剩余可用数量”
                    int currentAmount = item.getAmount();
                    int alreadyReserved = reservedSlots.getOrDefault(i, 0); // 获取之前已经预占用的数量
                    int available = currentAmount - alreadyReserved; // 真实剩余量

                    if (available > 0) {
                        // 决定从这个堆里扣除多少
                        int take = Math.min(available, needAmount);
                        // 更新需求剩余量
                        needAmount -= take;
                        reservedSlots.put(i, alreadyReserved + take);
                    }
                }

                if (needAmount <= 0) {
                    break;
                }
            }

            if (needAmount > 0) {
                player.sendMessage("§c您没有足够的物品可供消耗");
                return false;
            }
        }
        return true;
    }

}
