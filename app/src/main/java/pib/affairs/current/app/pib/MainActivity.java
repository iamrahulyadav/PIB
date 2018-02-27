package pib.affairs.current.app.pib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseState;
import com.anjlab.android.iab.v3.TransactionDetails;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.PurchaseEvent;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.messaging.FirebaseMessaging;


import io.fabric.sdk.android.Fabric;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.AppRater;
import utils.LanguageManager;
import utils.News;
import utils.NewsAdapter;
import utils.NewsParser;
import utils.NightModeManager;
import utils.RecyclerTouchListener;
import utils.SettingManager;
import utils.SqlDatabaseHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = "tag";
    private static final String PRODUCT_ID = "ads_free";
    public ArrayList<Object> newsArrayList = new ArrayList<>();

    RecyclerView recyclerView;
    NewsAdapter newsAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public static Toolbar toolbar;
    private BillingProcessor bp;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        if (NightModeManager.getNightMode(this)) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MobileAds.initialize(this, "ca-app-pub-8455191357100024~5774774045");




        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        recyclerView = (RecyclerView) findViewById(R.id.contentMain_recyclerView);

        openDynamicLink();


        FirebaseMessaging.getInstance().subscribeToTopic("subscribed");
        FirebaseMessaging.getInstance().subscribeToTopic("summaryNotification");
        FirebaseMessaging.getInstance().subscribeToTopic("OtherNewsNotification");

        //fetchNews();


        //initializeWebview();

        setLastUpdated();

        viewPager.setCurrentItem(1);


        initializeInAppBilling();

        try {
            if (AdsSubscriptionManager.getSubscription(this)) {
                View header = navigationView.getHeaderView(0);
                TextView statusTextView = (TextView) header.findViewById(R.id.nav_header_status);
                statusTextView.setText("Pro member (Ads free)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void initializeInAppBilling() {

        try {
            bp = new BillingProcessor(this,
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqtqEm+63jCmT5ite3xpOzQ4mdMlUsVhPsqB/K005vFpdf8g0kK/g0NYMk/e0Kujcfh+6I7fSsIcqWxieP40iEhYHdq7rQvfF8WR+Nh/6EOuoqob9nzxRUWaNfGhOWeCMOrNh4QgRh+W8bCxUcGAJoS4bBg59z4cm+j8zgqtnH7mfijmFJm7Gddy+ma6uQWgc3qvheW+MsdA+jIT30isVJsY5fYW2lYBeCyHbAbqxcoxARtDo4N3QtHxl/OmXBqY7CTc7ItynCUNShWlnw1tVCGQq6zEmt8XWQ2e4dfFdA5oPxC6uQ/baGORUgFAk/AuQVuW6Z98jBHSqu5uJhFBY3QIDAQAB",
                    new BillingProcessor.IBillingHandler() {
                        @Override
                        public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
                            //Toast.makeText(EditorialListWithNavActivity.this, "product purchased - " + productId, Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Thank you for Purchase");
                            builder.setMessage("We appreciate your contribution by going ads free.\n\nAds will be removed when you open the app next time. \n\n Contact us at acraftystudio@gmail.com in case of any problem");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();

                            AdsSubscriptionManager.setSubscription(MainActivity.this, true);

                            Answers.getInstance().logPurchase(new PurchaseEvent().putItemType("ads free").putSuccess(true));

                        }

                        @Override
                        public void onPurchaseHistoryRestored() {

                            Log.d(TAG, "onPurchaseHistoryRestored: ");
                        }

                        @Override
                        public void onBillingError(int errorCode, @Nullable Throwable error) {

                        }

                        @Override
                        public void onBillingInitialized() {
                            bp.loadOwnedPurchasesFromGoogle();

                            //bp.consumePurchase(PRODUCT_ID);

                            try {
                                TransactionDetails transactionDetails = bp.getPurchaseTransactionDetails(PRODUCT_ID);

                                if (transactionDetails == null) {
                                    AdsSubscriptionManager.setSubscription(MainActivity.this, false);
                                    return;
                                }

                                if (transactionDetails.purchaseInfo.purchaseData.purchaseState == PurchaseState.PurchasedSuccessfully) {
                                    AdsSubscriptionManager.setSubscription(MainActivity.this, true);
                                    Answers.getInstance().logCustom(new CustomEvent("Subscribed user enter"));
                                } else {
                                    AdsSubscriptionManager.setSubscription(MainActivity.this, false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    private void setupViewPager(ViewPager viewPager) {

        boolean isEnglish = LanguageManager.getLanguage(MainActivity.this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(RssFeedFragment.newInstance("http://pib.gov.in/newsite/rssenglish_fea.aspx", 2), "Offline");


        if (isEnglish) {
            adapter.addFragment(RssFeedFragment.newInstance("http://www.pib.gov.in/RssMain.aspx?ModId=6&Lang=1&Regid=3", 0), "PIB");
            //adapter.addFragment(RssFeedFragment.newInstance("http://www.pib.gov.in/RssMain.aspx?ModId=18&Lang=1&Regid=3", 0), "Featured");
        } else {
            adapter.addFragment(RssFeedFragment.newInstance("http://www.pib.gov.in/RssMain.aspx?ModId=6&Lang=2&Regid=3", 3), "PIB");
            //adapter.addFragment(RssFeedFragment.newInstance("http://www.pib.gov.in/RssMain.aspx?ModId=18&Lang=2&Regid=3", 3), "Featured");

            try {
                Answers.getInstance().logCustom(new CustomEvent("Hindi language"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        adapter.addFragment(RssFeedFragment.newInstance("http://pib.gov.in/newsite/rssenglish_fea.aspx", 5), "Summary");

        adapter.addFragment(DDNewsListFragment.newInstance(),"DD News");


        adapter.addFragment(RssFeedFragment.newInstance("http://pib.gov.in/newsite/rssenglish_fea.aspx", 4), "Key Initiative");

        //adapter.addFragment(RssFeedFragment.newInstance("http://pib.nic.in/RssMain.aspx?ModId=6", 3), "PIB Hindi");

        //adapter.addFragment(RssFeedFragment.newInstance("http://pib.gov.in/newsite/rssenglish_fea.aspx", 1), "Important");


        viewPager.setAdapter(adapter);
    }

    private void openDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d("DeepLink", "onSuccess: " + deepLink);

                            openNewsDescription(deepLink);

                            try {
                                Answers.getInstance().logCustom(new CustomEvent("User via dynamic link").putCustomAttribute("share link", deepLink.toString()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //fetchNews();

                            // downloadNewsArticle(newsArticleID);

                        } else {
                            Log.d("DeepLink", "onSuccess: ");

                            //download story list

                            //fetchNews();

                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //fetchNews();
                        Log.w("DeepLink", "getDynamicLink:onFailure", e);
                    }
                });
    }

    private void openNewsDescription(Uri deepLink) {

        News news = new News();
        news.setLink(deepLink.toString());

        Intent intent = new Intent(MainActivity.this, NewsFeedActivity.class);
        intent.putExtra("news", news);
        startActivity(intent);

    }

    private void fetchNews() {


// Tag used to cancel the request
        String tag_string_req = "string_req";

        final String url = "http://pib.gov.in/newsite/rssenglish.aspx";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                pDialog.hide();

                response = response.substring(response.indexOf("<"));

                SqlDatabaseHelper sqlDatabaseHelper = new SqlDatabaseHelper(MainActivity.this);

                for (News object : new NewsParser(response).parseJson()) {

                    object.setRead(sqlDatabaseHelper.getNewsReadStatus(object));

                    newsArrayList.add(object);
                }


                initializeActivity();


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                loadCache(url);

                pDialog.hide();
            }
        });

        strReq.setShouldCache(true);
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void loadCache(String url) {

        Cache cache = AppController.getInstance().getRequestQueue().getCache();

        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            //Cache data available.
            try {
                String response = new String(entry.data, "UTF-8");
                Log.d(TAG, "loadCache: " + response);

                response = response.substring(response.indexOf("<"));

                for (Object object : new NewsParser(response).parseJson()) {
                    newsArrayList.add(object);
                }

                initializeActivity();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // Cache data not exist.
        }

    }

    private void initializeActivity() {

        Log.d(TAG, "initializeActivity: " + newsArrayList);

        newsAdapter = new NewsAdapter(newsArrayList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(newsAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {


                Intent intent = new Intent(MainActivity.this, NewsDescriptionActivity.class);


                News news = (News) newsArrayList.get(position);

                intent.putExtra("news", news);
                startActivity(intent);

                SqlDatabaseHelper sqlDatabaseHelper = new SqlDatabaseHelper(MainActivity.this);
                sqlDatabaseHelper.addReadNews(news);

            }

            @Override
            public void onLongItemClick(View view, int position) {

               /* WebView webView = (WebView) findViewById(R.id.contentMain_cache_webView);
                webView.getSettings().setAppCacheEnabled(true);
                webView.getSettings().setAppCachePath(MainActivity.this.getCacheDir().getPath());
                webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

                webView.loadUrl(((News) newsArrayList.get(position)).getLink());
*/


            }
        }));


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.nav_airNews) {
            onAirNewsClick();
        } else if (id == R.id.nav_ddNews) {
            onDDNewsClick();
        } else if (id == R.id.nav_rstvNews) {
            onRstvNewsClick();
        } else if (id == R.id.nav_pibOld) {
            onOldNewsClick();
        }

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_language_english:
                onEnglishSelected();
                break;
            case R.id.nav_language_hindi:
                onHindiSelected();
                break;
            case R.id.nav_airNews:
                onAirNewsClick();
                break;
            case R.id.nav_theme_day:
                onDayThemeSelected();
                break;
            case R.id.nav_theme_night:
                onNightThemeSelected();
                break;
            case R.id.nav_share:
                onShareClick();
                break;
            case R.id.nav_rate_us:
                onRateUsClick();
                break;
            case R.id.nav_suggestion:
                onSuggestionClick();
                break;

            case R.id.nav_aptitude:
                onInstallAptitudeClick();
                break;

            case R.id.nav_logical_reasoning:
                onInstallLogicalClick();
                break;

            case R.id.nav_daily_editorial:
                onInstallEditorialClick();
                break;

            case R.id.nav_ddNews:
                onDDNewsClick();
                break;

            case R.id.nav_rstvNews:
                onRstvNewsClick();
                break;

            case R.id.nav_personality_development:
                onPersonalityDevelopmentClick();
                break;

            case R.id.nav_oldPib_news:
                onOldNewsClick();
                break;

            case R.id.nav_computer:
                onComputerClick();
                break;

            case R.id.nav_shortcuts:
                onShortcutsClick();
                break;

            case R.id.nav_ad_free:
                onPurchaseClick();
                break;


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onPurchaseClick() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Go Ads Free");
            builder.setMessage("We are not a money making app. Ads are integrated to help app development and maintenance of apps.\n\nPlease make a small contribution and go ads free @ Rs.29");
            builder.setPositiveButton("Go Ads Free", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (bp != null) {
                        bp.purchase(MainActivity.this, PRODUCT_ID);

                        Answers.getInstance().logCustom(new CustomEvent("Subscription Flow").putCustomAttribute("Selection", "yes"));

                    }
                }
            });
            builder.setNegativeButton("Maybe Later", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Answers.getInstance().logCustom(new CustomEvent("Subscription Flow").putCustomAttribute("Selection", "No"));

                }
            });

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //bp.consumePurchase(PRODUCT_ID);
    }

    private void onComputerClick() {
        try {
            String link = "https://play.google.com/store/apps/details?id=app.computer.basic.quiz.craftystudio.computerbasic";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            Answers.getInstance().logCustom(new CustomEvent("computer click"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onShortcutsClick() {
        try {
            String link = "https://play.google.com/store/apps/details?id=app.key.ashort.craftystudio.shortkeysapp";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            Answers.getInstance().logCustom(new CustomEvent("Keyboard Shortcuts click"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onOldNewsClick() {
        Intent intent = new Intent(this, PibOldNewsActivity.class);
        startActivity(intent);
    }

    private void onRstvNewsClick() {
        Intent intent = new Intent(MainActivity.this, RajyaSabhaListActivity.class);
        startActivity(intent);
    }

    private void onPersonalityDevelopmentClick() {
        try {
            String link = "https://play.google.com/store/apps/details?id=app.story.craftystudio.shortstory";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            Answers.getInstance().logCustom(new CustomEvent("personality app click"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
       Intent intent = new Intent(MainActivity.this, NewsDescriptionActivity.class);
       startActivity(intent);
       */

    }

    private void onDDNewsClick() {
        Intent intent = new Intent(MainActivity.this, DDNewsListActivity.class);
        startActivity(intent);
    }

    private void onNewsDescriptionClick() {
        Intent intent = new Intent(MainActivity.this, NewsDescriptionActivity.class);
        startActivity(intent);
    }

    private void onInstallEditorialClick() {
        try {
            String link = "https://play.google.com/store/apps/details?id=app.craftystudio.vocabulary.dailyeditorial";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            Answers.getInstance().logCustom(new CustomEvent("Editorial app click"));
        } catch (Exception e) {

        }
    }

    private void onInstallLogicalClick() {
        try {
            String link = "https://play.google.com/store/apps/details?id=app.reasoning.logical.quiz.craftystudio.logicalreasoning";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            Answers.getInstance().logCustom(new CustomEvent("Logical app click"));

        } catch (Exception e) {

        }
    }

    private void onAirNewsClick() {
        Intent intent = new Intent(MainActivity.this, AIRNewsActivity.class);
        startActivity(intent);
    }

    public void onInstallAptitudeClick() {
        try {
            String link = "https://play.google.com/store/apps/details?id=app.aptitude.quiz.craftystudio.aptitudequiz";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));


            Answers.getInstance().logCustom(new CustomEvent("Aptitude app click"));


        } catch (Exception e) {

        }
    }

    public void setLastUpdated() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm EEE dd MMM");
            String myDate = dateFormat.format(new Date(SettingManager.getLastUpdatedTime(MainActivity.this)));
            MainActivity.toolbar.setSubtitle("Last updated - " + myDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSuggestionClick() {
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"acraftystudio@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion for PIB News app");
        intent.putExtra(Intent.EXTRA_TEXT, "Your suggestion here \n");

        intent.setType("message/rfc822");

        startActivity(Intent.createChooser(intent, "Select Email via"));

    }

    private void onRateUsClick() {
        try {
            String link = "https://play.google.com/store/apps/details?id=" + this.getPackageName();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception e) {

        }
    }

    private void onShareClick() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String link = "https://play.google.com/store/apps/details?id=" + this.getPackageName();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download PIB News app");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, link);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));


        try {
            Answers.getInstance().logCustom(new CustomEvent("Shared").putCustomAttribute("from ", "main activity"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void onNightThemeSelected() {
        NightModeManager.setNightMode(MainActivity.this, true);
        recreate();
    }

    private void onDayThemeSelected() {
        NightModeManager.setNightMode(MainActivity.this, false);
        recreate();
    }

    private void onHindiSelected() {
        LanguageManager.setLanguage(MainActivity.this, false);

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


        finish();
    }

    private void onEnglishSelected() {
        LanguageManager.setLanguage(MainActivity.this, true);
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }



    public void onDDNewsClick(View view) {
        onDDNewsClick();
    }

    public void onGoAdsFreeClick(View view) {
        onPurchaseClick();
    }

    public void onPIBSummaryClick(View view) {
        viewPager.setCurrentItem(2,true);

    }

    public void onAIRNewsClick(View view) {
        onAirNewsClick();
    }

    public void onRateUsClick(View view) {
        onRateUsClick();
    }

    public void onRSTVNewsClick(View view) {
        onRstvNewsClick();
    }

    public void onSearchOldClick(View view) {
        onOldNewsClick();
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
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
