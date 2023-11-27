package com.skyd.imomoe.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.util.CoilUtils
import com.skyd.imomoe.App
import com.skyd.imomoe.R
import com.skyd.imomoe.database.getAppDataBase
import com.skyd.imomoe.util.Util.getDirectorySize
import com.skyd.imomoe.util.Util.getFormatSize
import com.skyd.imomoe.util.Util.showToastOnIOThread
import com.skyd.imomoe.util.coil.CoilUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingViewModel : ViewModel() {
    var mldDeleteAllHistory: MutableLiveData<Boolean> = MutableLiveData()
    var mldClearAllCache: MutableLiveData<Boolean> = MutableLiveData()
    var mldCacheSize: MutableLiveData<String> = MutableLiveData()

    fun deleteAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getAppDataBase().historyDao().deleteAllHistory()
                getAppDataBase().searchHistoryDao().deleteAllSearchHistory()
                mldDeleteAllHistory.postValue(true)
            } catch (e: Exception) {
                mldDeleteAllHistory.postValue(false)
                e.printStackTrace()
                (App.context.getString(R.string.delete_failed) + "\n" + e.message).showToastOnIOThread()
            }
        }
    }

    // 获取Glide磁盘缓存大小
    fun getCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            mldCacheSize.postValue(
                try {
                    getFormatSize(
                        getDirectorySize(CoilUtils.createDefaultCache(App.context).directory).toDouble()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    "获取缓存大小失败"
                }
            )
        }
    }


    fun clearAllCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Glide
                CoilUtil.clearMemoryDiskCache()
                mldClearAllCache.postValue(true)
            } catch (e: Exception) {
                mldClearAllCache.postValue(false)
                e.printStackTrace()
                (App.context.getString(R.string.delete_failed) + "\n" + e.message).showToastOnIOThread()
            }
        }
    }

    companion object {
        const val TAG = "SettingViewModel"
    }
}