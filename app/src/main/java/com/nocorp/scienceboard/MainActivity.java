package com.nocorp.scienceboard;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nocorp.scienceboard.utility.AdProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private AdProvider adProvider;
    private NavController navController;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        AdProvider adProvider = AdProvider.getInstance();
        adProvider.initAdMob(this);
        adProvider.loadSomeAds(5, this);

    }

    private void initView() {
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_bookmarks, R.id.navigation_history)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.navigation_home) {
                    if(getSupportActionBar()!=null)
                        getSupportActionBar().hide();
                }
                else if(destination.getId() == R.id.action_global_webviewFragment) {
                    if(getSupportActionBar()!=null)
                        getSupportActionBar().hide();
                }
                else {
                    if(getSupportActionBar()!=null)
                        getSupportActionBar().show();
                }
            }
        });

        actionBar = getSupportActionBar();

    }


    @Override
    public boolean onSupportNavigateUp() {
        navController.navigateUp();
        return super.onSupportNavigateUp();
    }


}// end MainActivity