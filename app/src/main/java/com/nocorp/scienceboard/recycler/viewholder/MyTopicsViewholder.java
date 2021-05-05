package com.nocorp.scienceboard.recycler.viewholder;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterMyTopics;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MyTopicsViewholder extends RecyclerView.ViewHolder {
    public RecyclerView recyclerView;
    public RecyclerAdapterMyTopics recyclerAdapterMyTopics;

    public MyTopicsViewholder(@NonNull @NotNull View itemView, RecyclerAdapterMyTopics.TopicCoverListener topicCoverListener) {
        super(itemView);
        this.recyclerView = itemView.findViewById(R.id.recyclerView_homeFragment_topics);
        initRecycleViewTopics(recyclerView, itemView.getContext(), topicCoverListener);
    }

    private void initRecycleViewTopics(RecyclerView recyclerView, Context context, RecyclerAdapterMyTopics.TopicCoverListener topicCoverListener) {
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapterMyTopics = new RecyclerAdapterMyTopics(new ArrayList<>(), topicCoverListener);
        recyclerView.setAdapter(recyclerAdapterMyTopics);
    }

}
