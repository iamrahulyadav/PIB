package pib.affairs.current.app.pib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.News;
import utils.NewsAdapter;
import utils.NewsParser;


public class DDNewsListFragment extends Fragment {
    private ArrayList<Object> newsArrayList = new ArrayList<>();


    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private String urlToOpen = "http://ddnews.gov.in/rss-feeds";

    SwipeRefreshLayout swipeRefreshLayout;


    private OnFragmentInteractionListener mListener;

    public DDNewsListFragment() {
        // Required empty public constructor
    }


    public static DDNewsListFragment newInstance() {
        DDNewsListFragment fragment = new DDNewsListFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        newsAdapter = new NewsAdapter(newsArrayList, getContext());
        fetchNews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ddnews_list, container, false);

        recyclerView = view.findViewById(R.id.ddnews_recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(newsAdapter);

        newsAdapter.setClickListener(new NewsAdapter.ClickListener() {
            @Override
            public void onBookMarkClick(View view, int position) {

            }

            @Override
            public void onTitleClick(View view, int position) {

                onItemClick(position);
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.ddnews_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNews();
            }
        });

        if (newsArrayList.isEmpty()){
            swipeRefreshLayout.setRefreshing(true);
        }

        return view;
    }



    private void fetchNews() {


// Tag used to cancel the request
        String tag_string_req = "dd_news_request";

        final String url = urlToOpen;

        loadCache(url);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                response = response.substring(response.indexOf("<"));


                newsArrayList.clear();
                for (News news : new NewsParser(response).parseDDNews()) {
                    news.setNewsType(1);
                    newsArrayList.add(news);
                }

                try {
                    addNativeExpressAds();

                    newsAdapter.notifyDataSetChanged();

                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    setLastUpdated();
                }catch (Exception e){
                    e.printStackTrace();
                }

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


    private void loadCache(String url) {

        Cache cache = AppController.getInstance().getRequestQueue().getCache();

        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            //Cache data available.
            try {
                String response = new String(entry.data, "UTF-8");


                newsArrayList.clear();
                for (News news : new NewsParser(response).parseDDNews()) {
                    news.setNewsType(1);
                    newsArrayList.add(news);
                }
                addNativeExpressAds();
                newsAdapter.notifyDataSetChanged();


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // Cache data not exist.
        }

    }

    private void setLastUpdated() {

    }

    private void addNativeExpressAds() {

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

    private void onItemClick(int position) {
        Intent intent = new Intent(getContext(), DDNewsFeedActivity.class);
        News news = (News) newsArrayList.get(position);

        intent.putExtra("news", news);

        startActivity(intent);

        news.setRead(true);
        newsAdapter.notifyDataSetChanged();

    }


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
