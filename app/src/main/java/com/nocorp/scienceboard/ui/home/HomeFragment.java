package com.nocorp.scienceboard.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.transition.MaterialElevationScale;
import com.nocorp.scienceboard.NavGraphDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksListOnChangedListener;
import com.nocorp.scienceboard.databinding.FragmentHomeBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.CustomizeMyTopicsButton;
import com.nocorp.scienceboard.model.MyTopicsItem;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterMyTopics;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.topics.repository.TopicRepository;
import com.nocorp.scienceboard.ui.bookmarks.BookmarksViewModel;
import com.nocorp.scienceboard.ui.timemachine.OnDateChangedListener;
import com.nocorp.scienceboard.ui.timemachine.TimeMachineViewModel;
import com.nocorp.scienceboard.ui.topics.TopicsViewModel;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.ui.webview.WebviewViewModel;
import com.nocorp.scienceboard.utility.MyValues;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HomeFragment extends Fragment implements
        RecyclerAdapterMyTopics.TopicCoverListener,
        RecyclerAdapterArticlesList.OnArticleClickedListener,
        OnDateChangedListener,
        BookmarksListOnChangedListener {
    private final String TAG = this.getClass().getSimpleName();
    private final String APP_NAME = "NOPPYS_BOARD - ";

    private View view;
    private FragmentHomeBinding viewBinding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toast toast;
    private FloatingActionButton switchTopicFAB;
    private View includeEmptyTopicsMessage;
    private Button addTopicButton_emptyMessage;
    private CircularProgressIndicator progressIndicator;
    private MenuItem switchThemeMenuItem;

    // recycler
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerViewArticles;

    // viewmodel
    private HomeViewModel homeViewModel;
    private SourceViewModel sourceViewModel;
    private TopicsViewModel topicsViewModel;
    private TimeMachineViewModel timeMachineViewModel;
    private WebviewViewModel webviewViewModel;
    private BookmarksViewModel bookmarksViewModel;

    //
    private AdProvider adProvider;
    private List<ListItem> elementsToDisplayInHome;
    private List<Topic> myFollowedTopics;
    private final int NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE = 2;
    private final int AD_DISTANCE = 5; // distance between ads (in terms of items)
    private long currentDateInMillis;
    private boolean recyclerIsLoading = false;
    // animations
    private int shortAnimationDuration;
    private boolean switchButtonIsVisible;
    private final long ANIMATION_DURATION = 4000L;
    private ObjectAnimator objectAnimator;
    private static boolean darkModeEnabled;

    // repository

    // flag variables
    private static boolean homeFragmentArticlesLoaded = false;
    private static boolean forceContentInitialization = false;


    //
    private static List<Source> sourcesFetched_cached;




    //---------------------------------------------------------------------------------------- CONSTRUCTORS

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }





    //---------------------------------------------------------------------------------------- ANDROID METHODS


    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
//        setExitTransition(new Hold().setDuration(1000));
        setExitTransition(new MaterialElevationScale(/* growing= */ false));
        setReenterTransition(new MaterialElevationScale(/* growing= */ true));

        //
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        viewBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

        //

//        if(sourcesFetched_cached==null) {
//            observeSourcesFetched();
//        }
//        else {
//            // init starting date
//            long startingDate = initStartingDate();
//
//
//            // this will prevent articles being fetched everytime the sources are observed
////                if(homeFragmentArticlesLoaded) {
////                    Log.d(TAG, "observeSourcesFetched: content already initilized");
////                    if(forceContentInitialization) {
////                    }
////                    else {
////                        // ignore, retain old articles
////                    }
////                }
////                else{
////                    // ignore
////                }
//
//            initContent(sourcesFetched_cached, startingDate);
//
//            //
//            myFollowedTopics = TopicRepository.getFollowedTopics_fromCached();
//            changeLayoutOnfollowedTopicsListChanged(myFollowedTopics);
//        }

        observeSourcesFetched();


        observeArticlesFetched();
        observerNextArticlesFetched();
        observeCustomizationStatus();
        observeTimeMachineStatus();

    }// end onViewCreated

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        switchThemeMenuItem = menu.findItem(R.id.option_homeFragment_changeTheme);
        checkDarkMode();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.option_homeFragment_changeTheme) {
            switchTheme();
            return true;
        }
        else if(item.getItemId() == R.id.option_homeFragment_about) {
            showAboutFragment();
            return true;
        }
        return false;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: called");
        super.onDestroyView();
        viewBinding = null;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: called");
        super.onPause();

        // reset previous observed articles
//        homeViewModel.setArticlesList(null);

        // setting flags
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: called");
        super.onDetach();
        homeFragmentArticlesLoaded = false;
    }

    //---------------------------------------------------------------------------------------- METHODS

    private void initView() {
        // views
        progressIndicator = viewBinding.progressIndicatorHomefragment;
        swipeRefreshLayout = viewBinding.swipeRefreshLayoutHomeFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange);
        recyclerViewArticles = viewBinding.recyclerViewHomeFragment;
        switchTopicFAB = viewBinding.floatingActionButtonHomeFragmentSwitchTopic;
        switchTopicFAB.setOnClickListener(v -> showSwitchTopicDialog());
        includeEmptyTopicsMessage = view.findViewById(R.id.include_mainActivity_oneEmptyMyTopicsList);
        addTopicButton_emptyMessage = viewBinding.includeMainActivityOneEmptyMyTopicsList.buttonLayoutEmptyTopics;
        addTopicButton_emptyMessage.setOnClickListener(v -> showCustomizeHomeFeedFragment());

        //
        currentDateInMillis = System.currentTimeMillis();
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // viewmodels
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        sourceViewModel = new ViewModelProvider(requireActivity()).get(SourceViewModel.class);
        topicsViewModel = new ViewModelProvider(requireActivity()).get(TopicsViewModel.class);
        timeMachineViewModel = new ViewModelProvider(requireActivity()).get(TimeMachineViewModel.class);
        bookmarksViewModel = new ViewModelProvider(requireActivity()).get(BookmarksViewModel.class);
        bookmarksViewModel.setBookmarksListOnChangedListener(this);
        webviewViewModel = new ViewModelProvider(requireActivity()).get(WebviewViewModel.class);
        webviewViewModel.setBookmarksListOnChangedListener(this);

        //
        initRecycleViewArticles(recyclerViewArticles);
        setupScrollListener(recyclerViewArticles);
        setupSwipeDownToRefresh(swipeRefreshLayout);
    }



    //---------------------------------------------------------- observing viewmodels

    private void observeSourcesFetched() {
        Log.d(TAG, "observeSourcesFetched: contentInitilized: " + homeFragmentArticlesLoaded);
        sourceViewModel.getObservableAllSources().observe(getViewLifecycleOwner(), resultSources -> {
            Log.d(TAG, "observeSourcesFetched: called");
            if(resultSources == null) {
                //TODO: error message
                Log.e(TAG, "SCIENCE_BOARD - loadSources: an error occurrend when fetching sources");
                showCenteredToast("an error occurred when fetching sources from remote server");
            }
            else if(resultSources.isEmpty()) {
                //TODO: warning message, no topics in memory
                Log.w(TAG, "SCIENCE_BOARD - loadSources: no sources in remote DB");
            }
            else {


                // getting enabled sources
                sourcesFetched_cached = sourceViewModel.getEnabledSources();

                // init starting date
                long startingDate = initStartingDate();


                // this will prevent articles being fetched everytime the sources are observed
//                if(homeFragmentArticlesLoaded) {
//                    Log.d(TAG, "observeSourcesFetched: content already initilized");
//                    if(forceContentInitialization) {
//                    }
//                    else {
//                        // ignore, retain old articles
//                    }
//                }
//                else{
//                    // ignore
//                }

                initContent(sourcesFetched_cached, startingDate);


            }
        });
    }

    private void initContent(List<Source> sourcesFetched, long startingDate) {
        Log.d(TAG, "initContent: content initilized");

        // setting flag
        homeFragmentArticlesLoaded = true;

        // fetching articles
        progressIndicator.setVisibility(View.VISIBLE);
        homeViewModel.fetchArticles(sourcesFetched,
                startingDate,
                false);

        //
        myFollowedTopics = TopicRepository.getFollowedTopics_fromCached();
        changeLayoutOnfollowedTopicsListChanged(myFollowedTopics);
    }

    private void observeArticlesFetched() {
        homeViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            Log.d(TAG, "getObservableArticlesList: called");
            swipeRefreshLayout.setRefreshing(false);
            progressIndicator.setVisibility(View.GONE);

            if(resultArticles==null) {
                recyclerIsLoading = false;
//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else if(resultArticles.isEmpty()) {
//                progressIndicator.setVisibility(View.GONE);
                recyclerIsLoading = false;
                // TODO this should only be called when the list is empty,
                // when errors occurs should be the case above

                elementsToDisplayInHome = new ArrayList<>();
                recyclerAdapterArticlesList.clearList();
            }
            else {
                elementsToDisplayInHome = new ArrayList<>();

                // setting up followed topics items
                setTopicsThumbnails(resultArticles,
                                    myFollowedTopics,
                                    homeViewModel.getPickedSources());

                populateHomeWithMyFollowedTopics(myFollowedTopics,
                                                 elementsToDisplayInHome);


                // setting up article items
                resultArticles = adProvider.populateListWithAds(resultArticles, AD_DISTANCE);
                elementsToDisplayInHome.addAll(resultArticles);
                recyclerAdapterArticlesList.loadNewData(elementsToDisplayInHome);

                //
                recyclerIsLoading = false;
            }
        });
    }

    private void observerNextArticlesFetched() {
        homeViewModel.getObservableNextArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            Log.d(TAG, "getObservableNextArticlesList: called");
            recyclerIsLoading = false;
            recyclerAdapterArticlesList.removeLoadingView(elementsToDisplayInHome);

            if(resultArticles!=null && !resultArticles.isEmpty()) {
                elementsToDisplayInHome = new ArrayList<>();

                // setting up followed topics items
                populateHomeWithMyFollowedTopics(myFollowedTopics, elementsToDisplayInHome);


                //  setting up article items
                resultArticles = adProvider.populateListWithAds(resultArticles, AD_DISTANCE);
                elementsToDisplayInHome.addAll(resultArticles);
                recyclerAdapterArticlesList.loadNewData(elementsToDisplayInHome);
            }
            else {
                // todo
            }
        });
    }

    private void observeTimeMachineStatus() {
        timeMachineViewModel.getObservablePickedDate().observe(getViewLifecycleOwner(), pickedDate -> {
            Log.d(TAG, "observeTimeMachineStatus: called");
            if(timeMachineViewModel.isDateChanged()) {
                Log.d(TAG, "observeTimeMachineStatus: using time machine");
                timeMachineViewModel.setDateChanged(false);
                recyclerAdapterArticlesList.clearList();
                refreshArticles();
            }
        });
    }

    private void observeCustomizationStatus() {
        topicsViewModel.getObservableCustomizationStatus().observe(getViewLifecycleOwner(), customizationCompleted -> {
            Log.d(TAG, "observeCustomizationStatus: called");
            if(customizationCompleted) {
                updateContentWithNewTopics();
            }
            else {
                // ignore
            }
        });
    }

    private void updateContentWithNewTopics() {
        refreshArticlesAndTopics();
        topicsViewModel.setCustomizationStatus(false);
    }


    //---------------------------------------------------------- listeners

    @Override
    public void onArticleClicked(int position, View itemView) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            homeViewModel.saveInHistory(article);
            article.setVisited(true);

            // add container transformation animation
//            FragmentNavigator.Extras animations = new FragmentNavigator
//                    .Extras
//                    .Builder()
//                    .addSharedElement(itemView, itemView.getTransitionName())
//                    .build();

            NavGraphDirections.ActionGlobalWebviewFragment action =
                    NavGraphDirections.actionGlobalWebviewFragment(article);
            Navigation.findNavController(view).navigate(action);
        }
    }

    @Override
    public void onTopicCoverClicked(int position) {
        RecyclerAdapterMyTopics recyclerAdapterMyTopics = recyclerAdapterArticlesList.getRecyclerAdapterMyTopics();
        ListItem listItem = recyclerAdapterMyTopics.getItem(position);

        if(listItem.getItemType() == MyValues.ItemType.TOPIC) {
            Topic clickedTopic = (Topic) listItem;
            if(clickedTopic!=null) {
                showTopicFeedsFragment(clickedTopic);
            }
        }
    }

    @Override
    public void onCustomizeMyTopicsButtonClicked(int position) {
        showCustomizeHomeFeedFragment();
    }

    @Override
    public void onBookmarksButtonClicked(int position) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            if(article.isBookmarked()) {
                article.setBookmarked(false);
                homeViewModel.removeFromBookmarks(article);
            }
            else {
                article.setBookmarked(true);
                homeViewModel.addToBookmarks(article);
            }
        }
    }

    @Override
    public void onDateChanged(long date) {
        // TODO
    }

    @Override
    public void onBookmarksListChanged() {
        homeViewModel.asyncBookmarksCheck(recyclerAdapterArticlesList.getAllItems(), () -> {
            recyclerAdapterArticlesList.notifyDataSetChanged();
        });
    }




    //---------------------------------------------------------- destinantions

    private void showAboutFragment() {
        Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_aboutFragment);
    }

    private void showCustomizeHomeFeedFragment() {
        Log.d(TAG, "showCustomizeHomeFeedFragment: called");
//        // add container transformation animation
//        FragmentNavigator.Extras animations = new FragmentNavigator
//                .Extras
//                .Builder()
//                .addSharedElement(view, view.getTransitionName())
//                .build();


        Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_topicsFragment);
    }

    private void showTopicFeedsFragment(Topic clickedTopic) {
        NavGraphDirections.ActionGlobalTopicFeedsFragment action =
                NavGraphDirections.actionGlobalTopicFeedsFragment(clickedTopic);
        Navigation.findNavController(view).navigate(action);
    }

    private void showSwitchTopicDialog() {
        List<String> list = buildMyTopicsChoices(myFollowedTopics);
        if(list==null || list.isEmpty()) return;

        //
        CharSequence[] items = list.toArray(new CharSequence[0]);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.switch_topic_dialog_label)
                .setItems(items, (dialog, which) -> {

                    Topic topicChosen = myFollowedTopics.get(which);
                    if(topicChosen!=null) {
                        showTopicFeedsFragment(topicChosen);
                    }

                })
                .setNegativeButton(R.string.newgative_button_message_switch_topic_dialog,
                        (dialog, which) -> dialog.dismiss())
                .show();
    }



    //----------------------------------------------------------


    private long initStartingDate() {
        long result = currentDateInMillis;
        if (timeMachineViewModel.timeMachineIsEnabled()) {
            try {
                result = timeMachineViewModel.getPickedDate();
            } catch (Exception e) {
                Log.e(TAG, "initStartingDate: ", e);
            }
        }
        return result;
    }

    private void changeLayoutOnfollowedTopicsListChanged(List<Topic> followedTopics) {
        if(followedTopics==null || followedTopics.isEmpty()) {
            Log.d(TAG, "changeLayoutOnfollowedTopicsListChanged: followed topics: 0");
            setLayoutWhenNoTopicsAreFollowed();
        }
        else {
            Log.d(TAG, "changeLayoutOnfollowedTopicsListChanged: followed topics: " + followedTopics.size());
            setLayoutWhenAtLeastOneTopicIsFollowed();
        }
    }

    private void setLayoutWhenAtLeastOneTopicIsFollowed() {
        Log.d(TAG, "setLayoutWhenAtLeastOneTopicIsFollowed: called");
        includeEmptyTopicsMessage.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        switchTopicFAB.setVisibility(View.VISIBLE);
        progressIndicator.setVisibility(View.VISIBLE);
    }

    private void setLayoutWhenNoTopicsAreFollowed() {
        Log.d(TAG, "setLayoutWhenNoTopicsAreFollowed: called");
        includeEmptyTopicsMessage.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
        switchTopicFAB.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.GONE);
    }

    private void populateHomeWithMyFollowedTopics(List<Topic> topics, List<ListItem> elementsToDisplayInHome) {
        MyTopicsItem myTopicsItem = new MyTopicsItem();

        // convert Topic --> to ListItem, for recycler list
        List<ListItem> convertedList = new ArrayList<>();
        if(topics!=null) {
            convertedList = new ArrayList<>(topics);
            // add customize button to the end
            convertedList.add(new CustomizeMyTopicsButton());
        }

        myTopicsItem.setMyTopics(convertedList);
        elementsToDisplayInHome.add(myTopicsItem); // add topics as first element
    }

    private List<String> buildMyTopicsChoices(List<Topic> topics) {
        List<String> result = new ArrayList<>();
        if(topics==null || topics.isEmpty()) return result;

        for(Topic current: topics) {
            String topicName = current.getDisplayName();
            if(topicName!=null)
                result.add(topicName);
        }

        return result;
    }

    private void setTopicsThumbnails(List<ListItem> elementsToDisplayInHome,
                                     List<Topic> givenTopics,
                                     List<Source> givenSources) {
        if(givenTopics==null || givenTopics.isEmpty()) return;
        if(elementsToDisplayInHome==null || elementsToDisplayInHome.isEmpty()) return;
        if(givenSources==null || givenSources.isEmpty()) return;

        List<Article> articles = filterArticles(elementsToDisplayInHome);
        if(articles==null || articles.isEmpty()) return;

//        SourceRepository sourceRepository = new SourceRepository();
//        List<Source> sourcesTarget = sourceRepository.getAsourceForEachFollowedCategory_randomly(givenSources, givenTopics);
//        for(Source currentSource: sourcesTarget) {
//
//        }

        for(Topic currentTopic: givenTopics) { // for each topic...
            String currentTopicId = currentTopic.getId();
            for(Source currentSource: givenSources) { // ...get a source...
                if(currentSource.getCategories().contains(currentTopicId)) {
                    String currentSourceId = currentSource.getId();
                    Collections.shuffle(articles); // ... randomize articles...
                    for(Article currentArticle: articles) { //...get an articles with a thumbnail ...
                        if(currentSourceId.equals(currentArticle.getSourceId())) {
                            String thumbnailUrl = currentArticle.getThumbnailUrl();
                            if(thumbnailUrl!=null) {
                                currentTopic.setThumbnailUrl(thumbnailUrl);
                                break;
                            }
                        }
                    }// end most inner for
                    break;
                }
            }// end middle for
        }// end outer for
    }

    private List<Article> filterArticles(List<ListItem> elementsToDisplayInHome) {
        List<Article> result = new ArrayList<>();

        for(ListItem listItem: elementsToDisplayInHome) {
            if(listItem.getItemType() == MyValues.ItemType.ARTICLE) {
                Article article = ((Article) listItem);
                result.add(article);
            }
        }

        return result;
    }

//    private List<Article> filterArticlesOfThisSources(List<Article> givenArticles, List<Source> givenSources) {
//        List<Article> result = new ArrayList<>();
//
//        List<Article> tempArticles = new ArrayList<>(givenArticles);
//
//
//        for(Source currentSource: givenSources) {
//            if(listItem.getItemType() == MyValues.ItemType.ARTICLE) {
//                Article article = ((Article) listItem);
//                result.add(article);
//            }
//        }
//
//        return result;
//    }





    private void initRecycleViewArticles(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this, this);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
    }

    private void setupScrollListener(RecyclerView recyclerView) {
//        if (nestedScrollView != null) {
//            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
//                    (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//                if (scrollY > oldScrollY) {
////                    Log.d(TAG, "Scroll DOWN");
//                    viewIsVisibleInLayout(nestedScrollView, recyclerViewTopics);
//                }
//                if (scrollY < oldScrollY) {
////                    Log.d(TAG, "Scroll UP");
//                    viewIsVisibleInLayout(nestedScrollView, recyclerViewTopics);
//                }
//
////                if (scrollY == 0) {
////                    Log.d(TAG, "TOP SCROLL");
////                }
//
//                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
//                    Log.d(TAG, "setupEndlessScroll: BOTTOM SCROLL");
//                    if ( ! recyclerIsLoading) {
//                        loadMoreArticles();
//                    }
//                }
//            });
//        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!recyclerIsLoading) {
                    if (reachedTheBottomOfList(linearLayoutManager)) {
                        // NOTE:
                        // this resolve the "cannot call this method in a scroll callback" exception
                        // it happens when we are adding elements while scrolling
                        recyclerView.post(() -> {
                            Log.d(TAG, "SCIENCE_BOARD - setupScrollListener: reached the end of the recycler");
                            loadMoreArticles();
                        });

                    }
                }
            }
        });
    }

    private boolean reachedTheBottomOfList(LinearLayoutManager linearLayoutManager) {
        return linearLayoutManager != null &&
                (elementsToDisplayInHome != null && !elementsToDisplayInHome.isEmpty()) &&
                linearLayoutManager.findLastCompletelyVisibleItemPosition() == elementsToDisplayInHome.size() - 1;
    }

    private void loadMoreArticles() {
        Log.d(TAG, "loadMoreArticles: called");
        recyclerIsLoading = true;

        // adding loading view
        recyclerAdapterArticlesList.addLoadingView(elementsToDisplayInHome);

        // load new items
        homeViewModel.fetchNextArticles(NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE);
    }

    private void setupSwipeDownToRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
            refreshArticles();
        });
    }

    private void refreshArticles() {
        Log.d(TAG, "refreshArticles: called");
        long startingDate = currentDateInMillis;
        if (timeMachineViewModel.timeMachineIsEnabled()) {
            startingDate = timeMachineViewModel.getPickedDate();
        }

        //
        homeViewModel.fetchArticles(sourcesFetched_cached,
                                    startingDate,
                                    true);
    }

    private void refreshArticlesAndTopics() {
        Log.d(TAG, "refreshArticlesAndTopics: called");
        myFollowedTopics = TopicRepository.getFollowedTopics_fromCached();

        if(myFollowedTopics==null || myFollowedTopics.isEmpty()) {
            setLayoutWhenNoTopicsAreFollowed();
            return;
        }
        else {
            setLayoutWhenAtLeastOneTopicIsFollowed();
        }

        //
        long startingDate = currentDateInMillis;
        if (timeMachineViewModel.timeMachineIsEnabled()) {
            startingDate = timeMachineViewModel.getPickedDate();
        }

        //
        homeViewModel.fetchArticles(sourcesFetched_cached,
                                    startingDate,
                                    true);
    }





    private void switchTheme() {
        Log.d(TAG, "enableDarkMode: clicked");
        if(darkModeEnabled) {
            disableDarkMode();
        }
        else {
            enableDarkMode();
        }
    }

    private void changeMenuItemIcon(MenuItem menuItem, int resourceId) {
        if(menuItem==null) return;

        try {
            menuItem.setIcon(resourceId);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - changeMenuItemICon: cannot change icon, cause:" + e.getMessage() );
        }
    }

    private void changeMenuItemTitle(MenuItem menuItem, String value) {
        if(menuItem==null) return;

        try {
            menuItem.setTitle(value);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - changeMenuItemTitle: cannot change icon, cause:" + e.getMessage() );
        }
    }

    private void disableDarkMode() {
        Log.d(TAG, "disableDarkMode: called");
        darkModeEnabled = false;
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        changeMenuItemIcon(switchThemeMenuItem, R.drawable.ic_sun);
        changeMenuItemTitle(switchThemeMenuItem, "Disable Dark Mode");
        // writing preference
        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.preference_app_theme_key), false);
        editor.apply();
    }

    private void enableDarkMode() {
        Log.d(TAG, "enableDarkMode: called");
        darkModeEnabled = true;
        changeMenuItemIcon(switchThemeMenuItem, R.drawable.ic_moon);
        changeMenuItemTitle(switchThemeMenuItem, "Enable Dark Mode");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // writing preference
        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.preference_app_theme_key), true);
        editor.apply();
    }

    private void checkDarkMode() {
        Log.d(TAG, "checkDarkMode: called");
        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = getResources().getBoolean(R.bool.preference_app_theme_default_value_key);
        darkModeEnabled = sharedPref.getBoolean(getString(R.string.preference_app_theme_key), defaultValue);

        if(darkModeEnabled) {
            changeMenuItemIcon(switchThemeMenuItem, R.drawable.ic_sun);
            changeMenuItemTitle(switchThemeMenuItem, "Disable Dark Mode");
        }
        else {
            changeMenuItemIcon(switchThemeMenuItem, R.drawable.ic_moon);
            changeMenuItemTitle(switchThemeMenuItem, "Enable Dark Mode");
        }
    }



    //---------------------------------------------------------------------------------------- UTILITY METHODS


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
                switchButtonIsVisible = false;
            }
        } else {
            // view is not within the visible window
//            Log.d(TAG, "viewIsVisibleInLayout: view is NOT visible");
            if( ! switchButtonIsVisible) {
                Log.d(TAG, "viewIsVisibleInLayout: switch button is now visible");
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

    private void showCenteredToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}// end HomeFragment