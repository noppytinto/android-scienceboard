package com.nocorp.scienceboard.recycler.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
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
import com.bumptech.glide.request.target.Target;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.recycler.viewholder.ArticleViewHolder;
import com.nocorp.scienceboard.utility.MyUtilities;


import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class RecyclerAdapterFeedsList extends RecyclerView.Adapter<ArticleViewHolder> {
    private List<Article> articlesList;
    private Context context;


    //------------------------------------------------------------------------CONSTRUCTORS
    public RecyclerAdapterFeedsList(List<Article> articlesList, Context context) {
        this.articlesList = articlesList;
        this.context = context;
    }



    //------------------------------------------------------------------------ METHODS


    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_article_viewholder, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articlesList.get(position);
        String thumbnailUrl = article.getThumbnailUrl();
        String readablePubDate = buildPubDate(article);
        String title = article.getTitle();

        if(thumbnailUrl==null) {
            holder.hideCardView();
        }
        else {
            Glide.with(context)
                    .load(thumbnailUrl)
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter()
                    .listener(new RequestListener<Drawable>() {
                                  @Override
                                  public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                      holder.hideCardView();
                                      return false;
                                  }

                                  @Override
                                  public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                      holder.showCardView();
                                      return false;
                                  }
                              }
                    )
                    .into(holder.thumbnail);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.title.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.title.setText(Html.fromHtml(title));
        }

        holder.pubDate.setText(readablePubDate);
    }

    @NotNull
    private String buildPubDate(Article article) {
        Date pubDate = article.getPublishDate();
        long pubDateInMillis = pubDate.getTime();
        return MyUtilities.convertMillisToReadableTimespan(pubDateInMillis);
    }

    @Override
    public int getItemCount() {
        return ( (articlesList != null) && (articlesList.size() != 0) ? articlesList.size() : 0);
    }


    public Article getArticle(int position) {
        return ( (articlesList != null) && (articlesList.size() != 0) ? articlesList.get(position) : null);
    }

    public void loadNewData(List<Article> newList) {
        articlesList = newList;
        notifyDataSetChanged();
    }




}// end RecyclerAdapterFeedsList
