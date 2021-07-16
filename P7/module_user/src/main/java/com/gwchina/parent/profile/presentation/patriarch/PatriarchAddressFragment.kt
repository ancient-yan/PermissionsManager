package com.gwchina.parent.profile.presentation.patriarch

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onSuccess
import com.android.base.kotlin.alwaysShow
import com.android.base.kotlin.gone
import com.android.base.kotlin.onMenuItemClick
import com.android.base.kotlin.visible
import com.android.base.permission.AutoPermissionRequester
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.profile.data.DeliveryAddress
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.third.location.LocationUtils
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.yanzhenjie.permission.runtime.Permission
import kotlinx.android.synthetic.main.profile_fragment_patriarch_address.*
import kotlinx.android.synthetic.main.profile_fragment_patriarch_address_layout.*
import javax.inject.Inject

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-05 16:03
 *      收货地址
 */
class PatriarchAddressFragment : InjectorBaseStateFragment() {

    @Inject
    lateinit var locationUtils: LocationUtils

    private val mCityPickerPresenter = CityPickerPresenter(this)

    private val patriarchInfoViewModel by lazy {
        getViewModel<PatriarchInfoViewModel>(viewModelFactory)
    }
    private var mAddress: DeliveryAddress? = null

    private var mProvinceCode: String? = null
    private var mCityCode: String? = null
    private var mDistrictCode: String? = null
    private var mPatriarchPhone: String? = null

    companion object {

        fun createFragment(address: DeliveryAddress?): PatriarchAddressFragment {
            val instance = PatriarchAddressFragment()
            val bundle = Bundle()
            bundle.putParcelable("data", address)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationUtils.initLocation(requireContext())
    }

    override fun provideLayout(): Any? {
        return R.layout.profile_fragment_patriarch_address
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        mAddress = arguments?.getParcelable("data")
        mCityPickerPresenter.init()
        if (mAddress == null) {
            tvDelete.gone()
            mPatriarchPhone = patriarchInfoViewModel.appDataSource.user().patriarch.phone
            etReceiverPhone.setText(mPatriarchPhone)
        } else {
            mProvinceCode = mAddress!!.province_area_code
            mCityCode = mAddress!!.city_area_code
            mDistrictCode = mAddress!!.district_area_code
            setAddressInfo()
            tvDelete.visible()
            tvDelete.setOnClickListener {
                showConfirmDialog {
                    message = "确定要删除收货地址吗？"
                    positiveListener = {
                        deleteAddress()
                    }
                }
            }
        }
        gwTitleLayout.menu.add(R.string.save)
                .alwaysShow()
                .onMenuItemClick {
                    save()
                }
        gwTitleLayout.setOnNavigationOnClickListener { exit() }
        tvAddress.setOnClickListener {
            mCityPickerPresenter.showDialog()
        }
        mCityPickerPresenter.onCitySelectListener = { provinceCode, cityCode, districtCode, address ->
            mProvinceCode = provinceCode
            mCityCode = cityCode
            mDistrictCode = districtCode
            tvAddress.text = address
        }
        ivLocation.setOnClickListener {

            AutoPermissionRequester.with(this)
                    .permission(Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION)
                    .onDenied { }
                    .onGranted {
                        showLoadingDialog("获取位置中....", true)
                        locationUtils.startOnceLocation()
                    }
                    .request()
        }
        locationUtils.mLocationResultListener = { isSuccess, address, districtCode ->
            dismissLoadingDialog()
            if (isSuccess) {
                ToastUtils.showShort("获取位置成功!")
                districtCode?.let {
                    val addressDetail = mCityPickerPresenter.getAddressDetailByDistrictCode(districtCode)
                    mProvinceCode = addressDetail.provinceCode
                    mCityCode = addressDetail.cityCode
                    mDistrictCode = addressDetail.districtCode
                    tvAddress.text = addressDetail.address
                }
                etAddressDetail.setText(address)
            } else {
                ToastUtils.showShort("定位失败!")
            }
        }
    }

    private fun setAddressInfo() {
        mAddress?.let {
            etReceiverName.setText(it.receiver_name)
            etReceiverPhone.setText(it.receiver_phone)
            if (mProvinceCode == null || mCityCode == null || mDistrictCode == null) {
                tvAddress.text = ""
            } else {
                tvAddress.text = mCityPickerPresenter.getAddressByAreaCode(mProvinceCode!!, mCityCode!!, mDistrictCode!!)
            }
            etAddressDetail.setText(it.address)
        }
    }

    private fun save() {
        if (checkValueIsNotNull(etReceiverName, getString(R.string.delivery_person_name))) return
        if (checkValueIsNotNull(etReceiverPhone, getString(R.string.delivery_person_phone))) return
        if (etReceiverPhone.text.toString().length != 11) {
            ToastUtils.showShort("请输入11位数的大陆手机号码喔")
            return
        }
        if (checkValueIsNotNull(tvAddress, "所在地区")) return
        if (checkValueIsNotNull(etAddressDetail, "详细地址")) return
        if (mProvinceCode == null || mCityCode == null || mDistrictCode == null) return
        showLoadingDialog()
        if (mAddress == null) {
            patriarchInfoViewModel.addAddress(etReceiverName.text.toString(), etReceiverPhone.text.toString(),
                    mProvinceCode!!, mCityCode!!, mDistrictCode!!, etAddressDetail.text.toString()).observe(this, Observer {
                dismissLoadingDialog()
                it?.let {
                    it.onSuccess {
                        ToastUtils.showShort("保存成功")
                        patriarchInfoViewModel.refreshPatriarchInfoEventCenter.setRefreshAddressEvent(false)
                        exitFragment()
                    }.onError {
                        patriarchInfoViewModel.errorHandler.handleError(it)
                    }
                }
            })
        } else {
            if (mAddress!!.record_id == null) return
            patriarchInfoViewModel.updateAddress(mAddress!!.record_id!!, etReceiverName.text.toString(), etReceiverPhone.text.toString(),
                    mProvinceCode!!, mCityCode!!, mDistrictCode!!, etAddressDetail.text.toString()).observe(this, Observer {
                dismissLoadingDialog()
                it?.let {
                    it.onSuccess {
                        ToastUtils.showShort("保存成功")
                        patriarchInfoViewModel.refreshPatriarchInfoEventCenter.setRefreshAddressEvent(false)
                        exitFragment()
                    }.onError {
                        patriarchInfoViewModel.errorHandler.handleError(it)
                    }
                }
            })
        }
    }

    private fun checkValueIsNotNull(textView: TextView, tips: String): Boolean {
        if (textView.text.toString().isEmpty()) {
            ToastUtils.showShort("${tips}未填写喔")
            return true
        }
        return false
    }

    private fun deleteAddress() {
        showLoadingDialog()
        if (mAddress!!.record_id == null) return
        patriarchInfoViewModel.deleteAddress(mAddress!!.record_id!!).observe(this, Observer {
            dismissLoadingDialog()
            it?.let {
                it.onSuccess {
                    ToastUtils.showShort("删除成功")
                    patriarchInfoViewModel.refreshPatriarchInfoEventCenter.setRefreshAddressEvent(false)
                    exitFragment()
                }.onError {
                    ToastUtils.showShort("删除失败")
                }
            }
        })
    }

    private fun isShowTip(): Boolean {
        val name = etReceiverName.text.toString()
        val phone = etReceiverPhone.text.toString()
        val area = tvAddress.text
        val address = etAddressDetail.text.toString()
        return if (mAddress == null) {
            name.isNotEmpty() || phone != mPatriarchPhone || area.isNotEmpty() || address.isNotEmpty()
        } else {
            mAddress?.let {
                it.receiver_name != name || it.receiver_phone != phone || it.address != address || mDistrictCode != it.district_area_code
            } ?: false
        }
    }

    private fun exit() {
        if (isShowTip()) {
            showConfirmDialog {
                message = "您的编辑未保存，确定要退出吗？"
                positiveListener = {
                    exitFragment()
                }
            }
        } else {
            exitFragment()
        }
    }

    override fun handleBackPress(): Boolean {
        return if (isShowTip()) {
            exit()
            true
        } else
            super.handleBackPress()
    }

    override fun onDestroy() {
        locationUtils.destroy()
        super.onDestroy()
    }
}