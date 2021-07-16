package com.gwchina.parent.main.presentation.location

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.IntentUtils
import com.gwchina.parent.main.data.ChildLocation

data class MapApp(val appName: String, val action: () -> Unit)

fun loadOtherMapAppList(context: Context?, location: ChildLocation, locationName: String = ""): List<MapApp> {
    if (context == null) {
        return emptyList()
    }
    val mapAppList = mutableListOf<MapApp>()
    addGaoDeMap(context, mapAppList, location, locationName)
    addBaiDuMap(context, mapAppList, location)
    return mapAppList
}

fun addGaoDeMap(context: Context, mapAppList: MutableList<MapApp>, location: ChildLocation, locationName: String) {
    assembleMapApp(context, "com.autonavi.minimap", "高德地图") {
        it.data = Uri.parse("androidamap://route/plan/?dlat=" + location.lat + "&dlon=" + location.lng + "&dname=" + locationName + "&dev=0&t=0")
    }?.let {
        mapAppList.add(it)
    }
}

fun addBaiDuMap(context: Context, mapAppList: MutableList<MapApp>, location: ChildLocation) {
    assembleMapApp(context, "com.baidu.BaiduMap", "百度地图") {
        it.data = Uri.parse("baidumap://map/geocoder?location=${location.lat},${location.lng}&src=andr.baidu.openAPIdemo")
    }?.let {
        mapAppList.add(it)
    }
}

private fun assembleMapApp(content: Context, packageName: String, appName: String, setData: (Intent) -> Unit): MapApp? {
    if (!AppUtils.isAppInstalled(packageName)) {
        return null
    }
    return MapApp(appName) {
        val intent = with(Intent()) {
            action = Intent.ACTION_VIEW
            addCategory(Intent.CATEGORY_DEFAULT)
            setData(this)
            this
        }
        if (IntentUtils.isIntentAvailable(intent)) {
            content.startActivity(intent)
        }
    }
}