package com.gwchina.parent.main.presentation.home.card

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.ChildLocation
import com.gwchina.parent.main.presentation.home.CardInteractor
import com.gwchina.parent.main.presentation.home.HomeVO
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.third.location.LocationUtils
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.formatMillisecondsToUpdateTime
import com.gwchina.sdk.base.utils.layoutCommonEdge
import com.gwchina.sdk.base.utils.timestampMillis
import kotlinx.android.synthetic.main.home_card_location.view.*
import timber.log.Timber


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-31 16:27
 */
class LocationCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var interactor: CardInteractor

    private val locationUtils by lazy {
        LocationUtils(context, AppContext.storageManager())
    }

    private var showingLocation: ChildLocation? = null

    private var showingMarker: Marker? = null
    private var showingText: Text? = null
    private val map: AMap

    init {
        orientation = VERTICAL
        setPadding(layoutCommonEdge, 0, layoutCommonEdge, 0)
        View.inflate(context, R.layout.home_card_location, this)
        mapHome.onCreate(null)
        map = mapHome.map
        configMap()
        setupViewsWhenNoDevice()
    }

    private fun configMap() {
        val uiSettings = map.uiSettings
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.isScaleControlsEnabled = true
    }

    fun setup(cardInteractor: CardInteractor) {
        interactor = cardInteractor
        setupListeners()
        setupLocationUtils()
        bindMapLifecycle()
        subscribeData()
    }

    private fun setupListeners() {
        ivHomeDummyLocation.setOnClickListener {
            interactor.navigator.commonJump()
        }
        tvLocationTitle.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_LOCATION)
            interactor.navigator.openChildLocationPageForDefaultChild(showingLocation)
        }
        mapHome.onMapClickListener = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_LOCATION)
            interactor.navigator.openChildLocationPageForDefaultChild(showingLocation)
        }
    }

    private fun setupLocationUtils() {
        locationUtils.locationListener = { latitude: Double, longitude: Double, success: Boolean, address: String ->
            Timber.d("LocationCard: $latitude,$longitude,$address")
            if (success) {
                val location = showingLocation
                if (location != null && location.lat == latitude && location.lng == longitude) {
                    addOrUpdateLocationMarker(address, LatLng(location.lat, location.lng))
                }
            }
        }
    }

    private fun subscribeData() {
        interactor.cardDataProvider.observeUser {
            if (it.currentDevice == null) {
                setupViewsWhenNoDevice()
            } else {
                setupViewsNormally()
            }
        }

        interactor.cardDataProvider.observeHomeData {
            showLocation(it)
        }
    }

    private fun showLocation(data: HomeVO?) {
        val location: ChildLocation? = data?.location

        if (location != null) {

            val point = LatLng(location.lat, location.lng)
            addOrUpdateIconMarker(point)

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
            map.uiSettings.setAllGesturesEnabled(false)
            if (location.upload_time == 0L) {
                tvHomeLocationUpdateTime.text = ""
            } else {
                tvHomeLocationUpdateTime.text = formatMillisecondsToUpdateTime(location.upload_time)
            }
            showingLocation = location.copy(battery_level = data.deviceInfo?.battery_level ?: 0)

            if (location.formatted_address.isNullOrEmpty()) {
                locationUtils.startGetLocation(location.lat, location.lng)
            } else {
                addOrUpdateLocationMarker(location.formatted_address, point)
            }

        } else {
            removeMarker()
            tvHomeLocationUpdateTime.text = resources.getString(R.string.no_location_info_temporarily)
            showingLocation = null
        }
    }

    private fun addOrUpdateIconMarker(latLng: LatLng) {
        val cachedMarker = showingMarker

        if (cachedMarker != null) {
            cachedMarker.position = latLng
            return
        }

        showingMarker = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.home_img_child_position)))
                .position(latLng).run { map.addMarker(this) }
    }

    private fun addOrUpdateLocationMarker(address: String, latLng: LatLng) {
//        val cachedText = showingText
//
//        if (cachedText != null) {
//            cachedText.text = address.foldText(25)
//            cachedText.position = latLng
//            return
//        }
//
//        showingText = TextOptions()
//                .position(latLng)
//                .text(address.foldText(25))
//                .backgroundColor(colorFromId(R.color.opacity_40_black))
//                .fontColor(colorFromId(R.color.white))
//                .fontSize(sp(10))
//                .align(Text.ALIGN_CENTER_HORIZONTAL, Text.ALIGN_CENTER_VERTICAL)
//                .zIndex(1f)
//                .run { map.addText(this) }
        if (address.isEmpty()) return
        tvLocation.visible()
        tvLocation.text = address.foldText(25)

    }

    private fun removeMarker() {
        tvLocation.gone()
        locationUtils.cancel()
        showingMarker?.remove()
        showingText?.remove()
        showingMarker = null
        showingText = null
    }

    private fun setupViewsWhenNoDevice() {
        tvHomeChildLocationDesc.visible()
        ivHomeDummyLocation.visible()

        llHomeChildLocationInfo.gone()
        mapHome.gone()

        removeMarker()
    }

    private fun setupViewsNormally() {
        llHomeChildLocationInfo.visible()
        mapHome.visible()

        ivHomeDummyLocation.gone()
        tvHomeChildLocationDesc.gone()
    }

    private fun bindMapLifecycle() {
        interactor.host.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) = mapHome.onPause()
            override fun onDestroy(owner: LifecycleOwner) = mapHome.onDestroy()
            override fun onResume(owner: LifecycleOwner) = mapHome.onResume()
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        locationUtils.cancel()
    }

    fun setMapEnable(enable: Boolean) {
        Timber.d("setMapEnable $enable")
        if (enable) {
            mapHome.visible()
        } else {
            mapHome.postDelayed({
                mapHome?.invisible()
            }, 200)
        }
    }

}

class FixedAMapView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MapView(context, attrs, defStyleAttr) {

    private val gestureDetector: GestureDetector

    var onMapClickListener: (() -> Unit)? = null

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            onMapClickListener?.invoke()
            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            return false
        }

        override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
            return false
        }
    }

}

class RoundConstraintLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var clipPath = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clipPath.reset()
        val radius = dip(5F)
        val padding = radius / 2
        clipPath.addRoundRect(RectF(padding, padding, width - padding, height - padding), radius, radius, Path.Direction.CW)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.clipPath(clipPath)
        super.dispatchDraw(canvas)
    }

}