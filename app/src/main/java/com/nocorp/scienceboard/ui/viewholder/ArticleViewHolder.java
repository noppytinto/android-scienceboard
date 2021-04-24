package com.nocorp.scienceboard.ui.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;

public class ArticleViewHolder extends RecyclerView.ViewHolder  {
    public ImageView thumbnail;
    public MaterialButton bookmarkButton;
    public ImageView visitedIcon;
    public TextView title;
    public TextView pubDate;
    public CardView cardViewThumbnail;
    public ConstraintLayout containerTitle;
    public RecyclerAdapterArticlesList.OnArticleClickedListener listener;


    public ArticleViewHolder(@NonNull View itemView, RecyclerAdapterArticlesList.OnArticleClickedListener listener) {
        super(itemView);
        this.listener = listener;
        this.thumbnail = itemView.findViewById(R.id.imageView_articleViewholder_thumbnail);
        this.title = itemView.findViewById(R.id.textView_articleViewholder_title);
        this.pubDate = itemView.findViewById(R.id.textView_articleViewholder_info);
        this.cardViewThumbnail = itemView.findViewById(R.id.cardView_articleViewholder_thumbnail);
        this.containerTitle = itemView.findViewById(R.id.container_articleViewholder_title);
        this.bookmarkButton = itemView.findViewById(R.id.toggleButton_articleViewholder_addToBookmarks);
        this.visitedIcon = itemView.findViewById(R.id.imageView_articleViewholder_markedAsRead);

        bookmarkButton.setOnClickListener(v -> {
            listener.onBookmarksButtonClicked(getAdapterPosition());
        });

        cardViewThumbnail.setOnClickListener(v -> {
            listener.onArticleClicked(getAdapterPosition(), itemView);
            bookmarkButton.setVisibility(View.VISIBLE);
        });

        containerTitle.setOnClickListener(v -> {
            listener.onArticleClicked(getAdapterPosition(), itemView);
            bookmarkButton.setVisibility(View.VISIBLE);
        });

    }

    public void hideCardView() {
        cardViewThumbnail.setVisibility(View.GONE);
    }

    public void showCardView() {
        cardViewThumbnail.setVisibility(View.VISIBLE);
    }
}
