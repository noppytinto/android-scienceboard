package com.nocorp.scienceboard.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.transition.MaterialElevationScale;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentHomeBinding;
import com.nocorp.scienceboard.ui.topics.TopicsViewModel;
import com.nocorp.scienceboard.viewpager.HomeViewPagerAdapter;


public class HomeFragment extends Fragment{
    private final String TAG = this.getClass().getSimpleName();
    private HomeViewPagerAdapter viewPagerAdapter;
    private ViewPager2 viewPager;
    private HomeViewModel homeViewModel;
    private View view;
    private TabLayout tabLayout;
    private FragmentHomeBinding viewBinding;
    private FloatingActionButton topicsButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setExitTransition(new Hold().setDuration(1000));
        setExitTransition(new MaterialElevationScale(/* growing= */ false));
        setReenterTransition(new MaterialElevationScale(/* growing= */ true));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        Log.d(TAG, "SCIENCE_BOARD - onCreateView: called");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "SCIENCE_BOARD - onViewCreated: called");

        topicsButton = viewBinding.floatingActionButtonHomeFragmentCustomizeTopics;

        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        Toolbar toolbar = viewBinding.toolbarHomeFragment;
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        tabLayout = viewBinding.tablayoutHomeFragment;
        viewPager = viewBinding.viewPagerHomeFragment;
        FragmentManager fm = getChildFragmentManager();
        Lifecycle lifecycle = getViewLifecycleOwner().getLifecycle();
        viewPagerAdapter = new HomeViewPagerAdapter(fm, lifecycle);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setUserInputEnabled(false); // disables horiz. swipe to scroll tabs gestures
        viewPager.setOffscreenPageLimit(1);// TODO: this might solve the "blank tab" problem, but needs more investigation, since the default strategy makes more sense



        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Home");
                    break;
                case 1:
                    tab.setText("Tech");
                    break;
                case 2:
                    tab.setText("Physics");
                    break;
                case 3:
                    tab.setText("Space");
                    break;
                case 4:
                    tab.setText("Biology");
                    break;
                case 5:
                    tab.setText("Medicine");
                    break;
            }
        });
        tabLayoutMediator.attach();


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case 0:
                        Log.d(TAG, "onPageSelected: " + position);
                        topicsButton.show();
                        break;
                    default:
                        topicsButton.hide();

                }
            }
        });




        topicsButton.setOnClickListener(v -> pickPreferredTopicsInHomeFeed());



    }

    private void pickPreferredTopicsInHomeFeed() {

        // add container transformation animation
        FragmentNavigator.Extras animations = new FragmentNavigator
                .Extras
                .Builder()
                .addSharedElement(topicsButton, topicsButton.getTransitionName())
                .build();

        Navigation.findNavController(view)
                .navigate(R.id.action_navigation_home_to_topicsFragment,null,null, animations);
    }



    protected boolean isDestroyed() {
        return (this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        adProvider.destroyAds();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }




    //--------------------------------------------------------------------- methods


}// end HomeFragment