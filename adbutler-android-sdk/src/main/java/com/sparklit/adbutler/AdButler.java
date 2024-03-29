package com.sparklit.adbutler;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Makes requests against the AdButler API.
 */
public class AdButler {

    static final String VERSION_NAME = "2.0.0";
    private boolean isInitialized = false;
    private String apiHostname = "servedbyadbutler.com";
    private String apiAppVersion = "adserve";
    private boolean personalAdsAllowed = true; // AdMob as an example assumes personal data can be sent by default, unless flagged "npa=1" in request

    private APIService service;

    private static AdButler instance;
    protected static FrequencyCappingManager frequencyCappingManager;

    private static int pageID = 0;
    private static HashMap<Integer, Integer> zonePlaceMap;

    /**
     * The AdButler object is a singleton.
     * @return AdButler
     */
    public static AdButler getInstance() {
        if (null == instance) {
            instance = new AdButler();
        }
        return instance;
    }

    /**
     * Required to be called at least once before you retrieve ads.
     * @param context
     */
    public static void initialize(Context context) {
        Context mainContext = context.getApplicationContext();
        AdButler sdk = AdButler.getInstance();
        sdk.init(mainContext);
        resetUniqueDelivery();
        frequencyCappingManager = new FrequencyCappingManager(mainContext);
    }

    protected void init(Context context) {
        if (isInitialized) {
            Log.w("Ads/AdButler", "Please try to avoid initializing the AdButlerSDK multiple times.");
            return;
        }
        isInitialized = true;

        Log.d("Ads/AdButler", "Initializing AdButler SDK v" + AdButler.VERSION_NAME);

        if (null == context) {
            throw new IllegalArgumentException("Context cannot be null.");
        } else {
            try {
                loadAdvertisingInfoViaTask(context);
            } catch (Exception e) {
                Log.d("Ads/AdButler", "Initialization failed to load the client AdvertisingId, proceeding without it.");
            }
        }
    }

    /**
     * Set the host name.
     * @param apiHostname
     */
    public void setApiHostname(String apiHostname) {
        this.apiHostname = apiHostname;
    }

    /**
     * Get the host name
     */
    public String getApiHostname(){
        return this.apiHostname;
    }

    /**
     * Set the app version.
     * @param apiAppVersion
     */
    public void setApiAppVersion(String apiAppVersion) {
        this.apiAppVersion = apiAppVersion;
    }

    /**
     * Used to set whether or not personal data can be sent to mediation.  (GDPR consent)
     * @param allowed
     */
    public static void setPersonalAdsAllowed(boolean allowed){ getInstance().personalAdsAllowed = allowed; }

    /**
     * Used to determine if personal data can be sent to mediation.
     * @return
     */
    public static boolean isPersonalAdsAllowed(){ return getInstance().personalAdsAllowed; }

    public static void incrementUniqueDelivery(int zoneID) {
        if(zonePlaceMap.get(zoneID) != null) {
            zonePlaceMap.put(zoneID, zonePlaceMap.get(zoneID) + 1);
        } else {
            zonePlaceMap.put(zoneID, 0);
        }
    }

    public static void resetUniqueDelivery() {
        int m = (int) Math.pow(10, 7 - 1);
        pageID = m + new Random().nextInt(9 * m);
        zonePlaceMap = new HashMap<>();
    }

    public static int getPage() {
        return pageID;
    }

    public static int getPlace(int zoneId) {
        Integer place = zonePlaceMap.get(zoneId);
        if(place != null) {
            return place;
        }
        return 0;
    }

    /**
     * Requests a pixel.
     *
     * @param url the URL string for this pixel.
     */
    public void requestPixel(String url) {
        final String _url = url;
        Call<ResponseBody> call = getAPIService().requestPixel(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Ads/AdButler", "Pixel Request [success=" + response.isSuccessful() + "]: " + _url);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Ads/AdButler", "Pixel Request [success=false]: " + _url);
                t.printStackTrace();
            }
        });
    }

    /**
     * Requests a single placement.
     *
     * @param config   the configuration used for requesting one placement.
     * @param listener the results, when ready, will be given to this given listener.
     */
    public void requestPlacement(PlacementRequestConfig config, final PlacementResponseListener listener) {
        config.setFrequencyCappingData(frequencyCappingManager.getData());
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(config);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        Call<PlacementResponse> call = getAPIService().requestPlacementPOST(buildConfigParamPOST(config), body);
        call.enqueue(new Callback<PlacementResponse>() {
            @Override
            public void onResponse(Call<PlacementResponse> call, Response<PlacementResponse> response) {
                if(Integer.parseInt(Integer.toString(response.code()).substring(0, 1)) == 5){
                    listener.error(new Throwable("Server Error"));
                }else {
                    if (response.body().getStatus().equals("SUCCESS")) {
                        frequencyCappingManager.parseResponseData(response.body().getPlacements().get(0));
                    }
                    listener.success(response.body());
                }
            }

            @Override
            public void onFailure(Call<PlacementResponse> call, Throwable t) {
                listener.error(t);
            }
        });
    }

    /**
     * Requests multiple placements.
     *
     * @param configs  the configurations, each used for one placement respectively.
     * @param listener the results, when ready, will be given to this given listener.
     */
    public void requestPlacements(List<PlacementRequestConfig> configs, final PlacementResponseListener listener) {
        final List<Call<PlacementResponse>> calls = new ArrayList<>();
        for (PlacementRequestConfig config : configs) {
            calls.add(getAPIService().requestPlacement(buildConfigParam(config)));
        }
        final List<PlacementResponse> responses = new ArrayList<>();
        final List<Throwable> throwables = new ArrayList<>();
        for (Call<PlacementResponse> call : calls) {
            call.enqueue(new Callback<PlacementResponse>() {
                @Override
                public void onResponse(Call<PlacementResponse> call, Response<PlacementResponse> response) {
                    responses.add(response.body());
                    checkResults(listener, calls, responses, throwables);
                }

                @Override
                public void onFailure(Call<PlacementResponse> call, Throwable t) {
                    throwables.add(t);
                    checkResults(listener, calls, responses, throwables);
                }
            });
        }
    }

    private void checkResults(final PlacementResponseListener listener,
                              List<Call<PlacementResponse>> calls,
                              List<PlacementResponse> responses,
                              List<Throwable> throwables) {
        if (responses.size() + throwables.size() != calls.size()) {
            return;
        }

        if (!throwables.isEmpty()) {
            listener.error(throwables.get(0));
            return;
        }

        List<Placement> placements = new ArrayList<>();
        for (PlacementResponse response : responses) {
            if (response.getStatus().equals("SUCCESS")) {
                placements.addAll(response.getPlacements());
            }
        }
        String status = placements.isEmpty() ? "NO_ADS" : "SUCCESS";
        PlacementResponse placementResponse = new PlacementResponse();
        placementResponse.setStatus(status);
        placementResponse.setPlacements(placements);

        listener.success(placementResponse);
    }


    private APIService getAPIService() {
        if (service == null) {
            String baseUrl = "https://" + this.apiHostname + "/" + this.apiAppVersion + "/";
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson));
            if(BuildConfig.DEBUG){
                builder.client(getUnsafeOkHttpClient().build());
            }
            service = builder.build().create(APIService.class);
        }

        return service;
    }

    private static Retrofit retrofit = null;

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private String buildConfigParamPOST(PlacementRequestConfig config) {
        String urlString = "";
        // Custom Extras
        if (null != config.getCustomExtras() && config.getCustomExtras().size() > 0) {
            Bundle bundle = config.getCustomExtras();
            StringBuilder sb = new StringBuilder();
            // process all custom extras, and
            for (String key : bundle.keySet()) {
                if (bundle.get(key) instanceof String) {
                    String value = (String) bundle.get(key);
                    sb.append(";");
                    sb.append(key);
                    sb.append("=");
                    sb.append(encodeParam(value));
                }
            }
            urlString += sb.toString();
        }

        return urlString;
    }

    private String buildConfigParam(PlacementRequestConfig config) {
        String urlString = String.format(";ID=%d;size=%dx%d;setID=%d",
                config.getAccountId(),
                config.getWidth(),
                config.getHeight(),
                config.getZoneId());

        urlString += ";type=json";

        urlString += ";apiv=" + AdButler.VERSION_NAME;

        if (config.getKeywords() != null && config.getKeywords().size() > 0) {
            String keywordsQuery = null;
            for (String keyword : config.getKeywords()) {
                if (keywordsQuery == null) {
                    keywordsQuery = ";kw=" + encodeParam(keyword);
                } else {
                    keywordsQuery += "," + encodeParam(keyword);
                }
            }
            urlString += keywordsQuery;
        }

        // User Agent
        if (config.getUserAgent() != null && config.getUserAgent().length() > 0) {
            urlString += ";ua=" + encodeParam(config.getUserAgent());
        }

        // IP
        if (config.getIp() != null) {
            urlString += ";ip=" + encodeParam(config.getIp());
        }

        // Advertising ID
        if (config.getAdvertisingId() != null) {
            urlString += ";aduid=" + encodeParam(config.getAdvertisingId());
            urlString += ";dnt=" + config.getDoNotTrack();
        }

        // Location
        if (config.getLatitude() != null && config.getLongitude() != null) {
            if (config.getLatitude() > 0) {
                urlString += ";lat=" + encodeParam(config.getLatitude().toString());
            }
            if (config.getLongitude() > 0) {
                urlString += ";long=" + encodeParam(config.getLongitude().toString());
            }
        }

        // User
        if (config.getAge() > 0) {
            urlString += ";age="+config.getAge();
        }
        if (config.getYearOfBirth() > 0) {
            urlString += ";yob="+config.getYearOfBirth();
        }
        if (config.getGender() != null) {
            urlString += ";gender="+encodeParam(config.getGender());
        }

        // Device
        if (config.getDeviceType() != null) {
            urlString += ";dvtype=" + encodeParam(config.getDeviceType());
        }
        if (config.getDeviceManufacturer() != null) {
            urlString += ";dvmake=" + encodeParam(config.getDeviceManufacturer());
        }
        if (config.getDeviceModel() != null) {
            urlString += ";dvmodel=" + encodeParam(config.getDeviceModel());
        }
        if (config.getOsName() != null) {
            urlString += ";os=" + encodeParam(config.getOsName());
        }
        if (config.getOsVersion() != null) {
            urlString += ";osv=" + encodeParam(config.getOsVersion());
        }
        if (config.getLanguage() != null) {
            urlString += ";lang=" + encodeParam(config.getLanguage());
        }

        // Screen
        if (config.getScreenWidth() > 0) {
            urlString += ";sw=" + config.getScreenWidth();
        }
        if (config.getScreenWidth() > 0) {
            urlString += ";sh=" + config.getScreenHeight();
        }
        if (config.getScreenPixelDensity() > 0) {
            urlString += ";spr=" + config.getScreenPixelDensity();
        }
        if (config.getScreenDotsPerInch() > 0) {
            urlString += ";sdpi=" + config.getScreenDotsPerInch();
        }

        // Network
        if (config.getNetworkClass() != null) {
            urlString += ";network=" + encodeParam(config.getNetworkClass());
        }
        if (config.getCarrier() != null) {
            urlString += ";carrier="+ encodeParam(config.getCarrier());
        }
        if (config.getCarrierCode() != null) {
            urlString += ";carriercode="+ encodeParam(config.getCarrierCode());
        }
        if (config.getCarrierCountryIso() != null) {
            urlString += ";carriercountry="+ encodeParam(config.getCarrierCountryIso());
        }

        // App
        if (null != config.getAppName()) {
            urlString += ";appname=" + encodeParam(config.getAppName());
        }
        if (null != config.getAppPackageName()) {
            urlString += ";appcode=" + encodeParam(config.getAppPackageName());
        }
        if (null != config.getAppVersion()) {
            urlString += ";appversion=" + encodeParam(config.getAppVersion());
        }

        // Flags
        urlString += ";coppa=" + config.getCoppa();


        // Custom Extras
        if (null != config.getCustomExtras() && config.getCustomExtras().size() > 0) {
            Bundle bundle = config.getCustomExtras();
            StringBuilder sb = new StringBuilder();
            // process all custom extras, and
            for (String key : bundle.keySet()) {
                if (bundle.get(key) instanceof String) {
                    String value = (String) bundle.get(key);
                    sb.append(";");
                    sb.append(key);
                    sb.append("=");
                    sb.append(encodeParam(value));
                }
            }
            urlString += sb.toString();
        }

        if (null != config.getDataKeys() && config.getDataKeys().size() > 0) {
            Bundle bundle = config.getDataKeys();
            StringBuilder sb = new StringBuilder();
            // process all custom extras, and
            for (String key : bundle.keySet()) {
                if (bundle.get(key) instanceof String) {
                    String value = (String) bundle.get(key);
                    sb.append(";_abdk[");
                    sb.append(key);
                    sb.append("]=");
                    sb.append(encodeParam(value));
                }
            }
            urlString += sb.toString();
        }


        // Click handled last
        if (config.getClick() != null) {
            urlString += ";click=" + encodeParam(config.getClick());
        }

        Log.d("Ads/AdButler", "QUERY: " + urlString);

        return urlString;
    }

    private String encodeParam(String param) {
        return URLEncoder.encode(param).replaceAll("\\+", "%20");
    }

    protected static AdvertisingInfo AdvertisingInfo;

    protected static class AdvertisingInfo {
        public final String advertisingId;
        public final boolean limitAdTrackingEnabled;

        public AdvertisingInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }
    }

    private void loadAdvertisingInfoViaTask(Context context) {
        AdvertisingInfo = new AdvertisingInfo(null, false);

        AdvertisingInfoLoadTask task = new AdvertisingInfoLoadTask(context);
        task.execute();
    }

    private void setAdvertisingInfo(String advertisingId, Boolean limitAdTracking) {
        AdvertisingInfo = new AdvertisingInfo(advertisingId, limitAdTracking);
    }

    private static class AdvertisingInfoLoadTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> weakRefContext;

        public AdvertisingInfoLoadTask(Context context) {
            weakRefContext = new WeakReference<Context>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AdvertisingIdClient.Info advertisingIdInfo = null;
            AdButler sdk = AdButler.getInstance();
            try {
                Context context = weakRefContext.get();
                advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (null != advertisingIdInfo) {
                sdk.setAdvertisingInfo(advertisingIdInfo.getId(), advertisingIdInfo.isLimitAdTrackingEnabled());
            } else {
                Log.d("Ads/AdButler", "Unable to retrieve the AdvertisingIdClient.Info data.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void voidValue) {
            // do nothing right now
        }
    }
}
