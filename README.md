# adbutler-android-mraid-sdk

This SDK will help you include AdButler ad items in your app.  It supports Image Banners, Interstitials, VAST/VPAID and MRAID 2.0 ads.

You can include this source code in your project, or build and import the AAR.

## Implementation

### Add permissions to your AndroidManifest.xml

``` xml
<!-- Required -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- Optional but recommended -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

__Note:__ *If you plan to use MRAID ads, you may want to include permissions for Photos, Calendar events, SMS, Location or any other MRAID functionality you wish to support. If you do not include them then the user will be prompted if and when an ad needs these permissions.*

### Add dependencies to your app build.gradle

``` none
implementation project(':adbutler-android-sdk')
implementation 'com.google.android.gms:play-services-ads:16.0.0' ← (may already by present)
implementation 'com.google.code.gson:gson:2.8.2'
implementation 'com.squareup.retrofit2:retrofit:2.1.0'
implementation 'com.squareup.retrofit2:converter-gson:2.1.0'
implementation 'com.squareup.okio:okio:1.11.0'
```

### Import required classes to any activity that will create ads

``` java
import com.sparklit.adbutler.AdButler;
import com.sparklit.adbutler.AdListener;
import com.sparklit.adbutler.AdRequest;
import com.sparklit.adbutler.BannerView;
import com.sparklit.adbutler.ErrorCode;
import com.sparklit.adbutler.Positions;
import com.sparklit.adbutler.Interstitial;
import android.app.FragmentManager;
```

### Initialize AdButler SDK as early as possible in Main Activity

``` java
AdButler.initialize(this); // 'this' being the context
```

## Retrieving and Displaying Ads

### Banners

Banners are image ads.  Typically they are displayed in the top or bottom of the screen.  Banners can be MRAID enabled, or just standard images.

It is recommended that you assign specific sizes to your AdButler zones, and match that size with your image file.  This size will be used to create the frame for the web view.  You can optionally specify the size in code.

#### Banner Fragment

Banners require the use of a Fragment, because they can have some Activity-like behaviour (MRAID ads).  Add a placeholder in your activity layout xml.

``` xml
<FrameLayout android:id="@+id/adbutler_frame"
   android:layout_width="match_parent"
   android:layout_height="match_parent">
   <fragment
       android:name="com.sparklit.adbutler.BannerView"
       android:id="@+id/adbutler_fragment"
       android:layout_height="wrap_content"
       android:layout_width="wrap_content"
       />
</FrameLayout>
```

#### In your Activity, you can retrieve an ad like this:

``` java
// get a reference to the fragment  (bannerView should be defined previously)
FragmentManager fm = getFragmentManager();
bannerView = (BannerView)fm.findFragmentById(R.id.adbutler_fragment);

// create an ad request
AdRequest request = new AdRequest(ACCOUNT_ID, ZONE_ID); // your account and zone id
// if you want to set any extra data, add it to the request object.
request.setCoppa(0);
request.setAge(30);
request.setGender(AdRequest.GENDER_MALE);
request.setBirthday(new Date());

// initialize will retrieve the banner, and place it in the fragment.
bannerView.initialize(request, Positions.BOTTOM_CENTER, this, new AdListener() {
   @Override
   public void onAdFetchSucceeded() {
       super.onAdFetchSucceeded();
   }

   @Override
   public void onAdFetchFailed(ErrorCode code) {
       super.onAdFetchFailed(code);
   }

   @Override
   public void onInterstitialDisplayed() {
       super.onInterstitialDisplayed();
   }

   @Override
   public void onAdExpanded(){
       super.onAdExpanded();
   }

   @Override
   public void onAdResized(){
       super.onAdResized();
   }

   @Override
   public void onAdLeavingApplication(){
       super.onAdLeavingApplication();
   }

   @Override
   public void onAdClosed() {
       super.onAdClosed();
   }

   @Override
   public void onAdClicked() {
       super.onAdClicked();
   }
});

```

## Interstitials

Interstitials are full screen ads, typically shown between levels of a game, or when loading content for a new view.  Interstitials can be MRAID enabled, or just standard images.

Interstitials do not require a fragment

### Interstitial example

``` java
interstitial = new Interstitial();
// create an ad request
AdRequest request = new AdRequest(0, 0); // your account and zone id
// if you want to set any extra data, add it to the request object.
request.setCoppa(0);
request.setAge(30);
request.setGender(AdRequest.GENDER_MALE);
request.setBirthday(new Date());

// initialize will fetch the ad, but not display it.  
// in this example we show the ad as soon as it is ready
interstitial.initialize(request, this, new AdListener() {
   @Override
   public void onAdFetchSucceeded() {
       super.onAdFetchSucceeded();
   }

   @Override
   public void onInterstitialReady(){
       super.onInterstitialReady();
        // for demo purposes we show the ad immediately once it’s ready.  You don’t have to do this.
       if(interstitial.isReady){  // you can check if it’s ready any time via this property
           interstitial.show();
       }
   }

   @Override
   public void onAdFetchFailed(ErrorCode code) {
       super.onAdFetchFailed(code);
   }

   @Override
   public void onInterstitialDisplayed() {
       super.onInterstitialDisplayed();
   }

   @Override
   public void onAdExpanded(){
       super.onAdExpanded();
   }

   @Override
   public void onAdResized(){
       super.onAdResized();
   }

   @Override
   public void onAdLeavingApplication(){
       super.onAdLeavingApplication();
   }

   @Override
   public void onAdClosed() {
       super.onAdClosed();
   }

   @Override
   public void onAdClicked() {
       super.onAdClicked();
   }
});
```

### VAST Video

A VAST Video ad requires a VASTListener to listen for VAST events.

Create one like this:

``` java
    new VASTListener() {
        @Override
        public void onMute() {
            super.onMute();
        }

        @Override
        public void onUnmute() {
            super.onUnmute();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onRewind() {
            super.onRewind();
        }

        @Override
        public void onSkip() {
            super.onSkip();
        }

        @Override
        public void onPlayerExpand() {
            super.onPlayerExpand();
        }

        @Override
        public void onPlayerCollapse() {
            super.onPlayerCollapse();
        }

        @Override
        public void onNotUsed() {
            super.onNotUsed();
        }

        @Override
        public void onLoaded() {
            super.onLoaded();
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onFirstQuartile() {
            super.onFirstQuartile();
        }

        @Override
        public void onMidpoint() {
            super.onMidpoint();
        }

        @Override
        public void onThirdQuartile() {
            super.onThirdQuartile();
        }

        @Override
        public void onComplete() {
            super.onComplete();
        }

        @Override
        public void onCloseLinear() {
            super.onCloseLinear();
        }

        @Override
        public void onClose(){
            // if you pass an orientation to VAST, then reset it here.
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        @Override
        public void onReady(){
            // you can display your VAST ad at any point after this is called.
        }

        @Override
        public void onError(){
            // handle errors
        }
    }
```

VAST ads must be preloaded, similarly to how interstitials work.  After the ad is loaded and prepared, the `onReady()` function will be called.  You will notice that the `play()` and `pause()` functions will be called first.  This is because the web view, in which the video will be played is loaded off screen, to make a smoother transition to the ad.

After the `onReady()` function is called, you can call the `display()` function on the VAST object to display the ad.

#### VAST Code Example

``` java
    // keep an instance of VASTVideo on your activity class, then intialize it like this:
    // orientation = "none", "portrait", "landscape", or null
    vastVideo = new VASTVideo(context, accountID, zoneID, publisherID, orientation, listener);
    vastVideo.preload();

    // after isReady()
    vastVideo.display();
```

If you try to display the ad before it is ready, nothing will happen.

You will need to do this process each time you want an ad to be displayed, so that your impressions are counted correctly.
