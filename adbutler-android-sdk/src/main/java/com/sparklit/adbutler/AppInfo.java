package com.sparklit.adbutler;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * A class used to describe the app using the AdButler SDK
 */
class AppInfo {

    public String packageName;
    public String appName;
    public String appVersion;

    /**
     * Uses the context to retrieve the package details.
     *
     * @param context Source context.
     */
    public void initialize(Context context) {
        appName = getApplicationName(context);
        packageName = context.getPackageName();

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //Handle exception
            appVersion = "";
        }
    }

    private String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}
