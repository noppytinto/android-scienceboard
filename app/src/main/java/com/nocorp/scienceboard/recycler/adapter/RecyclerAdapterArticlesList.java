package com.nocorp.scienceboard.recycler.adapter;

import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.bookmarks.model.BookmarkArticle;
import com.nocorp.scienceboard.history.model.HistoryArticle;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.LoadingView;
import com.nocorp.scienceboard.model.MyTopicsItem;
import com.nocorp.scienceboard.recycler.viewholder.LoadingViewHolder;
import com.nocorp.scienceboard.recycler.viewholder.MyTopicsViewholder;
import com.nocorp.scienceboard.ui.viewholder.ArticleViewHolder;
import com.nocorp.scienceboard.ui.viewholder.BookmarkViewHolder;
import com.nocorp.scienceboard.ui.viewholder.HistoryViewHolder;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.ui.viewholder.SmallAdViewHolder;
import com.nocorp.scienceboard.utility.MyUtilities;
import com.nocorp.scienceboard.utility.MyValues;
import com.nocorp.scienceboard.utility.ad.admob.model.ListAd;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class RecyclerAdapterArticlesList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    private List<ListItem> recyclerList;
    private OnArticleClickedListener onArticleClickedListener;
    private RecyclerAdapterMyTopics.TopicCoverListener topicCoverListener;
    private RecyclerAdapterMyTopics recyclerAdapterMyTopics;
    private final float THUMBNAIL_SIZE_MULTIPLIER = 0.50f;

    private static final int LOADING_VIEW_TYPE = 0;
    private static final int ARTICLE_TYPE = 1;
    private static final int SMALL_AD_TYPE = 2;
    private static final int HISTORY_ARTICLE_TYPE = 3;
    private static final int BOOKMARK_ARTICLE_TYPE = 4;
    private static final int MY_TOPICS_LIST_TYPE = 5;


    public interface OnArticleClickedListener {
        public void onArticleClicked(int position, View itemView);
        public void onBookmarksButtonClicked(int position);
    }


    //------------------------------------------------------------------------CONSTRUCTORS

    public RecyclerAdapterArticlesList(List<ListItem> recyclerList, OnArticleClickedListener onArticleClickedListener, RecyclerAdapterMyTopics.TopicCoverListener topicCoverListener) {
        this.recyclerList = recyclerList;
        this.onArticleClickedListener = onArticleClickedListener;
        this.topicCoverListener = topicCoverListener;
    }





    //------------------------------------------------------------------------ METHODS

    @Override
    public int getItemViewType(int position) {
        MyValues.ItemType type = recyclerList.get(position).getItemType();
        switch (type) {
            case LOADING_VIEW:
                return LOADING_VIEW_TYPE;
            case ARTICLE:
                return ARTICLE_TYPE;
            case SMALL_AD:
                return SMALL_AD_TYPE;
            case HISTORY_ARTICLE:
                return HISTORY_ARTICLE_TYPE;
            case BOOKMARK_ARTICLE:
                return BOOKMARK_ARTICLE_TYPE;
            case MY_TOPICS_LIST:
                return MY_TOPICS_LIST_TYPE;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == LOADING_VIEW_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_viewholder, parent, false);
            return new LoadingViewHolder(view);
        }
        else if(viewType == ARTICLE_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_article_viewholder, parent, false);
            return new ArticleViewHolder(view, onArticleClickedListener);
        }
        else if(viewType == SMALL_AD_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_small_ad_articles_list_level, parent, false);
            return new SmallAdViewHolder(view);
        }
        else if(viewType == HISTORY_ARTICLE_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history_viewholder, parent, false);
            return new HistoryViewHolder(view, onArticleClickedListener);
        }
        else if(viewType == BOOKMARK_ARTICLE_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_bookmark_viewholder, parent, false);
            return new BookmarkViewHolder(view, onArticleClickedListener);
        }
        else if(viewType == MY_TOPICS_LIST_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_my_topics_list_viewholder, parent, false);
            return new MyTopicsViewholder(view, topicCoverListener);
        }
        else {
            return new ArticleViewHolder(null, onArticleClickedListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == LOADING_VIEW_TYPE) {
            buildLoadingViewItem((LoadingViewHolder) holder, position);
        }
        else if(getItemViewType(position) == ARTICLE_TYPE) {
            Article article = (Article) recyclerList.get(position);

            //
            buildArticleItem((ArticleViewHolder) holder, article);
        }
        else if(getItemViewType(position) == SMALL_AD_TYPE) {
            ListAd listAd = (ListAd) recyclerList.get(position);

            //
            buildSmallAdItem((SmallAdViewHolder) holder, listAd);
        }
        else if(getItemViewType(position) == HISTORY_ARTICLE_TYPE) {
            HistoryArticle article = (HistoryArticle) recyclerList.get(position);

            //
            buildHistoryItem((HistoryViewHolder) holder, article);
        }
        else if(getItemViewType(position) == BOOKMARK_ARTICLE_TYPE) {
            BookmarkArticle article = (BookmarkArticle) recyclerList.get(position);

            //
            buildBookmarkItem((BookmarkViewHolder) holder, article);
        }
        else if(getItemViewType(position) == MY_TOPICS_LIST_TYPE) {
            MyTopicsItem myTopicsItem = (MyTopicsItem) recyclerList.get(position);

            //
            buildMyTopicsItem((MyTopicsViewholder) holder, myTopicsItem);
        }
    }

    private void buildMyTopicsItem(MyTopicsViewholder holder, MyTopicsItem item) {
        List<ListItem> myTopics = item.getMyTopics();
        if(myTopics!=null) {


            //
            recyclerAdapterMyTopics = holder.recyclerAdapterMyTopics;
//            holder.recyclerAdapterMyTopics.clearList();
            holder.recyclerAdapterMyTopics.loadNewData(myTopics);
        }
    }

    public RecyclerAdapterMyTopics getRecyclerAdapterMyTopics() {
        return recyclerAdapterMyTopics;
    }

    private void buildLoadingViewItem(LoadingViewHolder holder, int position) {
        // TODO: progressbar would be displayed
    }

    private void buildArticleItem(ArticleViewHolder holder, Article item) {
        String thumbnailUrl = item.getThumbnailUrl();
        String readablePubDate = buildPubDate(item);
        String title = item.getTitle();
        boolean visited = item.isVisited();
        boolean bookmarked = item.isBookmarked();


        if(visited)
            holder.visitedIcon.setVisibility(View.VISIBLE);
        else
            holder.visitedIcon.setVisibility(View.GONE);


        if(bookmarked)
            holder.bookmarkButton.setChecked(true);
        else
            holder.bookmarkButton.setChecked(false);


        if(thumbnailUrl==null) {
            holder.hideCardView();
        }
        else {
            holder.showCardView();
            try {
                // TODO: crahses on andorid 21 (resource "thumbnail" not found)
                RequestOptions gildeOptions = new RequestOptions()
                        .fallback(R.drawable.placeholder_image)
                        .placeholder(R.drawable.placeholder_image)
                        .fitCenter();
//                        .error(R.drawable.default_avatar)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                        .priority(Priority.HIGH);


//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//                    Glide.with(holder.itemView.getContext())
//                            .load(thumbnailUrl)
//                            .apply(gildeOptions)
//                            .transition(withCrossFade())
//                            .thumbnail(/*sizeMultiplier = 0.25% less than original*/ 0.25f)
//                            .listener(new RequestListener<Drawable>() {
//                                          @Override
//                                          public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                              holder.hideCardView();
//                                              return false;
//                                          }
//
//                                          @Override
//                                          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                                          holder.showCardView();
//                                              return false;
//                                          }
//                                      }
//                            )
//                            .into(holder.thumbnail);
//                } else {
//                    Glide.with(holder.itemView.getContext())
//                            .load(thumbnailUrl)
//                            .apply(gildeOptions)
//                            .thumbnail(/*sizeMultiplier = 0.25% less than original*/ 0.25f)
//                            .into(holder.thumbnail);
//                }
                Glide.with(holder.itemView.getContext())
                        .load(thumbnailUrl)
                        .apply(gildeOptions)
                        .thumbnail(/*sizeMultiplier = 0.25% less than original*/ THUMBNAIL_SIZE_MULTIPLIER)
                        .transition(withCrossFade())
                        .into(holder.thumbnail);

            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - buildArticleItem: cannot set thumbnail in recycler, cause: " + e.getMessage());
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.title.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.title.setText(Html.fromHtml(title));
        }

        //
        String sourceName = item.getSourceRealName();
        if(sourceName != null) readablePubDate = sourceName + "  •  " + readablePubDate;

        holder.pubDate.setText(readablePubDate);
    }

    private void buildBookmarkItem(BookmarkViewHolder holder, Article item) {
        String thumbnailUrl = item.getThumbnailUrl();
        String readablePubDate = buildPubDate(item);
        String title = item.getTitle();
        boolean isVisited = item.isVisited();
        boolean isSelected = item.isSelected();
        boolean isEditable = item.isEditable();


        if(isEditable)
            holder.checkbox.setVisibility(View.VISIBLE);
        else
            holder.checkbox.setVisibility(View.GONE);

        if(isVisited)
            holder.visitedIcon.setVisibility(View.VISIBLE);
        else
            holder.visitedIcon.setVisibility(View.GONE);


        if(isSelected)
            holder.checkbox.setChecked(true);
        else
            holder.checkbox.setChecked(false);


        if(thumbnailUrl==null) {
            holder.hideCardView();
        }
        else {
            holder.showCardView();
            try {
                // TODO: crahses on andorid 21 (resource "thumbnail" not found)
                RequestOptions gildeOptions = new RequestOptions()
                        .fallback(R.drawable.broken_image)
                        .placeholder(R.drawable.placeholder_image)
                        .fitCenter();
//                        .error(R.drawable.default_avatar)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                        .priority(Priority.HIGH);


//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//                    Glide.with(holder.itemView.getContext())
//                            .load(thumbnailUrl)
//                            .apply(gildeOptions)
//                            .transition(withCrossFade())
//                            .thumbnail(/*sizeMultiplier = 0.25% less than original*/ 0.25f)
//                            .listener(new RequestListener<Drawable>() {
//                                          @Override
//                                          public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                              holder.hideCardView();
//                                              return false;
//                                          }
//
//                                          @Override
//                                          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                                          holder.showCardView();
//                                              return false;
//                                          }
//                                      }
//                            )
//                            .into(holder.thumbnail);
//                } else {
//                    Glide.with(holder.itemView.getContext())
//                            .load(thumbnailUrl)
//                            .apply(gildeOptions)
//                            .thumbnail(/*sizeMultiplier = 0.25% less than original*/ 0.25f)
//                            .into(holder.thumbnail);
//                }
                Glide.with(holder.itemView.getContext())
                        .load(thumbnailUrl)
                        .apply(gildeOptions)
                        .thumbnail(/*sizeMultiplier = 0.25% less than original*/ THUMBNAIL_SIZE_MULTIPLIER)
                        .transition(withCrossFade())
                        .into(holder.thumbnail);

            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - buildArticleItem: cannot set thumbnail in recycler " + e.getMessage());
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.title.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.title.setText(Html.fromHtml(title));
        }

        //
        String sourceName = item.getSourceRealName();
        if(sourceName != null) readablePubDate = sourceName + "  •  " + readablePubDate;

        holder.pubDate.setText(readablePubDate);
    }

    private void buildHistoryItem(HistoryViewHolder holder, Article item) {
        String thumbnailUrl = item.getThumbnailUrl();
        String readablePubDate = buildPubDate(item);
        String title = item.getTitle();

        if(thumbnailUrl==null) {
            holder.hideCardView();
        }
        else {
            holder.showCardView();
            try {
                // TODO: crahses on andorid 21 (resource "thumbnail" not found)
                RequestOptions gildeOptions = new RequestOptions()
                        .fallback(R.drawable.broken_image)
                        .placeholder(R.drawable.placeholder_image)
                        .fitCenter();

                Glide.with(holder.itemView.getContext())
                        .load(thumbnailUrl)
                        .apply(gildeOptions)
                        .thumbnail(/*sizeMultiplier = 0.25% less than original*/ THUMBNAIL_SIZE_MULTIPLIER)
                        .transition(withCrossFade())
                        .into(holder.thumbnail);

            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - buildHistoryItem: cannot set thumbnail in recycler " + e.getMessage());
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.title.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.title.setText(Html.fromHtml(title));
        }

        //
        String sourceName = item.getSourceRealName();
        if(sourceName != null) readablePubDate = sourceName + "  •  " + readablePubDate;

        holder.pubDate.setText(readablePubDate);
    }

    private void buildSmallAdItem(SmallAdViewHolder holder, ListAd item) {
        NativeAd nativeAd = item.getAd();
        holder.displayNativeAd(nativeAd);
    }

    @NotNull
    private String buildPubDate(Article article) {
        long pubDate = article.getPubDate();
        return MyUtilities.convertMillisToReadableTimespan(pubDate);
    }

    @Override
    public int getItemCount() {
        return ( (recyclerList != null) && (recyclerList.size() != 0) ? recyclerList.size() : 0);
    }

    public ListItem getItem(int position) {
        return ( (recyclerList != null) && (recyclerList.size() != 0) ? recyclerList.get(position) : null);
    }

    public List<ListItem> getAllItems() {
        return recyclerList;
    }

    public void loadNewData(List<ListItem> newList) {
        Log.d(TAG, "loadNewData: called");
        recyclerList = newList;
        notifyDataSetChanged();
    }

    public void clearList() {
        if(recyclerList !=null) {
            recyclerList.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * NOTE: the articles must be given outside
     * cannot be the recyclerList !
     */
    public void addLoadingView(List<ListItem> articles) {
        if(articles==null || articles.isEmpty()) return;
        articles.add(new LoadingView());
        notifyItemInserted(articles.size() - 1);
        Log.d(TAG, "SCIENCE_BOARD - loading view added");
    }

    /**
     * NOTE: the articles must be given outside
     * cannot be the recyclerList !
     */
    public void removeLoadingView(List<ListItem> articles) {
        if(articles==null || articles.isEmpty()) return;

        ListItem lastItem = articles.get(articles.size()-1);
        if(lastItem instanceof LoadingView) {
            articles.remove(articles.size() - 1);
            int scrollPosition = articles.size();
            notifyItemRemoved(scrollPosition);
            Log.d(TAG, "SCIENCE_BOARD - loading view removed");
        }
    }

    public void removeItem(ListItem item) {
        if(recyclerList !=null) {
            recyclerList.remove(item);
            notifyDataSetChanged();
        }
    }



    public void enableEditMode() {
        if(recyclerList !=null) {
            for(ListItem article : recyclerList) {
                article.setEditable(true);
                article.setSelected(false);
            }
            notifyDataSetChanged();
        }
    }

    public void  disableEditMode() {
        if(recyclerList !=null) {
            for(ListItem article : recyclerList) {
                article.setEditable(false);
                article.setSelected(false);
            }
            notifyDataSetChanged();
        }
    }

    public void selectAllItems() {
        if(recyclerList !=null) {
            for(ListItem article : recyclerList) {
                article.setSelected(true);
            }
            notifyDataSetChanged();
        }
    }

    public void deselectAllItems() {
        if(recyclerList !=null) {
            for(ListItem article : recyclerList) {
                article.setSelected(false);
            }
            notifyDataSetChanged();
        }
    }


}// end RecyclerAdapterFeedsList
