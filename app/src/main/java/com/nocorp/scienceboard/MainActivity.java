package com.nocorp.scienceboard;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nocorp.scienceboard.databinding.ActivityMainBinding;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.system.ConnectionManager;
import com.nocorp.scienceboard.topics.repository.OnTopicRepositoryInitilizedListener;
import com.nocorp.scienceboard.topics.repository.TopicRepository;
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


public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {
    private final String TAG = this.getClass().getSimpleName();
    private NavController navController;
    private BottomNavigationView bottomNavBar;
    private ActivityMainBinding binding;
    private View view;
    private Snackbar snackbar;
    private Toolbar toolbar;
    private ActionBar appBar;
    private Toast toast;

    //
    private AdProvider adProvider;
    private SourceViewModel sourceViewModel;
    private TopicsViewModel topicsViewModel;
    private TopicRepository topicRepository;

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

//    /**
//     * enable toolbar back button
//     */
//    @Override
//    public boolean onSupportNavigateUp() {
//        navController.navigateUp();
//        return super.onSupportNavigateUp();
//    }

    /**
     * listen for bottom navigation, destination changes
     */
    @Override
    public void onDestinationChanged(@NonNull NavController controller,
                                     @NonNull NavDestination destination, @Nullable Bundle arguments) {
        if(destination.getId() == R.id.navigation_home) {
            hideToolbar();
            showBottomBar();
        }
        else if(destination.getId() == R.id.webviewFragment) {
            hideToolbar();
            hideBottomBar();
        }
        else if(destination.getId() == R.id.topicsFragment) {
            hideToolbar();
        }
        else {
            showToolbar();
            showBottomBar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }





    //------------------------------------------------------------------------------ MY METHODS


    private void initView() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        // toolbar
        toolbar = binding.toolbarMainActivity;
        setSupportActionBar(toolbar);
        appBar = getSupportActionBar();
        // Enable the Up button
        if(appBar!=null) appBar.setDisplayHomeAsUpEnabled(true);


        // bottom navigation
        bottomNavBar = binding.includeMainActivity.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_bookmarks, R.id.navigation_history)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavBar, navController);
        navController.addOnDestinationChangedListener(this);



        // viewmodels
        topicRepository = new TopicRepository();
        topicsViewModel = new ViewModelProvider(this).get(TopicsViewModel.class);
        sourceViewModel = new ViewModelProvider(this).get(SourceViewModel.class);

    }// end initView()

    private void initAdProvider(Context context, int numAdsToLoad) {
        AdProvider adProvider = AdProvider.getInstance();
        adProvider.initAdMob(context);
        adProvider.loadSomeAds(numAdsToLoad, context);
    }

    private void loadTopics() {
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
        observeFetchedTopics();
        topicsViewModel.fetchTopics();
    }

    private void observeFetchedTopics() {
        topicsViewModel.getObservableTopicsList().observe(this, topics -> {
            if(topics == null) {
                //TODO: error message
                Log.e(TAG, "SCIENCE_BOARD - loadTopics: an error occurrend when fetching topics");
            }
            else if(topics.isEmpty()) {
                //TODO: warning message, no topics in memory
                Log.w(TAG, "SCIENCE_BOARD - loadTopics: no topics in Room");
            }
            else {
                loadSources();
            }
        });
    }

    private void loadSources() {
        sourceViewModel.getObservableAllSources().observe(this, sources -> {
            if(sources == null) {
                //TODO: error message
                Log.e(TAG, "SCIENCE_BOARD - loadSources: an error occurrend when fetching sources");
                showCenteredToast("an error occurred when fetching sources from remote DB");
            }
            else if(sources.isEmpty()) {
                //TODO: warning message, no sources in remote DB
                Log.w(TAG, "SCIENCE_BOARD - loadSources: no sources in remote DB");

            }
            else {
                showCenteredToast("sources fetched from remote DB");
            }
        });

        //
        sourceViewModel.loadSourcesFromRemoteDb();
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
            snackbar.setAnchorView(binding.includeMainActivity.navView);
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

    private void retryAction() {
        boolean internetAvailable = ConnectionManager.getInternetStatus(this);
        if(internetAvailable) {
            showGreenSnackbar(getString(R.string.string_internet_available));
        }
        else {
            showErrorSnackbar(getString(R.string.string_no_internet_connection));
        }
    }

    private void showGreenSnackbar(String message) {
        try {
            if(snackbar!=null && snackbar.isShown()) snackbar.dismiss(); // dismiss any previous snackbar
            snackbar = Snackbar.make(view, message,Snackbar.LENGTH_SHORT);
            snackbar.setTextColor(getMyColor(R.color.white));
            snackbar.setBackgroundTint(getMyColor(R.color.green));
            snackbar.setAnchorView(binding.includeMainActivity.navView);
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
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(this,message, Toast.LENGTH_SHORT);
        toast.show();
    }




}// end MainActivity