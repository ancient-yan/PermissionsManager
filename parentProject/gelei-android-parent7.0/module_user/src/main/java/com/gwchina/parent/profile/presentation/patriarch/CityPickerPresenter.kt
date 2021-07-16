package com.gwchina.parent.profile.presentation.patriarch

import android.support.v4.app.Fragment
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.profile.citypicker.OnCityItemClickListener
import com.gwchina.parent.profile.citypicker.bean.CityBean
import com.gwchina.parent.profile.citypicker.bean.DistrictBean
import com.gwchina.parent.profile.citypicker.bean.ProvinceBean
import com.gwchina.parent.profile.citypicker.citywheel.CityParseHelper
import com.gwchina.parent.profile.citypicker.widget.CityConfig
import com.gwchina.parent.profile.citypicker.widget.CityPicker

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-12 10:19
 */
class CityPickerPresenter(val fragment: Fragment) {

    var onCitySelectListener: ((String, String, String, String) -> Unit)? = null
    private val cityPicker = CityPicker()
    private val mWheelType = CityConfig.ShowType.PRO_CITY_DIS
    private val cityConfig = CityConfig.Builder()
            .setTitleColor(R.color.black)
            .setTitleSize(16)
            .build()

    fun init() {
        cityPicker.init(fragment.requireContext())
        cityPicker.setConfig(cityConfig)
        cityPicker.setOnCityItemClickListener(object : OnCityItemClickListener() {

            override fun onSelected(province: ProvinceBean, city: CityBean, district: DistrictBean) {
                onCitySelectListener?.invoke(province.id, city.id, district.id, province.name + city.name + district.name)
            }

            override fun onCancel() {

            }
        })
    }

    fun showDialog() {
        cityPicker.showCityPicker()
    }

    fun getAddressByAreaCode(provinceCode: String, cityCode: String, directCode: String): String {
        return cityPicker.parseHelper.getAreaByCode(provinceCode, cityCode, directCode)
    }

    fun getAddressDetailByDistrictCode(districtCode: String): CityParseHelper.AddressDetail {
        return cityPicker.parseHelper.getAddressDetailByDistrictCode(districtCode)
    }
}