package utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import pib.affairs.current.app.pib.R;

/**
 * Created by bunny on 30/09/17.
 */

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    Context context;
    private List<Object> newsArrayList;

    private static final int NEWS_VIEW_TYPE = 1;
    private static final int AD_VIEW_TYPE = 2;


    public class NewsViewHolder extends RecyclerView.ViewHolder {
        public TextView title,description ,pubDate;
        CardView baseCardView;
        ImageView isReadMask;

        public NewsViewHolder(View view) {
            super(view);

            title =(TextView)view.findViewById(R.id.newsAdapter_title_textView);
            description =(TextView)view.findViewById(R.id.newsAdapter_description_textView);
            pubDate =(TextView)view.findViewById(R.id.newsAdapter_pubDate_textView);
            baseCardView =(CardView)view.findViewById(R.id.newsAdapter_base_cardView);
            isReadMask = (ImageView)view.findViewById(R.id.newsAdapter_isReadMask_imageView);

        }
    }

    public class NativeExpressAdViewHolder extends RecyclerView.ViewHolder {

        public NativeExpressAdViewHolder(View itemView) {
            super(itemView);
        }
    }

    public NewsAdapter(List<Object> newsArrayList, Context context) {
        this.newsArrayList = newsArrayList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

           /* case AD_VIEW_TYPE:
                View nativeExpressLayoutView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.native_express_ad_container, parent, false);
                return new NativeExpressAdViewHolder(nativeExpressLayoutView);
*/
            case NEWS_VIEW_TYPE:
            default:
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.news_adapter_row_layout, parent, false);


                return new NewsViewHolder(itemView);
        }
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        //switch statment for making native ads in every 8th card
        int viewType = getItemViewType(position);
        switch (viewType) {

            case AD_VIEW_TYPE:
            /*    NativeExpressAdViewHolder nativeExpressAdViewHolder = (NativeExpressAdViewHolder) holder;
                NativeExpressAdView adView = (NativeExpressAdView) editorialGeneralInfoList.get(position);
                ViewGroup adCardView = (ViewGroup) nativeExpressAdViewHolder.itemView;
                adCardView.removeAllViews();

                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                if (checkShowAds) {
                    adCardView.addView(adView);
                }
                break;
*/
            case NEWS_VIEW_TYPE:
            default:

                NewsViewHolder newsViewHolder = (NewsViewHolder) holder;
                News news = (News) newsArrayList.get(position);

                if (Build.VERSION.SDK_INT > 24) {
                    newsViewHolder.title.setText(Html.fromHtml(news.getTitle(),Html.FROM_HTML_MODE_COMPACT));
                }else{
                    newsViewHolder.title.setText(Html.fromHtml(news.getTitle()));
                }
                newsViewHolder.description.setText(news.getDescription());
                newsViewHolder.pubDate.setText(news.getPubDate());
                if (news.isRead()){
                    newsViewHolder.baseCardView.setCardElevation(0);
                    newsViewHolder.isReadMask.setVisibility(View.VISIBLE);
                }else{
                    newsViewHolder.baseCardView.setCardElevation(10);
                    newsViewHolder.isReadMask.setVisibility(View.GONE);
                }


                 }

    }

    @Override
    public int getItemCount() {
        return newsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //logic to implement ad in every 8th card

        //return (position % 8 == 0) ? AD_VIEW_TYPE : EDITORIAL_VIEW_TYPE;
        return NEWS_VIEW_TYPE;

    }

}
