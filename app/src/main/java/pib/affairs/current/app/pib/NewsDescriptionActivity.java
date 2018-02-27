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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.AppRater;
import utils.FireBaseHandler;
import utils.News;
import utils.NewsParser;
import utils.NightModeManager;
import utils.SettingManager;
import utils.SqlDatabaseHelper;
import utils.Translation;

import static com.android.volley.VolleyLog.TAG;

public class NewsDescriptionActivity extends AppCompatActivity {
    News news;
    boolean pushNotification;

    private NativeAd nativeAd;
    private String tableDataString;
    private TextView titleText;
    private TextView descriptionTextView;
    private TextView dateTextView;


    private BottomSheetBehavior mBottomSheetBehavior;
    public String selectedWord = "null";
    TextView translationTextView;
    private WebView webView;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (NightModeManager.getNightMode(this)) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }
        setContentView(R.layout.activity_news_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        news = (News) getIntent().getSerializableExtra("news");

        pushNotification = getIntent().getBooleanExtra("pushNotification", false);


        titleText = (TextView) findViewById(R.id.newsDescription_title_textView);
        descriptionTextView = (TextView) findViewById(R.id.newsDescription_description_textView);
        dateTextView = (TextView) findViewById(R.id.newsDescription_newsDate_textView);


        titleText.setText(news.getTitle());
        dateTextView.setText(news.getPubDate());


        if (pushNotification) {
            downloadSummaryById();

        } else {
            initializeUI();
        }


        if (news.getNewsType() != 1) {
            initializeActivityData(news.getDescription());
        }


        descriptionTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                getSelectedWord(1000);
                return false;
            }
        });

        try {
            Answers.getInstance().logCustom(new CustomEvent("Reader mode").putCustomAttribute("link", news.getLink()).putCustomAttribute("Title", news.getTitle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeBottomSheet();

        if (!AdsSubscriptionManager.getSubscription(this)) {
            initializeBottomNativeAds();
            initializeTopAdmob();
        }




    }

    private void downloadSummaryById() {

        showLoadingDialog("Loading...");
        new FireBaseHandler().downloadPIBSummaryById(news.getNewsID(), new FireBaseHandler.OnNewsListener() {
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


    private void initializeUI() {
        if (news.getNewsType() == 1) {
            news.setDescription(news.getDescription().replaceAll("\n", "<br>"));
        }

        if (Build.VERSION.SDK_INT >= 24) {
            descriptionTextView.setText(Html.fromHtml(news.getDescription(), Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH));
        } else {
            descriptionTextView.setText(Html.fromHtml(news.getDescription()));
        }
    }

    private void initializeBottomSheet() {

        View bottomSheet = findViewById(R.id.newsDescription_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setHideable(false);

        translationTextView = (TextView) findViewById(R.id.newsDescription_cardview_textview);

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()

        {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {


                    // loadWebview(selectedWord);

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        initializeWebView();

    }


    public void loadWebview(String mWord) {
        webView.loadUrl("http://www.dictionary.com/browse/" + mWord);
    }


    @Override
    public void onBackPressed() {

        if (mBottomSheetBehavior != null) {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else if (pushNotification) {
                super.onBackPressed();
                Intent intent = new Intent(NewsDescriptionActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
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
                try {
                    Answers.getInstance().logCustom(new CustomEvent("Fetch error").putCustomAttribute("Activity", "News feed activity").putCustomAttribute("reason", error.getMessage()));

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


        strReq.setShouldCache(true);
        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        strReq.setTag(TextUtils.isEmpty(tag_string_req) ? TAG : tag_string_req);
        Volley.newRequestQueue(getApplicationContext()).add(strReq);


    }

    public void initializeActivityData(final String data) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {

                    Document doc = Jsoup.parse(news.getDescription());
                    if (news.getTitle() == null) {
                        news.setTitle(doc.select("h2").text());

                    }

                    news.setPubDate(doc.select(".ReleaseDateSubHeaddateTime").text());

                    news.setDescription(doc.select("p").toString());


                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (Build.VERSION.SDK_INT >= 24) {
                            descriptionTextView.setText(Html.fromHtml(news.getDescription(), Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH));
                        } else {
                            descriptionTextView.setText(Html.fromHtml(news.getDescription()));
                        }

                        titleText.setText(news.getTitle());
                        dateTextView.setText(news.getPubDate());

                    }
                });


            }
        }).start();

        // hideLoadingDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_ddnews_feed, menu);
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
            onShareClick(titleText);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onPostInitiative() {
        News initiativeNews = new News();
        initiativeNews.setLink("link");
        initiativeNews.setTitle("title");
        initiativeNews.setPubDate("date");
        initiativeNews.setDescription("description");


        new FireBaseHandler().uploadInitiatives(initiativeNews, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {

            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

                Toast.makeText(NewsDescriptionActivity.this, "News posted " + isSuccessful, Toast.LENGTH_SHORT).show();
            }
        });
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


        try {
            Answers.getInstance().logCustom(new CustomEvent("Save offline").putCustomAttribute("offline article", news.getTitle()));
        } catch (Exception e) {
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

                            openShareDialog(news.getLink());

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

    private void openShareDialog(String shortUrl) {


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

    public void getSelectedWord(long timeDelay) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String string = descriptionTextView.getText().toString();
                if (descriptionTextView.hasSelection()) {
                    selectedWord = string.substring(descriptionTextView.getSelectionStart(), descriptionTextView.getSelectionEnd()).trim();
                }

                //Toast.makeText(NewsDescriptionActivity.this, "Selected - " + selectedWord, Toast.LENGTH_SHORT).show();

                loadWebview(selectedWord);

                translationTextView.setText(selectedWord);

                descriptionTextView.clearFocus();

                new Translation(selectedWord).fetchTranslation(new Translation.TranslateListener() {
                    @Override
                    public void onTranslation(Translation translation) {

                        if (translation.getWord().equalsIgnoreCase(selectedWord.trim())) {
                            translationTextView.setText(translation.getWord() + " = " + translation.wordTranslation);
                        }

                    }
                });


            }
        }, timeDelay);

    }


    public void initializeBottomNativeAds() {
        nativeAd = new NativeAd(this, "1963281763960722_1972656879689877");
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                try {
                    Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "reader mode").putCustomAttribute("error", adError.getErrorMessage()));
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


                View adView = NativeAdView.render(NewsDescriptionActivity.this, nativeAd, NativeAdView.Type.HEIGHT_400, viewAttributes);
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


    public void initializeWebView() {
        webView = (WebView) findViewById(R.id.newsDescription_bottomSheet_webview);

        webView.getSettings().setLoadsImagesAutomatically(false);

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

        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setAppCachePath(this.getCacheDir().getPath());
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);


    }


    public void onDictionaryClick(View view) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onGoogleClick(View view) {

        webView.loadUrl("https://www.google.co.in/search?q=" + selectedWord);
    }
}
