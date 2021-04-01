package com.nocorp.scienceboard.viewpager;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nocorp.scienceboard.ui.tabs.allarticlestab.AllArticlesTabFragment;

public class HomeViewPagerAdapter extends FragmentStateAdapter {

    public HomeViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0: {
                Fragment allArticlesTabFragment = AllArticlesTabFragment.newInstance();
//                Bundle args = new Bundle();
//                // Our object is just an integer :-P
//                args.putInt(allArticlesTabFragment.ARG_OBJECT, position + 1);

                return allArticlesTabFragment;
            }

            default:
                return AllArticlesTabFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
