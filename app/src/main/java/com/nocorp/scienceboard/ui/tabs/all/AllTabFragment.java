package com.nocorp.scienceboard.ui.tabs.all;

import androidx.lifecycle.ViewModelProvider;

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
import com.nocorp.scienceboard.MobileNavigationDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksListOnChangedListener;
import com.nocorp.scienceboard.bookmarks.repository.OnBookmarksCheckedListener;
import com.nocorp.scienceboard.databinding.FragmentAllTabBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.ui.bookmarks.BookmarksViewModel;
import com.nocorp.scienceboard.ui.timemachine.OnDateChangedListener;
import com.nocorp.scienceboard.ui.timemachine.TimeMachineViewModel;
import com.nocorp.scienceboard.ui.topics.TopicsViewModel;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.ui.webview.WebviewViewModel;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import java.util.ArrayList;
import java.util.List;

public class AllTabFragment extends Fragment implements
        RecyclerAdapterArticlesList.OnArticleClickedListener,
        OnDateChangedListener,
        BookmarksListOnChangedListener {

    private final String TAG = this.getClass().getSimpleName();
    private FragmentAllTabBinding viewBinding;
    private View view;
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toast toast;

    // viewmodels
    private AllTabViewModel allTabViewModel;
    private SourceViewModel sourceViewModel;
    private TopicsViewModel topicsViewModel;
    private TimeMachineViewModel timeMachineViewModel;
    private BookmarksViewModel bookmarksViewModel;
    private WebviewViewModel webviewViewModel;

    //
    private AdProvider adProvider;
    private List<Source> sourcesFetched;
    private List<ListItem> articlesToDisplay;
    private boolean isLoading = false;

    //
    private final int NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE = 10;
    private final int AD_DISTANCE = 5; // distance between ads (in terms of items)
    private long currentDateInMillis;



    //----------------------------------------------------------------------------------------- CONSTRUCTORS

    public static AllTabFragment newInstance() {
        return new AllTabFragment();
    }





    //----------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentAllTabBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        Log.d(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);
        initView();

        observeSourcesFetched();
        observeArticlesFetched();
        observerNextArticlesFetched();
        observeCustomizationStatus();
        observeTimeMachineStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }





    //----------------------------------------------------------------------------------------- METHODS

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
                // TODO
                sourcesFetched = new ArrayList<>(sources);
                long targetDate = currentDateInMillis;

                Log.d(TAG, "onChanged: using sources");
                if (timeMachineViewModel.timeMachineIsEnabled()) {
                    targetDate = timeMachineViewModel.getPickedDate();
                }

                allTabViewModel.fetchArticles(
                        sources,
                        NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE,
                        false,
                        targetDate);

            }
        });
    }

    private void observeArticlesFetched() {
        allTabViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            Log.d(TAG, "getObservableArticlesList: called");
            swipeRefreshLayout.setRefreshing(false);
            progressIndicator.setVisibility(View.GONE);

            if(resultArticles==null) {
                isLoading = false;


//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else if(resultArticles.isEmpty()) {
                isLoading = false;
                // TODO this should only be called when the list is empty,
                // when errors occurs should be the case above

                articlesToDisplay = new ArrayList<>();
                recyclerAdapterArticlesList.clearList();
            }
            else {
                resultArticles = adProvider.populateListWithAds(resultArticles, AD_DISTANCE);
                articlesToDisplay = new ArrayList<>(resultArticles);
                recyclerAdapterArticlesList.loadNewData(articlesToDisplay);
                showCenteredToast("articles fetched");
                isLoading = false;
            }
        });
    }



    private void observerNextArticlesFetched() {
        allTabViewModel.getObservableNextArticlesList().observe(getViewLifecycleOwner(), fetchedArticles -> {
            Log.d(TAG, "getObservableNextArticlesList: called");
            isLoading = false;
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
                progressIndicator.setVisibility(View.VISIBLE);
                refreshArticles();
            }
        });
    }

    private void refreshArticles() {
        long targetDate = currentDateInMillis;

        if (timeMachineViewModel.timeMachineIsEnabled()) {
            targetDate = timeMachineViewModel.getPickedDate();
        }

        allTabViewModel.fetchArticles(
                sourcesFetched,
                NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE,
                true,
                targetDate);
    }

    private void observeCustomizationStatus() {
        topicsViewModel.getObservableCustomizationStatus().observe(getViewLifecycleOwner(), customizationCompleted -> {
            Log.d(TAG, "observeCustomizationStatus: called");

            if(customizationCompleted) {
                refreshArticles();
            }
            else {
                // ignore
            }
        });
    }




    private void initView() {
        progressIndicator = viewBinding.progressIndicatorAllArticlesTabFragment;
        swipeRefreshLayout = viewBinding.swipeRefreshAllArticlesTabFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        //
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        timeMachineViewModel = new ViewModelProvider(requireActivity()).get(TimeMachineViewModel.class);
        sourceViewModel = new ViewModelProvider(requireActivity()).get(SourceViewModel.class);
        allTabViewModel = new ViewModelProvider(this).get(AllTabViewModel.class);
        topicsViewModel = new ViewModelProvider(requireActivity()).get(TopicsViewModel.class);
        bookmarksViewModel = new ViewModelProvider(requireActivity()).get(BookmarksViewModel.class);
        bookmarksViewModel.setBookmarksListOnChangedListener(this);
        webviewViewModel = new ViewModelProvider(requireActivity()).get(WebviewViewModel.class);
        webviewViewModel.setBookmarksListOnChangedListener(this);

        //
        currentDateInMillis = System.currentTimeMillis();

        //
        initRecycleView();
        setupSwipeDownToRefresh();
    }

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = viewBinding.recyclerViewAllArticlesTabFragment;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
//        SnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(recyclerView);
        initScrollListener();
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (reachedTheBottomOfList(linearLayoutManager)) {
                        // NOTE:
                        // this resolve the "cannot call this method in a scroll callback" exception
                        // it happens when we are adding elements while scrolling
                        recyclerView.post(() -> {
                            Log.d(TAG, "SCIENCE_BOARD - initScrollListener: reached the end of the recycler");
                            loadMoreArticles();
                        });

                    }
                }
            }
        });
    }

    private boolean reachedTheBottomOfList(LinearLayoutManager linearLayoutManager) {
        return linearLayoutManager != null &&
                (articlesToDisplay != null && !articlesToDisplay.isEmpty()) &&
                linearLayoutManager.findLastCompletelyVisibleItemPosition() == articlesToDisplay.size() - 1;
    }

    private void loadMoreArticles() {
        isLoading = true;

        // adding loading view
        recyclerAdapterArticlesList.addLoadingView(articlesToDisplay);

        // load new items
        allTabViewModel.fetchNextArticles(NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE);
    }

    private void setupSwipeDownToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "onRefresh called from SwipeRefreshLayout");
            refreshArticles();
        });
    }




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
//
//            MobileNavigationDirections.ActionGlobalWebviewFragment action =
//                    MobileNavigationDirections.actionGlobalWebviewFragment(article);
//            Navigation.findNavController(view).navigate(action, extras);




            MobileNavigationDirections.ActionGlobalWebviewFragment action =
                    MobileNavigationDirections.actionGlobalWebviewFragment(article);
            Navigation.findNavController(view).navigate(action);
        }
    }

    @Override
    public void onBookmarksButtonClicked(int position) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            if(article.isBookmarked()) {
                article.setBookmarked(false);
                allTabViewModel.removeFromBookmarks(article);
            }
            else {
                article.setBookmarked(true);
                allTabViewModel.addToBookmarks(article);
            }
        }
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

    private void showToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onDateChanged(long date) {

    }

    @Override
    public void onBookmarksListChanged() {
        allTabViewModel.asyncBookmarksCheck(recyclerAdapterArticlesList.getAllItems(), () -> {
            recyclerAdapterArticlesList.notifyDataSetChanged();
        });
    }
}// end AllArticlesTabFragment