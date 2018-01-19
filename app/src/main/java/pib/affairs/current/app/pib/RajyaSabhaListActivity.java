package pib.affairs.current.app.pib;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import utils.AppController;
import utils.News;
import utils.NewsAdapter;
import utils.NewsParser;
import utils.NightModeManager;


public class RajyaSabhaListActivity extends AppCompatActivity {

    private ArrayList<Object> newsArrayList = new ArrayList<>();


    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private String urlToOpen = "http://rstv.nic.in/feed";

    ProgressDialog pDialog;

    SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (NightModeManager.getNightMode(this)) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }
        setContentView(R.layout.activity_rajya_sabha_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pDialog = new ProgressDialog(this);

        recyclerView = (RecyclerView) findViewById(R.id.rajyasabha_news_recyclerView);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        newsAdapter = new NewsAdapter(newsArrayList, this);

        recyclerView.setAdapter(newsAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.rajyasabha_news_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNews();
            }
        });

        newsAdapter.setClickListener(new NewsAdapter.ClickListener() {
            @Override
            public void onBookMarkClick(View view, int position) {

                Toast.makeText(RajyaSabhaListActivity.this, "Support for offline DD News will be available in next update", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onTitleClick(View view, int position) {

                onItemClick(position);
            }
        });

        showLoadingDialog("Loading");
        fetchNews();


        try {

            Answers.getInstance().logCustom(new CustomEvent("RSTV News list Opened"));

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeAds();



    }


    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void onItemClick(int position) {
        Intent intent = new Intent(this, RajyaSabhaFeedActivity.class);
        News news = (News) newsArrayList.get(position);

        intent.putExtra("news", news);

        startActivity(intent);

        news.setRead(true);
        newsAdapter.notifyDataSetChanged();

    }

    private void fetchNews() {


// Tag used to cancel the request
        String tag_string_req = "rstv_news_request";

        final String url = urlToOpen;

        loadCache(url);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                response = response.substring(response.indexOf("<"));


                newsArrayList.clear();
                for (Object news : new NewsParser(response).parseRstvNews()) {
                    newsArrayList.add(news);
                }

                newsAdapter.notifyDataSetChanged();


                hideLoadingDialog();
                setLastUpdated();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();


                try {
                    Answers.getInstance().logCustom(new CustomEvent("Fetch error").putCustomAttribute("Activity", "AIR Rss activity").putCustomAttribute("reason", error.getMessage()));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        );


        strReq.setShouldCache(true);
        // Adding request to request queue
        AppController.getInstance().getRequestQueue().getCache().remove(urlToOpen);
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void setLastUpdated() {
        try {


            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm EEE dd MMM");

            String myDate = dateFormat.format(new Date(System.currentTimeMillis()));
            toolbar.setSubtitle("Last updated - " + myDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        swipeRefreshLayout.setRefreshing(false);
    }


    private void loadCache(String url) {

        Cache cache = AppController.getInstance().getRequestQueue().getCache();

        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            //Cache data available.
            try {
                String response = new String(entry.data, "UTF-8");


                newsArrayList.clear();
                for (Object news : new NewsParser(response).parseDDNews()) {
                    newsArrayList.add(news);
                }

                newsAdapter.notifyDataSetChanged();

                hideLoadingDialog();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // Cache data not exist.
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

    public void initializeAds() {
        // Instantiate an AdView view
        adView = new AdView(this, "1963281763960722_2001913650097533", AdSize.BANNER_HEIGHT_50);

        // Find the Ad Container
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);

        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        adView.loadAd();

        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                try {
                    Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "RSTV banner").putCustomAttribute("error", adError.getErrorMessage()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        });

    }

}
