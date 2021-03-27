package com.nocorp.scienceboard.recycler.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nocorp.scienceboard.R;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    public ImageView thumbnail;
    public TextView title;
    public TextView pubDate;


    public ArticleViewHolder(@NonNull View itemView) {
        super(itemView);
        this.thumbnail = itemView.findViewById(R.id.imageView_articleViewholder_thumbnail);
        this.title = itemView.findViewById(R.id.textView_articleViewholder_title);
        this.pubDate = itemView.findViewById(R.id.textView_articleViewholder_info);
    }
}
