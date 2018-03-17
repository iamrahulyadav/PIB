package pib.affairs.current.app.pib;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.facebook.ads.NativeAdViewAttributes;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;


import java.util.ArrayList;
import java.util.EnumSet;

import utils.AdsSubscriptionManager;
import utils.FireBaseHandler;
import utils.News;
import utils.NightModeManager;
import utils.Translation;

import static android.util.Log.VERBOSE;
import static com.android.volley.VolleyLog.TAG;


public class RajyaSabhaFeedActivity extends AppCompatActivity {

    TextView titleText, dateTextView;
    WebView webView;

    private News news;
    private NativeAd nativeAd;


    ProgressDialog pDialog;
    private boolean pushNotification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (NightModeManager.getNightMode(this)) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }
        setContentView(R.layout.activity_rajya_sabha_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        news = (News) getIntent().getSerializableExtra("news");
        pushNotification = getIntent().getBooleanExtra("pushNotification", false);

        titleText = (TextView) findViewById(R.id.ddnews_title_textView);
        webView = (WebView) findViewById(R.id.rajyasabha_news_webView);
        dateTextView = (TextView) findViewById(R.id.ddnews_newsDate_textView);

        if (pushNotification) {
            downloadNewsById();
        }
        initializeUI();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openShareDialog(news.getLink());
            }
        });

        try {
            Answers.getInstance().logCustom(new CustomEvent("RSTV News feed Opened").putCustomAttribute("title", news.getTitle()).putCustomAttribute("Link", news.getLink()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!AdsSubscriptionManager.getSubscription(RajyaSabhaFeedActivity.this)) {
            initializeBottomNativeAds();
            initializeTopNativeAds();
        }


    }

    private void downloadNewsById() {
        showLoadingDialog("Loading...");
        new FireBaseHandler().downloadOtherNewsById(news.getNewsID(), new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (!newsArrayList.isEmpty()) {
                    news = newsArrayList.get(0);
                    initializeUI();
                    hideLoadingDialog();
                }
            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

            }
        });
    }

    private void initializeUI() {


        news.setDescription(news.getDescription() + "function getSelected() {\n" +
                "\n" +
                "\n" +
                "\n" +
                "    if(window.getSelection) { \n" +
                "    alert(window.getSelection());\n" +
                "    return window.getSelection(); }\n" +
                "        else if(document.getSelection) { \n" +
                "        alert(document.getSelection());\n" +
                "        return document.getSelection(); }\n" +
                "                    else {\n" +
                "                            var selection = document.selection && document.selection.createRange();\n" +
                "                            if(selection.text) { \n" +
                "                                      alert(selection.text);\n" +
                "                            return selection.text; \n" +
                "                            }\n" +
                "                return false;\n" +
                "            }\n" +
                "            return false;\n" +
                "        }\n" +
                "        \n" +
                "     document.addEventListener('dblclick', function(){ \n" +
                "\n" +
                "getSelected(); myWordSelection.getWord('from webview');\n" +
                "});");


        webView.getSettings().setJavaScriptEnabled(true);
        titleText.setText(news.getTitle());
        webView.loadDataWithBaseURL("", news.getDescription(), "text/html", "UTF-8", "");
        dateTextView.setText(news.getPubDate());


        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                getSelectedWord(1000);
                return false;
            }
        });



    }

    public void callJavaScript() {


   /*     if (Build.VERSION.SDK_INT >= 19) {
            webView.evaluateJavascript("function getSelected() {\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "    if(window.getSelection) { \n" +
                    "    alert(window.getSelection());\n" +
                    "    return window.getSelection(); }\n" +
                    "        else if(document.getSelection) { \n" +
                    "        alert(document.getSelection());\n" +
                    "        return document.getSelection(); }\n" +
                    "                    else {\n" +
                    "                            var selection = document.selection && document.selection.createRange();\n" +
                    "                            if(selection.text) { \n" +
                    "                                      alert(selection.text);\n" +
                    "                            return selection.text; \n" +
                    "                            }\n" +
                    "                return false;\n" +
                    "            }\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        \n" +
                    "     document.addEventListener('taphold', function(){ \n" +
                    "\n" +
                    "getSelected();\n" +
                    "});", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Toast.makeText(RajyaSabhaFeedActivity.this, "Value is " + s, Toast.LENGTH_SHORT).show();
                }
            });
        }*/

        if (Build.VERSION.SDK_INT >= 19) {
            webView.evaluateJavascript("(function(){return window.getSelection().toString()})()",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Toast.makeText(RajyaSabhaFeedActivity.this, "Selection "+ value, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    public void getSelectedWord(long timeDelay) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                callJavaScript();

            }
        }, timeDelay);

    }


    public void onBackPressed() {

        if (pushNotification) {
            super.onBackPressed();
            Intent intent = new Intent(RajyaSabhaFeedActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }

    }

    private void initializeAppnext() {

/*
        try {
            com.appnext.nativeads.NativeAd appNextNative = new com.appnext.nativeads.NativeAd(this, "ac73473d-6ca6-4e38-baa8-5a81ae7b908c");
            appNextNative.setAdListener(new com.appnext.nativeads.NativeAdListener() {
                @Override
                public void onAdLoaded(com.appnext.nativeads.NativeAd nativeAd) {
                    super.onAdLoaded(nativeAd);
                    Log.d(TAG, "onAdLoaded: ");

                    showAppnextNative(nativeAd);
                }

                @Override
                public void onAdClicked(com.appnext.nativeads.NativeAd nativeAd) {
                    super.onAdClicked(nativeAd);
                }

                @Override
                public void onError(com.appnext.nativeads.NativeAd nativeAd, AppnextError appnextError) {
                    super.onError(nativeAd, appnextError);
                    Log.d(TAG, "onError: ");
                    try {
                        Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "App Next").putCustomAttribute("error", appnextError.getErrorMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void adImpression(com.appnext.nativeads.NativeAd nativeAd) {
                    super.adImpression(nativeAd);
                }
            });

            appNextNative.loadAd(new NativeAdRequest()
                    .setCachingPolicy(NativeAdRequest.CachingPolicy.ALL)
                    .setCreativeType(NativeAdRequest.CreativeType.ALL)
                    .setVideoLength(NativeAdRequest.VideoLength.SHORT)
                    .setVideoQuality(NativeAdRequest.VideoQuality.LOW)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
*/

    }

    private void showAppnextNative() {
/*
        try {
            CardView nativeAdContainer = (CardView) findViewById(R.id.ddnews_adContainer_LinearLayout);
            nativeAdContainer.removeAllViews();


            View appNextNativeLayout = getLayoutInflater().inflate(R.layout.native_appnext_container, null);


            ImageView imageView = appNextNativeLayout.findViewById(R.id.appnextNative_na_icon);
            //The ad Icon
            appNextNative.downloadAndDisplayImage(imageView, appNextNative.getIconURL());


            TextView textView = appNextNativeLayout.findViewById(R.id.appnextNative_na_title);
            //The ad title
            textView.setText(appNextNative.getAdTitle());

            MediaView mediaView = appNextNativeLayout.findViewById(R.id.appnextNative_na_media);
            //Setting up the Appnext MediaView

            mediaView.setMute(true);
            mediaView.setAutoPLay(false);
            mediaView.setClickEnabled(true);
            appNextNative.setMediaView(mediaView);

            TextView description = appNextNativeLayout.findViewById(R.id.appnextNative_description);
            //The ad description
            String str = appNextNative.getAdDescription() + "\n" + appNextNative.getStoreDownloads() + " peoples have used the app";

            description.setText(str);

            Button ctaButton = appNextNativeLayout.findViewById(R.id.appnextNative_install);
            //ctaButton.setText(appNextNative.getCTAText());


            //Registering the clickable areas - see the array object in `setViews()` function
            ArrayList<View> clickableView = new ArrayList<>();
            clickableView.add(mediaView);
            clickableView.add(textView);
            clickableView.add(imageView);
            clickableView.add(ctaButton);
            appNextNative.registerClickableViews(clickableView);

            com.appnext.nativeads.NativeAdView nativeAdView = appNextNativeLayout.findViewById(R.id.appnextNative_na_view);
            //Setting up the entire native ad view
            appNextNative.setNativeAdView(nativeAdView);


            nativeAdContainer.addView(appNextNativeLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ddnews_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_open_browser) {
            onOpenInBrowser();
            return true;
        } else if (id == R.id.action_share) {
            openShareDialog(news.getLink());
        }

        return super.onOptionsItemSelected(item);
    }

    private void onOpenInBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getLink()));
        startActivity(browserIntent);
    }


    public void onShareClick() {
        showLoadingDialog("Loading...");

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(news.getLink()))
                .setDynamicLinkDomain("mbj78.app.goo.gl")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("app.crafty.studio.current.affairs.pib")
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("user")
                                .setMedium("share")
                                .setCampaign("linkshare")
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(news.getTitle())
                                .setDescription(news.getDescription())
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {

                            Uri uri = task.getResult().getShortLink();
                            //openShareDialog(uri);

                            hideLoadingDialog();
                        } else {

                        }
                    }
                });


    }

    private void openShareDialog(String shortUrl) {


        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        //sharingIntent.putExtra(Intent.EXTRA_STREAM, newsMetaInfo.getNewsImageLocalPath());

        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shortUrl
                + "\n\nRead RSTV News update from PIB Reader & DD News App -\n https://play.google.com/store/apps/details?id=app.crafty.studio.current.affairs.pib");
        startActivity(Intent.createChooser(sharingIntent, "share link via"));


        try {
            Answers.getInstance().logCustom(new CustomEvent("Share Link Created").putCustomAttribute("shared dd news link", news.getTitle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showLoadingDialog(String message) {
        pDialog = new ProgressDialog(this);

        pDialog.setMessage(message);
        pDialog.show();
    }

    public void hideLoadingDialog() {
        try {
            pDialog.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void initializeBottomNativeAds() {

        if (nativeAd == null) {

            nativeAd = new NativeAd(this, "1963281763960722_1972656879689877");
            nativeAd.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.d(TAG, "onError: " + adError);

                    try {
                        Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "RSTV FEED").putCustomAttribute("error", adError.getErrorMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    initializeAdmob();
                }

                @Override
                public void onAdLoaded(Ad ad) {


                    View adView = NativeAdView.render(RajyaSabhaFeedActivity.this, nativeAd, NativeAdView.Type.HEIGHT_400);
                    CardView nativeAdContainer = (CardView) findViewById(R.id.ddnews_adContainer_LinearLayout);
                    // Add the Native Ad View to your ad container
                    nativeAdContainer.addView(adView);
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });

            // Initiate a request to load an ad.
            nativeAd.loadAd();
        }


    }

    private void initializeTopNativeAds() {

        final NativeAd topNativeAd = new NativeAd(this, "1963281763960722_2001918973430334");
        topNativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "onError: " + adError);

                try {
                    Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "RSTV FEED TOP").putCustomAttribute("error", adError.getErrorMessage()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                initializeTopAdmob();

            }

            @Override
            public void onAdLoaded(Ad ad) {

                NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes()
                        .setBackgroundColor(Color.LTGRAY)
                        .setButtonBorderColor(getResources().getColor(R.color.colorPrimary))
                        .setButtonColor(getResources().getColor(R.color.colorPrimary))
                        .setButtonTextColor(Color.WHITE);


                View adView = NativeAdView.render(RajyaSabhaFeedActivity.this, topNativeAd, NativeAdView.Type.HEIGHT_120, viewAttributes);
                CardView nativeAdContainer = (CardView) findViewById(R.id.rajyasabha_top_adContainer_LinearLayout);
                // Add the Native Ad View to your ad container
                nativeAdContainer.addView(adView);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        // Initiate a request to load an ad.
        topNativeAd.loadAd();

    }

    private void initializeAdmob() {

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId("ca-app-pub-8455191357100024/4291164035");

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        CardView nativeAdContainer = findViewById(R.id.admobAdContainer_LinearLayout);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(adView);
    }


    private void initializeTopAdmob() {

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-8455191357100024/4291164035");

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        CardView nativeAdContainer = (CardView) findViewById(R.id.admobAdContainer_top_LinearLayout);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(adView);
    }


}
