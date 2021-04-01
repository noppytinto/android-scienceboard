package com.nocorp.scienceboard.ui.tabs.allarticlestab;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nocorp.scienceboard.MobileNavigationDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterFeedsList;
import com.nocorp.scienceboard.repository.FeedProvider;
import com.nocorp.scienceboard.ui.home.HomeViewModel;
import com.nocorp.scienceboard.utility.AdProvider;

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

    public static AllArticlesTabFragment newInstance() {
        return new AllArticlesTabFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.all_articles_tab_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        progressIndicator = view.findViewById(R.id.progressIndicator_allArticlesTabFragment);
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        initRecycleView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AllArticlesTabViewModel.class);

        viewModel = new ViewModelProvider(this).get(AllArticlesTabViewModel.class);
        viewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), articles -> {
            if(articles==null || articles.size()==0) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "An error occurred during articles fetch, contact the developer.", Toast.LENGTH_SHORT).show();
            }
            else {
                progressIndicator.setVisibility(View.GONE);
                articles = adProvider.populateListWithAds(articles, 5);
                recyclerAdapterFeedsList.loadNewData(articles);
            }
        });

        FeedProvider feedProvider = new FeedProvider(this);
        feedProvider.downloadRssSources();
    }



    //---------------------------------------------------------------------

    private void initRecycleView(View view) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_allArticlesTabFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterFeedsList = new RecyclerAdapterFeedsList(new ArrayList<>(), requireContext(), this);
        recyclerView.setAdapter(recyclerAdapterFeedsList);
    }

    @Override
    public void onFeedsDownloadCompleted(List<Source> sources) {
        viewModel.fetchArticles(sources);
        Log.d(TAG, "SCIENCE_BOARD - onFeedsDownloadFailed: feeds fetched");

        // this (runOnUiThread) is unstable, can cause crashes, so better not use it
//        requireActivity().runOnUiThread(() ->
//                Toast.makeText(requireContext(), "feeds fetched", Toast.LENGTH_SHORT).show()
//        );
    }

    @Override
    public void onFeedsDownloadFailed(String cause) {
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
            if(url!=null || !url.isEmpty()) {
                MobileNavigationDirections.ActionGlobalWebviewFragment action =
                        MobileNavigationDirections.actionGlobalWebviewFragment(url);
                Navigation.findNavController(view).navigate(action);
            }
        }
    }



}// end AllArticlesTabFragment