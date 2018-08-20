package pib.affairs.current.app.pib;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.gms.measurement.AppMeasurementInstallReferrerReceiver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.Console;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dm.audiostreamer.AudioStreamingManager;
import dm.audiostreamer.CurrentSessionCallback;
import dm.audiostreamer.MediaMetaData;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.common.SafeToast;
import utils.AIRNews;
import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.LanguageManager;
import utils.News;
import utils.NewsParser;
import utils.NightModeManager;
import utils.SettingManager;

import static com.android.volley.VolleyLog.TAG;


public class AIRNewsActivity extends AppCompatActivity implements CurrentSessionCallback {
    ArrayList<AIRNews> airNewsArrayList;
    public static AudioStreamingManager streamingManager;

    private TabLayout tabLayout;
    private ViewPager viewPager;


    TextView newsTitleTextView, timeElapsedTextView, maxTimeTextView;
    SeekBar timeSeekbar;

    public MediaPlayer mediaPlayer;

    public MediaMetaData currentMetaData;
    ImageView playImageView;
    public static Toolbar toolbar;

    public static ProgressDialog pDialog;
    private AdView adView;


    ArrayList<String> audioLinkArrayList = new ArrayList<>();

    ArrayList<News> newsArrayList = new ArrayList<>();

    Elements elements;

    ArrayList<News> englishNewsArrayList = new ArrayList<>();
    ArrayList<News> hindiNewsArrayList = new ArrayList<>();
    ArrayList<News> urduNewsArrayList = new ArrayList<>();
    ArrayList<News> dailyNewsArrayList = new ArrayList<>();

    ArrayList<News> weeklyNewsArrayList = new ArrayList<>();


    AIRNewsActivity.ViewPagerAdapter adapter;
    private boolean isSeekBarTouched;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (NightModeManager.getNightMode(this)) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }

        setContentView(R.layout.activity_airnews);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        streamingManager = AudioStreamingManager.getInstance(this);
        streamingManager.setShowPlayerNotification(true);
        streamingManager.setPendingIntentAct(getNotificationPendingIntent());

        pDialog = new ProgressDialog(AIRNewsActivity.this);
        pDialog.setMessage("Loading...");

        viewPager = (ViewPager) findViewById(R.id.airActivity_viewpager);
        //setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        newsTitleTextView = (TextView) findViewById(R.id.airActivity_newsTitle_textView);
        timeElapsedTextView = (TextView) findViewById(R.id.airActivity_timeElapsed_textView);
        playImageView = (ImageView) findViewById(R.id.airActivity_play_imageView);
        maxTimeTextView = findViewById(R.id.airActivity_maxTime_textView);

        timeSeekbar = findViewById(R.id.airActivity_time_seekbar);

        try {


            if (streamingManager.isPlaying()) {
                newsTitleTextView.setText(streamingManager.getCurrentAudio().getMediaTitle());
                playImageView.setImageDrawable(getResources().getDrawable(R.drawable.mr_media_pause_dark));
            } else {
                playImageView.setImageDrawable(getResources().getDrawable(R.drawable.mr_media_play_dark));
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            Answers.getInstance().logCustom(new CustomEvent("AIR Radio opened"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        viewPager.setCurrentItem(1);

        //initializeAds();

        mediaPlayer = new MediaPlayer();

        adapter = new AIRNewsActivity.ViewPagerAdapter(getSupportFragmentManager());


        //getWebsite("http://www.newsonair.nic.in/Default.aspx");

        getWebsite("http://newsonair.nic.in/NSD_Audio_rss.aspx", 1);


        timeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (isSeekBarTouched) {
                    if (streamingManager.isPlaying()) {

                        //float x = 100;

                        //long seetTime = (long) (i / x) * Long.parseLong(currentMetaData.getMediaDuration());

                        streamingManager.onSeekTo(i);
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                isSeekBarTouched = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTouched = false;
            }
        });




    }

    private void getWeeklyRssData(String url) {


        String tag_string_req = "string_req";


        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                //initializeActivityData(response);

                //initializeRssData(response);


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                try {
                    Toast.makeText(AIRNewsActivity.this, "Something went wrong. Showing chached data", Toast.LENGTH_SHORT).show();
                    //loadCache(url);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_airnews_feed, menu);
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
            getWebsite("http://newsonair.nic.in/NSD_Audio_rss.aspx", 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void playNews() {
        MediaMetaData obj = new MediaMetaData();
        obj.setMediaId(airNewsArrayList.get(0).getNewsTitle());
        obj.setMediaUrl(airNewsArrayList.get(0).getNewsLink());
        obj.setMediaTitle(airNewsArrayList.get(0).getNewsTitle());
        obj.setMediaArt("http://www.newsonair.com/image/ALL-INDIA-RADIO-LOGO.jpg");


        streamingManager.onPlay(obj);


    }

    public void initializeSeekBarData(String url) {

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();


            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(final MediaPlayer mp) {


                    //start media player
                    //mp.start();

                    int mDuration = mediaPlayer.getDuration();

                    Toast.makeText(AIRNewsActivity.this, "Duration is - " + mDuration, Toast.LENGTH_SHORT).show();


                    newsTitleTextView.setText(getTimeString(mDuration));

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private PendingIntent getNotificationPendingIntent() {
        Intent intent = new Intent(AIRNewsActivity.this, AIRNewsActivity.class);
        intent.setAction("openplayer");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mPendingIntent = PendingIntent.getActivity(AIRNewsActivity.this, 0, intent, 0);
        return mPendingIntent;
    }


    private void fetchNews() {


// Tag used to cancel the request
        String tag_string_req = "news_request";

        final String url = "http://www.newsonair.nic.in/Eng.asp";


        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                response = response.substring(response.indexOf("<"));


                //airNewsArrayList = new NewsParser(response).parseAIRNews();


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                //loadCache(url);

            }
        }

        );


        strReq.setShouldCache(true);
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (streamingManager != null) {
            streamingManager.subscribesCallBack(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (streamingManager != null) {
            streamingManager.unSubscribeCallBack();
        }
    }

    @Override
    public void updatePlaybackState(int state) {

        switch (state) {
            case PlaybackStateCompat.STATE_PLAYING:

                break;
            case PlaybackStateCompat.STATE_PAUSED:


                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                timeElapsedTextView.setText(DateUtils.formatElapsedTime(0));
                break;
            case PlaybackStateCompat.STATE_BUFFERING:

                break;
        }

    }

    @Override
    public void playSongComplete() {


    }


    private void getWebsite(final String url, final int urlType) {

        // urltype =1 for rss feed and 2 for weekly feeds


        showLoadingDialog("Loading...");

        String tag_string_req = "string_req";


        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                //initializeActivityData(response);

                initializeRssData(response, urlType);


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                try {
                    Toast.makeText(AIRNewsActivity.this, "Something went wrong. Showing chached data", Toast.LENGTH_SHORT).show();
                    loadCache(url, urlType);
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


    private void loadCache(String url, int urlType) {

        Cache cache = AppController.getInstance().getRequestQueue().getCache();


        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            //Cache data available.
            try {
                String response = new String(entry.data, "UTF-8");

                initializeRssData(response, urlType);

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

                    elements = doc.select(".nwslist li audio");

                    Log.d(TAG, "run: " + elements);

                    for (int i = 0; i < elements.size(); i++) {
                        String link = elements.get(i).attr("src");
                        Log.d(TAG, "run: " + link);

                        audioLinkArrayList.add(link);
                    }

                    setNewsList();


                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //webView.loadDataWithBaseURL("", elements.html(), "text/html", "UTF-8", "");


                        setupViewPager(viewPager);

                        hideLoadingDialog();

                    }
                });


            }
        }).start();


        // hideLoadingDialog();
    }

    private void setNewsList() {

        //add news title and link manually to list
        try {
            News news = new News();

            news.setTitle("Morning News");
            news.setLink(audioLinkArrayList.get(0));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);


            news = new News();

            news.setTitle("Midday News");
            news.setLink(audioLinkArrayList.get(1));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);


            news = new News();

            news.setTitle("News at Nine");
            news.setLink(audioLinkArrayList.get(2));
            news.setPubDate("15 mins #recommended");


            newsArrayList.add(news);


            news = new News();

            news.setTitle("Hourly News");
            news.setLink(audioLinkArrayList.get(3));
            news.setPubDate("5 mins ");


            newsArrayList.add(news);


        /*English section end
        * Hindi section started*/

            news = new News();

            news.setTitle("Samachar Prabhat");
            news.setLink(audioLinkArrayList.get(4));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Dopehar Samachar");
            news.setLink(audioLinkArrayList.get(5));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Samachar Sandhya");
            news.setLink(audioLinkArrayList.get(6));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Prati Ghanta Samachar");
            news.setLink(audioLinkArrayList.get(7));
            news.setPubDate("5 mins ");

            newsArrayList.add(news);

        /*Hindi section over
        * URdu section started*/

            news = new News();

            news.setTitle("Khabarnama");
            news.setLink(audioLinkArrayList.get(8));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Khabarein (Day)");
            news.setLink(audioLinkArrayList.get(9));
            news.setPubDate("10 mins ");
            newsArrayList.add(news);


            news = new News();

            news.setTitle("Khabrein (Evening)");
            news.setLink(audioLinkArrayList.get(10));
            news.setPubDate("15 mins ");
            newsArrayList.add(news);

        /*urddu section over
        * FM Gold section started*/

            news = new News();

            news.setTitle("Ajj Savere");
            news.setLink(audioLinkArrayList.get(11));

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Parikrama");
            news.setLink(audioLinkArrayList.get(12));

            newsArrayList.add(news);

        /*fmgold  section over
        * Daily special section started*/

            news = new News();

            news.setTitle("Market Mantra");
            news.setLink(audioLinkArrayList.get(13));
            news.setPubDate("30 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Samayki");
            news.setLink(audioLinkArrayList.get(14));
            news.setPubDate("10 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Sports Scan");
            news.setLink(audioLinkArrayList.get(15));
            news.setPubDate("15 mins #recommended");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Spot Light News");
            news.setLink(audioLinkArrayList.get(16));
            news.setPubDate("15 mins #recommended");

            newsArrayList.add(news);


        /*Daily special   section over
        * Weekly special section started*/

            news = new News();

            news.setTitle("Country Wide");
            news.setLink(audioLinkArrayList.get(17));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Surkhiyo se pare");
            news.setLink(audioLinkArrayList.get(18));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Charcha ka Vishai");
            news.setLink(audioLinkArrayList.get(19));
            news.setPubDate("30 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Vaad Samvaad");
            news.setLink(audioLinkArrayList.get(20));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Money Talk");
            news.setLink(audioLinkArrayList.get(21));
            news.setPubDate("15 mins ");

            newsArrayList.add(news);

            news = new News();

            news.setTitle("Current Affairs");
            news.setLink(audioLinkArrayList.get(22));
            news.setPubDate("30 mins #recommended");

            newsArrayList.add(news);

        } catch (Exception e) {
            Answers.getInstance().logCustom(new CustomEvent("AIR News error").putCustomAttribute("exception", e.getMessage()));
        }


    }


    private void initializeRssData(final String response, final int urlType) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {

                    ArrayList<News> arrayList = new NewsParser(response).parseAIRNews();

                    if (urlType == 2) {
                        weeklyNewsArrayList = arrayList;
                    } else {

                        categoriesList(arrayList);
                    }

                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //webView.loadDataWithBaseURL("", elements.html(), "text/html", "UTF-8", "");


                        if (urlType == 2) {
                            // for weekly feed response
                            addWeeklyAirFragment();

                        } else {
                            setupViewPager(viewPager);
                        }

                        hideLoadingDialog();

                    }
                });


            }
        }).start();


    }

    private void addWeeklyAirFragment() {

        if (adapter != null) {
            adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Weekly.asp", 0, weeklyNewsArrayList), "Weekly");


            adapter.notifyDataSetChanged();
        }

    }

    private void categoriesList(ArrayList<News> arrayList) {

        englishNewsArrayList = new ArrayList<>();

        for (int i = 0; i < arrayList.size(); i++) {

            News airNews = arrayList.get(i);

            switch (airNews.getNewsAuthor()) {

                case "English":
                    englishNewsArrayList.add(airNews);
                    break;

                case "Hindi":
                    hindiNewsArrayList.add(airNews);
                    break;

                case "Urdu":
                    urduNewsArrayList.add(airNews);
                    break;

                case "Daily":
                    dailyNewsArrayList.add(airNews);
                    break;

            }


        }


    }


    @Override
    public void currentSeekBarPosition(int progress) {

        timeElapsedTextView.setText(DateUtils.formatElapsedTime(progress / 1000));


        //int seekProgress = (int) ((progress*100)/ Long.parseLong(currentMetaData.getMediaDuration()));


        timeSeekbar.setProgress(progress);




    }

    @Override
    public void playCurrent(int i, MediaMetaData mediaMetaData) {

        newsTitleTextView.setText(mediaMetaData.getMediaTitle());

        currentMetaData = mediaMetaData;
        playImageView.setImageDrawable(getResources().getDrawable(R.drawable.mr_media_pause_dark));

        //initializeSeekBarData(mediaMetaData.getMediaUrl());


        currentMetaData.setMediaDuration("1000");
        initializeMusic();


        try {
            Answers.getInstance().logCustom(new CustomEvent("AIR Radio played").putCustomAttribute("News title", mediaMetaData.getMediaTitle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initializeMusic() {

        String url = currentMetaData.getMediaUrl(); // your URL here
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    int length = mediaPlayer.getDuration();

                    Log.d(TAG, "initializeMusic: " + length);

                    currentMetaData.setMediaDuration(String.valueOf(length));

                    timeSeekbar.setMax(length);

                    maxTimeTextView.setText(DateUtils.formatElapsedTime(Long.parseLong(currentMetaData.getMediaDuration()) / 1000));


                }
            });




        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void playNext(int i, MediaMetaData mediaMetaData) {
        onNextClick(playImageView);


    }

    @Override
    public void playPrevious(int i, MediaMetaData mediaMetaData) {
        onPreviousClick(playImageView);
    }


    private void setupViewPager(ViewPager viewPager) {


        adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Hindi.asp", 1, new ArrayList<News>()), "AIR Offline");


        try {

            adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Eng.asp", 0, englishNewsArrayList), "AIR English");


            adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Daily.asp", 0, dailyNewsArrayList), "DAILY SPECIAL");


            adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Hindi.asp", 0, hindiNewsArrayList), "AIR Hindi");
            adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Hindi.asp", 0, urduNewsArrayList), "AIR Urdu");
            //adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Hindi.asp", 0, englishNewsArrayList), "FM Gold");


            viewPager.setAdapter(adapter);

            viewPager.setCurrentItem(1);


            getWebsite("http://newsonair.nic.in/weekly_program_rss.aspx", 2);


        } catch (Exception e) {
            e.printStackTrace();
            viewPager.setAdapter(adapter);
        }

    }

    public void onPreviousClick(View view) {
        try {
            streamingManager.onSeekTo(streamingManager.lastSeekPosition() - 180000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPlayClick(View view) {
        if (streamingManager.isPlaying()) {
            streamingManager.onPause();
            playImageView.setImageDrawable(getResources().getDrawable(R.drawable.mr_media_play_dark));
        } else {
            streamingManager.onPlay(currentMetaData);
            playImageView.setImageDrawable(getResources().getDrawable(R.drawable.mr_media_pause_dark));
        }

        Log.d(TAG, "onPlayClick: " + mediaPlayer.getDuration() + mediaPlayer.getCurrentPosition());

    }

    public void onNextClick(View view) {
        try {
            streamingManager.onSeekTo(streamingManager.lastSeekPosition() + 180000);
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
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.airActivity_adContainer);

        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        adView.loadAd();

        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                try {
                    Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "AIR NEWS").putCustomAttribute("error", adError.getErrorMessage()));
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

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = ((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        buf.append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
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

    public void onSlowPreviousClick(View view) {
        try {
            streamingManager.onSeekTo(streamingManager.lastSeekPosition() - 10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSlowNextClick(View view) {

        try {
            streamingManager.onSeekTo(streamingManager.lastSeekPosition() + 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
