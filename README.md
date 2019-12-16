# adbutler-android-sdk

This SDK will help you include AdButler ad items in your app.  It supports Image Banners, Interstitials, and MRAID 2.0 ads.

You can include this source code in your project, or import the AAR.

#####Add permissions to your AndroidManifest.xml:

```
<!-- Required -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- Optional but recommended -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

__Note:__ *If you plan to use MRAID ads, you may want to include permissions for Photos, Calendar events, SMS, Location or any other MRAID functionality you wish to support. If you do not include them then the user will be prompted if and when an ad needs these permissions.*

#####Add dependencies to your app build.gradle:

```
implementation project(':adbutler-android-sdk')
implementation 'com.google.android.gms:play-services-ads:16.0.0' ← (may already by present)
implementation 'com.google.code.gson:gson:2.8.2'
implementation 'com.squareup.retrofit2:retrofit:2.1.0'
implementation 'com.squareup.retrofit2:converter-gson:2.1.0'
implementation 'com.squareup.okio:okio:1.11.0'
```

#####Import required classes to any activity that will create ads.

```
import com.sparklit.adbutler.AdButler;
import com.sparklit.adbutler.AdListener;
import com.sparklit.adbutler.AdRequest;
import com.sparklit.adbutler.BannerView;
import com.sparklit.adbutler.ErrorCode;
import com.sparklit.adbutler.Positions;
import com.sparklit.adbutler.Interstitial;
import android.app.FragmentManager;
```


#####Initialize AdButler SDK as early as possible in Main Activity:

```
AdButler.initialize(this); // 'this' being the context
```

#Retrieving and Displaying Ads:

##Banners:

Banners are image ads.  Typically they are displayed in the top or bottom of the screen.  Banners can be MRAID enabled, or just standard images.

It is recommended that you assign specific sizes to your AdButler zones, and match that size with your image file.  This size will be used to create the frame for the web view.  You can optionally specify the size in code.

####Banners require the use of a Fragment, because they have some Activity-like behaviour.  Add a placeholder in your activity layout xml.

```
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


####In your Activity, you can retrieve an ad like this:

```
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

##Interstitials:

Interstitials are full screen ads, typically shown between levels of a game, or when loading content for a new view.  Interstitials can be MRAID enabled, or just standard images.

####Interstitials do not require a fragment.

```
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