package com.sparklit.adbutler;

import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

/**
 * Configurations for requesting a Placement.
 */
class PlacementRequestConfig {
    private int accountId;
    private int zoneId;
    private int width;
    private int height;
    private Set<String> keywords;
    private String click;
    private String advertisingId;
    private int doNotTrack;
    private int age;
    private int yearOfBirth;
    private String gender;
    private int coppa;
    private String carrier;
    private String carrierCode;
    private String networkClass;
    private String carrierCountryIso;
    private String ip;
    private Double latitude;
    private Double longitude;
    private String deviceManufacturer;
    private String deviceModel;
    private String deviceType;
    private String osName;
    private String osVersion;
    private String language;
    private int screenWidth;
    private int screenHeight;
    private float screenPixelDensity;
    private double screenDotsPerInch;
    private String userAgent;
    private String appName;
    private String appPackageName;
    private String appVersion;
    private Bundle customExtras;


    /**
     * The account ID for this request.
     * @return int
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * The publisher zone ID to select advertisements from.
     */
    public int getZoneId() {
        return zoneId;
    }

    /**
     * The width of the publisher zone.
     */
    public int getWidth() {
        return width;
    }

    /**
     * The height of the publisher zone.
     */
    public int getHeight() {
        return height;
    }

    /**
     * A comma delimited list of keywords.
     */
    public Set<String> getKeywords() {
        return keywords;
    }

    /**
     * A pass-through click URL.
     */
    public String getClick() {
        return click;
    }

    public int getAge() {
        return age;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public int getCoppa() {
        return coppa;
    }

    public String getAdvertisingId() {
        return advertisingId;
    }

    public int getDoNotTrack() {
        return doNotTrack;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public String getNetworkClass() {
        return networkClass;
    }

    public String getCarrierCountryIso() {
        return carrierCountryIso;
    }

    public String getIp() {
        return ip;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getLanguage() {
        return language;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public float getScreenPixelDensity() {
        return screenPixelDensity;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public double getScreenDotsPerInch() {
        return screenDotsPerInch;
    }

    public Bundle getCustomExtras() {
        return customExtras;
    }


    /**
     * Builder to configure the parameters used in requesting a Placement.
     */
    protected static class Builder {
        private int accountId;
        private int zoneId;
        private int width;
        private int height;
        private Set<String> keywords;
        private String click;
        private int age;
        private int yearOfBirth;
        private String gender;
        private int coppa;
        private String advertisingId;
        private int doNotTrack;
        private String carrier;
        private String carrierCode;
        private String networkClass;
        private String carrierCountryIso;
        private String ip;
        private Double latitude;
        private Double longitude;
        private String deviceManufacturer;
        private String deviceModel;
        private String deviceType;
        private String osName;
        private String osVersion;
        private String language;
        private int screenWidth;
        private int screenHeight;
        private float screenPixelDensity;
        private double screenDotsPerInch;
        private String userAgent;
        private String appName;
        private String appPackageName;
        private String appVersion;
        private Bundle customExtras;

        /**
         * @param accountId The account ID for this request.
         * @param zoneId    The publisher zone ID to select advertisements from.
         * @param width     The width of the publisher zone.
         * @param height    The height of the publisher zone.
         */
        public Builder(int accountId, int zoneId, int width, int height) {
            this.accountId = accountId;
            this.zoneId = zoneId;
            this.width = width;
            this.height = height;
        }

        /**
         * @param accountId The account ID for this request.
         * @param zoneId    The publisher zone ID to select advertisements from.
         */
        public Builder(int accountId, int zoneId) {
            this.accountId = accountId;
            this.zoneId = zoneId;
        }


        /**
         * Sets keywords used in the request.
         * This will override all existing keywords.
         *
         * @param keywords Keywords used in the request
         */
        public Builder setKeywords(Set<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        /**
         * Adds one keyword for the request.
         *
         * @param keyword Keyword used in the request
         */
        public Builder addKeyword(String keyword) {
            if (keywords == null) {
                keywords = new HashSet<>();
            }
            keywords.add(keyword);
            return this;
        }

        /**
         * Sets the pass-through click used in the request.
         * This will override existing click URL.
         *
         * @param click Click URL used in the request
         */
        public Builder setClick(String click) {
            this.click = click;
            return this;
        }

        public Builder setAge(int age) {
            this.age = age;
            return this;
        }

        public Builder setYearOfBirth(int yearOfBirth) {
            this.yearOfBirth = yearOfBirth;
            return this;
        }

        public Builder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder setCoppa(int coppa) {
            this.coppa = coppa;
            return this;
        }

        public Builder setAdvertisingId(String advertisingId) {
            this.advertisingId = advertisingId;
            return this;
        }

        public Builder setDoNotTrack(int doNotTrack) {
            this.doNotTrack = doNotTrack;
            return this;
        }

        public Builder setCarrier(String carrier) {
            this.carrier = carrier;
            return this;
        }

        public Builder setCarrierCode(String carrierCode) {
            this.carrierCode = carrierCode;
            return this;
        }

        public Builder setNetworkClass(String networkClass) {
            this.networkClass = networkClass;
            return this;
        }

        public Builder setCarrierCountryIso(String carrierCountryIso) {
            this.carrierCountryIso = carrierCountryIso;
            return this;
        }

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder setLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder setLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder setDeviceManufacturer(String deviceManufacturer) {
            this.deviceManufacturer = deviceManufacturer;
            return this;
        }

        public Builder setDeviceModel(String deviceModel) {
            this.deviceModel = deviceModel;
            return this;
        }

        public Builder setDeviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder setOsName(String osName) {
            this.osName = osName;
            return this;
        }

        public Builder setOsVersion(String osVersion) {
            this.osVersion = osVersion;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setScreenWidth(int screenWidth) {
            this.screenWidth = screenWidth;
            return this;
        }

        public Builder setScreenHeight(int screenHeight) {
            this.screenHeight = screenHeight;
            return this;
        }

        public Builder setScreenPixelDensity(float screenPixelDensity) {
            this.screenPixelDensity = screenPixelDensity;
            return this;
        }

        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder setAppPackageName(String appPackageName) {
            this.appPackageName = appPackageName;
            return this;
        }

        public Builder setAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public Builder setScreenDotsPerInch(double screenDotsPerInch) {
            this.screenDotsPerInch = screenDotsPerInch;
            return this;
        }

        public Builder setCustomExtras(Bundle customExtras) {
            this.customExtras = customExtras;
            return this;
        }

        /**
         * @return The PlacementRequestConfig that can be used in requesting a Placement.
         */
        public PlacementRequestConfig build() {
            return new PlacementRequestConfig(this);
        }
    }

    private PlacementRequestConfig(Builder builder) {
        accountId = builder.accountId;
        zoneId = builder.zoneId;
        width = builder.width;
        height = builder.height;
        keywords = builder.keywords;
        click = builder.click;
        age = builder.age;
        yearOfBirth = builder.yearOfBirth;
        gender = builder.gender;
        coppa = builder.coppa;
        advertisingId = builder.advertisingId;
        doNotTrack = builder.doNotTrack;
        carrier = builder.carrier;
        carrierCode = builder.carrierCode;
        networkClass = builder.networkClass;
        carrierCountryIso = builder.carrierCountryIso;
        ip = builder.ip;
        latitude = builder.latitude;
        longitude = builder.longitude;
        deviceManufacturer = builder.deviceManufacturer;
        deviceModel = builder.deviceModel;
        deviceType = builder.deviceType;
        osName = builder.osName;
        osVersion = builder.osVersion;
        language = builder.language;
        screenWidth = builder.screenWidth;
        screenHeight = builder.screenHeight;
        screenPixelDensity = builder.screenPixelDensity;
        screenDotsPerInch = builder.screenDotsPerInch;
        userAgent = builder.userAgent;
        appName = builder.appName;
        appPackageName = builder.appPackageName;
        appVersion = builder.appVersion;
        customExtras = builder.customExtras;
    }
}
