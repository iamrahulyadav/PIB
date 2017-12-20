package pib.affairs.current.app.pib;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
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
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.measurement.AppMeasurementInstallReferrerReceiver;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dm.audiostreamer.AudioStreamingManager;
import dm.audiostreamer.CurrentSessionCallback;
import dm.audiostreamer.MediaMetaData;
import io.fabric.sdk.android.services.common.SafeToast;
import utils.AIRNews;
import utils.AppController;
import utils.LanguageManager;
import utils.News;
import utils.NewsParser;
import utils.NightModeManager;


public class AIRNewsActivity extends AppCompatActivity implements CurrentSessionCallback {
    ArrayList<AIRNews> airNewsArrayList;
    public static AudioStreamingManager streamingManager;

    private TabLayout tabLayout;
    private ViewPager viewPager;


    TextView newsTitleTextView, timeElapsedTextView;

    public static MediaPlayer mediaPlayer;

    public MediaMetaData currentMetaData;
    ImageView playImageView;
    public static Toolbar toolbar;

    public static ProgressDialog pDialog;

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
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        newsTitleTextView = (TextView) findViewById(R.id.airActivity_newsTitle_textView);
        timeElapsedTextView = (TextView) findViewById(R.id.airActivity_timeElapsed_textView);
        playImageView = (ImageView) findViewById(R.id.airActivity_play_imageView);


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
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void playNews() {
        MediaMetaData obj = new MediaMetaData();
        obj.setMediaId(airNewsArrayList.get(0).getNewsTitle());
        obj.setMediaUrl(airNewsArrayList.get(0).getNewsLink());
        obj.setMediaTitle(airNewsArrayList.get(0).getNewsTitle());
        obj.setMediaArt("http://www.newsonair.com/image/ALL-INDIA-RADIO-LOGO.jpg");


        streamingManager.onPlay(obj);


    }

    public static void playFromURL(String url) {

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        try {

            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();

            int i = mediaPlayer.getDuration();


            Log.d("MP", "playFromURL: " + i);

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // mediaPlayer.start();


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


                airNewsArrayList = new NewsParser(response).parseAIRNews();


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

    @Override
    public void currentSeekBarPosition(int progress) {

        timeElapsedTextView.setText(DateUtils.formatElapsedTime(progress / 1000));


    }

    @Override
    public void playCurrent(int i, MediaMetaData mediaMetaData) {

        newsTitleTextView.setText(mediaMetaData.getMediaTitle());

        currentMetaData = mediaMetaData;
        playImageView.setImageDrawable(getResources().getDrawable(R.drawable.mr_media_pause_dark));


        try {
            Answers.getInstance().logCustom(new CustomEvent("AIR Radio opened").putCustomAttribute("News title", mediaMetaData.getMediaTitle()));
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


        AIRNewsActivity.ViewPagerAdapter adapter = new AIRNewsActivity.ViewPagerAdapter(getSupportFragmentManager());


        adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Eng.asp", 0), "AIR English");
        adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Daily.asp", 0), "DAILY SPECIAL");

        adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Weekly.asp", 0), "Weekly");

        adapter.addFragment(AIRRssFragment.newInstance("http://www.newsonair.nic.in/Hindi.asp", 0), "AIR Hindi");


        viewPager.setAdapter(adapter);
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
    }

    public void onNextClick(View view) {
        try {
            streamingManager.onSeekTo(streamingManager.lastSeekPosition() + 180000);
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
