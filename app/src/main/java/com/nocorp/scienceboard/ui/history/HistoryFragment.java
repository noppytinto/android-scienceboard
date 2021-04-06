package com.nocorp.scienceboard.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nocorp.scienceboard.MobileNavigationDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentHistoryBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;

import java.util.ArrayList;

public class HistoryFragment extends Fragment implements
        RecyclerAdapterArticlesList.OnArticleClickedListener  {
    private final String TAG = this.getClass().getSimpleName();
    private HistoryViewModel historyViewModel;
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FragmentHistoryBinding binding;
    private Toast toast;
    private View view;
    private CircularProgressIndicator progressIndicator;



    //--------------------------------------------------------------------- CONSTRUCTORS

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }



    //--------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressIndicator = binding.progressIndicatorHistoryFragment;
        swipeRefreshLayout = binding.swipeRefreshHistoryFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        initRecycleView();
        setupSwipeDownToRefresh();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        historyViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), articles -> {
            if(articles==null || articles.size()==0) {
                swipeRefreshLayout.setRefreshing(false);
                progressIndicator.setVisibility(View.GONE);
                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else {
                swipeRefreshLayout.setRefreshing(false);
                progressIndicator.setVisibility(View.GONE);
                recyclerAdapterArticlesList.loadNewData(articles);
                showCenteredToast("history fetched");
            }
        });

        historyViewModel.fetchHistory(0);
    }




    //--------------------------------------------------------------------- My METHODS

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = binding.recyclerViewHistoryFragment;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
    }

    private void setupSwipeDownToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.i(TAG, "SCIENCE_BOARD - onRefresh called from SwipeRefreshLayout");
            refreshAction();
        });
    }

    private void refreshAction() {
        // TODO
    }

    @Override
    public void onArticleClicked(int position) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            String url = article.getWebpageUrl();
            String sourceName = article.getSourceName();

            if(url!=null && !url.isEmpty()) {
                MobileNavigationDirections.ActionGlobalWebviewFragment action =
                        MobileNavigationDirections.actionGlobalWebviewFragment(url, sourceName);
                Navigation.findNavController(view).navigate(action);
            }
        }
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


}// end HistoryFragment