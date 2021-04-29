package com.nocorp.scienceboard;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.nocorp.scienceboard.databinding.ActivityMainBinding;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.system.ConnectionManager;
import com.nocorp.scienceboard.topics.repository.OnTopicRepositoryInitilizedListener;
import com.nocorp.scienceboard.topics.repository.TopicRepository;
import com.nocorp.scienceboard.ui.timemachine.TimeMachineViewModel;
import com.nocorp.scienceboard.ui.topics.TopicsViewModel;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


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
    private TextView toolbarTextLogo;
    private Chip chipTimeMachine;
    private View toolbarInnerContainer;

    //
    private AdProvider adProvider;
    private SourceViewModel sourceViewModel;
    private TopicsViewModel topicsViewModel;
    private TopicRepository topicRepository;
    private TimeMachineViewModel timeMachineViewModel;

    //
    private final int NUM_ADS_TO_LOAD = 5;




    //------------------------------------------------------------------------------ ANDROID METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAdProvider(this, NUM_ADS_TO_LOAD);

        loadTopics();
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
        toolbarTextLogo = viewBinding.textViewMainActivityToolbarLogo;
        chipTimeMachine = viewBinding.chipMainActivityTimeMachine;
        toolbarInnerContainer = viewBinding.constraintActivityMainActivityToolbarInnerContainer;

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



        // viewmodels
        topicRepository = new TopicRepository();
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




    //--------------------------------------------------

    private void loadTopics() {
        observeFetchedTopics();

        //
        topicRepository.init(this, new OnTopicRepositoryInitilizedListener() {
            @Override
            public void onComplete() {
                //
                fetchTopics();
            }

            @Override
            public void onFailded(String message) {
                // use cached topics
                fetchTopics();
                Log.e(TAG, "SCIENCE_BOARD - loadTopics: " + message);
            }
        });
    }

    private void fetchTopics() {
        topicsViewModel.fetchTopics();
    }

    private void loadSources() {
        sourceViewModel.getObservableAllSources().observe(this, sources -> {
            if(sources == null || sources.isEmpty()) {
                //TODO: warning message, no sources in remote DB
                Log.w(TAG, "SCIENCE_BOARD - loadSources: no sources in remote DB");
                showCenteredToast("an error occurred when fetching sources from remote DB");
            }
            else {
                showCenteredToast("sources fetched from remote DB");
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