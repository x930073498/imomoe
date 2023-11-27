package com.skyd.imomoe.model.interfaces

import com.skyd.imomoe.bean.AnimeCoverBean
import com.skyd.imomoe.bean.AnimeShowBean
import com.skyd.imomoe.bean.TabBean

/**
 * 获取每日更新番剧界面数据接口
 */
interface IEverydayAnimeModel : IBase {
    /**
     * 获取每日更新动漫数据
     *
     * @return Triple，不可为null
     * ArrayList<TabBean>：Tab信息ArrayList，不可为null；
     * ArrayList<List<AnimeCoverBean>>：每个Tab内容的ArrayList，不可为null；
     * AnimeShowBean：标题，例如：日更动漫，不可为null
     */
    suspend fun getEverydayAnimeData(): Triple<ArrayList<TabBean>, ArrayList<List<AnimeCoverBean>>, AnimeShowBean>

    companion object {
        const val implName = "EverydayAnimeModel"
    }
}