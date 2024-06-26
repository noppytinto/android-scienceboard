package com.nocorp.scienceboard.recycler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.topics.model.Topic;

import java.util.List;

public class RecyclerAdapterCustomizeTopics extends
        RecyclerView.Adapter<RecyclerAdapterCustomizeTopics.TopicViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    private List<Topic> recyclerList;



    private FollowChipListener listener;
    public interface FollowChipListener {
        void onFollowChipChecked(int position, Chip chip);
        void onFollowChipUnchecked(int position, Chip chip);

    }


    public RecyclerAdapterCustomizeTopics(List<Topic> list,
                                          FollowChipListener listener) {
        this.recyclerList = list;
        this.listener = listener;

    }


    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_topic_viewholder, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = recyclerList.get(position);

        holder.sourceNameLabel.setText(topic.getDisplayName());

        if(topic.getFollowed()) {
            holder.followChip.setChecked(true);
            holder.followChip.setText("followed");
        }
        else {
            holder.followChip.setChecked(false);
            holder.followChip.setText("follow");
        }

        // todo
    }

    @Override
    public int getItemCount() {
        return ( (recyclerList != null) && (recyclerList.size() != 0) ? recyclerList.size() : 0);
    }

    public Topic getItem(int position) {
        return ( (recyclerList != null) && (recyclerList.size() != 0) ? recyclerList.get(position) : null);
    }

    public void loadNewData(List<Topic> newList) {
        recyclerList = newList;
        notifyDataSetChanged();
    }





    class TopicViewHolder extends RecyclerView.ViewHolder {
        private TextView sourceNameLabel;
        private Chip followChip;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            sourceNameLabel = itemView.findViewById(R.id.textView_layoutTopic_name);
            followChip = itemView.findViewById(R.id.chip_layoutTopic_follow);

            followChip.setOnClickListener((buttonView) -> {
                if(followChip.isChecked()) {
                    listener.onFollowChipChecked(getAdapterPosition(), followChip);
                    followChip.setText("followed");
                }
                else {
                    listener.onFollowChipUnchecked(getAdapterPosition(), followChip);
                    followChip.setText("follow");
                }
            });
        }

    }// end TopicViewHolder



}// end RecyclerAdapterTopics
