package com.nocorp.scienceboard.ui.bookmarks;

import android.os.Bundle;
import android.util.Log;
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
import com.nocorp.scienceboard.MainActivity;
import com.nocorp.scienceboard.NavGraphDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentBookmarksBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookmarksFragment extends Fragment implements
        RecyclerAdapterArticlesList.OnArticleClickedListener {
    private final String TAG = this.getClass().getSimpleName();
    private BookmarksViewModel bookmarksViewModel;
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FragmentBookmarksBinding viewBinding;
    private Toast toast;
    private View view;
    private CircularProgressIndicator progressIndicator;

    //

    //
    private MenuItem deleteMenuItem;
    private MenuItem selectAllMenuItem;
    private MenuItem editMenuItem;
    private MenuItem exitMenuItem;

    //
    private boolean editModeEnabled;
    private boolean atLeastOneItemSelected;
    private boolean allItemsSelected;

    //
    private Set<Article> selectedItems;


    //--------------------------------------------------------------------- CONSTRUCTORS

    public static BookmarksFragment newInstance() {
        return new BookmarksFragment();
    }





    //--------------------------------------------------------------------- ANDROID METHODS


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentBookmarksBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

        selectedItems = new HashSet<>();

        bookmarksViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), articles -> {
            swipeRefreshLayout.setRefreshing(false);
            progressIndicator.setVisibility(View.GONE);

            if(articles==null || articles.isEmpty()) {
                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else {
                recyclerAdapterArticlesList.loadNewData(articles);
                showCenteredToast("bookmarks fetched");
            }

            // update top menu items visibili
            requireActivity().invalidateOptionsMenu();
        });

        bookmarksViewModel.fetchBookmarks(0);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bookmarks_fragment, menu);
        selectAllMenuItem = menu.findItem(R.id.option_bookmarksMenu_selectAll);
        editMenuItem = menu.findItem(R.id.option_bookmarksMenu_edit);
        deleteMenuItem = menu.findItem(R.id.option_bookmarksMenu_delete);
        exitMenuItem = menu.findItem(R.id.option_bookmarksMenu_exit);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(recyclerAdapterArticlesList.getAllItems()==null ||
                recyclerAdapterArticlesList.getAllItems().isEmpty()) {
            editMenuItem.setVisible(false);
        }
        else {
            editMenuItem.setVisible(true);
            if(editModeEnabled) {
                selectAllMenuItem.setVisible(true);
                exitMenuItem.setVisible(true);
                editMenuItem.setVisible(false);

                if(atLeastOneItemSelected || allItemsSelected) {
                    deleteMenuItem.setVisible(true);
                }
                else {
                    deleteMenuItem.setVisible(false);
                }
            }
            else {
                selectAllMenuItem.setVisible(false);
                deleteMenuItem.setVisible(false);
                exitMenuItem.setVisible(false);
                editMenuItem.setVisible(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.option_bookmarksMenu_edit) {
            startEditingModeAction();
            return true;
        }
        else if(itemId == R.id.option_bookmarksMenu_selectAll) {
            selectAllItemsAction();
            return true;
        }
        else if(itemId == R.id.option_bookmarksMenu_delete) {
            new MaterialAlertDialogBuilder(requireContext(), R.style.ScieceBoard_Dialog_MaterialAlertDialog)
                    .setTitle("Do you want delete selected items?")
                    .setPositiveButton("yes", (dialog, listener) -> {
                        removeArticlesFromBookmarks();
                        dialog.dismiss();
                        showCenteredToast("items deleted");
                    })
                    .setNegativeButton("no", (dialog, listener)-> {
                        dialog.dismiss();
                    })
                    .show();

            return true;
        }
        if(itemId == R.id.option_bookmarksMenu_exit) {
            stopEditingModeAction();
            return true;
        }

        return false;
    }




    //--------------------------------------------------------------------- My METHODS

    private void initView() {
        progressIndicator = viewBinding.progressIndicatorBookmarksFragment;
        swipeRefreshLayout = viewBinding.swipeRefreshBookmarksFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        bookmarksViewModel = new ViewModelProvider(requireActivity()).get(BookmarksViewModel.class);
        initRecycleView();
        setupSwipeDownToRefresh();
    }

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = viewBinding.recyclerViewBookmarksFragment;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this, null);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
    }

    private void setupSwipeDownToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "SCIENCE_BOARD - onRefresh called from SwipeRefreshLayout");
            refreshAction();
        });
    }

    private void refreshAction() {
        bookmarksViewModel.fetchBookmarks(0);
    }

    @Override
    public void onArticleClicked(int position, View itemView) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {

            if(editModeEnabled) {
                selectItem(article, position);
            }
            else {
                article.setSelected(false);
                openArticle(article);
            }
        }
    }

    private void openArticle(Article article) {
        NavGraphDirections.ActionGlobalWebviewFragment action =
                NavGraphDirections.actionGlobalWebviewFragment(article);
        Navigation.findNavController(view).navigate(action);
    }



    @Override
    public void onBookmarksButtonClicked(int position) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            if(editModeEnabled) {
                selectItem(article, position);
            }
        }
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


    //------- EDIT MODE

    private void selectAllItemsAction() {
        if(allItemsSelected) {
            deselectAllItems();
        }
        else {
            selectAllItems();
        }
    }

    private void selectAllItems() {
        allItemsSelected = true;
        atLeastOneItemSelected = true;
        requireActivity().invalidateOptionsMenu();
        recyclerAdapterArticlesList.selectAllItems();
        List<ListItem> articles = recyclerAdapterArticlesList.getAllItems();
        if(articles!=null) {
            for(ListItem article: articles) {
                selectedItems.add((Article) article);
            }
        }

        Log.d(TAG, "selectItem: " + selectedItems.toString());
        ((MainActivity)requireActivity()).changeToolbarTitle("Selected: " + selectedItems.size());

    }

    private void deselectAllItems() {
        allItemsSelected = false;
        atLeastOneItemSelected = false;
        requireActivity().invalidateOptionsMenu();
        recyclerAdapterArticlesList.deselectAllItems();
        selectedItems.clear();
        Log.d(TAG, "selectItem: " + selectedItems);
        ((MainActivity)requireActivity()).changeToolbarTitle("Selected: " + selectedItems.size());
    }

    private void startEditingModeAction() {
        editModeEnabled = true;
        requireActivity().invalidateOptionsMenu();
        recyclerAdapterArticlesList.enableEditMode();
        selectedItems.clear();
        ((MainActivity)requireActivity()).changeToolbarTitle("Selected: " + selectedItems.size());
    }

    private void stopEditingModeAction() {
        editModeEnabled = false;
        atLeastOneItemSelected = false;
        allItemsSelected = false;
        selectedItems.clear();
        requireActivity().invalidateOptionsMenu();
        recyclerAdapterArticlesList.disableEditMode();
        ((MainActivity)requireActivity()).changeToolbarTitle("Bookmarks");
    }

    private void selectItem(Article article, int position) {
        if(article.isSelected()) {
            article.setSelected(false);
            selectedItems.remove(article);

            if(selectedItems.isEmpty()) {
                allItemsSelected = false;
                atLeastOneItemSelected = false;
                requireActivity().invalidateOptionsMenu();
            }
        }
        else {
            article.setSelected(true);
            selectedItems.add(article);

            atLeastOneItemSelected = true;
            requireActivity().invalidateOptionsMenu();
        }
        recyclerAdapterArticlesList.notifyItemChanged(position);

        Log.d(TAG, "selectItem: " + selectedItems);
        ((MainActivity)requireActivity()).changeToolbarTitle("Selected: " + selectedItems.size());
    }

    public void removeArticlesFromBookmarks() {
        if(selectedItems==null) return;

        //
        List<Article> articlesToRemove = new ArrayList<>();
        for(ListItem article: selectedItems) {
            articlesToRemove.add((Article) article);
            recyclerAdapterArticlesList.removeItem(article);
        }

        // delete from room
        bookmarksViewModel.removeArticlesFromBookmarks(articlesToRemove);

        // update selected items list to update counter
        // TODO: improve this
        try {
            ((MainActivity)requireActivity()).changeToolbarTitle("Selected: " + (selectedItems.size() - articlesToRemove.size()));
        } catch (Exception e) {
            Log.e(TAG, "removeArticlesFromBookmarks: an error occurred when updateing selected items counter, cause: " + e.getMessage() );
        }

        // il bookmarks list is empty then exit from edito mode
        if(recyclerAdapterArticlesList.getAllItems()==null || recyclerAdapterArticlesList.getAllItems().isEmpty()){
            stopEditingModeAction();
            ((MainActivity)requireActivity()).changeToolbarTitle("Bookmarks");
        }

        selectedItems.clear();
        Log.d(TAG, "selectItem: " + selectedItems);
    }

}// end  BookmarksFragment