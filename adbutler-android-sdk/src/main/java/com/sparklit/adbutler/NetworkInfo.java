package com.sparklit.adbutler;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetworkInfo {

    public String networkClass = null;
    public String carrierName = null;
    public String carrierCode = null;
    public String carrierCountryIso = null;

    public void initialize(Context context) {

        networkClass = getNetworkClass(context);

        if (networkClass.equals("WIFI")) {
            // Check for wifi first
            // TODO consider adding additional information in here later

        } else if (!networkClass.equals("")) {
            // Check to see if it's a known network class and pull the carrier info from there otherwise.
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != manager) {
                carrierName = manager.getNetworkOperatorName();
                carrierCode = manager.getNetworkOperator();
                carrierCountryIso = manager.getNetworkCountryIso();
            }
        }
    }


    public static String getNetworkClass(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null == cm) {
            return "";
        }

        android.net.NetworkInfo info;
        try {
            info = cm.getActiveNetworkInfo();
        } catch (NullPointerException e) {
            return "";
        }

        if (!info.isConnected())
            return ""; //not connected
        if (info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                case TelephonyManager.NETWORK_TYPE_GSM:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return "4G";
                default:
                    return "";
            }
        }
        return "";
    }
}
