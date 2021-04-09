package com.nocorp.scienceboard.recycler.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nocorp.scienceboard.R;

public class LoadingViewHolder extends RecyclerView.ViewHolder {
    public CircularProgressIndicator progressIndicator;

    public LoadingViewHolder(@NonNull View itemView) {
        super(itemView);
        this.progressIndicator = itemView.findViewById(R.id.progressIndicator_loadingViewHolder);
    }

}// end LoadingViewHolder
