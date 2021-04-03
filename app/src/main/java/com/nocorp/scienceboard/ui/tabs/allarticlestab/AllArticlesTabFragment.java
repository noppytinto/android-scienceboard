package com.nocorp.scienceboard.ui.tabs.allarticlestab;

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
import com.nocorp.scienceboard.databinding.AllArticlesTabFragmentBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterFeedsList;
import com.nocorp.scienceboard.repository.FeedProvider;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import java.util.ArrayList;
import java.util.List;

public class AllArticlesTabFragment extends Fragment implements FeedProvider.OnFeedsDownloadedListener, RecyclerAdapterFeedsList.OnArticleClickedListener {
    private final String TAG = this.getClass().getSimpleName();
    private RecyclerAdapterFeedsList recyclerAdapterFeedsList;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private AllArticlesTabViewModel viewModel;
    private View view;
    private AdProvider adProvider;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AllArticlesTabFragmentBinding binding;
    private FeedProvider feedProvider;
    private static boolean feedLoadedAtStartup = false;
    private boolean feedsLoading = false;
    private Toast toast;

    public static AllArticlesTabFragment newInstance() {
        return new AllArticlesTabFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AllArticlesTabFragmentBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressIndicator = binding.progressIndicatorAllArticlesTabFragment;
        swipeRefreshLayout = binding.swipeRefreshAllArticlesTabFragment;
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        feedProvider = new FeedProvider(this);
        viewModel = new ViewModelProvider(this).get(AllArticlesTabViewModel.class);
        initRecycleView();
        setupSwipeDownToRefresh();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), articles -> {
            if(articles==null || articles.size()==0) {
                swipeRefreshLayout.setRefreshing(false);
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "An error occurred during articles fetch, contact the developer.", Toast.LENGTH_SHORT).show();
            }
            else {
                swipeRefreshLayout.setRefreshing(false);
                feedsLoading = false;
                progressIndicator.setVisibility(View.GONE);
                articles = adProvider.populateListWithAds(articles, 5);
                recyclerAdapterFeedsList.loadNewData(articles);
            }
        });

//        if(feedLoadedAtStartup == false) {
//            feedProvider.downloadRssSources_dom();
//            feedLoadedAtStartup = true;
//            feedsLoading = true;
//        }

        feedProvider.downloadRssSources_dom(requireContext());
        feedLoadedAtStartup = true;
        feedsLoading = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }





    //---------------------------------------------------------------------

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = binding.recyclerViewAllArticlesTabFragment;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterFeedsList = new RecyclerAdapterFeedsList(new ArrayList<>(), requireContext(), this);
        recyclerView.setAdapter(recyclerAdapterFeedsList);
    }

    private void setupSwipeDownToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                if( ! feedsLoading) updateFeeds();
            }
        });
    }


    private void updateFeeds() {
        feedProvider.downloadRssSources_dom(requireContext());
    }

    @Override
    public void onFeedsDownloadCompleted(List<Source> sources) {
        viewModel.fetchArticles(sources);
        Log.d(TAG, "SCIENCE_BOARD - onFeedsDownloadCompleted: feeds fetched");

        // this (runOnUiThread) is unstable, can cause crashes, so better not use it
        runToastOnUiThread("feeds fetched");
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

    @Override
    public void onFeedsDownloadFailed(String cause) {
//        viewModel.setArticlesList(null);

        // this (runOnUiThread) is unstable, can cause crashes, so better not use it
        Log.d(TAG, "SCIENCE_BOARD - onFeedsDownloadFailed: feeds not fetched, cause: $cause");
//        requireActivity().runOnUiThread(() ->
//                Toast.makeText(requireContext(), "Cannot fetch feeds, contact the developer.\n$cause", Toast.LENGTH_SHORT).show()
//        );
    }


    @Override
    public void onArticleClicked(int position) {
        Article article = (Article) recyclerAdapterFeedsList.getItem(position);
        if(article!=null) {
            String url = article.getWebpageUrl();
            String sourceLogoUrl = article.getSource().getLogoUrl();

            if(url!=null || !url.isEmpty()) {
                MobileNavigationDirections.ActionGlobalWebviewFragment action =
                        MobileNavigationDirections.actionGlobalWebviewFragment(url, sourceLogoUrl);
                Navigation.findNavController(view).navigate(action);
            }
        }
    }



}// end AllArticlesTabFragment