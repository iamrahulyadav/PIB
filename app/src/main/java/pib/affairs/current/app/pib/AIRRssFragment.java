package pib.affairs.current.app.pib;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import dm.audiostreamer.MediaMetaData;
import utils.AIRNews;
import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.News;
import utils.NewsAdapter;
import utils.NewsParser;
import utils.SettingManager;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AIRRssFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AIRRssFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AIRRssFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String urlToOpen;
    private int sourceType;

    public ProgressDialog pDialog;

    private OnFragmentInteractionListener mListener;


    private ArrayList<Object> newsArrayList = new ArrayList<>();


    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private DownloadManager downloadManager;

    public AIRRssFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AIRRssFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AIRRssFragment newInstance(String param1, int param2) {
        AIRRssFragment fragment = new AIRRssFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, param2);
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

        if (pDialog == null) {
            pDialog = AIRNewsActivity.pDialog;

        }

        newsAdapter = new NewsAdapter(newsArrayList, getContext());

        if (sourceType == 1) {
            readAIRNewsFromStorage();
        } else {
            fetchNews();
        }


    }


    private void fetchNews() {


// Tag used to cancel the request
        String tag_string_req = "news_request";

        final String url = urlToOpen;

        loadCache(url);
        AIRNewsActivity.pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                response = response.substring(response.indexOf("<"));


                newsArrayList.clear();
                for (Object news : new NewsParser(response).parseJson()) {
                    newsArrayList.add(news);
                }

                addNativeExpressAds();
                newsAdapter.notifyDataSetChanged();

                AIRNewsActivity.pDialog.hide();
                setLastUpdated();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                AIRNewsActivity.pDialog.hide();
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
                for (Object news : new NewsParser(response).parseJson()) {
                    newsArrayList.add(news);
                }

                addNativeExpressAds();
                newsAdapter.notifyDataSetChanged();
                AIRNewsActivity.pDialog.hide();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // Cache data not exist.
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_airrss, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_airrss_list_recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        recyclerView.setAdapter(newsAdapter);

        newsAdapter.setClickListener(new NewsAdapter.ClickListener() {
            @Override
            public void onBookMarkClick(View view, int position) {

                Toast.makeText(getContext(), "Downloading News", Toast.LENGTH_SHORT).show();

                News news = (News) newsArrayList.get(position);

                if (sourceType == 1) {

                    onRemoveBookMark(news);
                    news.setBookMark(false);
                } else {
                    onBookMark(news);
                    news.setBookMark(true);

                }

                newsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTitleClick(View view, int position) {

                onItemClick(position);
            }
        });

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_rss_swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (sourceType == 1) {
                    readAIRNewsFromStorage();
                } else {
                    fetchNews();
                }

            }
        });

        return view;
    }

    private void onRemoveBookMark(News news) {
        try {
            File fdelete = new File(news.getLink());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    System.out.println("file Deleted :");
                } else {
                    System.out.println("file not Deleted :");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onBookMark(News news) {
        try {
            long downloadReference;

            // Create request for android download manager
            downloadManager = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(news.getLink()));

            //Setting title of request
            request.setTitle(news.getTitle());

            //Setting description of request
            request.setDescription(news.getTitle());

            String title;
            if (news.getTitle().indexOf(" ") > 0) {
                title = news.getTitle().substring(0, news.getTitle().indexOf(" "));
            } else {
                title = news.getTitle();
            }

            title = news.getTitle().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

            request.setDestinationInExternalFilesDir(getContext(),
                    Environment.DIRECTORY_DOWNLOADS, title + ".mp3");

            //Set the local destination for the downloaded file to a path
            //within the application's external files directory

            //Enqueue download and save into referenceId
            downloadReference = downloadManager.enqueue(request);


            Answers.getInstance().logCustom(new CustomEvent("AIR Radio bookmarked").putCustomAttribute("News", news.getTitle()));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void readAIRNewsFromStorage() {

        try {
            String path = "storage/emulated/0/Android/data/app.crafty.studio.current.affairs.pib/files/Download";
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files == null) {
                return;
            }

            newsArrayList.clear();
            for (int i = 0; i < files.length; i++) {
                Log.d("Files", "FileName:" + files[i].getName());

                News news = new News();
                news.setTitle(files[i].getName());
                news.setLink(Uri.fromFile(files[i]).getPath());
                news.setPubDate(getDateFromMillis(files[i].lastModified()));
                news.setBookMark(true);


                newsArrayList.add(news);
            }
            addNativeExpressAds();
            newsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getDateFromMillis(long millis) {

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM");
            String myDate = dateFormat.format(new Date(millis));
            return myDate;


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    private void addNativeExpressAds() {

        boolean subscription = AdsSubscriptionManager.getSubscription(getContext());

        int count = AdsSubscriptionManager.ADSPOSITION_COUNT;
        for (int i = 4; i < (newsArrayList.size()); i += count) {
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
        News news = (News) newsArrayList.get(position);

        MediaMetaData obj = new MediaMetaData();
        obj.setMediaId(news.getTitle());
        obj.setMediaUrl(news.getLink());
        obj.setMediaTitle(news.getTitle());
        obj.setMediaArt("http://www.newsonair.com/image/ALL-INDIA-RADIO-LOGO.jpg");


        AIRNewsActivity.streamingManager.onPlay(obj);

        Toast.makeText(getContext(), "Buffering...", Toast.LENGTH_SHORT).show();

        //AIRNewsActivity.playFromURL(news.getLink());


    }

    public void setLastUpdated() {
        try {


            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm EEE dd MMM");

            String myDate = dateFormat.format(new Date(System.currentTimeMillis()));
            AIRNewsActivity.toolbar.setSubtitle("Last updated - " + myDate);
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
