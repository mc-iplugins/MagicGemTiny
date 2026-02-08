package com.illtamer.plugin.magicgemtiny.entity;

public interface NBTKey {

    String MAGICGEM_NAME = "MAGICGEM_NAME";

    String MAGICGEM_EMBED_GEMS = "MAGICGEM_EMBED_GEMS";

    String MAGICGEM_CHANGE_LOG = "MAGICGEM_CHANGE_LOG";

    // 物品宝石成功概率增加 计算公式: 概率=原有成功机率+n%
    String GEM_SUCCESS_ADD = "GEM_SUCCESS_ADD";

    // 物品宝石成功概率增加 计算公式: 计算公式: 概率=原有成功机率*（1+0.01n)
    String GEM_SUCCESS_MULTIPLE = "GEM_SUCCESS_MULTIPLE";

}
