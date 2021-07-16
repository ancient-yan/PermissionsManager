package com.gwchina.parent.profile.citypicker.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ProvinceBean implements Parcelable {

    private String id; /*110101*/

    private String name; /*东城区*/

    private String parent_id;

    private int area_level;

    private ArrayList<CityBean> cityList;

    @Override
    public String toString() {
        return name;
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public int getArea_level() {
        return area_level;
    }

    public void setArea_level(int area_level) {
        this.area_level = area_level;
    }

    public ArrayList<CityBean> getCityList() {
        return cityList;
    }

    public void setCityList(ArrayList<CityBean> cityList) {
        this.cityList = cityList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.parent_id);
        dest.writeInt(this.area_level);
        dest.writeTypedList(this.cityList);
    }

    public ProvinceBean() {
    }

    protected ProvinceBean(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.parent_id = in.readString();
        this.area_level = in.readInt();
        this.cityList = in.createTypedArrayList(CityBean.CREATOR);
    }

    public static final Creator<ProvinceBean> CREATOR = new Creator<ProvinceBean>() {
        @Override
        public ProvinceBean createFromParcel(Parcel source) {
            return new ProvinceBean(source);
        }

        @Override
        public ProvinceBean[] newArray(int size) {
            return new ProvinceBean[size];
        }
    };
}
