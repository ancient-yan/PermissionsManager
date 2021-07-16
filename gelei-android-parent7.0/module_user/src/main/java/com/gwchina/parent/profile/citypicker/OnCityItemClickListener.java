package com.gwchina.parent.profile.citypicker;

import com.gwchina.parent.profile.citypicker.bean.CityBean;
import com.gwchina.parent.profile.citypicker.bean.DistrictBean;
import com.gwchina.parent.profile.citypicker.bean.ProvinceBean;

public abstract class OnCityItemClickListener {
    
    /**
     * 当选择省市区三级选择器时，需要覆盖此方法
     * @param province
     * @param city
     * @param district
     */
    public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
        
    }
    
    /**
     * 取消
     */
    public void onCancel() {
        
    }
}
