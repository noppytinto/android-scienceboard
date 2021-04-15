package com.nocorp.scienceboard.ui.tabs.alltab;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nocorp.scienceboard.MobileNavigationDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentAllTabBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.LoadingViewItem;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import java.util.ArrayList;
import java.util.List;

public class AllTabFragment extends Fragment implements
        RecyclerAdapterArticlesList.OnArticleClickedListener {
    private final String TAG = this.getClass().getSimpleName();
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private AllTabViewModel allTabViewModel;
    private View view;
    private AdProvider adProvider;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FragmentAllTabBinding binding;
    private Toast toast;
    private SourceViewModel sourceViewModel;
    private List<Source> sources;
    private final int NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE = 3;
    private boolean isLoading = false;
    private List<ListItem> articlesToDisplay;




    //--------------------------------------------------------------------- CONSTRUCTORS

    public static AllTabFragment newInstance() {
        return new AllTabFragment();
    }





    //--------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAllTabBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sourceViewModel.getObservableAllSources().observe(getViewLifecycleOwner(), sources -> {
            if(sources!=null && sources.size()>0) {
                // TODO
                this.sources = sources;
                allTabViewModel.fetchArticles(sources, NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE, false);
            }
        });

        allTabViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            if(resultArticles==null || resultArticles.size()==0) {
                swipeRefreshLayout.setRefreshing(false);
                progressIndicator.setVisibility(View.GONE);
//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else {
                swipeRefreshLayout.setRefreshing(false);
                progressIndicator.setVisibility(View.GONE);
                resultArticles = adProvider.populateListWithAds(resultArticles, 5);
                articlesToDisplay = new ArrayList<>(resultArticles);
                recyclerAdapterArticlesList.loadNewData(articlesToDisplay);
                showCenteredToast("articles fetched");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }





    //--------------------------------------------------------------------- METHODS

    private void initView() {
        progressIndicator = binding.progressIndicatorAllArticlesTabFragment;
        swipeRefreshLayout = binding.swipeRefreshAllArticlesTabFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        sourceViewModel = new ViewModelProvider(requireActivity()).get(SourceViewModel.class);
        allTabViewModel = new ViewModelProvider(this).get(AllTabViewModel.class);
        initRecycleView();
        setupSwipeDownToRefresh();
    }

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = binding.recyclerViewAllArticlesTabFragment;
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
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == articlesToDisplay.size() - 1) {
                        //bottom of list!
                        Log.d(TAG, "SCIENCE_BOARD - loadMore: reached the end of the recycler");
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });
    }

    private void loadMore() {
        // adding loading view
        recyclerAdapterArticlesList.addLoadingView(articlesToDisplay);

        // TODO: load new items (asynchronously)
        // recyclerAdapterArticlesList.notifyDataSetChanged();

        // TODO: removing loading view
//        recyclerAdapterArticlesList.removeLoadingView(articlesToDisplay);


//        final Handler handler = new Handler();
//        handler.postDelayed(() -> {
//            // Do something after 5s
//
//        }, 5000);


//        int currentSize = scrollPosition;
//        int nextLimit = currentSize + 10;
//
//        //
//        while (currentSize - 1 < nextLimit) {
////            articlesToDisplay.add("Item " + currentSize); //TODO
//            currentSize++;
//        }

        isLoading = false;

        //TODO
    }

    private void setupSwipeDownToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
            refreshAction();
        });
    }

    private void refreshAction() {
        allTabViewModel.fetchArticles(sources, NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE, true);
    }

    @Override
    public void onArticleClicked(int position) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            allTabViewModel.smartSaveInHistory(article);
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

}// end AllArticlesTabFragment