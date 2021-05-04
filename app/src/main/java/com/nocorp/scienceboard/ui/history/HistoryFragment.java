package com.nocorp.scienceboard.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nocorp.scienceboard.NavGraphDirections;
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
    private View includeEmptyMessage;

    //
    private MenuItem deleteMenuItem;

    //
    private boolean listIsEmpty;


    //--------------------------------------------------------------------- CONSTRUCTORS

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }



    //--------------------------------------------------------------------- ANDROID METHODS


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

        historyViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), articles -> {
            if(articles==null || articles.size()==0) {
                includeEmptyMessage.setVisibility(View.VISIBLE);
                recyclerAdapterArticlesList.clearList();
                swipeRefreshLayout.setRefreshing(false);
                progressIndicator.setVisibility(View.GONE);
                updateMenu(true);
//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else {
                includeEmptyMessage.setVisibility(View.GONE);
                updateMenu(false);
                swipeRefreshLayout.setRefreshing(false);
                progressIndicator.setVisibility(View.GONE);
                recyclerAdapterArticlesList.loadNewData(articles);
//                showCenteredToast(getString(R.string.string_history_fetched));
            }
        });

        historyViewModel.fetchHistory(0);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_history_fragment, menu);
        deleteMenuItem = menu.findItem(R.id.option_historyMenu_deleteAll);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(listIsEmpty) {
            deleteMenuItem.setVisible(false);
        }
        else {
            deleteMenuItem.setVisible(true);
        }
    }

    private void updateMenu(boolean listIsEmpty) {
        this.listIsEmpty = listIsEmpty;
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.option_historyMenu_deleteAll) {
            clearHistoryAction();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }





    //-------------------------------------------------------------- MY METHODS
    private void clearHistoryAction() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.ScieceBoard_Dialog_MaterialAlertDialog)
                .setTitle("Do you want clear the history?")
                .setPositiveButton("yes", (dialog, listener) -> {
                    //
                    clearHistory();
                    dialog.dismiss();
                    updateMenu(true);
                    showCenteredToast("history deleted");

                })
                .setNegativeButton("no", (dialog, listener)-> {
                    //
                    dialog.dismiss();
//                    showCenteredToast("operation aborted");

                })
                .show();
    }

    private void clearHistory() {
        historyViewModel.clearHistory();
        includeEmptyMessage.setVisibility(View.VISIBLE);
    }

    private void initView() {
        progressIndicator = binding.progressIndicatorHistoryFragment;
        swipeRefreshLayout = binding.swipeRefreshHistoryFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        includeEmptyMessage = view.findViewById(R.id.include_historyFragment_emptyMessage);

        // viewmodels
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        //
        initRecycleView();
        setupSwipeDownToRefresh();
    }

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = binding.recyclerViewHistoryFragment;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this, null);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
    }

    private void setupSwipeDownToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.i(TAG, "SCIENCE_BOARD - onRefresh called from SwipeRefreshLayout");
            refreshAction();
        });
    }

    private void refreshAction() {
        historyViewModel.fetchHistory(0);
    }

    @Override
    public void onArticleClicked(int position, View itemView) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if (article != null) {
            NavGraphDirections.ActionGlobalWebviewFragment action =
                    NavGraphDirections.actionGlobalWebviewFragment(article);
            Navigation.findNavController(view).navigate(action);
        }
    }

    @Override
    public void onBookmarksButtonClicked(int position) {

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