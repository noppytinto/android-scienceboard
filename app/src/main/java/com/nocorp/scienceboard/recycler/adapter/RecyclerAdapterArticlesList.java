package com.nocorp.scienceboard.recycler.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.nativead.NativeAd;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.utility.ad.admob.model.ListAd;
import com.nocorp.scienceboard.ui.viewholder.ArticleViewHolder;
import com.nocorp.scienceboard.ui.viewholder.ListAdViewHolder;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyUtilities;
import com.nocorp.scienceboard.utility.MyValues;


import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class RecyclerAdapterArticlesList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    private List<ListItem> recyclerList;
    private OnArticleClickedListener listener;

    private static final int ARTICLE_TYPE = 1;
    private static final int LIST_AD_TYPE = 2;
    private static final int VISITED_ARTICLE_TYPE = 3;
    private static final int BOOKMARKED_ARTICLE_TYPE = 4;

    public interface OnArticleClickedListener {
        public void onArticleClicked(int position);
    }


    //------------------------------------------------------------------------CONSTRUCTORS

    public RecyclerAdapterArticlesList(List<ListItem> recyclerList, OnArticleClickedListener listener) {
        this.recyclerList = recyclerList;
        this.listener = listener;
    }





    //------------------------------------------------------------------------ METHODS

    @Override
    public int getItemViewType(int position) {
        MyValues.ItemType type = recyclerList.get(position).getItemType();
        switch (type) {
            case ARTICLE:
                return ARTICLE_TYPE;
            case LIST_AD:
                return LIST_AD_TYPE;
            case BOOKMARKED_ARTICLE:
                return BOOKMARKED_ARTICLE_TYPE;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == ARTICLE_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_article_viewholder, parent, false);
            return new ArticleViewHolder(view, listener);
        }
        else if(viewType == LIST_AD_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_native_ad_articles_list_level, parent, false);
            return new ListAdViewHolder(view);
        }
        else if(viewType == VISITED_ARTICLE_TYPE) {
            //TODO create visited article view holder
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_article_viewholder, parent, false);
            return new ArticleViewHolder(view, listener);
        }
        else {
            return new ArticleViewHolder(null, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == ARTICLE_TYPE) {
            Article article = (Article) recyclerList.get(position);

            //
            buildArticleItem((ArticleViewHolder) holder, article);
        }
        else if(getItemViewType(position) == LIST_AD_TYPE) {
            ListAd listAd = (ListAd) recyclerList.get(position);

            //
            buildListAdItem((ListAdViewHolder) holder, listAd);
        }
        else if(getItemViewType(position) == VISITED_ARTICLE_TYPE) {
            //TODO create visited article view holder
            ListAd listAd = (ListAd) recyclerList.get(position);

            //
            buildListAdItem((ListAdViewHolder) holder, listAd);
        }


    }

    private void buildArticleItem(ArticleViewHolder holder, Article item) {
        String thumbnailUrl = item.getThumbnailUrl();
        String readablePubDate = buildPubDate(item);
        String title = item.getTitle();

        if(thumbnailUrl==null) {
            holder.hideCardView();
        }
        else {
            holder.showCardView();
            try {
                // TODO: crahses on andorid 21 (resource "thumbnail" not found)
                RequestOptions gildeOptions = new RequestOptions()
                        .fallback(R.drawable.broken_image)
                        .placeholder(R.drawable.placeholder_image)
                        .fitCenter();
//                        .error(R.drawable.default_avatar)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                        .priority(Priority.HIGH);


                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    Glide.with(holder.itemView.getContext())
                            .load(thumbnailUrl)
                            .apply(gildeOptions)
                            .transition(withCrossFade())
                            .thumbnail(/*sizeMultiplier = 0.25% less than original*/ 0.25f)
                            .listener(new RequestListener<Drawable>() {
                                          @Override
                                          public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                              holder.hideCardView();
                                              return false;
                                          }

                                          @Override
                                          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                          holder.showCardView();
                                              return false;
                                          }
                                      }
                            )
                            .into(holder.thumbnail);
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load(thumbnailUrl)
                            .apply(gildeOptions)
                            .thumbnail(/*sizeMultiplier = 0.25% less than original*/ 0.25f)
                            .into(holder.thumbnail);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "SCIENCE_BOARD - buildArticleItem: cannot set thumbnail in recycler " + e.getMessage());
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.title.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.title.setText(Html.fromHtml(title));
        }

        //
        String sourceName = item.getSourceName();
        if(sourceName != null) readablePubDate = sourceName + " / " + readablePubDate;

        holder.pubDate.setText(readablePubDate);
    }

    private void buildListAdItem(ListAdViewHolder holder, ListAd item) {
        NativeAd nativeAd = item.getAd();
        holder.displayNativeAd(nativeAd);
    }

    @NotNull
    private String buildPubDate(Article article) {
        Date pubDate = article.getPubDate();
        if(pubDate==null) return "0"; // TODO
        long pubDateInMillis = pubDate.getTime();
        return MyUtilities.convertMillisToReadableTimespan(pubDateInMillis);
    }

    @Override
    public int getItemCount() {
        return ( (recyclerList != null) && (recyclerList.size() != 0) ? recyclerList.size() : 0);
    }

    public ListItem getItem(int position) {
        return ( (recyclerList != null) && (recyclerList.size() != 0) ? recyclerList.get(position) : null);
    }

    public void loadNewData(List<ListItem> newList) {
        recyclerList = newList;
        notifyDataSetChanged();
    }


}// end RecyclerAdapterFeedsList
