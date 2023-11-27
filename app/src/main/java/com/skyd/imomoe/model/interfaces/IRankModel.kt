package com.skyd.imomoe.model.interfaces

import com.skyd.imomoe.bean.TabBean

/**
 * 获取排行榜界面每个Tab详细数据的接口
 */
interface IRankModel : IBase {
    /**
     * 获取排行榜Tab数据
     *
     * @return ArrayList，不可为null。排行榜TabArrayList
     */
    suspend fun getRankTabData(): ArrayList<TabBean>

    companion object {
        const val implName = "RankModel"
    }
}