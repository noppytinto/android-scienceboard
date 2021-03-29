package com.nocorp.scienceboard.ui.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.nocorp.scienceboard.R;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    public ImageView thumbnail;
    public TextView title;
    public TextView pubDate;
    public CardView cardViewThumbnail;


    public ArticleViewHolder(@NonNull View itemView) {
        super(itemView);
        this.thumbnail = itemView.findViewById(R.id.imageView_articleViewholder_thumbnail);
        this.title = itemView.findViewById(R.id.textView_articleViewholder_title);
        this.pubDate = itemView.findViewById(R.id.textView_articleViewholder_info);
        this.cardViewThumbnail = itemView.findViewById(R.id.cardView_articleViewholder_thumbnail);
    }

    public void hideCardView() {
        cardViewThumbnail.setVisibility(View.GONE);
    }

    public void showCardView() {
        cardViewThumbnail.setVisibility(View.VISIBLE);
    }
}
