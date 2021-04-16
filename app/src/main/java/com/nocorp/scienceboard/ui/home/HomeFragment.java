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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentHomeBinding;
import com.nocorp.scienceboard.databinding.FragmentTechTabBinding;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;
import com.nocorp.scienceboard.viewpager.HomeViewPagerAdapter;


public class HomeFragment extends Fragment{
    private final String TAG = this.getClass().getSimpleName();
    private HomeViewPagerAdapter viewPagerAdapter;
    private ViewPager2 viewPager;
    private HomeViewModel homeViewModel;
    private View view;
    private TabLayout tabLayout;
    private FragmentHomeBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        Log.d(TAG, "SCIENCE_BOARD - onCreateView: called");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "SCIENCE_BOARD - onViewCreated: called");

        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        Toolbar toolbar = binding.toolbarHomeFragment;
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        tabLayout = binding.tablayoutHomeFragment;
        viewPager = binding.viewPagerHomeFragment;
//        FragmentManager fm = getChildFragmentManager();
//        Lifecycle lifecycle = getViewLifecycleOwner().getLifecycle();
        viewPagerAdapter = new HomeViewPagerAdapter(requireActivity());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setUserInputEnabled(false); // disables horiz. swipe to scroll tabs gestures
        viewPager.setOffscreenPageLimit(1);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("ALL");
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





    }




    protected boolean isDestroyed() {
        return (this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        adProvider.destroyAds();
    }



    //--------------------------------------------------------------------- methods


}// end HomeFragment