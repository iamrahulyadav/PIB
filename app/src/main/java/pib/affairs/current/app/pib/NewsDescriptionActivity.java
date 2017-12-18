package pib.affairs.current.app.pib;

import android.annotation.TargetApi;
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
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import utils.AppController;
import utils.AppRater;
import utils.FireBaseHandler;
import utils.News;
import utils.NewsParser;
import utils.NightModeManager;
import utils.SqlDatabaseHelper;

public class NewsDescriptionActivity extends AppCompatActivity {
    News news;
    WebView webView;
    boolean pushNotification;

    private NativeAd nativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }

        news = (News) getIntent().getSerializableExtra("news");
        boolean isOffline = getIntent().getBooleanExtra("isOffline", false);
        pushNotification= getIntent().getBooleanExtra("pushNotification",false);


        String htmlTextString = "";

        if (news.isBookMark()) {

            htmlTextString = new SqlDatabaseHelper(this)
                    .getFullNews(news.getLink());

        }

        webView = (WebView) findViewById(R.id.newsDesription_webView);
       /* webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMinimumFontSize(50);
        //webView.getSettings().setTextZoom(250);

        //initializeWebView();

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setUserAgentString("Android");

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setAppCachePath(this.getCacheDir().getPath());
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (NightModeManager.getNightMode(this)) {
            webView.setBackgroundColor(Color.parseColor("#5a666b"));
        }
        webView.setInitialScale(110);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

*/

        webView.loadUrl("http://pib.nic.in/index.aspx");




        if (news.isBookMark()) {
            if (!htmlTextString.isEmpty()) {
                webView.loadDataWithBaseURL("", htmlTextString, "text/html", "UTF-8", "");
            } else {
                webView.loadUrl("http://pib.nic.in/index.aspx");
            }
        } else {

            webView.loadUrl(news.getLink());
        }

        AppRater.app_launched(this);


        try{
            Answers.getInstance().logContentView(new ContentViewEvent().putContentId(news.getLink()).putContentName(news.getTitle()));
        }catch (Exception e){
            e.printStackTrace();
        }


        initializeBottomNativeAds(2000l);

        //initializeAds();

       /* if (Build.VERSION.SDK_INT > 19) {
            webView.evaluateJavascript("(function(){return window.getSelection().toString()})()",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.v("Web view", "SELECTION:" + value);
                        }
                    });

            webView.addJavascriptInterface(new JavaScriptInterface(), "javascriptinterface");
            webView.loadUrl("javascript:javascriptinterface.callback(window.getSelection().toString())");

        }
*/
        //temp();

        //getWebsite(news.getLink());

    }

    public void temp() {

        String tag_string_req = "string_req";

        final String url = news.getLink();

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                pDialog.hide();

                response = response.replaceFirst("width:60%", "width:90%");

                XmlToJson xmlToJson = new XmlToJson.Builder(response).build();

                Log.d("NEWS", "onResponse: "+xmlToJson.toString());

                //webView.loadDataWithBaseURL("", response, "text/html", "UTF-8", "");


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                pDialog.hide();
            }
        });

        strReq.setShouldCache(true);
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);


    }

    public void initializeWebView() {


        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                return shouldOverrideUrlLoading(url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                Uri uri = request.getUrl();
                return shouldOverrideUrlLoading(uri.toString());
            }

            private boolean shouldOverrideUrlLoading(final String url) {
                // Log.i(TAG, "shouldOverrideUrlLoading() URL : " + url);

                // Here put your code
                webView.loadUrl(url);

                return true; // Returning True means that application wants to leave the current WebView and handle the url itself, otherwise return false.
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (pushNotification){
            Intent intent =new Intent(NewsDescriptionActivity.this ,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_description_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_offline) {
            onSaveOfflineClick();
            return true;
        }
        else if (id == R.id.action_open_browser) {
            onOpenInBrowser();
            return true;
        } else if (id == R.id.action_share) {
            onShareClick(webView);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onOpenInBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getLink()));
        startActivity(browserIntent);
    }

    private void onPostClick() {
        new FireBaseHandler().uploadNews(news, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {

            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

                Toast.makeText(NewsDescriptionActivity.this, "News posted " + isSuccessful, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onSaveOfflineClick() {

        String tag_string_req = "string_req";

        final String url = news.getLink();

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                pDialog.hide();

                SqlDatabaseHelper sqlDatabaseHelper = new SqlDatabaseHelper(NewsDescriptionActivity.this);
                sqlDatabaseHelper.addSavedNews(news, response);

                RssFeedFragment.newNoteSaved = true;


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                pDialog.hide();
            }
        });

        strReq.setShouldCache(true);
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);


        try{
            Answers.getInstance().logCustom(new CustomEvent("Save offline").putCustomAttribute("offline article",news.getTitle()));
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void onShareClick(View view) {

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
                            openShareDialog(uri);

                        } else {

                        }
                    }
                });


    }

    private void openShareDialog(Uri shortUrl) {


        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        //sharingIntent.putExtra(Intent.EXTRA_STREAM, newsMetaInfo.getNewsImageLocalPath());

        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shortUrl
                + "\n\nRead Press Information Bureau update from PIB News");
        startActivity(Intent.createChooser(sharingIntent, "share link via"));


        try{
            Answers.getInstance().logCustom(new CustomEvent("Share Link Created").putCustomAttribute("share link",news.getTitle()));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void initializeBottomNativeAds(long timeDelay){
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                initializeBottomNativeAds();
            }
        }, timeDelay);
    }

    public void initializeBottomNativeAds(){
        nativeAd = new NativeAd(this, "1963281763960722_1972656879689877");
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {
                View adView = NativeAdView.render(NewsDescriptionActivity.this, nativeAd, NativeAdView.Type.HEIGHT_300);
                LinearLayout nativeAdContainer = (LinearLayout) findViewById(R.id.newsDesription_adContainer_linearLayout);
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

    private void getWebsite(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    Document doc = Jsoup.connect(url).get();
                    String title = doc.title();
                    Elements links = doc.select("#condiv");


                    builder.append(links.toString());


                } catch (IOException e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("NEWS", "run: "+builder.toString());
                        webView.loadDataWithBaseURL("", builder.toString(), "text/html", "UTF-8", "");
                    }
                });
            }
        }).start();
    }


}
