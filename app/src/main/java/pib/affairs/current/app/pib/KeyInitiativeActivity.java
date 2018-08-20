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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdView;
import com.facebook.ads.NativeAd;

import java.util.ArrayList;

import utils.AdsSubscriptionManager;
import utils.FireBaseHandler;
import utils.News;
import utils.NewsAdapter;
import utils.SqlDatabaseHelper;


public class KeyInitiativeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;


    SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private AdView adView;

    private ArrayList<Object> newsArrayList = new ArrayList<>();
    private boolean isLoading = false;


    SqlDatabaseHelper sqlDatabaseHelper;

    public ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_initiative);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        sqlDatabaseHelper = new SqlDatabaseHelper(this);


        pDialog = new ProgressDialog(KeyInitiativeActivity.this);
        pDialog.setMessage("Loading...");

        recyclerView = (RecyclerView) findViewById(R.id.keyInitiative_recyclerView);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        newsAdapter = new NewsAdapter(newsArrayList, this);

        recyclerView.setAdapter(newsAdapter);

        fetchKeyInitiative();


        newsAdapter.setClickListener(new NewsAdapter.ClickListener() {
            @Override
            public void onBookMarkClick(View view, int position) {

            }

            @Override
            public void onTitleClick(View view, int position) {

                onItemClick(position);

            }
        });


        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Answers.getInstance().logCustom(new CustomEvent("Key Initiative opened"));

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void onItemClick(int position) {

        Intent intent = new Intent(this, NewsFeedActivity.class);


        News news = (News) newsArrayList.get(position);

        intent.putExtra("news", news);


        startActivity(intent);

        sqlDatabaseHelper = new SqlDatabaseHelper(this);
        sqlDatabaseHelper.addReadNews(news);

        news.setRead(true);
        newsAdapter.notifyDataSetChanged();


    }

    private void fetchKeyInitiative() {

        showLoadingDialog("Loading...");

        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadInitiativesList(30, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {
                    for (News news : newsArrayList) {

                        news.setRead(sqlDatabaseHelper.getNewsReadStatus(news));
                        news.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(news));


                        KeyInitiativeActivity.this.newsArrayList.add(news);


                    }

                    addNativeExpressAds(false);
                    newsAdapter.notifyDataSetChanged();

                    hideLoadingDialog();

                }
            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

            }
        });

    }

    private void addNativeExpressAds(boolean isCached) {

        if (newsArrayList == null) {
            return;
        }


        boolean subscription = AdsSubscriptionManager.getSubscription(this);

        int count = AdsSubscriptionManager.ADSPOSITION_COUNT;
        for (int i = 2; i < (newsArrayList.size()); i += count) {
            if (newsArrayList.get(i) != null) {
                if (newsArrayList.get(i).getClass() != NativeAd.class) {


                    NativeAd nativeAd = new NativeAd(this, "1963281763960722_2012202609068637");
                    nativeAd.setAdListener(new com.facebook.ads.AdListener() {

                        @Override
                        public void onError(Ad ad, AdError error) {
                            // Ad error callback
                            try {
                                Answers.getInstance().logCustom(new CustomEvent("Ad failed")
                                        .putCustomAttribute("Placement", "List native").putCustomAttribute("errorType", error.getErrorMessage()).putCustomAttribute("Source", "Facebook"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            // Ad loaded callback
                            newsAdapter.notifyDataSetChanged();
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
                    if (!(subscription || isCached)) {
                        nativeAd.loadAd();
                    }
                    newsArrayList.add(i, nativeAd);

                }
            }
        }


    }


    public void showLoadingDialog(String message) {
        try {
            pDialog.setMessage(message);
            pDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideLoadingDialog() {
        try {
            pDialog.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
