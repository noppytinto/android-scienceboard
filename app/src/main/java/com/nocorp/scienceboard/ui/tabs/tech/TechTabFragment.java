package com.nocorp.scienceboard.ui.tabs.tech;

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
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentTechTabBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TechTabFragment extends Fragment implements
        RecyclerAdapterArticlesList.OnArticleClickedListener
{
    private final String TAG = this.getClass().getSimpleName();
    private TechTabViewModel techTabViewModel;
    private FragmentTechTabBinding viewBinding;

    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private View view;
    private AdProvider adProvider;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toast toast;
    private SourceViewModel sourceViewModel;
    private List<Source> sourcesFetched;
    private final int NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE = 1;
    private final int AD_DISTANCE = 5; // distance between ads (in terms of items)
    private List<ListItem> articlesToDisplay;
    private boolean isLoading = false;




    //--------------------------------------------------------------------- CONSTRUCTORS

    public static TechTabFragment newInstance() {
        Log.d(TechTabFragment.class.getSimpleName(), "SCIENCE_BOARD - newInstance: called");
        return new TechTabFragment();
    }



    //--------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentTechTabBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        Log.d(TAG, "SCIENCE_BOARD - onCreateView: called");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initiView();

        observeSourcesFetched();
        observeArticlesFetched();
        observerNextArticlesFetch();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }





    //--------------------------------------------------------------------- METHODS

    private void initiView() {
        progressIndicator = viewBinding.progressIndicatorTechTabFragment;
        swipeRefreshLayout = viewBinding.swipeRefreshTechTabFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        sourceViewModel = new ViewModelProvider(requireActivity()).get(SourceViewModel.class);
        techTabViewModel = new ViewModelProvider(this).get(TechTabViewModel.class);
        initRecycleView();
        setupSwipeDownToRefresh();
    }

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = viewBinding.recyclerViewTechTabFragment;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this, null);
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
                        (articlesToDisplay != null && !articlesToDisplay.isEmpty()) &&
                        linearLayoutManager.findLastCompletelyVisibleItemPosition() == articlesToDisplay.size() - 1) {

                        // NOTE:
                        // this resolve the "cannot call this method in a scroll callback" problem
                        // it happens when we are adding elements while scrolling
                        recyclerView.post(new Runnable() {
                            public void run() {
                                //bottom of list!
                                Log.d(TAG, "SCIENCE_BOARD - initScrollListener: reached the end of the recycler");
                                loadMoreArticles();
                            }
                        });
                    }
                }
            }
        });
    }

    private void loadMoreArticles() {
        isLoading = true;

        // adding loading view
        recyclerAdapterArticlesList.addLoadingView(articlesToDisplay);

        // load new items
        techTabViewModel.fetchNextArticles(NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE);
    }

    private void observerNextArticlesFetch() {
        techTabViewModel.getObservableNextArticlesList().observe(getViewLifecycleOwner(), fetchedArticles -> {
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

    private void observeArticlesFetched() {
        techTabViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            swipeRefreshLayout.setRefreshing(false);
            progressIndicator.setVisibility(View.GONE);

            if(resultArticles==null || resultArticles.isEmpty()) {
                isLoading = false;
//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
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

    private void observeSourcesFetched() {
        sourceViewModel.getObservableAllSources().observe(getViewLifecycleOwner(), sources -> {
            if(sources!=null && !sources.isEmpty()) {
                // TODO
                this.sourcesFetched = new ArrayList<>(sources);
                techTabViewModel.fetchArticles(sources, NUM_ARTICLES_TO_FETCH_FOR_EACH_SOURCE, false);
            }
        });
    }

    /**
     * PRECONDITIONS:
     * the givenDate is guaranteed to be >0
     */
    private boolean equalsTheCurrentDate(Long givenDateInMillis) {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        Calendar cal = convertMillisInCalendar(givenDateInMillis);
        int year2 = cal.get(Calendar.YEAR);
        int month2 = cal.get(Calendar.MONTH);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);

        return (day==day2) && (month == month2) && (year == year2);
    }

    @NotNull
    private Calendar convertMillisInCalendar(Long pickedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pickedDate);
        return cal;
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
    public void onArticleClicked(int position, View itemView) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            techTabViewModel.saveInHistory(article);
//            MobileNavigationDirections.ActionGlobalWebviewFragment action =
//                    MobileNavigationDirections.actionGlobalWebviewFragment(article);
//            Navigation.findNavController(view).navigate(action);
        }
    }

    @Override
    public void onBookmarksButtonClicked(int position) {

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