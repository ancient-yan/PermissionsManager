package com.gwchina.parent.main.presentation.location

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.android.base.app.mvvm.getViewModel
import com.android.base.kotlin.*
import com.android.sdk.social.wechat.WeChatManager
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.ChildLocation
import com.gwchina.sdk.base.third.location.LocationUtils
import com.gwchina.parent.main.widget.BottomShareDialog
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.formatDecimal
import com.gwchina.sdk.base.utils.formatMillisecondsToUpdateTime
import com.gwchina.sdk.base.utils.mapChildAvatarSmall
import com.gwchina.sdk.base.widget.dialog.showBottomSheetListDialog
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.dialog.showSwitchDeviceDialog
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import kotlinx.android.synthetic.main.home_fragment_child_location_map.*
import javax.inject.Inject


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-03-12 15:30
 */
class ChildLocationFragment : InjectorBaseFragment() {

    companion object {

        private const val CHILD_LOCATION = "child_location_key"

        fun newInstance(childLocation: ChildLocation?) = ChildLocationFragment().apply {
            if (childLocation != null) {
                arguments = Bundle().apply {
                    putParcelable(CHILD_LOCATION, childLocation)
                }
            }
        }

    }

    @Inject lateinit var locationUtils: LocationUtils

    private val viewModel by lazy {
        getViewModel<ChildLocationViewModel>(viewModelFactory)
    }

    private lateinit var map: AMap
    private lateinit var mapView: MapView
    private var marker: Marker? = null

    private var currentChild: Child? = null

    private var location: ChildLocation? = null

    private lateinit var shareMenu: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.home_fragment_child_location_map

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        //share
        shareMenu = gtlLocation.menu.add(R.string.share)
                .setIcon(R.drawable.home_icon_share)
                .alwaysShow()
                .onMenuItemClick {
                    location?.let {
                        if (it.lat.toInt() != 0 && it.lng.toInt() != 0) {
                            BottomShareDialog(this@ChildLocationFragment, it.lat, it.lng, tvLocationDetail.text.toString(), currentChild).show()
                        }
                    }
                }
                .setVisible(false)

        //refresh listener
        flLocationRefresh.setOnClickListener { viewModel.refreshLocation() }
        //map
        setupMapView(savedInstanceState)
        setupLocationInfoView()
        //init device
        showChildDeviceInfo()
    }

    private fun showChildDeviceInfo() {
        val childLocation = arguments?.getParcelable<ChildLocation>(CHILD_LOCATION)
        val childWithDevice = viewModel.childWithDevice

        val currentChild = childWithDevice.child ?: return
        this.currentChild = currentChild
        ivLocationAvatar.setImageResource(mapChildAvatarSmall(currentChild.sex))
        tvLocationName.text = currentChild.nick_name.foldText(10)
        //device index info
        val device = childWithDevice.device ?: return
        if (currentChild.moreThanOneDevice()) {
            showCurrentDevice(device)
        }
        viewModel.startPositioningForDevice(device, childLocation)
    }

    private fun showCurrentDevice(device: Device) {
        llLocationDevice.visible()
        tvLocationDeviceName.text = getString(R.string.location_device_mask, device.device_name)

        if (device.index > 0) {
            tvLocationDeviceIndex.visible()
            tvLocationDeviceIndex.text = device.index.toString()
        } else {
            tvLocationDeviceIndex.gone()
        }

        //切换设备监听
        llLocationDevice.setOnClickListener {
            showSwitchDeviceDialog(context, currentChild, device, onSelectedDevice = {
                if (isMemberGuardExpired(it.status)) {
                    showMemberExpiredDialog()
                } else if (device.device_id != it.device_id) {
                    showCurrentDevice(it)
                    viewModel.startPositioningForDevice(it, null)
                }
            })
        }
    }

    private fun showMemberExpiredDialog() {
        OpenMemberDialog.show(requireContext()) {
            messageId = R.string.as_member_expired_support_one_tips
        }
    }

    private fun setupMapView(savedInstanceState: Bundle?) {
        mapView = mvLocation
        mapView.onCreate(savedInstanceState)
        map = mapView.map
        //逆地理编码
        locationUtils.locationListener = { latitude: Double, longitude: Double, success: Boolean, address: String ->
            processReGeoCodeResult(latitude, longitude, success, address)
        }

    }

    private fun setupLocationInfoView() {
        tvLocationFailedInfo.setOnClickListener {
            showConfirmDialog {
                titleId = R.string.reason_of_location_failed
                messageId = R.string.location_error_tips
                messageGravity = Gravity.START
                noNegative()
                positiveId = R.string.i_got_it
            }?.setCanceledOnTouchOutside(false)
        }
    }

    private fun setShowOtherMapAppListDialogListener(location: ChildLocation?) {
        if (location == null) {
            return
        }
        flLocationNavigator.setOnClickListener {
            location.let {
                val mapAppList = loadOtherMapAppList(context, location, tvLocationDetail.text.toString())
                if (mapAppList.isEmpty()) {
                    showMessage(getString(R.string.no_other_map_app_tips))
                } else {
                    showMapAppList(mapAppList)
                }
            }
        }
    }

    private fun showMapAppList(mapAppList: List<MapApp>) {
        showBottomSheetListDialog {
            items = mapAppList.map { it.appName }
            actionTextId = R.string.cancel_
            itemSelectedListener = { position, _ ->
                mapAppList[position].action()
            }
        }
    }

    private fun startReGeocode(childLocation: ChildLocation?) {
        if (childLocation == null) {
            return
        }
        locationUtils.startGetLocation(childLocation.lat, childLocation.lng)
    }

    private fun processReGeoCodeResult(latitude: Double, longitude: Double, success: Boolean, result: String) {
        //OnGeocodeSearchListener doesn't bind lifecycle of fragment.
        if (tvLocationDetail == null) {
            return
        }
        if (success) {
            tvLocationDetail.text = result
            val point = LatLng(latitude, longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
            addMarker().position = point
            viewModel.locationEnded(true)
        } else {
            viewModel.locationEnded(false)
        }
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mapView.onDestroy()
        WeChatManager.destroyShareCallback()
        super.onDestroy()
    }

    private fun subscribeViewModel() {
        viewModel.locationLoadingStatus
                .observe(this, Observer {
                    it ?: return@Observer
                    pbLocation.visibleOrGone(it.isLoading)
                    tvLocationFailedInfo.visibleOrGone(it.locationError)
                })

        viewModel.childLocation
                .observe(this, Observer {
                    shareMenu.isVisible = it != null
                    it ?: return@Observer
                    showPreGeoLocation(it)
                    startReGeocode(it)
                    setShowOtherMapAppListDialogListener(it)
                })

        viewModel.showLocationSuccessMessage
                .observe(this, Observer {
                    showMessage(getString(R.string.location_refresh_success))
                })
    }

    @SuppressLint("SetTextI18n")
    private fun showPreGeoLocation(location: ChildLocation?) {
        if (location != null) {
            this.location = location
            tvLocationDetail.text = location.formatted_address
            tvLocationPhoneElectric.text = location.battery_level.toString()
            val locationPrecision = getString(R.string.location_precision_mask, formatDecimal(location.accurate, 0, 0)) + "m"
            tvLocationUpdateTime.text = "$locationPrecision    ${formatMillisecondsToUpdateTime(location.upload_time)}"
            val point = LatLng(location.lat, location.lng)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
            addMarker().position = point
        } else {
            removeMarker()
            tvLocationPhoneElectric.text = "0"
            tvLocationDetail.text = getString(R.string.no_location_info_temporarily)
            tvLocationUpdateTime.text = getString(com.app.base.R.string.last_update_time_mask, "无")
        }
    }

    private fun addMarker(): Marker {
        val cachedMarker = marker
        if (cachedMarker != null) {
            return cachedMarker
        }
        //位置标识
        return with(MarkerOptions()) {
            icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.home_img_child_position)))
            val addMarker = map.addMarker(this)
            marker = addMarker
            addMarker
        }
    }

    private fun removeMarker() {
        marker?.remove()
        marker = null
    }

}