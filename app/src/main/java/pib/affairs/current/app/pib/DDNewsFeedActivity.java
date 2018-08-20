package pib.affairs.current.app.pib;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


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

import utils.AdsSubscriptionManager;
import utils.FireBaseHandler;
import utils.News;
import utils.NightModeManager;

import static com.android.volley.VolleyLog.TAG;


public class DDNewsFeedActivity extends AppCompatActivity {


    TextView titleText, descriptionTextView, dateTextView;
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

        setContentView(R.layout.activity_ddnews_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        news = (News) getIntent().getSerializableExtra("news");
        pushNotification = getIntent().getBooleanExtra("pushNotification", false);

        titleText = (TextView) findViewById(R.id.ddnews_title_textView);
        descriptionTextView = (TextView) findViewById(R.id.ddnews_description_textView);
        dateTextView = (TextView) findViewById(R.id.ddnews_newsDate_textView);
        webView = findViewById(R.id.ddnews_news_webView);


        if (pushNotification) {
            downloadNewsById();
        }
        initializeUI();

       /* news.setDescription(news.getDescription().replaceAll("<p>","<br>"));
        news.setDescription(news.getDescription().replaceAll("</p>","<br>"));



        if (Build.VERSION.SDK_INT >= 24) {
            descriptionTextView.setText(Html.fromHtml(news.getDescription(), Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH));
        } else {
            descriptionTextView.setText(Html.fromHtml(news.getDescription()));
        }*/

        //descriptionTextView.setText(news.getDescription());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openShareDialog(news.getLink());
            }
        });

        try {
            Answers.getInstance().logCustom(new CustomEvent("DD News feed Opened").putCustomAttribute("title", news.getTitle()).putCustomAttribute("Link", news.getLink()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeBottomNativeAds();




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
        titleText.setText(news.getTitle());

        webView.loadDataWithBaseURL("", news.getDescription(), "text/html", "UTF-8", "");
        dateTextView.setText(news.getPubDate());

        if (news.getDescription()==null){
            webView.loadUrl(news.getLink());
            webView.getSettings().setJavaScriptEnabled(true);
        }else{
            if (news.getDescription().isEmpty()){
                webView.loadUrl(news.getLink());
                webView.getSettings().setJavaScriptEnabled(true);
            }
        }

    }

    public void onBackPressed() {

        if (pushNotification) {
            super.onBackPressed();
            Intent intent = new Intent(DDNewsFeedActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }

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


    private void openShareDialog(String shortUrl) {


        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        //sharingIntent.putExtra(Intent.EXTRA_STREAM, newsMetaInfo.getNewsImageLocalPath());

        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shortUrl
                + "\n\nRead DD News update from PIB Reader & DD News App -\n https://play.google.com/store/apps/details?id=app.crafty.studio.current.affairs.pib");
        startActivity(Intent.createChooser(sharingIntent, "share link via"));


        try {
            Answers.getInstance().logCustom(new CustomEvent("Share Link Created").putCustomAttribute("shared dd news link", news.getTitle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        if (AdsSubscriptionManager.getSubscription(this)) {
            return;
        }

        initializeTopNative();

        if (nativeAd == null) {

            nativeAd = new NativeAd(this, "1963281763960722_1972656879689877");
            nativeAd.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.d(TAG, "onError: " + adError);

                    try {
                        Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "DD Newsfeed").putCustomAttribute("error", adError.getErrorMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //initializeAppnext();
                    initializeAdmob();

                }

                @Override
                public void onAdLoaded(Ad ad) {
                    NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes()
                            .setBackgroundColor(Color.LTGRAY)
                            .setButtonBorderColor(getResources().getColor(R.color.colorPrimary))
                            .setButtonColor(getResources().getColor(R.color.colorPrimary))
                            .setButtonTextColor(Color.WHITE);


                    View adView = NativeAdView.render(DDNewsFeedActivity.this, nativeAd, NativeAdView.Type.HEIGHT_400, viewAttributes);
                    CardView nativeAdContainer = (CardView) findViewById(R.id.ddnews_adContainer_LinearLayout);
                    // Add the Native Ad View to your ad container
                    nativeAdContainer.removeAllViews();
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

    private void initializeAdmob() {

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId("ca-app-pub-8455191357100024/4291164035");

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        CardView nativeAdContainer =  findViewById(R.id.admobAdContainer_LinearLayout);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(adView);
    }

    private void initializeTopNative() {


        com.facebook.ads.AdView adView = new com.facebook.ads.AdView(this, "1963281763960722_2041347772820787", com.facebook.ads.AdSize.BANNER_HEIGHT_50);

        CardView nativeAdContainer = findViewById(R.id.admobAdContainer_top_LinearLayout);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(adView);

        // Request an ad
        adView.loadAd();

        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                initializeTopAdmob();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "onAdLoaded: " + ad);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

    }

    private void initializeTopAdmob() {

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-8455191357100024/4291164035");

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        CardView nativeAdContainer =  findViewById(R.id.admobAdContainer_top_LinearLayout);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(adView);
    }



}
