package com.nocorp.scienceboard.recycler.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.recycler.viewholder.ArticleViewHolder;


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

        Glide.with(context)
                .load(article.getThumbnailUrl())
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter()
                .into(holder.thumbnail);

        holder.title.setText(article.getTitle());
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
