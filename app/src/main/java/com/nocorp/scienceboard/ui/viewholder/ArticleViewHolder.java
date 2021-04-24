package com.nocorp.scienceboard.ui.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;

public class ArticleViewHolder extends RecyclerView.ViewHolder  {
    public ImageView thumbnail;
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

        cardViewThumbnail.setOnClickListener(v -> {
            this.listener.onArticleClicked(getAdapterPosition(), itemView);
        });

        containerTitle.setOnClickListener(v -> {
            this.listener.onArticleClicked(getAdapterPosition(), itemView);
        });

    }

    public void hideCardView() {
        cardViewThumbnail.setVisibility(View.GONE);
    }

    public void showCardView() {
        cardViewThumbnail.setVisibility(View.VISIBLE);
    }
}
