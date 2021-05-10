package com.nocorp.scienceboard.ui.topicfeeds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nocorp.scienceboard.NavGraphDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksListOnChangedListener;
import com.nocorp.scienceboard.databinding.FragmentTopicFeedsBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterArticlesList;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.topics.repository.TopicRepository;
import com.nocorp.scienceboard.ui.bookmarks.BookmarksViewModel;
import com.nocorp.scienceboard.ui.timemachine.OnDateChangedListener;
import com.nocorp.scienceboard.ui.timemachine.TimeMachineViewModel;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.ui.webview.WebviewViewModel;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class TopicFeedsFragment extends Fragment implements
        RecyclerAdapterArticlesList.OnArticleClickedListener,
        OnDateChangedListener,
        BookmarksListOnChangedListener
{

    private final String TAG = this.getClass().getSimpleName();
    private FragmentTopicFeedsBinding viewBinding;
    private View view;
    private CircularProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toast toast;
    private ImageView toolbarImage;
    private MaterialToolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private FloatingActionButton switchTopicFAB;

    // recycler
    private RecyclerAdapterArticlesList recyclerAdapterArticlesList;
    private RecyclerView recyclerViewArticles;

    // viewmodels
    private TopicFeedsViewModel topicFeedsViewModel;
    private SourceViewModel sourceViewModel;
    private TimeMachineViewModel timeMachineViewModel;
    private WebviewViewModel webviewViewModel;
    private BookmarksViewModel bookmarksViewModel;

    //
    private AdProvider adProvider;
    private List<Source> sourcesFetched;
    private List<ListItem> articlesToDisplay;
    private Topic currentTopic;
    private final int AD_DISTANCE = 5; // distance between ads (in terms of items)
    private long currentDateInMillis;
    private boolean recyclerIsLoading = false;
    // animations
    private int shortAnimationDuration;
    private boolean switchButtonIsVisible;
    // statusbar color
    private int previousStatusBarColor;
    private int previousNavigationIcon;
    // cover dominant colors
    private int backgroundDominantColor;
    private int titleDominantoColor;





    //--------------------------------------------------------------------------------------------- CONSTRUCTORS

    public static TopicFeedsFragment newInstance() {
        Log.d(TopicFeedsFragment.class.getSimpleName(), "SCIENCE_BOARD - newInstance: called");
        return new TopicFeedsFragment();
    }




    //--------------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentTopicFeedsBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        Log.d(TAG, "SCIENCE_BOARD - onCreateView: called");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initiView();

        Bundle arguments = getArguments();

        if(arguments!=null) {
            currentTopic = TopicFeedsFragmentArgs.fromBundle(arguments).getTopicArgument();

            String displayName = currentTopic.getDisplayName();
            toolbar.setTitle(displayName);

            observeSourcesFetched();
            observeArticlesFetched();
            observerNextArticlesFetch();
            observeTimeMachineStatus();
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        super.onDestroyView();
        viewBinding = null;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        restoreStatusbarColor();
//        topicFeedsViewModel.setArticlesList(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTransparentStatusBar();
    }

    //    @Override
//    public void onDetach() {
//        Log.d(TAG, "onDetach: ");
//        super.onDetach();
//        restoreToolbarColor();
//    }





    //--------------------------------------------------------------------------------------------- METHODS

    private void initiView() {
        progressIndicator = viewBinding.progressIndicatorTopicFeedsFragment;
        swipeRefreshLayout = viewBinding.swipeRefreshTopicFeedsFragment;
        swipeRefreshLayout.setColorSchemeResources(R.color.orange_light);
        recyclerViewArticles = viewBinding.recyclerViewTopicFeedsFragment;
        toolbarImage = viewBinding.imageViewTopicFeedsFragmentAppBar;
        toolbar = viewBinding.toolbarTopicFeedsFragment;
        collapsingToolbar = viewBinding.collapsingToolbarTopicFeedsFragment;
        initToolbar(toolbar);
        switchTopicFAB = viewBinding.floatingActionButtonTopicFeedsFragmentSwitchTopic;
        switchTopicFAB.setOnClickListener(v -> showSwitchTopicDialog());

        //
        currentDateInMillis = System.currentTimeMillis();
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // viewmodels
        topicFeedsViewModel = new ViewModelProvider(this).get(TopicFeedsViewModel.class);
        sourceViewModel = new ViewModelProvider(requireActivity()).get(SourceViewModel.class);
        timeMachineViewModel = new ViewModelProvider(requireActivity()).get(TimeMachineViewModel.class);
        bookmarksViewModel = new ViewModelProvider(requireActivity()).get(BookmarksViewModel.class);
        bookmarksViewModel.setBookmarksListOnChangedListener(this);
        webviewViewModel = new ViewModelProvider(requireActivity()).get(WebviewViewModel.class);
        webviewViewModel.setBookmarksListOnChangedListener(this);

        //
        initRecycleView(recyclerViewArticles);
        setupScrollListener(recyclerViewArticles);
        setupSwipeDownToRefresh(swipeRefreshLayout);
    }




    //---------------------------------------------------------- observing viewmodels

    private void observeSourcesFetched() {
        sourceViewModel.getObservableAllSources().observe(getViewLifecycleOwner(), resultSources -> {
            Log.d(TAG, "observeSourcesFetched: called");
            if(resultSources == null) {
                //TODO: error message
                Log.e(TAG, "SCIENCE_BOARD - loadSources: an error occurrend when fetching sources");
                showCenteredToast("an error occurred when fetching sources from server");
            }
            else if(resultSources.isEmpty()) {
                //TODO: warning message, no topics in memory
                Log.w(TAG, "SCIENCE_BOARD - loadSources: no sources in DB");
            }
            else {
                // getting enabled sources
                sourcesFetched = sourceViewModel.getEnabledSources();


                // init starting date
                long startingDate = initStartingDate();

                // fetching articles
                topicFeedsViewModel.fetchArticles(sourcesFetched,
                                                  false,
                                                  currentTopic.getId(),
                                                  startingDate);
            }
        });
    }

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

    private List<Source> extractEnabledSources(List<Source> sourcesFetched) {
        List<Source> result = new ArrayList<>();

        for(Source currentSource: sourcesFetched) {
            if(currentSource.getEnabled())
                result.add(currentSource);
        }

        return result;
    }

    private void observeArticlesFetched() {
        topicFeedsViewModel.getObservableArticlesList().observe(getViewLifecycleOwner(), resultArticles -> {
            Log.d(TAG, "getObservableArticlesList: called");
            swipeRefreshLayout.setRefreshing(false);
            progressIndicator.setVisibility(View.GONE);

            if(resultArticles==null) {
                recyclerIsLoading = false;
//                showCenteredToast(getString(R.string.string_articles_fetch_fail_message));// TODO: change message, do not refer to developer
            }
            else if(resultArticles.isEmpty()) {
                recyclerIsLoading = false;
                // TODO this should only be called when the list is empty,
                // when errors occurs should be the case above

                articlesToDisplay = new ArrayList<>();
                recyclerAdapterArticlesList.clearList();
            }
            else {
                setImageToToolbar(toolbarImage, resultArticles, toolbar);
                resultArticles = adProvider.populateListWithAds(resultArticles, AD_DISTANCE);
                articlesToDisplay = new ArrayList<>(resultArticles);
                recyclerAdapterArticlesList.loadNewData(articlesToDisplay);
                recyclerIsLoading = false;
            }
        });
    }

    private void setImageToToolbar(ImageView imageView, List<ListItem> articles, Toolbar toolbar) {
        List<ListItem> randomizedList = new ArrayList<>(articles);
        Collections.shuffle(randomizedList);

        Article randomArticle = (Article) randomizedList.get(0);// get the first one
        String thumbnailUrl = randomArticle.getThumbnailUrl();

        try {
            RequestOptions gildeOptions = new RequestOptions()
                    .fallback(R.drawable.placeholder_image)
                    .placeholder(R.drawable.placeholder_image)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop();

            Glide.with(requireContext())
                    .load(thumbnailUrl)
                    .apply(gildeOptions)
//                    .thumbnail(/*sizeMultiplier = 0.25% less than original*/ THUMBNAIL_SIZE_MULTIPLIER)
                    .transition(withCrossFade())
                    .into(imageView);

//            RequestOptions gildeOptions = new RequestOptions()
//                    .fallback(R.drawable.placeholder_image)
//                    .placeholder(R.drawable.placeholder_image)
//                    .centerCrop();
//
//            Glide.with(requireContext())
//                    .asBitmap()
//                    .load(thumbnailUrl)
//                    .apply(gildeOptions)
//                    .into(new CustomTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            imageView.setImageBitmap(resource);
//                            applyDominantoColor(resource, toolbar);
//                        }
//
//                        @Override
//                        public void onLoadCleared(@Nullable Drawable placeholder) {
////                            holder.thumbnail.setImageDrawable(placeholder);
//                        }
//
//                        @Override
//                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
//                            super.onLoadFailed(errorDrawable);
//                            imageView.setImageDrawable(errorDrawable);
//                        }
//                    });


        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - setImageToToolbar: cannot set thumbnail in toolbar, cause: " + e.getMessage());
        }
    }

    private void applyDominantoColor(@NonNull Bitmap resource, @NonNull Toolbar toolbar) {
        Palette myPalette = createPaletteSync(resource);
        Palette.Swatch swatch = myPalette.getDominantSwatch();
        if(swatch != null){
            titleDominantoColor = swatch.getBodyTextColor();
            backgroundDominantColor = swatch.getRgb();

//            collapsingToolbar.setContentScrimColor(backgroundDominantColor);
//            collapsingToolbar.setCollapsedTitleTextColor(titleDominantoColor);
        }
    }

    private void setDominantColors(@NonNull Toolbar toolbar, Palette.Swatch vibrant) {
        int titleColor = vibrant.getBodyTextColor();
        toolbar.setTitleTextColor(titleColor);

        int backgroundColor = vibrant.getRgb();
        toolbar.setBackgroundColor(backgroundColor);
    }

    private void observerNextArticlesFetch() {
        topicFeedsViewModel.getObservableNextArticlesList().observe(getViewLifecycleOwner(), fetchedArticles -> {
            recyclerIsLoading = false;
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

    // Generate palette synchronously and return it
    public Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }

    private void observeTimeMachineStatus() {
        timeMachineViewModel.getObservablePickedDate().observe(getViewLifecycleOwner(), pickedDate -> {
            if(timeMachineViewModel.isDateChanged()) {
                Log.d(TAG, "observeTimeMachineStatus: using time machine");
                timeMachineViewModel.setDateChanged(false);
                recyclerAdapterArticlesList.clearList();
                refreshArticles();
            }
        });
    }





    //---------------------------------------------------------- listeners

    @Override
    public void onArticleClicked(int position, View itemView) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            topicFeedsViewModel.saveInHistory(article);
            article.setVisited(true);

            NavGraphDirections.ActionGlobalWebviewFragment action =
                    NavGraphDirections.actionGlobalWebviewFragment(article);
            Navigation.findNavController(view).navigate(action);
        }
    }

    @Override
    public void onBookmarksButtonClicked(int position) {
        Article article = (Article) recyclerAdapterArticlesList.getItem(position);
        if(article!=null) {
            if(article.isBookmarked()) {
                article.setBookmarked(false);
                topicFeedsViewModel.removeFromBookmarks(article);
            }
            else {
                article.setBookmarked(true);
                topicFeedsViewModel.addToBookmarks(article);
            }
        }
    }

    @Override
    public void onDateChanged(long date) {

    }

    @Override
    public void onBookmarksListChanged() {
        topicFeedsViewModel.asyncBookmarksCheck(recyclerAdapterArticlesList.getAllItems(), () -> {
            recyclerAdapterArticlesList.notifyDataSetChanged();
        });
    }

    private void showSwitchTopicDialog() {
        List<Topic> topics = TopicRepository.getFollowedTopics();
        if(topics==null) return;

        // remove the current topic destinantion
        List<Topic> followedTopics = new ArrayList<>(topics);
        followedTopics.remove(currentTopic);

        //
        List<String> list = buildMyTopicsChoices(followedTopics);
        if(list==null || list.isEmpty()) return;

        //
        CharSequence[] items = list.toArray(new CharSequence[0]);
//        String[] items = new String[stockList.size()];
//        items = stockList.toArray(items);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.switch_topic_dialog_label)
                .setItems(items, (dialog, which) -> {

                    Topic topicChosen = followedTopics.get(which);
                    if(topicChosen!=null) {
                        Log.d(TAG, "showSwitchTopicDialog: choice: " + topicChosen.getDisplayName());

                        NavGraphDirections.ActionGlobalTopicFeedsFragment action =
                                NavGraphDirections.actionGlobalTopicFeedsFragment(topicChosen);
                        Navigation.findNavController(view).popBackStack();
                        Navigation.findNavController(view).navigate(action);
                    }

                })
                .setNegativeButton(R.string.newgative_button_message_switch_topic_dialog,
                        (dialog, which) -> dialog.dismiss())
                .show();
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






    //----------------------------------------------------------

    private void initRecycleView(RecyclerView recyclerView) {
        // defining Recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterArticlesList = new RecyclerAdapterArticlesList(new ArrayList<>(), this, null);
        recyclerView.setAdapter(recyclerAdapterArticlesList);
    }

    private void initToolbar(Toolbar toolbar) {
        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        // set transparent toolbar
//        setTransparentStatusBar();
    }

    private void setTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "setTransparentStatusBar: called");
            Window window = requireActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            previousStatusBarColor = window.getStatusBarColor();
            window.setStatusBarColor(Color.TRANSPARENT);
            Log.d(TAG, "setTransparentStatusBar: previousStatusBarColor: " + previousStatusBarColor);
        }
    }

    private void restoreStatusbarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "restoreStatusbarColor: called");
            Window window = requireActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(previousStatusBarColor);
            Log.d(TAG, "restoreStatusbarColor: previousStatusBarColor: " + previousStatusBarColor);
        }
    }

    private void setupScrollListener(RecyclerView recyclerView) {
//        if (nestedScrollView != null) {
//            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
//                    (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//                        if (scrollY > oldScrollY) {
////                    Log.d(TAG, "Scroll DOWN");
////                            viewIsVisibleInLayout(nestedScrollView, recyclerViewTopics);
//                        }
//                        if (scrollY < oldScrollY) {
////                    Log.d(TAG, "Scroll UP");
////                            viewIsVisibleInLayout(nestedScrollView, recyclerViewTopics);
//                        }
//
////                if (scrollY == 0) {
////                    Log.d(TAG, "TOP SCROLL");
////                }
//
//                        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
//                            Log.d(TAG, "setupEndlessScroll: BOTTOM SCROLL");
//                            if ( ! recyclerIsLoading) {
//                                loadMoreArticles();
//                            }
//                        }
//                    });
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
                (articlesToDisplay != null && !articlesToDisplay.isEmpty()) &&
                linearLayoutManager.findLastCompletelyVisibleItemPosition() == articlesToDisplay.size() - 1;
    }

    private void loadMoreArticles() {
        Log.d(TAG, "loadMoreArticles: called");
        recyclerIsLoading = true;

        // adding loading view
        recyclerAdapterArticlesList.addLoadingView(articlesToDisplay);

        // load new items
        topicFeedsViewModel.fetchNextArticles();
    }

    private void setupSwipeDownToRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
            refreshArticles();
        });
    }

    private void refreshArticles() {
        long startingDate = currentDateInMillis;

        if (timeMachineViewModel.timeMachineIsEnabled()) {
            startingDate = timeMachineViewModel.getPickedDate();
        }

        topicFeedsViewModel.fetchArticles(sourcesFetched,
                                          true,
                                          currentTopic.getId(),
                                          startingDate);
    }




    //---------------------------------------------------------------------------------------- UTILITY METHODS

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
//                applyCrossfadeExit(switchTopicButton, shortAnimationDuration);
                switchButtonIsVisible = false;
            }
        } else {
            // view is not within the visible window
//            Log.d(TAG, "viewIsVisibleInLayout: view is NOT visible");
            if( ! switchButtonIsVisible) {
                Log.d(TAG, "viewIsVisibleInLayout: switch button is now visible");
//                applyCrossfadeEnter(switchTopicButton, shortAnimationDuration);
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
    @NotNull
    private Calendar convertMillisInCalendar(Long pickedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pickedDate);
        return cal;
    }



}// end TopicFeedsFragment