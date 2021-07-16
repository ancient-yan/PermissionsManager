package com.gwchina.parent.profile.citypicker.widget;

public class CityConfig {

    /**
     * 默认显示的城市数据，只包含省市区名称
     * 定义显示省市区三种显示状态
     * PRO:只显示省份的一级选择器
     * PRO_CITY:显示省份和城市二级联动的选择器
     * PRO_CITY_DIS:显示省份和城市和县区三级联动的选择器
     */
    public enum ShowType {
        PRO_CITY, PRO_CITY_DIS
    }

    String titleText;
    int titleColor;
    int titleSize;
    int titleRightImg;
    int selectColor;
    int selectImg;


    private ShowType showType;

    public ShowType getShowType() {
        return showType;
    }

    public CityConfig(Builder builder) {
        this.titleText = builder.titleText;
        this.titleColor = builder.titleColor;
        this.titleSize = builder.titleSize;
        this.titleRightImg = builder.titleRightImg;
        this.selectColor = builder.selectColor;
        this.selectImg = builder.selectImg;
        this.showType = builder.showType;
    }


    public static class Builder {

        public Builder() {

        }

        private String titleText;
        private int titleColor;
        private int titleSize;
        private int titleRightImg;
        private int selectColor;
        private int selectImg;

        public Builder setTitleText(String titleText) {
            this.titleText = titleText;
            return this;
        }

        public Builder setTitleColor(int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder setTitleSize(int titleSize) {
            this.titleSize = titleSize;
            return this;
        }

        public Builder setTitleRightImg(int titleRightImg) {
            this.titleRightImg = titleRightImg;
            return this;
        }

        public Builder setSelectColor(int selectColor) {
            this.selectColor = selectColor;
            return this;
        }

        public Builder setSelectImg(int selectImg) {
            this.selectImg = selectImg;
            return this;
        }

        public ShowType showType = ShowType.PRO_CITY_DIS;

        /**
         * 显示省市区三级联动的显示状态
         * PRO_CITY:显示省份和城市二级联动的选择器
         * PRO_CITY_DIS:显示省份和城市和县区三级联动的选择器
         *
         * @param showType
         * @return
         */
        public Builder setJDCityShowType(ShowType showType) {
            this.showType = showType;
            return this;
        }

        public CityConfig build() {
            CityConfig config = new CityConfig(this);
            config.titleText = titleText;
            config.titleColor = titleColor;
            config.titleSize = titleSize;
            config.titleRightImg = titleRightImg;
            config.selectColor = selectColor;
            config.selectImg = selectImg;
            return config;
        }
    }
}
