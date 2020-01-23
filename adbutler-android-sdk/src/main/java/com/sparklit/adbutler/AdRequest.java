package com.sparklit.adbutler;


import android.location.Location;
import android.os.Bundle;

import java.util.Date;
import java.util.Set;

/**
 * An AdButler SDK ad request used to load an ad.
 */
public class AdRequest {

    // Mediation data
    private Boolean isTestMode;
    private Date birthday;
    private int gender;
    private Location location;
    private int age = 0;
    private int yearOfBirth = 0;
    private int coppa = 0;
    private Bundle customExtras;
    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    private int accountID;
    private int zoneID;
    private int width;
    private int height;

    /**
     * Creates a new {@link AdRequest}.
     */
    public AdRequest(int accountID, int zoneID) {
        this.accountID = accountID;
        this.zoneID = zoneID;
    }

    /**
     * Sets keywords for targeting purposes.
     *
     * @param keywords A set of keywords to pass to mediation.
     */
    public void setKeywords(Set<String> keywords) {
        // Normally we'd save the keywords. But since this is a sample network, we'll do nothing.
    }

    /**
     * Designates a request for test mode.
     *
     * @param useTesting {@code true} to enable test mode.
     */
    public void setTestMode(boolean useTesting) {
        this.isTestMode = useTesting;
    }

    public Boolean getTestMode() {
        return isTestMode;
    }

    /**
     * Sets the mediation location data.
     *
     * @param location The android.Location (optional)
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Gets the mediation location data.
     *
     * @return Location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Set the mediation birthday data.
     *
     * @param birthday The full date of birth.
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * Get the mediation birthday data.
     *
     * @return Date
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * Set the mediation gender data.
     *
     * GENDER_UNKNOWN = 0
     * GENDER_MALE = 1
     * GENDER_FEMALE = 2
     *
     * @param gender An integer representing a gender.
     */
    public void setGender(int gender) {
        this.gender = gender;
    }

    /**
     * Get the mediation gender data.
     *
     * GENDER_UNKNOWN = 0
     * GENDER_MALE = 1
     * GENDER_FEMALE = 2
     *
     * @return int
     */
    public int getGender() {
        return gender;
    }

    /**
     * Get the mediation age data.
     *
     * @return int
     */
    public int getAge() {
        return age;
    }

    /**
     * Set the mediation age data
     *
     * @param age
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Get the mediation year of birth data.
     *
     * @return int
     */
    public int getYearOfBirth() {
        return yearOfBirth;
    }

    /**
     * Set the mediation year of birth data.
     *
     * @param yearOfBirth An integer representing the year of birth.
     */
    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    /**
     * Get the mediation width.
     *
     * @return int
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Set the mediation width.
     *
     * @param width An integer representing the width of the ad you wish to retrieve
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get the mediation height.
     *
     * @return int
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Set the mediation height.
     *
     * @param height An integer representing the height of the ad you wish to retrieve
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the mediation coppa data.
     *
     * @return int
     */
    public int getCoppa() {
        return coppa;
    }

    /**
     * Set the mediation coppa data.
     *
     * @param coppa An integer representing coppa.
     */
    public void setCoppa(int coppa) {
        this.coppa = coppa;
    }

    /**
     * Get the mediation custom extras data.
     *
     * @return Bundle
     */
    public Bundle getCustomExtras() {
        return customExtras;
    }

    /**
     * Set the mediation custom extras data.
     *
     * @param customExtras An optional collection (Bundle) of custom data.
     */
    public void setCustomExtras(Bundle customExtras) {
        this.customExtras = customExtras;
    }

    /**
     * Get the account ID.
     *
     * @return int
     */
    public int getAccountID(){return this.accountID;}

    /**
     * Get the zone ID.
     *
     * @return int
     */
    public int getZoneID(){return this.zoneID;}
}
