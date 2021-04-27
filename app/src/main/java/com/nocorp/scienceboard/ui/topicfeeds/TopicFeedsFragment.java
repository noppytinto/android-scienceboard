package com.nocorp.scienceboard.ui.topicfeeds;

import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nocorp.scienceboard.NavGraphDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksListOnChangedListener;
import com.nocorp.scienceboard.databinding.FragmentTopicFeedsBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.ui.bookmarks.BookmarksViewModel;
import com.nocorp.scienceboard.ui.home.HomeFragmentDirections;
import com.nocorp.scienceboard.ui.timemachine.OnDateChangedListener;
import com.nocorp.scienceboard.ui.timemachine.TimeMachineViewModel;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.ui.webview.WebviewViewModel;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TopicFeedsFragment extends Fragment implements
        RecyclerAdapterArticlesList.OnArticleClickedListener,
        OnDateChangedListener,
        BookmarksListOnChangedListener
{
    private final String TAG = this.getClass().getSimpleName();
    private FragmentTopicFeedsBinding viewBinding;
    private View view;
    private CircularProgressIndicator progressIndicator;
    private NestedScrollView nestedScrollView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toast toast;

    // recycler
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerViewArticles;

    // viewmodels
    private TopicFeedsViewModel topicFeedsViewModel;
    private SourceViewModel sourceViewModel;
    private TimeMachineViewModel timeMachineViewModel;
    private WebviewViewModel webviewViewModel;
    private BookmarksViewModel bookmarksViewModel;

    //
    private AdProvider adProvider;
    private List<Source> sourcesFetched;
    private List<ListItem> articlesToDisplay;
    private Topic currentTopic;
    private final int NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE = 1;
    private final int AD_DISTANCE = 5; // distance between ads (in terms of items)
    private long currentDateInMillis;
    private boolean recyclerIsLoading = false;
    // animations
    private int shortAnimationDuration;
    private boolean switchButtonIsVisible;





    //--------------------------------------------------------------------------------------------- CONSTRUCTORS

    public static TopicFeedsFragment newInstance() {
        Log.d(TopicFeedsFragment.class.getSimpleName(), "SCIENCE_BOARD - newInstance: called");
        return new TopicFeedsFragment();
    }




    //--------------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentTopicFeedsBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        Log.d(TAG, "SCIENCE_BOARD - onCreateView: called");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initiView();

        Bundle arguments = getArguments();

        if(arguments!=null) {
            currentTopic = TopicFeedsFragmentArgs.fromBundle(arguments).getTopicArgument();

            observeSourcesFetched();
            observeArticlesFetched();
            observerNextArticlesFetch();
            observeTimeMachineStatus();

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }





    //--------------------------------------------------------------------------------------------- METHODS

    private void initiView() {
        nestedScrollView = viewBinding.nestedScrollViewTopicFeedsFragment;
        progressIndicator = viewBinding.progressIndicatorTopicFeedsFragment;
        swipeRefreshLayout = viewBinding.swipeRefreshTopicFeedsFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        recyclerViewArticles = viewBinding.recyclerViewTopicFeedsFragment;

        //
        currentDateInMillis = System.currentTimeMillis();
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // viewmodels
        topicFeedsViewModel = new ViewModelProvider(this).get(TopicFeedsViewModel.class);
        sourceViewModel = new ViewModelProvider(requireActivity()).get(SourceViewModel.class);
        timeMachineViewModel = new ViewModelProvider(requireActivity()).get(TimeMachineViewModel.class);
        bookmarksViewModel = new ViewModelProvider(requireActivity()).get(BookmarksViewModel.class);
        bookmarksViewModel.setBookmarksListOnChangedListener(this);
        webviewViewModel = new ViewModelProvider(requireActivity()).get(WebviewViewModel.class);
        webviewViewModel.setBookmarksListOnChangedListener(this);

        //
        initRecycleView(recyclerViewArticles);
        setupScrollListener(nestedScrollView);
        setupSwipeDownToRefresh(swipeRefreshLayout);
    }




    //---------------------------------------------------------- observing viewmodels

    private void observeSourcesFetched() {
        sourceViewModel.getObservableAllSources().observe(getViewLifecycleOwner(), sources -> {
            Log.d(TAG, "observeSourcesFetched: called");
            if(sources == null) {
                //TODO: error message
                Log.e(TAG, "SCIENCE_BOARD - loadSources: an error occurrend when fetching sources");
                showCenteredToast("an error occurred when fetching sources from remote DB");
            }
            else if(sources.isEmpty()) {
                //TODO: warning message, no topics in memory
                Log.w(TAG, "SCIENCE_BOARD - loadSources: no sources in remote DB");
            }
            else {
//                // TODO
                sourcesFetched = new ArrayList<>(sources);
                long startingDate = currentDateInMillis;

                Log.d(TAG, "onChanged: using sources");
                if (timeMachineViewModel.timeMachineIsEnabled()) {
                    startingDate = timeMachineViewModel.getPickedDate();
                }

                topicFeedsViewModel.fetchArticles(
                        sources,
                        NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE,
                        false,
                        currentTopic.getId(),
                        startingDate);
            }
        });
    }

    private void observeArticlesFetched() {
        topicFeedsViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            Log.d(TAG, "getObservableArticlesList: called");
            swipeRefreshLayout.setRefreshing(false);
            progressIndicator.setVisibility(View.GONE);

            if(resultArticles==null) {
                recyclerIsLoading = false;
//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else if(resultArticles.isEmpty()) {
                recyclerIsLoading = false;
                // TODO this should only be called when the list is empty,
                // when errors occurs should be the case above

                articlesToDisplay = new ArrayList<>();
                recyclerAdapterArticlesList.clearList();
            }
            else {
                resultArticles = adProvider.populateListWithAds(resultArticles, AD_DISTANCE);
                articlesToDisplay = new ArrayList<>(resultArticles);
                recyclerAdapterArticlesList.loadNewData(articlesToDisplay);
//                showCenteredToast("articles fetched");
                recyclerIsLoading = false;
            }
        });
    }

    private void observerNextArticlesFetch() {
        topicFeedsViewModel.getObservableNextArticlesList().observe(getViewLifecycleOwner(), fetchedArticles -> {
            recyclerIsLoading = false;
            recyclerAdapterArticlesList.removeLoadingView(articlesToDisplay);

            if(fetchedArticles!=null && !fetchedArticles.isEmpty()) {
                fetchedArticles = adProvider.populateListWithAds(fetchedArticles, AD_DISTANCE);
                articlesToDisplay = new ArrayList<>(fetchedArticles);
                recyclerAdapterArticlesList.loadNewData(articlesToDisplay);
            }
            else {
                // todo
            }
        });
    }

    private void observeTimeMachineStatus() {
        timeMachineViewModel.getObservablePickedDate().observe(getViewLifecycleOwner(), pickedDate -> {
            if(timeMachineViewModel.isDateChanged()) {
                Log.d(TAG, "observeTimeMachineStatus: using time machine");
                timeMachineViewModel.setDateChanged(false);
                recyclerAdapterArticlesList.clearList();
                refreshArticles();
            }
        });
    }





    //---------------------------------------------------------- listeners

    @Override
    public void onArticleClicked(int position, View itemView) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            topicFeedsViewModel.saveInHistory(article);
            article.setVisited(true);

            NavGraphDirections.ActionGlobalWebviewFragment action =
                    TopicFeedsFragmentDirections.actionGlobalWebviewFragment(article);
            Navigation.findNavController(view).navigate(action);
        }
    }

    @Override
    public void onBookmarksButtonClicked(int position) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            if(article.isBookmarked()) {
                article.setBookmarked(false);
                topicFeedsViewModel.removeFromBookmarks(article);
            }
            else {
                article.setBookmarked(true);
                topicFeedsViewModel.addToBookmarks(article);
            }
        }
    }

    @Override
    public void onDateChanged(long date) {

    }

    @Override
    public void onBookmarksListChanged() {
        topicFeedsViewModel.asyncBookmarksCheck(recyclerAdapterArticlesList.getAllItems(), () -> {
            recyclerAdapterArticlesList.notifyDataSetChanged();
        });
    }




    //----------------------------------------------------------

    private void initRecycleView(RecyclerView recyclerView) {
        // defining Recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
    }

    private void setupScrollListener(NestedScrollView nestedScrollView) {
        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                    (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                        if (scrollY > oldScrollY) {
//                    Log.d(TAG, "Scroll DOWN");
//                            viewIsVisibleInLayout(nestedScrollView, recyclerViewTopics);
                        }
                        if (scrollY < oldScrollY) {
//                    Log.d(TAG, "Scroll UP");
//                            viewIsVisibleInLayout(nestedScrollView, recyclerViewTopics);
                        }

//                if (scrollY == 0) {
//                    Log.d(TAG, "TOP SCROLL");
//                }

                        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                            Log.d(TAG, "setupEndlessScroll: BOTTOM SCROLL");
                            if ( ! recyclerIsLoading) {
                                loadMoreArticles();
                            }
                        }
                    });
        }
    }

    private void loadMoreArticles() {
        recyclerIsLoading = true;

        // adding loading view
        recyclerAdapterArticlesList.addLoadingView(articlesToDisplay);

        // load new items
        topicFeedsViewModel.fetchNextArticles(NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE);
    }

    private void setupSwipeDownToRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
            refreshArticles();
        });
    }

    private void refreshArticles() {
        long startingDate = currentDateInMillis;

        if (timeMachineViewModel.timeMachineIsEnabled()) {
            startingDate = timeMachineViewModel.getPickedDate();
        }

        topicFeedsViewModel.fetchArticles(
                sourcesFetched,
                NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE,
                true,
                currentTopic.getId(),
                startingDate);
    }


    //---------------------------------------------------------------------------------------- UTILITY METHODS

    /**
     * check whether view is visible to the user
     * (in terms of visible screen bounds)
     */
    private void viewIsVisibleInLayout(NestedScrollView scrollView, View view) {
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (view.getLocalVisibleRect(scrollBounds)) {
            // view is within the visible window
//            Log.d(TAG, "viewIsVisibleInLayout: view is visible");
            if(switchButtonIsVisible) {
                Log.d(TAG, "viewIsVisibleInLayout: switch button is now gone");
//                applyCrossfadeExit(switchTopicButton, shortAnimationDuration);
                switchButtonIsVisible = false;
            }
        } else {
            // view is not within the visible window
//            Log.d(TAG, "viewIsVisibleInLayout: view is NOT visible");
            if( ! switchButtonIsVisible) {
                Log.d(TAG, "viewIsVisibleInLayout: switch button is now visible");
//                applyCrossfadeEnter(switchTopicButton, shortAnimationDuration);
                switchButtonIsVisible = true;
            }
        }
    }

    private void applyCrossfadeEnter(View view, int duration) {
        final float STARTING_ALPHA = 0f;
        final float ENDING_ALPHA = 1f;

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(STARTING_ALPHA);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(ENDING_ALPHA)
                .setDuration(duration)
                .setListener(null);
    }

    private void applyCrossfadeExit(View view, int duration) {
        final float STARTING_ALPHA = 0f;

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        view.animate()
                .alpha(STARTING_ALPHA)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    private void runToastOnUiThread(String message) {
        requireActivity().runOnUiThread(() -> {
                    try {
                        if(toast!=null) toast.cancel();
                        toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT);
                        toast.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private void showCenteredToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    @NotNull
    private Calendar convertMillisInCalendar(Long pickedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pickedDate);
        return cal;
    }

}// end TopicFeedsFragment