package pib.affairs.current.app.pib;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import utils.AppController;
import utils.AppRater;
import utils.News;
import utils.NewsParser;
import utils.NightModeManager;
import utils.SettingManager;
import utils.SqlDatabaseHelper;

import static com.android.volley.VolleyLog.TAG;

public class NewsFeedActivity extends AppCompatActivity  {


    TextView newsTextView, newsHeadingTextView, newsDateTextView, newsMinistryTextView;

    News news;

    String newsMinistry, newsTextString;

    WebView webView;
    String tableDataString;

    ProgressDialog pDialog;

    private NativeAd nativeAd;
    private boolean pushNotification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (NightModeManager.getNightMode(this)) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }

        setContentView(R.layout.activity_news_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pDialog = new ProgressDialog(this);

        news = (News) getIntent().getSerializableExtra("news");
        pushNotification= getIntent().getBooleanExtra("pushNotification",false);

        newsTextView = (TextView) findViewById(R.id.newsFeed_text_textView);
        newsHeadingTextView = (TextView) findViewById(R.id.newsFeed_newsHeading_textView);
        newsDateTextView = (TextView) findViewById(R.id.newsFeed_newsDate_textView);
        newsMinistryTextView = (TextView) findViewById(R.id.newsFeed_newsministry_textView);

        webView = (WebView) findViewById(R.id.newsFeed_webView);

        newsHeadingTextView.setText(news.getTitle());

        newsDateTextView.setText(news.getPubDate());
        setTextSize(newsTextView);

        if (news.isBookMark()) {
            String htmlTextString = new SqlDatabaseHelper(this)
                    .getFullNews(news.getLink());

            initializeActivityData(htmlTextString);

        }

        getWebsite(news.getLink());

        showLoadingDialog("Loading...");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShareClick();
            }
        });


        //initializeNewsText();

        AppRater.app_launched(this);




            try{
                AppRater.app_launched(this);


                Answers.getInstance().logContentView(new ContentViewEvent().putContentId(news.getLink()).putContentName(news.getTitle()));
            }catch (Exception e){
                e.printStackTrace();
            }
        initializeBottomNativeAds(1000l);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news_feed, menu);
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
        } else if (id == R.id.action_open_browser) {
            onOpenInBrowser();
            return true;
        } else if (id == R.id.action_share) {
            onShareClick();
            return true;
        } else if (id == R.id.action_textSize) {
            onTextSizeClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (pushNotification){
            Intent intent =new Intent(NewsFeedActivity.this ,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void onSaveOfflineClick() {

        String tag_string_req = "string_req";

        final String url = news.getLink();


        showLoadingDialog("Loading...");


        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                pDialog.hide();

                SqlDatabaseHelper sqlDatabaseHelper = new SqlDatabaseHelper(NewsFeedActivity.this);
                sqlDatabaseHelper.addSavedNews(news, response);

                RssFeedFragment.newNoteSaved = true;

                Snackbar snackbar = Snackbar
                        .make(newsTextView, "Article saved Offline 👍", Snackbar.LENGTH_LONG);


                snackbar.show();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                hideLoadingDialog();
            }
        });

        strReq.setShouldCache(true);
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);


        try {
            Answers.getInstance().logCustom(new CustomEvent("Save offline").putCustomAttribute("offline article", news.getTitle()));
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
                            openShareDialog(uri);

                            hideLoadingDialog();
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


        try {
            Answers.getInstance().logCustom(new CustomEvent("Share Link Created").putCustomAttribute("share link", news.getTitle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void onTextSizeClick() {
        final CharSequence sources[] = new CharSequence[]{"Small", "Medium", "Large", "Extra Large"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Text Size");
        builder.setItems(sources, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                int size = 18;

                if (which == 0) {
                    size = 16;
                } else if (which == 1) {
                    size = 18;
                } else if (which == 2) {
                    size = 20;
                } else if (which == 3) {
                    size = 22;
                }

                setTextSize(newsTextView, size);

                SettingManager.setTextSize(NewsFeedActivity.this, size);


            }
        });

        builder.show();
    }


    private void onOpenInBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getLink()));
        startActivity(browserIntent);
    }


    private void initializeNewsText() {
        newsTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                String selectedword = newsTextString.substring(newsTextView.getSelectionStart(), newsTextView.getSelectionEnd());
                Toast.makeText(NewsFeedActivity.this, "Word is " + selectedword, Toast.LENGTH_SHORT).show();

                newsTextView.setSelected(false);


                return false;
            }
        });

    }


    private void getWebsite(final String url) {


        String tag_string_req = "string_req";

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                initializeActivityData(response);
                //webView.loadDataWithBaseURL("", response, "text/html", "UTF-8", "");


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                loadCache(url);

            }
        });


        strReq.setShouldCache(true);
        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        strReq.setTag(TextUtils.isEmpty(tag_string_req) ? TAG : tag_string_req);
        Volley.newRequestQueue(getApplicationContext()).add(strReq);


    }


    private void loadCache(String url) {

        Cache cache = AppController.getInstance().getRequestQueue().getCache();

        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            //Cache data available.
            try {
                String response = new String(entry.data, "UTF-8");

                initializeActivityData(response);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // Cache data not exist.
        }

    }


    public void initializeActivityData(final String data) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {

                    Document doc = Jsoup.parse(data);
                    //Document doc = Jsoup.connect(url).get();
                    String title = doc.title();

                    doc.select("noscript").remove();
                    doc.select(".footer-nic").remove();

                    newsMinistry = doc.select(".MinistryNameSubhead").text();

                    if (news.getTitle()==null){
                        news.setTitle(doc.select("h2").text());

                    }

                    tableDataString = doc.select("table").toString();
                    doc.select("table").remove();

                    Elements links = doc.select("p");
                    links.select("style").remove();




                    builder.append(links.toString());
                    Log.d(TAG, "run: "+links.toString()+tableDataString);

                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("NEWS", "run: " + builder.toString());
                        if (Build.VERSION.SDK_INT >= 24) {
                            newsTextView.setText(Html.fromHtml(builder.toString(), Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            newsTextView.setText(Html.fromHtml(builder.toString()));
                        }

                        newsTextString = builder.toString();

                        newsMinistryTextView.setText(newsMinistry);
                        newsHeadingTextView.setText(news.getTitle());

                        webView.loadDataWithBaseURL("", tableDataString, "text/html", "UTF-8", "");                    }
                });
            }
        }).start();

        hideLoadingDialog();
    }


    public void setTextSize(TextView tv) {

        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingManager.getTextSize(this));
    }

    public void setTextSize(TextView tv, int size) {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
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
                Log.d(TAG, "onError: "+adError);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                View adView = NativeAdView.render(NewsFeedActivity.this, nativeAd, NativeAdView.Type.HEIGHT_400);
                LinearLayout nativeAdContainer = (LinearLayout) findViewById(R.id.newsFeed_adContainer_LinearLayout);
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
