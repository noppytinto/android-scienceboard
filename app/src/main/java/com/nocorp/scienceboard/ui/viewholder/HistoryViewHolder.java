package com.nocorp.scienceboard.ui.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;

public class HistoryViewHolder extends RecyclerView.ViewHolder  {
    public ImageView thumbnail;
    public TextView title;
    public TextView pubDate;
    public CardView cardViewThumbnail;
    public CardView cardViewTitle;
    public RecyclerAdapterArticlesList.OnArticleClickedListener listener;


    public HistoryViewHolder(@NonNull View itemView, RecyclerAdapterArticlesList.OnArticleClickedListener listener) {
        super(itemView);
        this.listener = listener;
        this.thumbnail = itemView.findViewById(R.id.imageView_historyViewHolder_thumbnail);
        this.title = itemView.findViewById(R.id.textView_historyViewHolder_title);
        this.pubDate = itemView.findViewById(R.id.textView_historyViewHolder_info);
        this.cardViewThumbnail = itemView.findViewById(R.id.cardView_historyViewHolder_thumbnail);
        this.cardViewTitle = itemView.findViewById(R.id.cardView_historyViewHolder_title);

        cardViewThumbnail.setOnClickListener(v -> {
            this.listener.onArticleClicked(getAdapterPosition());
        });

        cardViewTitle.setOnClickListener(v -> {
            this.listener.onArticleClicked(getAdapterPosition());
        });

    }

    public void hideCardView() {
        cardViewThumbnail.setVisibility(View.GONE);
    }

    public void showCardView() {
        cardViewThumbnail.setVisibility(View.VISIBLE);
    }
}
