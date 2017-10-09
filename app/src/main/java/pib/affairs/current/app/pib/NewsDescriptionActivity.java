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
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import utils.AppController;
import utils.News;
import utils.NewsParser;
import utils.SqlDatabaseHelper;

public class NewsDescriptionActivity extends AppCompatActivity {
    News news;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        news = (News) getIntent().getSerializableExtra("news");
        boolean isOffline = getIntent().getBooleanExtra("isOffline", false);

        if (isOffline) {

        } else {

        }
         webView = (WebView) findViewById(R.id.newsDesription_webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMinimumFontSize(50);
        //webView.getSettings().setTextZoom(250);
        webView.getSettings().setDisplayZoomControls(true);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setUserAgentString("Android");

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setAppCachePath(this.getCacheDir().getPath());
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

         //webView.setBackgroundColor(Color.parseColor("#5a666b"));
        webView.setInitialScale(110);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);


        webView.loadUrl(news.getLink());

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

    }

    public void temp(){

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

                response= response.replaceFirst("width:60%","width:90%");


                webView.loadDataWithBaseURL("", response, "text/html", "UTF-8", "");


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

        return super.onOptionsItemSelected(item);
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
                + "\n\nFrom PIB app");
        startActivity(Intent.createChooser(sharingIntent, "share link via"));


    }

    public class JavaScriptInterface
    {
        @JavascriptInterface
        public void callback(String value)
        {
            Log.v("JS", "SELECTION:" + value);
        }
    }
}
