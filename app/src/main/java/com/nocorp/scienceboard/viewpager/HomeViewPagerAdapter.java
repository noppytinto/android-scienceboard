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
//                Bundle args = new Bundle();
//                // Our object is just an integer :-P
//                args.putInt(allArticlesTabFragment.ARG_OBJECT, position + 1);
                return AllTabFragment.newInstance();
            }
            case 1:
                return TechTabFragment.newInstance();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
