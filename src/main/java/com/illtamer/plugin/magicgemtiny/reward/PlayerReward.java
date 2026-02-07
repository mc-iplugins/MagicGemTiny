package com.illtamer.plugin.magicgemtiny.reward;

import com.illtamer.plugin.magicgemtiny.condition.PlayerCondition;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * 玩家奖励对物品和玩家宝石都有效
 * */
@Getter
public abstract class PlayerReward extends Reward implements PlayerCondition {

    // 加在玩家奖励后面，有这个标志时，奖励在宝石使用成功后才会执行
    protected boolean onSuccess = false;
    // 加在玩家奖励后面，有这个标志时，奖励在宝石使用失败后才会执行
    protected boolean onFail = false;
    // TODO 加在玩家奖励后面，有这个标志时，该奖励只在拆卸宝石时执行, 只对可镶嵌的物品宝石生效
    protected boolean onRemove = false;

    @Getter(AccessLevel.PROTECTED)
    private boolean init = false;

    abstract public void execute(Player player);

    protected boolean tryTest(Player player) {
        return true;
    }

    @Override
    public boolean test(Player player) {
        if (!init) {
            init();
            init = true;
        }
        return tryTest(player);
    }

}
