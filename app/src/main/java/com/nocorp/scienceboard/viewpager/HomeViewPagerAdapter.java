package com.nocorp.scienceboard.viewpager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nocorp.scienceboard.ui.tabs.alltab.AllTabFragment;
import com.nocorp.scienceboard.ui.tabs.techtab.TechTabFragment;

public class HomeViewPagerAdapter extends FragmentStateAdapter {

    public HomeViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                Fragment allArticlesTabFragment = AllTabFragment.newInstance();
//                Bundle args = new Bundle();
//                // Our object is just an integer :-P
//                args.putInt(allArticlesTabFragment.ARG_OBJECT, position + 1);
                return allArticlesTabFragment;
            }
            default:
                return TechTabFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
