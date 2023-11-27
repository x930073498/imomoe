package com.skyd.imomoe.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyd.imomoe.App
import com.skyd.imomoe.R
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.DataSourceManager
import com.skyd.imomoe.model.impls.AnimeDetailModel
import com.skyd.imomoe.model.interfaces.IAnimeDetailModel
import com.skyd.imomoe.util.Util.showToastOnIOThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AnimeDetailViewModel : ViewModel() {
    private val animeDetailModel: IAnimeDetailModel by lazy {
        DataSourceManager.create(IAnimeDetailModel::class.java) ?: AnimeDetailModel()
    }
    var cover: ImageBean = ImageBean("", "", "", "")
    var title: String = ""
    var animeDetailList: MutableList<IAnimeDetailBean> = ArrayList()
    var mldAnimeDetailList: MutableLiveData<Pair<GetDataEnum, MutableList<IAnimeDetailBean>>> =
        MutableLiveData()

    //www.yhdm.io
    fun getAnimeDetailData(partUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                animeDetailModel.getAnimeDetailData(partUrl).apply {
                    cover = first
                    title = second
                    mldAnimeDetailList.postValue(Pair(GetDataEnum.REFRESH, third))
                }
            } catch (e: Exception) {
                mldAnimeDetailList.postValue(Pair(GetDataEnum.FAILED, ArrayList()))
                e.printStackTrace()
                (App.context.getString(R.string.get_data_failed) + "\n" + e.message).showToastOnIOThread()
            }
        }
    }

    companion object {
        const val TAG = "AnimeDetailViewModel"
    }
}