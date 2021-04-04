package com.nocorp.scienceboard;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nocorp.scienceboard.databinding.ActivityMainBinding;
import com.nocorp.scienceboard.repository.SourceViewModel;
import com.nocorp.scienceboard.utility.ConnectionManager;
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
    private AdProvider adProvider;
    private NavController navController;
    private BottomNavigationView bottomNavBar;
    private ActivityMainBinding binding;
    private View view;
    private Snackbar snackbar;
    private Toolbar toolbar;
    private ActionBar appBar;
    private SourceViewModel sourceViewModel;





    //------------------------------------------------------------------------------ ANDROID METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAdProvider();

        sourceViewModel = new ViewModelProvider(this).get(SourceViewModel.class);
        sourceViewModel.getObservableSources().observe(this, sources -> {
            // TODO: Update the UI.
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean internetAvailable = ConnectionManager.getInternetStatus(this);
        if(internetAvailable) {
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
        toolbar = binding.toolbarMainActivity;
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        appBar = getSupportActionBar();
        // Enable the Up button
        if(appBar!=null) appBar.setDisplayHomeAsUpEnabled(true);


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

    }

    private void initAdProvider() {
        AdProvider adProvider = AdProvider.getInstance();
        adProvider.initAdMob(this);
        adProvider.loadSomeAds(5, this);
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
        if(snackbar!=null) snackbar.dismiss(); // dismiss any previous snackbar
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
        if(snackbar!=null) snackbar.dismiss(); // dismiss any previous snackbar
        snackbar = Snackbar.make(view, message,Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(getMyColor(R.color.white));
        snackbar.setBackgroundTint(getMyColor(R.color.green));
        snackbar.setAnchorView(binding.includeMainActivity.navView);
        snackbar.setAction(R.string.string_ok, v -> snackbar.dismiss());
        snackbar.setActionTextColor(getMyColor(R.color.white));
        snackbar.show();
    }




}// end MainActivity