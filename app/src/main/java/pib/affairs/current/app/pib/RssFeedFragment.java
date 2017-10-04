package pib.affairs.current.app.pib;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import utils.AppController;
import utils.FireBaseHandler;
import utils.News;
import utils.NewsAdapter;
import utils.NewsParser;
import utils.RecyclerTouchListener;
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

    private final int SOURCETYPE_OFFLINE =2;
    private final int SOURCETYPE_FIREBASE =1;

    // TODO: Rename and change types of parameters
    private String urlToOpen;
    private int sourceType;

    private OnFragmentInteractionListener mListener;
    private ArrayList<Object> newsArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    SqlDatabaseHelper sqlDatabaseHelper;


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

        if (sourceType == SOURCETYPE_FIREBASE) {
            fetchNewsFromFireBase();
        } else if (sourceType == SOURCETYPE_OFFLINE) {
            fetchNewsFromDatabse();
        } else {
            fetchNews();
        }

    }

    private void fetchNewsFromDatabse() {

        for (News news : sqlDatabaseHelper.getAllSavedNotes()) {
            news.setRead(sqlDatabaseHelper.getNewsReadStatus(news.getLink()));
            newsArrayList.add(news);
        }

        newNoteSaved = false;
        initializeFragment();

    }

    private void fetchNewsFromFireBase() {

        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.downloadNewsList(10, new FireBaseHandler.OnNewsListener() {
            @Override
            public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful) {
                if (isSuccessful) {
                    for (News news : newsArrayList) {
                        news.setRead(sqlDatabaseHelper.getNewsReadStatus(news.getLink()));

                        RssFeedFragment.this.newsArrayList.add(news);
                    }
                }
            }

            @Override
            public void onNewsUpload(boolean isSuccessful) {

            }
        });

    }

    private void fetchNews() {


// Tag used to cancel the request
        String tag_string_req = "string_req";

        final String url = urlToOpen;

        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                pDialog.hide();

                response = response.substring(response.indexOf("<"));


                for (News object : new NewsParser(response).parseJson()) {

                    object.setRead(sqlDatabaseHelper.getNewsReadStatus(object.getLink()));

                    newsArrayList.add(object);
                }


                initializeFragment();


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

                for (News object : new NewsParser(response).parseJson()) {
                    object.setRead(sqlDatabaseHelper.getNewsReadStatus(object.getLink()));
                    newsArrayList.add(object);
                }

                initializeFragment();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // Cache data not exist.
        }

    }

    private void initializeFragment() {

        newsAdapter.notifyDataSetChanged();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_rss_feed, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_rss_list_recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        recyclerView.setAdapter(newsAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {


                Intent intent = new Intent(getContext(), NewsDescriptionActivity.class);


                News news = (News) newsArrayList.get(position);

                intent.putExtra("news", news);

                if (sourceType == SOURCETYPE_OFFLINE){
                    intent.putExtra("isOffline", true);
                }

                startActivity(intent);

                sqlDatabaseHelper = new SqlDatabaseHelper(getContext());
                sqlDatabaseHelper.addReadNews(news);

                news.setRead(true);
                newsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onLongItemClick(View view, int position) {



               /* WebView webView = (WebView) view.findViewById(R.id.contentMain_cache_webView);
                webView.getSettings().setAppCacheEnabled(true);
                webView.getSettings().setAppCachePath(getContext().getCacheDir().getPath());
                webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

                webView.loadUrl(((News) newsArrayList.get(position)).getLink());
*/

            }
        }));

        if (newNoteSaved && sourceType == 2) {
            newsArrayList.clear();
            fetchNewsFromDatabse();
        }

        initializeFragment();


        return view;
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
