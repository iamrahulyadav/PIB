package pib.affairs.current.app.pib;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.facebook.ads.NativeAd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.News;
import utils.NewsAdapter;
import utils.NightModeManager;
import utils.SettingManager;
import utils.SqlDatabaseHelper;

public class PibOldNewsActivity extends AppCompatActivity {

    ArrayList<Object> newsArrayList = new ArrayList<>();

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;

    ProgressDialog pDialog;

    TextView textView;

    String dateString;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (NightModeManager.getNightMode(this)) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }
        setContentView(R.layout.activity_pib_old_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pDialog = new ProgressDialog(this);

        recyclerView = (RecyclerView) findViewById(R.id.pibOld_recyclerView);
        textView = (TextView) findViewById(R.id.pibOld_date_textView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        newsAdapter = new NewsAdapter(newsArrayList, this);

        recyclerView.setAdapter(newsAdapter);

        newsAdapter.setClickListener(new NewsAdapter.ClickListener() {
            @Override
            public void onBookMarkClick(View view, int position) {

                Toast.makeText(PibOldNewsActivity.this, "Support for offline news will be available in next update ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTitleClick(View view, int position) {
                Intent intent = new Intent(PibOldNewsActivity.this, PibOldNewsFeedActivity.class);


                News news = (News) newsArrayList.get(position);

                intent.putExtra("news", news);


                startActivity(intent);


                news.setRead(true);
                newsAdapter.notifyDataSetChanged();

            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        Calendar cal = Calendar.getInstance();

        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);

        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
        dateString = df.format(cal.getTime());

        textView.setText(dateString);

        fetchNews(year, month, dayofmonth);

        try {
            Answers.getInstance().logCustom(new CustomEvent("old news search"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        } catch (Exception e) {
            e.printStackTrace();
        }

        //initializeAds();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void fetchNews(final int year, final int month, final int date) {

        showLoadingDialog("Loading..");

        String url = "http://pib.nic.in/newsite/erelease.aspx";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                //response = response.substring(response.indexOf("<"));

                initializeActivityData(response);


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();


            }
        }) {
            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();
                String postBody = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=k5Qlk7gSZKmSWbQbjjvOETq0wyqiIij6WToIYbrzXjsWmefHHptXLRqCzdeKqngZ8VivAAoeDtqXU%2Bh1vQnWz0K0p6M%3D&__VIEWSTATEGENERATOR=0A4F8E42&__VIEWSTATEENCRYPTED=&=&minname=0&rdate=18&rmonth=1&ryear=2018&__CALLBACKID=__Page&__CALLBACKPARAM=1%7C" + date + "%7C" + month + "%7C" + year + "%7C0";

                //params.put("__CALLBACKPARAM","1|"+date+"|"+month+"|"+year+"|0");
                return postBody.getBytes();
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, "StrRequest");


    }


    private void addNativeExpressAds() {

        boolean subscription = AdsSubscriptionManager.getSubscription(this);

        int count = AdsSubscriptionManager.ADSPOSITION_COUNT;
        for (int i = 4; i < (newsArrayList.size()); i += count) {
            if (newsArrayList.get(i) != null) {
                if (newsArrayList.get(i).getClass() != NativeAd.class) {


                    NativeAd nativeAd = new NativeAd(this, "1963281763960722_2012202609068637");
                    nativeAd.setAdListener(new com.facebook.ads.AdListener() {

                        @Override
                        public void onError(Ad ad, AdError error) {
                            // Ad error callback
                            try {
                                Answers.getInstance().logCustom(new CustomEvent("Ad failed to load")
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
                    if (!subscription) {
                        nativeAd.loadAd();
                    }
                    newsArrayList.add(i, nativeAd);

                }
            }
        }


    }


    public void onDateClick(View view) {

        Calendar cal = Calendar.getInstance();

        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);


        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar cal = Calendar.getInstance();
                cal.set(year, monthOfYear, dayOfMonth);

                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
                dateString = df.format(cal.getTimeInMillis());

                textView.setText(dateString);

                fetchNews(year, monthOfYear + 1, dayOfMonth);


            }

        }, year, month, day);

        datePickerDialog.show();

    }


    public void initializeActivityData(final String data) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {

                    Document doc = Jsoup.parse(data);


                    Elements links = doc.select("li");

                    String ministry = "";

                    newsArrayList.clear();
                    for (Element element : links) {

                        if (!element.attr("id").isEmpty()) {


                            News news = new News();

                            news.setTitle(element.text());
                            news.setLink("http://pib.nic.in/newsite/PrintRelease.aspx?relid=" + element.attr("id"));
                            news.setPubDate(ministry);
                            news.setNewsID(dateString);
                            newsArrayList.add(news);

                        } else {
                            ministry = element.text();
                        }

                    }


                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        addNativeExpressAds();

                        newsAdapter.notifyDataSetChanged();
                        hideLoadingDialog();


                    }
                });


            }
        }).start();


        // hideLoadingDialog();
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

        if (AdsSubscriptionManager.getSubscription(this)) {
            return;
        }

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
                    Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "old news banner").putCustomAttribute("error", adError.getErrorMessage()));
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
