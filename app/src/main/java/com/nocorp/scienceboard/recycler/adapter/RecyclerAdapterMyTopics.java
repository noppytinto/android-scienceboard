package com.nocorp.scienceboard.recycler.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.topics.model.Topic;

import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class RecyclerAdapterMyTopics extends
        RecyclerView.Adapter<RecyclerAdapterMyTopics.TopicCoverViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    private List<Topic> recyclerList;
    private final float THUMBNAIL_SIZE_MULTIPLIER = 0.50f;
    private TopicCoverListener listener;

    public interface TopicCoverListener {
        void onTopicCoverClicked(int position);
    }



    public RecyclerAdapterMyTopics(List<Topic> list,
                                   TopicCoverListener listener) {
        this.recyclerList = list;
        this.listener = listener;
    }


    @NonNull
    @Override
    public TopicCoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_topic_cover_viewholder, parent, false);
        return new TopicCoverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicCoverViewHolder holder, int position) {
        Topic topic = recyclerList.get(position);
        String topicName = topic.getDisplayName();
        String thumbnailUrl = topic.getThumbnailUrl();

        holder.topicNameLabel.setText(topicName);







        try {
            // TODO: crahses on andorid 21 (resource "thumbnail" not found)
            RequestOptions gildeOptions = new RequestOptions()
                    .fallback(R.drawable.placeholder_image)
                    .placeholder(R.drawable.placeholder_image)
                    .centerCrop();
//                        .error(R.drawable.default_avatar)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                        .priority(Priority.HIGH);

//            Glide.with(holder.itemView.getContext())
//                    .load(thumbnailUrl)
//                    .apply(gildeOptions)
//                    .thumbnail(/*sizeMultiplier = 0.25% less than original*/ THUMBNAIL_SIZE_MULTIPLIER)
//                    .transition(withCrossFade())
//                    .into(holder.thumbnail);

            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(thumbnailUrl)
                    .apply(gildeOptions)
                    .thumbnail(/*sizeMultiplier = 0.25% less than original*/ THUMBNAIL_SIZE_MULTIPLIER)
                    .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        holder.thumbnail.setImageBitmap(resource);
                        applyDominantoColor(resource, holder);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
//                        holder.thumbnail.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        holder.thumbnail.setImageDrawable(errorDrawable);
                    }
                });

        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - buildArticleItem: cannot set thumbnail in recycler, cause: " + e.getMessage());
        }

        // todo
    }

    private void applyDominantoColor(@NonNull Bitmap resource, @NonNull TopicCoverViewHolder holder) {
        Palette myPalette = createPaletteSync(resource);
        Palette.Swatch vibrant = myPalette.getDominantSwatch();
        if(vibrant != null){
            setDominantColors(holder, vibrant);
        }
        else {
            vibrant = myPalette.getDarkVibrantSwatch();
            if(vibrant != null){
                setDominantColors(holder, vibrant);
            }
            else {
                holder.topicNameLabel.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            }
        }
    }

    private void setDominantColors(@NonNull TopicCoverViewHolder holder, Palette.Swatch vibrant) {
//        int titleColor = vibrant.getBodyTextColor();
//        holder.topicNameLabel.setTextColor(titleColor);

        int backgroundColor = vibrant.getRgb();
        holder.container.setBackgroundColor(backgroundColor);
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





    class TopicCoverViewHolder extends RecyclerView.ViewHolder {
        private TextView topicNameLabel;
        private ImageView thumbnail;
        private CardView cardView;
        private ConstraintLayout container;

        public TopicCoverViewHolder(@NonNull View itemView) {
            super(itemView);
            topicNameLabel = itemView.findViewById(R.id.textView_layoutTopicCoverViewholder_title);
            thumbnail = itemView.findViewById(R.id.imageView_layoutTopicCoverViewholder_thumbnail);
            cardView = itemView.findViewById(R.id.cardView_layoutTopicCoverViewholder);
            container = itemView.findViewById(R.id.constraintLayout_layoutTopicCoverViewholder_container);

            cardView.setOnClickListener((buttonView) -> {
                listener.onTopicCoverClicked(getAdapterPosition());
            });
        }
    }// end TopicViewHolder

    // Generate palette synchronously and return it
    public Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }




}// end RecyclerAdapterTopics
