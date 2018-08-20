package pib.affairs.current.app.pib;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
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
import com.facebook.ads.NativeAd;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.FireBaseHandler;
import utils.News;
import utils.NewsAdapter;
import utils.NewsParser;
import utils.SettingManager;
import utils.SqlDatabaseHelper;

import static com.android.volley.VolleyLog.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RssFeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RssFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RssFeedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static boolean newNoteSaved = false;

    private final int SOURCETYPE_OFFLINE = 2;
    private final int SOURCETYPE_FIREBASE = 1;
    private final int SOURCETYPE_HINDI = 3;
    private final int SOURCETYPE_INITIATIVES = 4;
    private final int SOURCETYPE_SUMMARY = 5;


    // TODO: Rename and change types of parameters
    private String urlToOpen;
    private int sourceType;

    private OnFragmentInteractionListener mListener;
    private ArrayList<Object> newsArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    SqlDatabaseHelper sqlDatabaseHelper;

    SwipeRefreshLayout swipeRefreshLayout;

    public ProgressDialog pDialog;

    public static int newsCount;

    private boolean isLoading = false;


    public RssFeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RssFeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RssFeedFragment newInstance(String urlToOpen, int sourceType) {
        RssFeedFragment fragment = new RssFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, urlToOpen);
        args.putInt(ARG_PARAM2, sourceType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            urlToOpen = getArguments().getString(ARG_PARAM1);
            sourceType = getArguments().getInt(ARG_PARAM2, 0);
        }
        sqlDatabaseHelper = new SqlDatabaseHelper(getContext());
        newsAdapter = new NewsAdapter(newsArrayList, getContext());


        if (pDialog == null) {
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Loading...");
        }


        if (sourceType == SOURCETYPE_FIREBASE) {
            fetchNewsFromFireBase();
        } else if (sourceType == SOURCETYPE_OFFLINE) {
            fetchNewsFromDatabse();
        } else if (sourceType == SOURCETYPE_INITIATIVES) {
            fetchInitiative();
        } else if (sourceType == SOURCETYPE_SUMMARY) {
            fetchPibSummary();
            isLoading = true;
        } else {
            fetchNews();
        }

    }

    private void fetchPibSummary() {
        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadPIBSummaryList(7, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {

                    RssFeedFragment.this.newsArrayList.addAll(newsArrayList);

                    addNativeExpressAds(false);
                    initializeFragment();
                    isLoading = false;

                    if (MainActivity.newsArrayList.size() < 8) {
                        MainActivity.newsArrayList.add(0, newsArrayList.get(0));
                    }
                }
            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

            }
        });
    }

    private void fetchInitiative() {

        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadInitiativesList(30, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {
                    for (News news : newsArrayList) {

                        news.setRead(sqlDatabaseHelper.getNewsReadStatus(news));
                        news.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(news));


                        RssFeedFragment.this.newsArrayList.add(news);


                    }

                    addNativeExpressAds(false);
                    initializeFragment();


                }
            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

            }
        });

    }

    private void fetchNewsFromDatabse() {

        for (News news : sqlDatabaseHelper.getAllSavedNotes()) {
            news.setRead(sqlDatabaseHelper.getNewsReadStatus(news));
            news.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(news));

            news.rectifyNewsLink();
            newsArrayList.add(news);
        }

        Collections.reverse(newsArrayList);

        newNoteSaved = false;
        addNativeExpressAds(true);
        initializeFragment();

    }

    public void updateNewsStatus() {

    }

    private void fetchNewsFromFireBase() {

        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadNewsList(20, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {
                    for (News news : newsArrayList) {
                        news.setRead(sqlDatabaseHelper.getNewsReadStatus(news));
                        news.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(news));

                        news.rectifyNewsLink();
                        RssFeedFragment.this.newsArrayList.add(news);


                    }

                    addNativeExpressAds(true);
                    initializeFragment();

                }
            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

            }
        });

    }

    public void checkShowSurvey() {
        if (newsCount == 3 || newsCount == 5) {

        }
    }

    private void fetchNews() {


// Tag used to cancel the request
        String tag_string_req = "news_request";

        final String url = urlToOpen;


        pDialog.show();
        loadCache(url);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                pDialog.hide();

                response = response.substring(response.indexOf("<"));
                ArrayList<News> arrayList = new NewsParser(response).parseJson();

                if (arrayList.size() > 0) {
                    newsArrayList.clear();

                    for (News object : arrayList) {

                        object.setRead(sqlDatabaseHelper.getNewsReadStatus(object));
                        object.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(object));
                        object.rectifyNewsLink();

                        newsArrayList.add(object);
                    }
                }

                addNativeExpressAds(false);

                initializeFragment();

                setLastUpdated();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                //Toast.makeText(getContext(), "Unable to load data. Please try again later", Toast.LENGTH_SHORT).show();

                initializeFragment();

                try {
                    pDialog.hide();
                    Answers.getInstance().logCustom(new CustomEvent("Fetch error").putCustomAttribute("Activity", "Main activity").putCustomAttribute("reason", error.getMessage()));

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

        );


    /*    {
            public Map<String, String> getHeaders() {
            Map<String, String> params = new HashMap<String, String>();
            if (sourceType == SOURCETYPE_HINDI) {
                params.put("Cookie", "ext_name=jaehkpjddfdgiiefcnhahapilbejohhj; style=null; _ga=GA1.3.1895171585.1513447950; PIB_Accessibility=Lang=2&Region=3; __atuvc=7%7C50%2C2%7C51; _gid=GA1.3.220297058.1513617359; ASP.NET_SessionId=5yfd1d1zsbg20xe2d2yg52rj; _gat_gtag_UA_110683570_1=1");

            } else {
                params.put("Cookie", "ext_name=jaehkpjddfdgiiefcnhahapilbejohhj; _ga=GA1.3.2027251216.1513617291; style=null; PIB_Accessibility=Lang=1&Region=3; __atuvc=12%7C51; ASP.NET_SessionId=lapxd40g1yzxjlpktzotxf3a; _gid=GA1.3.1984463729.1514055749; _gat_gtag_UA_110683570_1=1");

            }
            params.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
            //params.put("DNT", "1");
            //params.put("Upgrade-Insecure-Requests", "1");
            //params.put("Host", "pib.nic.in");
            params.put("Connection", "keep-alive");
            //params.put("Accept-Encoding", "gzip, deflate");
            //params.put("Accept-Language", "en-GB,en;q=0.9");
            //params.put("Cache-Control", "max-age=0");
            params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng;q=0.8");

            return params;
        }
        };*/


       /* if (sourceType == 3) {
            strReq.setShouldCache(false);
            AppController.getInstance().getRequestQueue().getCache().remove(url);
        } else {
            strReq.setShouldCache(true);
        }*/

        // Adding request to request queue
        strReq.setShouldCache(true);
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

                newsArrayList.clear();
                for (News object : new NewsParser(response).parseCacheJson()) {
                    object.setRead(sqlDatabaseHelper.getNewsReadStatus(object));
                    object.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(object));
                    object.rectifyNewsLink();
                    newsArrayList.add(object);
                }

                addNativeExpressAds(true);
                newsAdapter.notifyDataSetChanged();
                pDialog.hide();
                //initializeFragment();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // Cache data not exist.
        }

    }

    private void initializeFragment() {

        newsAdapter.notifyDataSetChanged();


        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_rss_feed, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_rss_list_recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());





        recyclerView.setAdapter(newsAdapter);

        newsAdapter.setClickListener(new NewsAdapter.ClickListener() {
            @Override
            public void onBookMarkClick(View view, int position) {


                if (sourceType == SOURCETYPE_SUMMARY) {

                    onPibSummaryBookMark();
                } else {
                    onBookMark(position);

                }

            }

            @Override
            public void onTitleClick(View view, int position) {

                onItemClick(position);
            }
        });


        initializeSwipeRefresh(view);


        if (newNoteSaved && sourceType == 2) {
            newsArrayList.clear();
            fetchNewsFromDatabse();
        }

        initializeFragment();

        if (sourceType == SOURCETYPE_INITIATIVES) {
            if (newsArrayList.size() < 1 && swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }


        if (sourceType == 0) {
            HorizontalScrollView horizontalScrollView = view.findViewById(R.id.fragmentRss_option_horizontalView);
            horizontalScrollView.setVisibility(View.VISIBLE);
        }


/*
       recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {

                    if (sourceType == SOURCETYPE_SUMMARY) {

                        if (!isLoading) {

                            isLoading = true;


                            fetchMorePibSummary();

                        }
                    }

                }
            }
        });


*/






        return view;
    }

    private void fetchMorePibSummary() {

        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadPIBSummaryList(12, ((News) newsArrayList.get(newsArrayList.size() - 1)).getNewsID(), new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {
                    for (News news : newsArrayList) {

                        //news.setRead(sqlDatabaseHelper.getNewsReadStatus(news));
                        //news.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(news));


                        RssFeedFragment.this.newsArrayList.add(news);


                    }
                    addNativeExpressAds(true);
                    initializeFragment();
                    isLoading = false;


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

        if (getContext() == null) {
            return;
        }

        boolean subscription = AdsSubscriptionManager.getSubscription(getContext());

        int count = AdsSubscriptionManager.ADSPOSITION_COUNT;
        for (int i = 2; i < (newsArrayList.size()); i += count) {
            if (newsArrayList.get(i) != null) {
                if (newsArrayList.get(i).getClass() != NativeAd.class) {


                    NativeAd nativeAd = new NativeAd(getContext(), "1963281763960722_2012202609068637");
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


    public void onItemClick(int position) {

        if(position<0){
            return;
        }

        Intent intent = new Intent(getContext(), NewsFeedActivity.class);


        News news = (News) newsArrayList.get(position);

        intent.putExtra("news", news);

        if (sourceType == SOURCETYPE_OFFLINE) {
            intent.putExtra("isOffline", true);
        }

        if (news.getNewsType() == 1) {
            /*for news summary*/
            Intent intentSummary = new Intent(getContext(), NewsDescriptionActivity.class);


            News newsSummary = (News) newsArrayList.get(position);

            intentSummary.putExtra("news", newsSummary);
            startActivity(intentSummary);

            return;
        }

        startActivity(intent);

        sqlDatabaseHelper = new SqlDatabaseHelper(getContext());
        sqlDatabaseHelper.addReadNews(news);

        news.setRead(true);
        newsAdapter.notifyDataSetChanged();

        newsCount++;
        checkShowSurvey();


    }

    public void onBookMark(int position) {

        if(position<0){
            return;
        }

        String tag_string_req = "string_req";

        final News news = (News) newsArrayList.get(position);

        final String url = news.getLink();


        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                pDialog.hide();

                SqlDatabaseHelper sqlDatabaseHelper = new SqlDatabaseHelper(getContext());


                sqlDatabaseHelper.addSavedNews(news, response);


                RssFeedFragment.newNoteSaved = true;
                for (Object object : newsArrayList) {
                    if (object.getClass() == News.class) {
                        News newsObject = (News) object;
                        newsObject.setRead(sqlDatabaseHelper.getNewsReadStatus(newsObject));
                        newsObject.setBookMark(sqlDatabaseHelper.getNewsBookMarkStatus(newsObject));
                    }
                }

                newsAdapter.notifyDataSetChanged();


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

    private void onPibSummaryBookMark() {


    }

    private void initializeSwipeRefresh(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_rss_swipeRefresh);

        if (newsArrayList.size() < 1) {
            swipeRefreshLayout.setRefreshing(true);

        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                newsArrayList.clear();
                newsAdapter.notifyDataSetChanged();

                if (sourceType == SOURCETYPE_FIREBASE) {
                    fetchNewsFromFireBase();
                } else if (sourceType == SOURCETYPE_OFFLINE) {
                    fetchNewsFromDatabse();
                } else if (sourceType == 4) {
                    fetchInitiative();
                } else if (sourceType == SOURCETYPE_SUMMARY) {
                    fetchPibSummary();
                } else {
                    fetchNews();
                }


            }
        });
    }

    public void setLastUpdated() {
        try {

            SettingManager.setLastUpdatedTime(getContext(), System.currentTimeMillis());

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm EEE dd MMM");

            String myDate = dateFormat.format(new Date(System.currentTimeMillis()));
            MainActivity.toolbar.setSubtitle("Last Updated - " + myDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
