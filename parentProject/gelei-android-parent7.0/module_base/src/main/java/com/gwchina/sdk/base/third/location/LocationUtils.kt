package com.gwchina.sdk.base.third.location

import android.content.Context
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.android.base.app.dagger.ContextType
import com.gwchina.sdk.base.data.app.StorageManager
import timber.log.Timber
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-03-18 13:34
 */
class LocationUtils @Inject constructor(
        @ContextType context: Context,
        storageManager: StorageManager
) {

    private var queryingLatitude = 0.0
    private var queryingLongitude = 0.0

    private val geocodeSearch: GeocodeSearch = GeocodeSearch(context)
    private val locationStorage = storageManager.stableStorage()

    var locationListener: ((latitude: Double, longitude: Double, success: Boolean, address: String) -> Unit)? = null

    private var mLocationOption: AMapLocationClientOption? = null
    private var mLocationClient: AMapLocationClient? = null

    var mLocationResultListener: ((Boolean, String?, String?) -> Unit)? = null

    init {
        geocodeSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {

            override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                if (rCode == AMapException.CODE_AMAP_SUCCESS && result != null) {
                    if (result.regeocodeAddress != null && result.regeocodeAddress.formatAddress != null) {
                        val point = result.regeocodeQuery.point
                        val regeocodeAddress = result.regeocodeAddress
                        locationStorage.putString(buildLocationCacheKey(point.latitude, point.longitude), regeocodeAddress.formatAddress)
                        if (queryingLatitude == point.latitude && queryingLongitude == point.longitude) {
                            locationListener?.invoke(queryingLatitude, queryingLongitude, true, regeocodeAddress.formatAddress)
                        }
                        Timber.d("onReGeocodeSearched  -->{code = $rCode} location = ${regeocodeAddress.formatAddress}")
                    } else {
                        Timber.d("onReGeocodeSearched  -->{code = $rCode}")
                        locationListener?.invoke(queryingLatitude, queryingLongitude, false, "")
                    }
                } else {
                    locationListener?.invoke(queryingLatitude, queryingLongitude, false, "")
                }
            }

            override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) = Unit
        })

    }

    fun initLocation(context: Context) {
        //初始化定位参数
        mLocationOption = AMapLocationClientOption()
        initLocationOptions()

        mLocationClient = AMapLocationClient(context.applicationContext)
        mLocationClient?.setLocationOption(mLocationOption)
        mLocationClient?.setLocationListener { amapLocation ->
            if (amapLocation != null) {
                if (amapLocation.errorCode == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    Timber.e("locationSuccess===$amapLocation")
                    mLocationResultListener?.invoke(true, amapLocation.street + amapLocation.streetNum + amapLocation.aoiName, amapLocation.adCode)
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Timber.e("AmapError====" + "location Error, ErrCode:"
                            + amapLocation.errorCode + ", errInfo:"
                            + amapLocation.errorInfo)
                    mLocationResultListener?.invoke(false, amapLocation.errorInfo, "")
                }
            }
        }
    }

    private fun initLocationOptions() {
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption?.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //设置单次定位
        mLocationOption?.isOnceLocation = true
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption?.isOnceLocationLatest = true
        //设置是否允许模拟位置,默认为true，允许模拟位置
        mLocationOption?.isMockEnable = false
        //关闭缓存机制
        mLocationOption?.isLocationCacheEnable = false
    }

    fun startGetLocation(latitude: Double, longitude: Double) {
        Timber.d("startGetLocation, latitude $latitude, longitude $longitude")
        val location: String? = locationStorage.getString(buildLocationCacheKey(latitude, longitude))
        Timber.d("startGetLocation, location $location")
        if (!location.isNullOrEmpty()) {
            locationListener?.invoke(latitude, longitude, true, location)
        } else {
            val latLng = LatLonPoint(latitude, longitude)
            // 第一个参数表示一个经纬度，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
            val query = RegeocodeQuery(latLng, 200F, GeocodeSearch.AMAP)
            geocodeSearch.getFromLocationAsyn(query)
        }
        this.queryingLatitude = latitude
        this.queryingLongitude = longitude
    }

    fun cancel() {
        queryingLatitude = 0.0
        queryingLongitude = 0.0
    }

    private fun buildLocationCacheKey(latitude: Double, longitude: Double): String {
        return "child_location_($latitude,$longitude)"
    }


    fun startOnceLocation() {
        mLocationClient?.startLocation()
    }

    private fun stopLocation() {
        mLocationClient?.stopLocation()
    }

    fun destroy() {
        stopLocation()
        mLocationClient?.onDestroy()
        mLocationOption = null
        mLocationClient = null
    }
}