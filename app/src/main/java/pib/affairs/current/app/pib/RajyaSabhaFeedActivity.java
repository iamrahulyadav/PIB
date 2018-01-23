package pib.affairs.current.app.pib;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.facebook.ads.NativeAdViewAttributes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;



import java.util.EnumSet;

import utils.AdsSubscriptionManager;
import utils.News;
import utils.NightModeManager;

import static com.android.volley.VolleyLog.TAG;


public class RajyaSabhaFeedActivity extends AppCompatActivity {

    TextView titleText, dateTextView;
    WebView webView;

    private News news;
    private NativeAd nativeAd;


    ProgressDialog pDialog;



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

        titleText = (TextView) findViewById(R.id.ddnews_title_textView);
        webView = (WebView) findViewById(R.id.rajyasabha_news_webView);
        dateTextView = (TextView) findViewById(R.id.ddnews_newsDate_textView);

        titleText.setText(news.getTitle());


        webView.loadDataWithBaseURL("", news.getDescription(), "text/html", "UTF-8", "");


        dateTextView.setText(news.getPubDate());

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



        pDialog = new ProgressDialog(this);


    }

    private void initializeMoPubAds() {

/*
        mobfoxNative = new Native(this);


        mobfoxNative.setListener(new NativeListener() {
            @Override
            public void onNativeReady(Native aNative, CustomEventNative customEventNative, com.mobfox.sdk.nativeads.NativeAd nativeAd) {
                Log.d(TAG, "onNativeReady: " + aNative);
            }

            @Override
            public void onNativeError(Exception e) {
                Log.d(TAG, "onNativeError: " + e);
            }

            @Override
            public void onNativeClick(com.mobfox.sdk.nativeads.NativeAd nativeAd) {

            }
        });

        mobfoxNative.load("ae23e4db7c8c880d086e805e0322a1d5");

        ViewBinder viewBinder =new ViewBinder.Builder(R.layout.mopub_adrendrer)
                .mainImageId(R.id.native_ad_main_image)
                .iconImageId(R.id.native_ad_icon_image)
                .titleId(R.id.native_ad_title)
                .textId(R.id.native_ad_text)
                .build();

         MoPubNative moPubNative = new MoPubNative(this, "abf28c2b7a4c416e929c4d441f45f24e", new MoPubNative.MoPubNativeNetworkListener() {
             @Override
             public void onNativeLoad(com.mopub.nativeads.NativeAd nativeAd) {
                 Log.d(TAG, "onNativeLoad: "+nativeAd);
                 nativeAd.setMoPubNativeEventListener(new com.mopub.nativeads.NativeAd.MoPubNativeEventListener() {
                     @Override
                     public void onImpression(View view) {

                     }

                     @Override
                     public void onClick(View view) {

                     }
                 });

                 CardView nativeAdContainer = (CardView) findViewById(R.id.rajyasabha_top_adContainer_LinearLayout);

                 nativeAd.renderAdView(nativeAdContainer);
                 nativeAd.prepare(nativeAdContainer);
             }

             @Override
             public void onNativeFail(NativeErrorCode errorCode) {
                 Log.d(TAG, "onNativeFail: "+errorCode);
             }
         });
        moPubNative.registerAdRenderer(new MoPubStaticNativeAdRenderer(viewBinder));

        moPubNative.makeRequest(new RequestParameters.Builder()
                .desiredAssets(EnumSet.of(
                        RequestParameters.NativeAdAsset.TITLE,
                        RequestParameters.NativeAdAsset.TEXT,
                        RequestParameters.NativeAdAsset.MAIN_IMAGE,
                        RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT))
                .build());
*/
    }

    private void initializeStartappAds() {


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

}
