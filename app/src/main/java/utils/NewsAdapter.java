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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.facebook.ads.NativeAdViewAttributes;
import com.google.android.gms.ads.NativeExpressAdView;

import org.w3c.dom.Text;

import java.util.List;

import pib.affairs.current.app.pib.R;

/**
 * Created by bunny on 30/09/17.
 */

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    private List<Object> newsArrayList;

    private static final int NEWS_VIEW_TYPE = 1;
    private static final int AD_VIEW_TYPE = 2;

    ClickListener clickListener;


    public class NewsViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description, pubDate;
        CardView baseCardView;
        ImageView isReadMask, bookMarkImageView;

        public NewsViewHolder(final View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.newsAdapter_title_textView);
            description = (TextView) view.findViewById(R.id.newsAdapter_description_textView);
            pubDate = (TextView) view.findViewById(R.id.newsAdapter_pubDate_textView);
            baseCardView = (CardView) view.findViewById(R.id.newsAdapter_base_cardView);
            isReadMask = (ImageView) view.findViewById(R.id.newsAdapter_isReadMask_imageView);
            bookMarkImageView = (ImageView) view.findViewById(R.id.newsAdapter_bookMark_imageView);


            bookMarkImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (clickListener != null) {
                        clickListener.onBookMarkClick(v, getAdapterPosition());
                    }

                }
            });


            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onTitleClick(v, getAdapterPosition());
                    }
                }
            };

            title.setOnClickListener(onClickListener);
            pubDate.setOnClickListener(onClickListener);

        }
    }

    public class NativeExpressAdViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        CardView cardView;

        TextView recommendedTextView;

        public NativeExpressAdViewHolder(View itemView) {
            super(itemView);

            linearLayout = (LinearLayout) itemView.findViewById(R.id.nativeExpress_container_linearLayout);

            cardView = (CardView) itemView.findViewById(R.id.nativeExpress_background_cardView);

            recommendedTextView = (TextView) itemView.findViewById(R.id.nativeExpress_recommended_textView);

        }
    }

    public NewsAdapter(List<Object> newsArrayList, Context context) {
        this.newsArrayList = newsArrayList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case AD_VIEW_TYPE:
                View nativeExpressLayoutView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.native_express_ad_container, parent, false);
                return new NativeExpressAdViewHolder(nativeExpressLayoutView);

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


               NativeExpressAdViewHolder adView = (NativeExpressAdViewHolder) holder;
                boolean nightMode = NightModeManager.getNightMode(context);

                NativeAd nativeAd = (NativeAd) newsArrayList.get(position);
                if (nativeAd.isAdLoaded()) {
                    adView.cardView.setVisibility(View.VISIBLE);
                    //adView.recommendedTextView.setVisibility(View.VISIBLE);
                    View view;
                    if (nightMode) {

                        NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes()
                                .setBackgroundColor(Color.parseColor("#28292e"))
                                .setTitleTextColor(Color.WHITE)
                                .setButtonTextColor(Color.WHITE)
                                .setDescriptionTextColor(Color.WHITE)
                                .setButtonColor(Color.parseColor("#F44336"));

                        view = NativeAdView.render(context, nativeAd, NativeAdView.Type.HEIGHT_120, viewAttributes);
                    } else {
                        NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes()
                                .setButtonTextColor(Color.WHITE)
                                .setButtonColor(Color.parseColor("#F44336"));

                        view = NativeAdView.render(context, nativeAd, NativeAdView.Type.HEIGHT_120, viewAttributes);
                    }


                    adView.linearLayout.removeAllViews();
                    adView.linearLayout.addView(view);

                } else {

                    adView.cardView.setVisibility(View.GONE);
                    adView.recommendedTextView.setVisibility(View.GONE);


                }
                break;

            case NEWS_VIEW_TYPE:
            default:

                NewsViewHolder newsViewHolder = (NewsViewHolder) holder;
                News news = (News) newsArrayList.get(position);

                if (Build.VERSION.SDK_INT > 24) {
                    newsViewHolder.title.setText(Html.fromHtml(news.getTitle(), Html.FROM_HTML_MODE_COMPACT));
                } else {
                    newsViewHolder.title.setText(Html.fromHtml(news.getTitle()));
                }
                newsViewHolder.description.setText(news.getDescription());
                newsViewHolder.pubDate.setText(news.getPubDate());
                if (news.isRead()) {
                    newsViewHolder.baseCardView.setCardElevation(0);
                    newsViewHolder.isReadMask.setVisibility(View.VISIBLE);
                } else {
                    newsViewHolder.baseCardView.setCardElevation(10);
                    newsViewHolder.isReadMask.setVisibility(View.GONE);
                }

                if (news.isBookMark()) {
                    newsViewHolder.bookMarkImageView.setImageResource(R.drawable.book_marked);
                } else {
                    newsViewHolder.bookMarkImageView.setImageResource(R.drawable.not_bookmark);

                }


        }

    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return newsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //logic to implement ad in every 8th card

        return (position % AdsSubscriptionManager.ADSPOSITION_COUNT == 4) ? AD_VIEW_TYPE : NEWS_VIEW_TYPE;
        //return NEWS_VIEW_TYPE;

    }


    public interface ClickListener {
        public void onBookMarkClick(View view, int position);

        public void onTitleClick(View view, int position);
    }

}
