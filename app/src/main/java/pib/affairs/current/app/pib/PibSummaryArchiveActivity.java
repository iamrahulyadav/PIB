package pib.affairs.current.app.pib;

import android.app.DatePickerDialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdView;
import com.facebook.ads.NativeAd;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import utils.AdsSubscriptionManager;
import utils.FireBaseHandler;
import utils.News;
import utils.NewsAdapter;


public class PibSummaryArchiveActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;


    SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private AdView adView;

    private ArrayList<Object> newsArrayList = new ArrayList<>();
    private boolean isLoading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pib_summary_archive);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        recyclerView = (RecyclerView) findViewById(R.id.pibArchive_recyclerView);
        swipeRefreshLayout = findViewById(R.id.pibArchive_refresh_layout);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        newsAdapter = new NewsAdapter(newsArrayList, this);

        recyclerView.setAdapter(newsAdapter);

        fetchPibSummary();


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {


                    if (!isLoading) {

                        isLoading = true;


                        fetchMorePibSummary();

                    }


                }
            }
        });


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
            Answers.getInstance().logCustom(new CustomEvent("Pib summary archieve opened"));

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
        if (position < 0) {
            return;
        }

        Intent intentSummary = new Intent(this, NewsDescriptionActivity.class);


        News newsSummary = (News) newsArrayList.get(position);

        intentSummary.putExtra("news", newsSummary);
        startActivity(intentSummary);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pib_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            fetchPibSummary();
            return true;
        } else if (id == R.id.action_search_date_icon) {
            onDateClick();
            return true;
        } else if (id == R.id.action_search_date_text) {
            onDateClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchPibSummary() {

        showLoading(true);

        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadPIBSummaryList(15, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {

                    PibSummaryArchiveActivity.this.newsArrayList.addAll(newsArrayList);

                    addNativeExpressAds(false);

                    isLoading = false;

                    showLoading(false);
                    newsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

            }
        });
    }

    private void showLoading(boolean b) {

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(b);
        }
    }

    private void fetchMorePibSummary() {

        showLoading(isLoading);

        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadPIBSummaryList(7, ((News) newsArrayList.get(newsArrayList.size() - 1)).getNewsID(), new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {
                    for (News news : newsArrayList) {

                        //news.setRead(sqlDatabaseHelper.getNewsReadStatus(news));
                        //news.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(news));


                        PibSummaryArchiveActivity.this.newsArrayList.add(news);


                    }
                    addNativeExpressAds(true);
                    newsAdapter.notifyDataSetChanged();

                    if (newsArrayList.size() > 1) {
                        isLoading = false;
                    } else {
                        isLoading = true;
                    }


                    showLoading(isLoading);

                }
            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

            }
        });


    }


    public void onDateClick() {

        Calendar c = Calendar.getInstance();


        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                String str_date = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    Date date = (Date) formatter.parse(str_date);

                    long sortDateMillis = date.getTime();

                    fetchPibSummaryByDate(sortDateMillis);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();

    }

    private void fetchPibSummaryByDate(long sortDateMillis) {

        showLoading(true);

        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadPIBSummary(sortDateMillis, (sortDateMillis + 172800000l), new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {

                    PibSummaryArchiveActivity.this.newsArrayList.clear();

                    PibSummaryArchiveActivity.this.newsArrayList.addAll(newsArrayList);

                    addNativeExpressAds(true);

                    isLoading = false;

                    showLoading(false);

                    newsAdapter.notifyDataSetChanged();
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


}
