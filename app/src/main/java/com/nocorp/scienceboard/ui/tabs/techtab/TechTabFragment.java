package com.nocorp.scienceboard.ui.tabs.techtab;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nocorp.scienceboard.MobileNavigationDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentTechTabBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import java.util.ArrayList;
import java.util.List;

public class TechTabFragment extends Fragment implements
        RecyclerAdapterArticlesList.OnArticleClickedListener {
    private final String TAG = this.getClass().getSimpleName();
    private TechTabViewModel techTabViewModel;
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private View view;
    private AdProvider adProvider;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FragmentTechTabBinding binding;
    private Toast toast;
    private SourceViewModel sourceViewModel;
    private List<Source> sourcesFetched;
    private final int NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE = 1;
    private final int AD_DISTANCE = 5; // distance between teh ads (in terms of items)
    private List<ListItem> articlesToDisplay;
    private boolean isLoading = false;




    //--------------------------------------------------------------------- CONSTRUCTORS

    public static TechTabFragment newInstance() {
        return new TechTabFragment();
    }



    //--------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTechTabBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        Log.d(TAG, "SCIENCE_BOARD - onCreateView: called");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initiView();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "SCIENCE_BOARD - onActivityCreated: called");
        sourceViewModel.getObservableAllSources().observe(getViewLifecycleOwner(), sources -> {
            Log.d(TAG, "SCIENCE_BOARD - observe: called");
            if(sources!=null && !sources.isEmpty()) {
                // TODO
                this.sourcesFetched = new ArrayList<>(sources);
                techTabViewModel.fetchArticles(sources, NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE, false);
            }
        });

        techTabViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            swipeRefreshLayout.setRefreshing(false);
            progressIndicator.setVisibility(View.GONE);

            if(resultArticles==null || resultArticles.isEmpty()) {
//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else {
                resultArticles = adProvider.populateListWithAds(resultArticles, AD_DISTANCE);
                articlesToDisplay = resultArticles;
                recyclerAdapterArticlesList.loadNewData(articlesToDisplay);
                showCenteredToast("articles fetched");
                isLoading = false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }





    //--------------------------------------------------------------------- METHODS

    private void initiView() {
        progressIndicator = binding.progressIndicatorTechTabFragment;
        swipeRefreshLayout = binding.swipeRefreshTechTabFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        sourceViewModel = new ViewModelProvider(requireActivity()).get(SourceViewModel.class);
        techTabViewModel = new ViewModelProvider(this).get(TechTabViewModel.class);
        initRecycleView();
        setupSwipeDownToRefresh();
    }

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = binding.recyclerViewTechTabFragment;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
        initScrollListener();
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() == articlesToDisplay.size() - 1) {
                        //bottom of list!
                        Log.d(TAG, "SCIENCE_BOARD - loadMore: reached the end of the recycler");
                        loadMore();
                    }
                }
            }
        });
    }

    private void loadMore() {
        isLoading = true;

        // adding loading view
        observerNextArticlesFetch();
        recyclerAdapterArticlesList.addLoadingView(articlesToDisplay);

        // TODO: move into viewmodel ?
        // load new items (asynchronously)
        techTabViewModel.fetchNextArticles(NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE);
    }

    private void observerNextArticlesFetch() {
        techTabViewModel.getObservableNextArticlesList().observe(getViewLifecycleOwner(), fetchedArticles -> {
            if(fetchedArticles==null || fetchedArticles.isEmpty()) {
//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else {
                recyclerAdapterArticlesList.removeLoadingView(articlesToDisplay);
                fetchedArticles = adProvider.populateListWithAds(fetchedArticles, AD_DISTANCE);
                articlesToDisplay = fetchedArticles;
                recyclerAdapterArticlesList.loadNewData(articlesToDisplay);
                isLoading = false;
            }
        });
    }


    private void setupSwipeDownToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
            refreshAction();
        });
    }

    private void refreshAction() {
        techTabViewModel.fetchArticles(sourcesFetched, NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE, true);
    }

    @Override
    public void onArticleClicked(int position) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            techTabViewModel.smartSaveInHistory(article);
            MobileNavigationDirections.ActionGlobalWebviewFragment action =
                    MobileNavigationDirections.actionGlobalWebviewFragment(article);
            Navigation.findNavController(view).navigate(action);
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
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT);
        toast.show();
    }



}// end TechTabFragment