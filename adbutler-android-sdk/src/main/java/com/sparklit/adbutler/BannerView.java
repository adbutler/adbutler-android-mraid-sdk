package com.sparklit.adbutler;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * The fragment extension containing a Banner.
 */
public class BannerView extends Fragment {
    public Banner banner;
    public boolean initializing = false;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(banner != null){
            banner.setContext(getActivity());
            return banner.getWebView();
        }
        return inflater.inflate(R.layout.banner_layout, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle bundle){
        super.onViewStateRestored(bundle);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(banner != null && banner.getMRAIDHandler() != null && !banner.isWebViewProvided){
            banner.addToRoot();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Permissions.CALENDAR: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banner.getMRAIDHandler().writeCalendarEvent();
                } else {
                    // TODO show permission denied?
                }
                return;
            }
            case Permissions.PHOTO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banner.getMRAIDHandler().savePhoto();
                } else {
                    // TODO show permission denied?
                }
                return;
            }
            case Permissions.CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banner.getMRAIDHandler().makePhoneCall();
                } else {
                    // TODO show permission denied?
                }
                return;
            }
            case Permissions.SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banner.getMRAIDHandler().sendSMS();
                } else {
                    // TODO show permission denied?
                }
                return;
            }
        }
    }

    /*
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    /*
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        if(banner != null)
            banner.removeFromParent();
        super.onDetach();
    }

    public BannerView(){
    }

    /**
     * Create a simple banner without custom data.  Use if you do not need to set any details such as custom extras, user location, coppa etc.
     *
     * @param accountID Your AdButler Account ID.
     * @param zoneID The Zone you'd like a placement from.
     * @param position A string representing the screen location at which to show the banner I.E BOTTOM_CENTER.  (com.sparklit.adbutler.Positions)
     * @param context The context in which to show the banner.
     * @param listener Event listener for Ad related events.
     */
    public void initialize(int accountID, int zoneID, String position, Context context, AdListener listener){
        initialize(new AdRequest(accountID, zoneID), position, context, listener);
    }

    /**
     * Create a banner with custom data (AdRequest object).  Use if you want to specify any custom data in the ad request E.G. coppa
     *
     * @param request An AdRequest object, containing details required for mediation.
     * @param position A string representing the screen location at which to show the banner I.E BOTTOM_CENTER.  (com.sparklit.adbutler.Positions)
     * @param context The context in which to show the banner.
     * @param listener Event listener for Ad related events.
     */
    public void initialize(AdRequest request, String position, Context context, AdListener listener){
        if(initializing){
            return;
        }
        initializing = true;
        if(banner != null){
            banner.destroy();
        }
        banner = new Banner(this);
        banner.initialize(request, position, context, listener, this);
    }

    /**
     * Create a banner with custom data (AdRequest object).  Use if you want to specify any custom data in the ad request E.G. coppa
     *
     * @param request An AdRequest object, containing details required for mediation.
     * @param container A container view to display the banner in (not compatible with MRAID)
     * @param context The context in which to show the banner.
     * @param listener Event listener for Ad related events.
     */
    public void initialize(AdRequest request, FrameLayout container, Context context, AdListener listener){
        if(initializing){
            return;
        }
        initializing = true;
        if(banner != null){
            banner.destroy();
        }
        banner = new Banner(this);
        banner.initialize(request, container, context, listener);
    }

    public void destroy(){
        if(banner != null){
            banner.destroy();
            banner = null;
        }
    }
}
