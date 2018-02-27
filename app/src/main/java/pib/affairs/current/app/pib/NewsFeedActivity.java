package pib.affairs.current.app.pib;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
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
import com.facebook.ads.NativeAdViewAttributes;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.AppRater;
import utils.FireBaseHandler;
import utils.News;
import utils.NewsParser;
import utils.NightModeManager;
import utils.SettingManager;
import utils.SqlDatabaseHelper;

import static com.android.volley.VolleyLog.TAG;

public class NewsFeedActivity extends AppCompatActivity implements
        TextToSpeech.OnInitListener {


    TextView newsTextView, newsHeadingTextView, newsDateTextView, newsMinistryTextView;

    News news;

    String newsMinistry, newsTextString;

    WebView webView;
    String tableDataString;

    ProgressDialog pDialog;

    private NativeAd nativeAd;
    private boolean pushNotification;

    SwipeRefreshLayout swipeRefreshLayout;

    private TextToSpeech tts;
    private int voiceReaderChunk;


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
        pushNotification = getIntent().getBooleanExtra("pushNotification", false);

        newsTextView = (TextView) findViewById(R.id.newsFeed_text_textView);
        newsHeadingTextView = (TextView) findViewById(R.id.newsFeed_newsHeading_textView);
        newsDateTextView = (TextView) findViewById(R.id.newsFeed_newsDate_textView);
        newsMinistryTextView = (TextView) findViewById(R.id.newsFeed_newsministry_textView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.newsFeed_refresh_swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if (news.getNewsID() != null) {
                    try {
                        if (news.getNewsID().equalsIgnoreCase("Initiatives")) {

                            webView.loadUrl(news.getLink());
                            swipeRefreshLayout.setRefreshing(false);
                            hideLoadingDialog();
                            initializeBottomNativeAds();

                        } else {
                            getWebsite(news.getLink());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        getWebsite(news.getLink());
                    }
                } else {
                    getWebsite(news.getLink());
                }


            }
        });

        webView = (WebView) findViewById(R.id.newsFeed_webView);

        newsHeadingTextView.setText(news.getTitle());

        newsDateTextView.setText(news.getPubDate());
        setTextSize(newsTextView);

        showLoadingDialog("Loading...");

        if (news.isBookMark()) {
            String htmlTextString = new SqlDatabaseHelper(this)
                    .getFullNews(news);

            if (news.getLink().length() > 40) {
                news.setNewsID(news.getLink().substring(29, 40));
            }
            if (news.getNewsID() != null) {
                try {
                    if (news.getNewsID().equalsIgnoreCase("Initiatives")) {

                        webView.loadDataWithBaseURL("", htmlTextString, "text/html", "UTF-8", "");

                        initializeBottomNativeAds();

                        hideLoadingDialog();

                    } else {
                        initializeActivityData(htmlTextString);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    initializeActivityData(htmlTextString);
                }
            } else {
                initializeActivityData(htmlTextString);
            }


        }


        if (news.getNewsID() != null) {
            try {
                if (news.getNewsID().equalsIgnoreCase("Initiatives")) {
                    if (!news.isBookMark()) {
                        webView.loadUrl(news.getLink());
                        hideLoadingDialog();
                        initializeBottomNativeAds();
                    }

                } else {
                    getWebsite(news.getLink());
                }

            } catch (Exception e) {
                e.printStackTrace();
                getWebsite(news.getLink());
            }
        } else {
            getWebsite(news.getLink());
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReaderModeClick();
            }
        });


        //initializeNewsText();


        try {
            AppRater.app_launched(this);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            Answers.getInstance().logContentView(new ContentViewEvent().putContentId(news.getLink()).putContentName(news.getTitle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        tts = new TextToSpeech(this, this);

        initializeWebView();



    }

    @Override
    public void onStop() {
        super.onStop();
        if (tts != null) {
            speakOutWord(".");
            tts.stop();
            tts.shutdown();
        }
    }

    private void speakOutWord(String speakWord) {

        try {

            tts.speak(speakWord, TextToSpeech.QUEUE_FLUSH, null);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeWebView() {

        if (NightModeManager.getNightMode(this)) {
            webView.setBackgroundColor(Color.parseColor("#5a666b"));
        }

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
        } else if (id == R.id.action_refresh) {
            recreate();
            return true;
        } else if (id == R.id.action_tts_reader) {
            onTtsReaderClick(item);
            return true;
        } else if (id == R.id.action_reader_mode) {
            onReaderModeClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onReaderModeClick() {

        if (tableDataString == null) {
            Toast.makeText(this, "Refresh and try again", Toast.LENGTH_SHORT).show();
            return;
        } else if (tableDataString.isEmpty()) {
            Toast.makeText(this, "Refresh and try again", Toast.LENGTH_SHORT).show();
            return;
        }

        News readerNews = new News();
        readerNews.setTitle(news.getTitle());
        readerNews.setDescription(tableDataString);
        readerNews.setPubDate(news.getPubDate());
        readerNews.setLink(news.getLink());

        Intent intent = new Intent(NewsFeedActivity.this, NewsDescriptionActivity.class);
        intent.putExtra("news", readerNews);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (pushNotification) {
            Intent intent = new Intent(NewsFeedActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            Locale locale = new Locale("en", "IN");
            int availability = tts.isLanguageAvailable(locale);
            int result = 0;
            switch (availability) {
                case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE: {
                    result = tts.setLanguage(locale);
                    break;
                }
                case TextToSpeech.LANG_NOT_SUPPORTED:
                case TextToSpeech.LANG_MISSING_DATA:
                case TextToSpeech.LANG_AVAILABLE: {
                    result = tts.setLanguage(Locale.US);
                    tts.setPitch(0.9f);
                    tts.setSpeechRate(0.9f);
                }
            }


            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                // btnSpeak.setEnabled(true);
                speakOutWord("");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }


    private void onPostClick() {
        new FireBaseHandler().uploadNews(news, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {

            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

                Toast.makeText(NewsFeedActivity.this, "News posted " + isSuccessful, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onTtsReaderClick(MenuItem item) {

        if (tts.isSpeaking()) {
            speakOutWord("");
            item.setTitle("Read Editorial (Voice)");

        } else {
            item.setTitle("Stop Reader");
            ttsReaderClick();
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

                getWebsite(news.getLink());


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

        loadCache(url);

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
                try {
                    Answers.getInstance().logCustom(new CustomEvent("Fetch error").putCustomAttribute("Activity", "News feed activity").putCustomAttribute("reason", error.getMessage()));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (tableDataString == null) {
                    Toast.makeText(NewsFeedActivity.this, "Unable to load data. Please try again later", Toast.LENGTH_SHORT).show();
                } else if (tableDataString.isEmpty()) {
                    Toast.makeText(NewsFeedActivity.this, "Unable to load data. Please try again later", Toast.LENGTH_SHORT).show();

                }

                hideLoadingDialog();

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

                    //doc.select("noscript").remove();
                    //doc.select(".footer-nic").remove();

                    //newsMinistry = doc.select(".MinistryNameSubhead").text();

                    if (news.getTitle() == null) {
                        news.setTitle(doc.select("h2").text());

                    }


                    tableDataString = doc.select(".content-area").toString();
                    //tableDataString = doc.select(".innner-page-main-about-us-content-right-part").toString();



/*

doc = Jsoup.parse(tableDataString);
                    doc.select(".ReleaseLang").remove();
                    doc.select(".BackgroundRelease").remove();
                    doc.select(".RelTag").remove();
                    doc.select(".RelLink").remove();

                    tableDataString = doc.toString();

                    Elements links = doc.select("p");
                    links.select("style").remove();


                    builder.append(links.toString());
                    Log.d(TAG, "run: " + links.toString() + tableDataString);*/

                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                       /* Log.d("NEWS", "run: " + builder.toString());
                        if (Build.VERSION.SDK_INT >= 24) {
                            newsTextView.setText(Html.fromHtml(builder.toString(), Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            newsTextView.setText(Html.fromHtml(builder.toString()));
                        }
                        newsTextString = builder.toString();
                        newsMinistryTextView.setText(newsMinistry);
                        newsHeadingTextView.setText(news.getTitle());
*/
                        tableDataString = "<html ><style>span{line-height: 140%;font-size:" + SettingManager.getTextSize(NewsFeedActivity.this) + "px}</style>" + tableDataString + "</html>";

                        webView.loadDataWithBaseURL("", tableDataString, "text/html", "UTF-8", "");

                        //webView.loadUrl(news.getLink());

                        hideLoadingDialog();
                        swipeRefreshLayout.setRefreshing(false);

                        initializeBottomNativeAds();

                    }
                });


            }
        }).start();


        // hideLoadingDialog();
    }


    public void ttsReaderClick() {

        if (tableDataString != null) {
            Document doc = Jsoup.parse(tableDataString);
            Elements textElement = doc.select("p");

            String ttsString = textElement.text();
            Log.d(TAG, "ttsReaderClick: " + ttsString);

            if (ttsString.length() < 3999) {
                speakOutWord(ttsString);
            } else {
                voiceReaderChunk = 0;
                voiceReaderChunkManager(ttsString);
            }
        }

    }

    private void voiceReaderChunkManager(final String ttsString) {

        if (ttsString.length() > (voiceReaderChunk)) {

            String chunk = ttsString.substring(voiceReaderChunk, Math.min(voiceReaderChunk + 3999, ttsString.length()));

            voiceReaderChunk = voiceReaderChunk + 3999;

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.d("TTS", "onDone: " + utteranceId);
                }

                @Override
                public void onDone(String utteranceId) {

                    Log.d("TTS", "onDone: " + utteranceId);
                    voiceReaderChunkManager(ttsString);

                }

                @Override
                public void onError(String utteranceId) {
                    Log.d("TTS", "onDone: " + utteranceId);
                }
            });

            try {
                if (Build.VERSION.SDK_INT > 21) {
                    tts.speak(chunk, TextToSpeech.QUEUE_FLUSH, null, "1");
                }
            } catch (Exception e) {

                e.printStackTrace();
            }
        }

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


    public void initializeBottomNativeAds(long timeDelay) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                initializeBottomNativeAds();
            }
        }, timeDelay);

    }

    public void initializeBottomNativeAds() {

        if (AdsSubscriptionManager.getSubscription(this)) {
            return;
        }

        if (nativeAd == null) {

            nativeAd = new NativeAd(this, "1963281763960722_1972656879689877");
            nativeAd.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.d(TAG, "onError: " + adError);

                    try {
                        Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "Newsfeed").putCustomAttribute("error", adError.getErrorMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    initializeAdmob();

                }

                @Override
                public void onAdLoaded(Ad ad) {

                    NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes()
                            .setBackgroundColor(Color.LTGRAY)
                            .setButtonBorderColor(getResources().getColor(R.color.colorPrimary))
                            .setButtonColor(getResources().getColor(R.color.colorPrimary))
                            .setButtonTextColor(Color.WHITE);


                    View adView = NativeAdView.render(NewsFeedActivity.this, nativeAd, NativeAdView.Type.HEIGHT_400, viewAttributes);
                    CardView nativeAdContainer = (CardView) findViewById(R.id.newsFeed_adContainer_LinearLayout);
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


    private void initializeAdmob(){

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId("ca-app-pub-8455191357100024/4291164035");

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        CardView nativeAdContainer =  findViewById(R.id.admobAdContainer_LinearLayout);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(adView);
    }


    public void OnShareButtonClick(View view) {
        onShareClick();
    }



    /*summary functions*/

    private void getSummaryWebsite(final String url) {


        String tag_string_req = "string_req";


        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                initializeSummaryData(response);


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                webView.loadDataWithBaseURL("", news.getDescription(), "text/html", "UTF-8", "");

                try {
                    Answers.getInstance().logCustom(new CustomEvent("Fetch error").putCustomAttribute("Activity", "News feed activity").putCustomAttribute("reason", error.getMessage()));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (tableDataString == null) {
                    Toast.makeText(NewsFeedActivity.this, "Unable to load data. Please try again later", Toast.LENGTH_SHORT).show();
                } else if (tableDataString.isEmpty()) {
                    Toast.makeText(NewsFeedActivity.this, "Unable to load data. Please try again later", Toast.LENGTH_SHORT).show();

                }

                hideLoadingDialog();

            }
        });


        strReq.setShouldCache(true);
        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        strReq.setTag(TextUtils.isEmpty(tag_string_req) ? TAG : tag_string_req);
        Volley.newRequestQueue(getApplicationContext()).add(strReq);


    }


    public void initializeSummaryData(final String data) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {


                    Document doc = Jsoup.parse(data);


                    if (news.getTitle() == null) {
                        news.setTitle(doc.select("h2").text());

                    }


                    //tableDataString = doc.select("#primary").toString();

                    tableDataString = doc.select(".entry-content article").toString();

                    Log.d(TAG, "run: " + tableDataString);

                    doc = Jsoup.parse(tableDataString);
                    doc.select(".breadcrumbs").remove();


                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        tableDataString = "<html ><style>span{line-height: 140%;font-size:" + SettingManager.getTextSize(NewsFeedActivity.this) + "px}</style>" + tableDataString + "</html>";

                        webView.loadDataWithBaseURL("", tableDataString, "text/html", "UTF-8", "");

                        //webView.loadUrl(news.getLink());

                        hideLoadingDialog();
                        swipeRefreshLayout.setRefreshing(false);

                        initializeBottomNativeAds();

                    }
                });


            }
        }).start();


        // hideLoadingDialog();
    }


}
