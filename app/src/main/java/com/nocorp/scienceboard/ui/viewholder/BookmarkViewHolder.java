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

public class BookmarkViewHolder extends RecyclerView.ViewHolder {
    public ImageView thumbnail;
    public MaterialButton checkbox;
    public ImageView visitedIcon;
    public TextView title;
    public TextView pubDate;
    public CardView cardViewThumbnail;
    public ConstraintLayout containerTitle;
    public RecyclerAdapterArticlesList.OnArticleClickedListener listener;


    public BookmarkViewHolder(@NonNull View itemView, RecyclerAdapterArticlesList.OnArticleClickedListener listener) {
        super(itemView);
        this.listener = listener;
        this.thumbnail = itemView.findViewById(R.id.imageView_bookmarkViewholder_thumbnail);
        this.title = itemView.findViewById(R.id.textView_bookmarkViewholder_title);
        this.pubDate = itemView.findViewById(R.id.textView_bookmarkViewholder_info);
        this.cardViewThumbnail = itemView.findViewById(R.id.cardView_bookmarkViewholder_thumbnail);
        this.containerTitle = itemView.findViewById(R.id.container_bookmarkViewholder_title);
        this.checkbox = itemView.findViewById(R.id.toggleButton_bookmarkViewholder_checkbox);
        this.visitedIcon = itemView.findViewById(R.id.imageView_bookmarkViewholder_markedAsRead);

        checkbox.setOnClickListener(v -> {
            listener.onBookmarksButtonClicked(getAdapterPosition());
        });

        cardViewThumbnail.setOnClickListener(v -> {
            listener.onArticleClicked(getAdapterPosition(), itemView);
        });

        containerTitle.setOnClickListener(v -> {
            listener.onArticleClicked(getAdapterPosition(), itemView);
        });

    }

    public void hideCardView() {
        cardViewThumbnail.setVisibility(View.GONE);
    }

    public void showCardView() {
        cardViewThumbnail.setVisibility(View.VISIBLE);
    }

    public void hideCheckbox() {
        checkbox.setVisibility(View.GONE);
    }

    public void showCheckbox() {
        checkbox.setVisibility(View.VISIBLE);
    }
}
