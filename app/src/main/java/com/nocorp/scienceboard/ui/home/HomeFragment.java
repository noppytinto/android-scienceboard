package com.nocorp.scienceboard.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.nocorp.scienceboard.NavGraphDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentHomeBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterMyTopics;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.ui.tabs.all.AllTabViewModel;
import com.nocorp.scienceboard.ui.timemachine.TimeMachineViewModel;
import com.nocorp.scienceboard.ui.topics.TopicsViewModel;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.ui.webview.WebviewViewModel;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements
        RecyclerAdapterMyTopics.TopicCoverListener,
        RecyclerAdapterArticlesList.OnArticleClickedListener{
    private final String TAG = this.getClass().getSimpleName();
    private View view;
    private FragmentHomeBinding viewBinding;
    private Toast toast;
    private NestedScrollView nestedScrollView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton switchTopicButton;

    // recycler
    private RecyclerAdapterMyTopics recyclerAdapterMyTopics;
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerViewArticles;
    private RecyclerView recyclerViewTopics;


    // viewmodel
    private HomeViewModel homeViewModel;
    private SourceViewModel sourceViewModel;
    private TopicsViewModel topicsViewModel;
    private TimeMachineViewModel timeMachineViewModel;
    private WebviewViewModel webviewViewModel;
    private AllTabViewModel allTabViewModel;



    //
    private AdProvider adProvider;
    private List<Source> sourcesFetched;
    private List<ListItem> articlesToDisplay;
    private List<Topic> topics;
    private final int NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE = 10;
    private final int AD_DISTANCE = 5; // distance between ads (in terms of items)
    private long currentDateInMillis;
    private boolean recyclerIsLoading = false;
    // animations
    private int shortAnimationDuration;
    private boolean switchButtonIsVisible;


    //---------------------------------------------------------------------------------------- CONSTRUCTORS

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }





    //---------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        return view;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // views
        nestedScrollView = viewBinding.nestedScrollViewHomeFragment;
        swipeRefreshLayout = viewBinding.swipeRefreshLayoutHomeFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange);
        recyclerViewTopics = viewBinding.recyclerViewHomeFragmentTopics;
        recyclerViewArticles = viewBinding.recyclerViewHomeFragmentHeadlines;
        switchTopicButton = viewBinding.imageButtonHomeFragmentSwitchTopic;

        // viewmodels
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        sourceViewModel = new ViewModelProvider(requireActivity()).get(SourceViewModel.class);
        topicsViewModel = new ViewModelProvider(requireActivity()).get(TopicsViewModel.class);
        allTabViewModel = new ViewModelProvider(this).get(AllTabViewModel.class);

        //
        currentDateInMillis = System.currentTimeMillis();
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);


        //
        initRecycleViewTopics(recyclerViewTopics);
        initRecycleViewArticles(recyclerViewArticles);
        setupScrollListener(nestedScrollView);
        setupSwipeDownToRefresh(swipeRefreshLayout, currentDateInMillis);

        //
        observeSourcesFetched();
        observeArticlesFetched();
        observerNextArticlesFetched();

    }// end onViewCreated


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }





    //---------------------------------------------------------------------------------------- METHODS




    //---------------- observing viewmodels

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
                long targetDate = currentDateInMillis;
//
//                Log.d(TAG, "onChanged: using sources");
//                if (timeMachineViewModel.timeMachineIsEnabled()) {
//                    targetDate = timeMachineViewModel.getPickedDate();
//                }
//
                allTabViewModel.fetchArticles(
                        sources,
                        NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE,
                        false,
                        targetDate);



                topics = topicsViewModel.getObservableTopicsList().getValue();

            }
        });
    }

    private void observeArticlesFetched() {
        allTabViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            Log.d(TAG, "getObservableArticlesList: called");
            swipeRefreshLayout.setRefreshing(false);

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
                setTopicsThumbnails(resultArticles, topics, allTabViewModel.getPickedSources());
                recyclerAdapterMyTopics.loadNewData(topics);
                resultArticles = adProvider.populateListWithAds(resultArticles, AD_DISTANCE);
                articlesToDisplay = new ArrayList<>(resultArticles);
                recyclerAdapterArticlesList.loadNewData(articlesToDisplay);
//                showCenteredToast("articles fetched");
                recyclerIsLoading = false;
            }
        });
    }

    private void observerNextArticlesFetched() {
        allTabViewModel.getObservableNextArticlesList().observe(getViewLifecycleOwner(), fetchedArticles -> {
            Log.d(TAG, "getObservableNextArticlesList: called");
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




    //---------------- listeners

    @Override
    public void onArticleClicked(int position, View itemView) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            allTabViewModel.saveInHistory(article);
            article.setVisited(true);



//            FragmentNavigator.Extras extras = new FragmentNavigator
//                    .Extras
//                    .Builder()
//                    .addSharedElement(view, view.getTransitionName())
//                    .build();

            NavGraphDirections.ActionGlobalWebviewFragment action =
                    HomeFragmentDirections.actionGlobalWebviewFragment(article);
            Navigation.findNavController(view).navigate(action);

//
//            MobileNavigationDirections.ActionGlobalWebviewFragment action =
//                    MobileNavigationDirections.actionGlobalWebviewFragment(article);
//            Navigation.findNavController(view).navigate(action, extras);

//            MobileNavigationDirections.ActionGlobalWebviewFragment action =
//                    MobileNavigationDirections.actionGlobalWebviewFragment(article);
//            Navigation.findNavController(view).navigate(action);
        }
    }

    @Override
    public void onBookmarksButtonClicked(int position) {

    }

    @Override
    public void onTopicCoverClicked(int position) {

    }




    //----------------


    private List<String> setTopicsThumbnails(List<ListItem> articles, List<Topic> topics, List<Source> sources) {
        List<String> result = null;
        if(topics==null || topics.isEmpty()) return result;
        if(articles==null || articles.isEmpty()) return result;


        result = new ArrayList<>();
        List<Source> sourcesTarget = sources;


        for(Topic topic: topics) {
            String topicId = topic.getId();
            for(Source source: sourcesTarget) {
                if(source.getCategories().contains(topicId)) {
                    String sourceId = source.getId();
                    for(ListItem listItem: articles) {
                        Article article = ((Article) listItem);
                        if(sourceId.equals(article.getSourceId())) {
                            String thumbnailUrl = article.getThumbnailUrl();
                            if(thumbnailUrl!=null) {
                                topic.setThumbnailUrl(thumbnailUrl);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }





        return result;
    }






    private void initRecycleViewTopics(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapterMyTopics = new RecyclerAdapterMyTopics(new ArrayList<>(), this);
        recyclerView.setAdapter(recyclerAdapterMyTopics);
    }


    private void initRecycleViewArticles(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
    }


    /**
     * check whether view is in VSISIBLE/INVISIBLE state
     */
    private void viewIsVisible(NestedScrollView scrollView, View view) {
        if (view.isShown()) {
            // view is within the visible window
            Log.d(TAG, "viewIsVisible: view is visible");
        } else {
            // view is not within the visible window
            Log.d(TAG, "viewIsVisible: view is NOT visible");
        }
    }

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
                applyCrossfadeExit(switchTopicButton, shortAnimationDuration);
                switchButtonIsVisible = false;
            }
        } else {
            // view is not within the visible window
//            Log.d(TAG, "viewIsVisibleInLayout: view is NOT visible");
            if( ! switchButtonIsVisible) {
                Log.d(TAG, "viewIsVisibleInLayout: switch button is now visible");
                applyCrossfadeEnter(switchTopicButton, shortAnimationDuration);
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






    private List<Source> getASourceForEachTopic(List<Source> sources, List<Topic> topics) {
        List<Source> result = null;
        if(topics==null || topics.isEmpty()) return result;
        if(sources==null || sources.isEmpty()) return result;
        boolean skip = false;

        result = new ArrayList<>();
        for(Topic topic: topics) {
            skip = false;
            String topicId = topic.getId();
            for(Source source: sourcesFetched) {
                List<String> categories = source.getCategories();
                for(String category: categories) {
                    if(topicId.equals(category)) {
                        result.add(source);
                        skip = true;
                        break;
                    }
                }
                if(skip==true){
                    break;
                }
            }
        }

        return result;
    }



//    private void initScrollListener() {
//        recyclerViewArticles.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                LinearLayoutManager linearLayoutManager =
//                        (LinearLayoutManager) recyclerView.getLayoutManager();
//
//                if (!recyclerIsLoading) {
//                    if (reachedTheBottomOfList(linearLayoutManager)) {
//                        // NOTE:
//                        // this resolve the "cannot call this method in a scroll callback" exception
//                        // it happens when we are adding elements while scrolling
//                        recyclerView.post(() -> {
//                            Log.d(TAG, "SCIENCE_BOARD - initScrollListener: reached the end of the recycler");
//                            loadMoreArticles();
//                        });
//
//                    }
//                }
//            }
//        });
//    }


    private void loadMoreArticles() {
        recyclerIsLoading = true;

        // adding loading view
        recyclerAdapterArticlesList.addLoadingView(articlesToDisplay);

        // load new items
        allTabViewModel.fetchNextArticles(NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE);
    }

    private boolean reachedTheBottomOfList(LinearLayoutManager linearLayoutManager) {
        return linearLayoutManager != null &&
                (articlesToDisplay != null && !articlesToDisplay.isEmpty()) &&
                linearLayoutManager.findLastCompletelyVisibleItemPosition() == articlesToDisplay.size() - 1;
    }

    private void setupScrollListener(NestedScrollView nestedScrollView) {
        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                    (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > oldScrollY) {
//                    Log.d(TAG, "Scroll DOWN");
                    viewIsVisibleInLayout(nestedScrollView, recyclerViewTopics);
                }
                if (scrollY < oldScrollY) {
//                    Log.d(TAG, "Scroll UP");
                    viewIsVisibleInLayout(nestedScrollView, recyclerViewTopics);
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

    private void setupSwipeDownToRefresh(SwipeRefreshLayout swipeRefreshLayout, long targetDate) {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
            refreshArticles(targetDate);
        });
    }

    private void refreshArticles(long targetDate) {
//        if (timeMachineViewModel.timeMachineIsEnabled()) {
//            targetDate = timeMachineViewModel.getPickedDate();
//        }

        allTabViewModel.fetchArticles(
                sourcesFetched,
                NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE,
                true,
                targetDate);
    }





    //---------------------------------------------------------------------------------------- UTILITY METHODS

    private void showCenteredToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}// end HomeFragment