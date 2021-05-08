package com.nocorp.scienceboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nocorp.scienceboard.databinding.ActivityMainBinding;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.rss.repository.SourceRepository;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.system.ConnectionManager;
import com.nocorp.scienceboard.topics.repository.TopicRepository;
import com.nocorp.scienceboard.ui.timemachine.DatePickerFragment;
import com.nocorp.scienceboard.ui.timemachine.TimeMachineViewModel;
import com.nocorp.scienceboard.ui.topics.TopicsViewModel;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity
        implements NavController.OnDestinationChangedListener {
    private final String TAG = this.getClass().getSimpleName();
    private NavController navController;
    private BottomNavigationView bottomNavBar;
    private ActivityMainBinding viewBinding;
    private View view;
    private Snackbar snackbar;
    private Toolbar toolbar;
    private ActionBar appBar;
    private Toast toast;
    private AppBarConfiguration appBarConfiguration;
//    private ImageView toolbarLogo;
    private TextView toolbarLogo;
    private Chip chipTimeMachine;
    private View toolbarInnerContainer;
    private FloatingActionButton timeMachineEnabledIndicator;

    //
    private AdProvider adProvider;
    private SourceViewModel sourceViewModel;
    private TopicsViewModel topicsViewModel;
    private TimeMachineViewModel timeMachineViewModel;

    //
    private final int DATE_PICKER_DEFAULT_CHIP_STROKE_WIDTH = 3;
    private final int DATE_PICKER_SET_CHIP_STROKE_WIDTH = 4;
    private long datePicked;
    private int MONTH_OFFSET_CORRECTION = 1;
    private final int NUM_ADS_TO_LOAD = 5;
    private final long ANIMATION_DURATION = 4000L;
    private ObjectAnimator objectAnimator;

    // repos
    private TopicRepository topicRepository;
    private SourceRepository sourceRepository;


    // rxjava
    private CompositeDisposable compositeDisposable;



    //------------------------------------------------------------------------------ ANDROID METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkDarkMode();
        super.onCreate(savedInstanceState);
        initView();
        initAdProvider(this, NUM_ADS_TO_LOAD);
        observeDatePickedFromTimeMachine();
        initAppContent();
    }

    private void checkDarkMode() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = getResources().getBoolean(R.bool.preference_app_theme_default_value_key);
        boolean darkModeEnabled = sharedPref.getBoolean(getString(R.string.preference_app_theme_key), defaultValue);

        if(darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean internetAvailable = ConnectionManager.getInternetStatus(this);
        if(internetAvailable) {
            // todo
        }
        else {
            showErrorSnackbar(getString(R.string.string_no_internet_connection));
        }
    }

    /**
     * listen for bottom navigation, destination changes
     */
    @Override
    public void onDestinationChanged(@NonNull NavController controller,
                                     @NonNull NavDestination destination,
                                     @Nullable Bundle arguments) {
        if(destination.getId() == R.id.homeFragment) {
            showView(toolbarInnerContainer);
            showBottomBar();
            showToolbar();
        }
        else if(destination.getId() == R.id.webviewFragment) {
            hideToolbar();
            hideBottomBar();
        }
        else if(destination.getId() == R.id.customizeTopicsFragment) {
            hideToolbar();
        }
        else if(destination.getId() == R.id.topicFeedsFragment) {
            hideToolbar();
        }
        else {
            showToolbar();
            showBottomBar();
            hideView(toolbarInnerContainer);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        if(compositeDisposable!=null) {
            compositeDisposable.clear();
        }

        super.onDestroy();
        viewBinding = null;
        Log.d(TAG, "onDestroy: called");
    }





    //------------------------------------------------------------------------------ MY METHODS

    private void initView() {
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        setContentView(view);

        // toolbar
        toolbar = viewBinding.toolbarMainActivity;
        setSupportActionBar(toolbar);
        appBar = getSupportActionBar();
        // Enable the Up button
        if(appBar!=null) appBar.setDisplayHomeAsUpEnabled(true);
        bottomNavBar = viewBinding.includeMainActivity.bottomNavView;
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        toolbarLogo = viewBinding.imageViewMainActivityToolbarLogo;
        chipTimeMachine = viewBinding.chipMainActivityTimeMachine;
        toolbarInnerContainer = viewBinding.constraintActivityMainActivityToolbarInnerContainer;
        chipTimeMachine.setOnClickListener(v -> showTimeMachineDatePicker());
        datePicked = System.currentTimeMillis();
        timeMachineEnabledIndicator = viewBinding.floatingActionButtonExploreFragmentTimeMachine;


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.bookmarksFragment, R.id.historyFragment)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavBar, navController);
        navController.addOnDestinationChangedListener(this);


//        FragmentNavigator navigator = new Keep(this, navHostFragment.getChildFragmentManager(), R.id.nav_host_fragment);
//        navController.getNavigatorProvider().addNavigator(navigator);
//        navController.setGraph(R.navigation.mobile_navigation);
//        NavigationUI.setupWithNavController(bottomNavBar, navController);



//        final FragmentManager fragmentManager = getSupportFragmentManager();
//
//        // define your fragments here
//        final Fragment exploreFragment = new ExploreFragment();
//        final Fragment bookmarksFragment = new BookmarksFragment();
//        final Fragment historyFragment = new HistoryFragment();


        // bottom navigation
//        bottomNavBar = viewBinding.includeMainActivity.bottomNavView;
//        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupWithNavController(bottomNavBar, navController);
//        bottomNavBar.setOnNavigationItemSelectedListener(item ->
//                onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host_fragment))
//        );


        //
        compositeDisposable = new CompositeDisposable();

        // repos
        topicRepository = new TopicRepository();
        sourceRepository = new SourceRepository();


        // viewmodels
        topicsViewModel = new ViewModelProvider(this).get(TopicsViewModel.class);
        sourceViewModel = new ViewModelProvider(this).get(SourceViewModel.class);
        timeMachineViewModel = new ViewModelProvider(this).get(TimeMachineViewModel.class);


    }// end initView()

    private void initAdProvider(Context context, int numAdsToLoad) {
        AdProvider adProvider = AdProvider.getInstance();
        adProvider.initAdMob(context);
        adProvider.loadSomeAds(numAdsToLoad, context);
    }




    //-------------------------------------------------- observing
    private void observeFetchedTopics() {
        topicsViewModel.getObservableTopicsList().observe(this, topics -> {
            if(topics == null || topics.isEmpty()) {
                //TODO: warning message, no topics in memory
                Log.w(TAG, "SCIENCE_BOARD - loadTopics: no topics in Room");
            }
            else {
                loadSources();
            }
        });
    }

    private void observeDatePickedFromTimeMachine() {
        timeMachineViewModel.getObservablePickedDate().observe(this, pickedDateInMillis-> {
            Log.d(TAG, "observeDatePickedFromTimeMachine: called");
            if(timeMachineViewModel.timeMachineIsEnabled()) {
                applyTimeMachineModeLayout(pickedDateInMillis);
            }
            else {
                removeTimeMachineModeLayout();
            }
        });
    }




    //--------------------------------------------------

    private void showTimeMachineDatePicker() {
        final String DATE_PICKER_DIALOG_TAG = "datePickerDialog";

        Bundle bundle = new Bundle();
        bundle.putLong("givenDialogCalendarDate", datePicked);

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), DATE_PICKER_DIALOG_TAG);
    }

    private void applyTimeMachineModeLayout(Long pickedDateInMillis) {
        Log.d(TAG, "applyTimeMachineModeLayout: called");
        datePicked = pickedDateInMillis;
        Calendar cal = convertMillisInCalendar(pickedDateInMillis);
        blinkView(timeMachineEnabledIndicator);
        String ddmmyyyy_dateFormat = getString(R.string.slash_formatted_date,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH)+MONTH_OFFSET_CORRECTION,
                cal.get(Calendar.YEAR));
        chipTimeMachine.setText(ddmmyyyy_dateFormat);
        chipTimeMachine.setChipStrokeWidth(DATE_PICKER_SET_CHIP_STROKE_WIDTH);
        chipTimeMachine.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.primary_blue)));
        chipTimeMachine.setCloseIconVisible(true);
        chipTimeMachine.setOnCloseIconClickListener(v -> {
            removeTimeMachineModeLayout();
            timeMachineViewModel.setPickedDate(datePicked);
        });
//        chipTimeMachine.setCloseIconVisible(true);
//        chipTimeMachine.setOnCloseIconClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    private void removeTimeMachineModeLayout() {
        Log.d(TAG, "removeTimeMachineModeLayout: called");
        datePicked = System.currentTimeMillis();
        chipTimeMachine.setText(R.string.today_label_date_picker);

        chipTimeMachine.setCloseIconVisible(false);
        chipTimeMachine.setChipStrokeWidth(DATE_PICKER_DEFAULT_CHIP_STROKE_WIDTH);
        chipTimeMachine.setOnCloseIconClickListener(null);
        chipTimeMachine.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.gray_500)));

        stopBlinkingView(timeMachineEnabledIndicator);
    }

    @NotNull
    private Calendar convertMillisInCalendar(Long pickedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pickedDate);
        return cal;
    }

    private void initAppContent() {
//        observeFetchedTopics();

        //
//        topicRepository.init(this, new OnTopicRepositoryInitilizedListener() {
//            @Override
//            public void onComplete() {
//                //
//                topicsViewModel.fetchTopics();
//            }
//
//            @Override
//            public void onFailed(String message) {
//                // use cached topics
//                topicsViewModel.fetchTopics();
//                Log.e(TAG, "SCIENCE_BOARD - loadTopics failed, cause: " + message);
//            }
//        });



        compositeDisposable.add(
                topicRepository.checkLastFetchDate(this)
                        .flatMap(timeElapsed -> {
                            if(timeElapsed) {
                                return topicRepository.fetchRemotely_strategy_rxjava(this)
                                        .flatMap(topics -> topicRepository.saveFetchedTopicsInRoom_rxjava(topics, this))
                                        .subscribeOn(Schedulers.io());
                            }
                            else {
                                return topicRepository.fetchLocally_strategy_rxjava(this);
                            }
                        })
                .flatMap(topics ->
                        topicRepository.getUpdatedTopicsFromRoom_sync_rxjava( this)
                                .subscribeOn(Schedulers.io()))
                .flatMap(topics -> {
                        topicsViewModel.setTopicsList(topics);
                        return sourceRepository.checkLastFetchDate( this)
                                .subscribeOn(Schedulers.io());
                })
                .flatMap(timeElapsed -> {
                    if(timeElapsed) {
                        return sourceRepository.fetchRemotely_strategy_rxjava(this)
                                .flatMap(sources -> sourceRepository.saveFetchedSourcesInRoom_sync_rxjava(sources, this))
                                .subscribeOn(Schedulers.io());
                    }
                    else {
                        return sourceRepository.fetchLocally_strategy_rxjava( this);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<Source>>() {
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Source> sources) {
                        sourceViewModel.setAllSources(sources);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        sourceViewModel.setAllSources(null);

                    }
                })
        );
    }

    private void loadSources() {
        sourceViewModel.getObservableAllSources().observe(this, sources -> {
            if(sources == null || sources.isEmpty()) {
                //TODO: warning message, no sources in remote DB
                Log.w(TAG, "SCIENCE_BOARD - loadSources: no sources in remote DB");
                showCenteredToast("an error occurred when fetching sources from remote DB");
            }
            else {
                //TODO
//                showCenteredToast("sources fetched from remote DB");
            }
        });

        //
        sourceViewModel.loadSourcesFromRemoteDb();
    }

    private void retryAction() {
        boolean internetAvailable = ConnectionManager.getInternetStatus(this);
        if(internetAvailable) {
            showGreenSnackbar(getString(R.string.string_internet_available));
        }
        else {
            showErrorSnackbar(getString(R.string.string_no_internet_connection));
        }
    }










    //-------------------------------------------------- UTILITIES


    private void blinkView(View view) {
        view.setVisibility(View.VISIBLE);
        fadeIn(view);
    }

    private void fadeIn(View view) {
        objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        objectAnimator.setDuration(ANIMATION_DURATION);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        objectAnimator.start();
    }


    private void fadeOut(View view) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        objectAnimator.setDuration(2000L);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeIn(view);
            }
        });
        objectAnimator.start();
    }

    private void stopBlinkingView(View view) {
        if(objectAnimator!=null) {
            objectAnimator.cancel();
            view.setVisibility(View.GONE);
        }
    }

    private void hideView(View view) {
        if(view!=null)
            view.setVisibility(View.GONE);
    }

    private void showView(View view) {
        if (view != null)
            view.setVisibility(View.VISIBLE);
    }

    private void hideBottomBar() {
        if(bottomNavBar!=null)
            bottomNavBar.setVisibility(View.GONE);
    }

    private void showToolbar() {
        if (appBar != null)
            appBar.show();
//        toolbar.setVisibility(View.VISIBLE);
    }

    private void showBottomBar() {
        if(bottomNavBar!=null)
            bottomNavBar.setVisibility(View.VISIBLE);
    }

    private void hideToolbar() {
        if (appBar != null)
            appBar.hide();
//        toolbar.setVisibility(View.GONE);
    }

    private void showErrorSnackbar(String message) {
        try {
            if(snackbar!=null && snackbar.isShown()) snackbar.dismiss(); // dismiss any previous snackbar
            snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setTextColor(getMyColor(R.color.white));
            snackbar.setBackgroundTint(getMyColor(R.color.red));
            snackbar.setAnchorView(viewBinding.includeMainActivity.bottomNavView);
            snackbar.setAction(R.string.string_retry, v -> {
                snackbar.dismiss();
                retryAction();
            });
            snackbar.setActionTextColor(getMyColor(R.color.white));
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getMyColor(int resourceId) {
        return getResources().getColor(resourceId);
    }

    private void showGreenSnackbar(String message) {
        try {
            if(snackbar!=null && snackbar.isShown()) snackbar.dismiss(); // dismiss any previous snackbar
            snackbar = Snackbar.make(view, message,Snackbar.LENGTH_SHORT);
            snackbar.setTextColor(getMyColor(R.color.white));
            snackbar.setBackgroundTint(getMyColor(R.color.green));
            snackbar.setAnchorView(viewBinding.includeMainActivity.bottomNavView);
            snackbar.setAction(R.string.string_ok, v -> snackbar.dismiss());
            snackbar.setActionTextColor(getMyColor(R.color.white));
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCenteredToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(this,message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(this,message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void changeToolbarTitle(String value) {
        if(toolbar!=null) {
            toolbar.setTitle(value);
        }
    }

}// end MainActivity