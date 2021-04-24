package com.nocorp.scienceboard.viewpager;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.nocorp.scienceboard.ui.tabs.all.AllTabFragment;
import com.nocorp.scienceboard.ui.tabs.physics.PhysicsTabFragment;
import com.nocorp.scienceboard.ui.tabs.space.SpaceTabFragment;
import com.nocorp.scienceboard.ui.tabs.tech.TechTabFragment;

public class HomeViewPagerAdapter extends FragmentStateAdapter {
    private final String TAG = this.getClass().getSimpleName();
    private int itemCount;

    public HomeViewPagerAdapter(@NonNull FragmentManager fm,
                                @NonNull Lifecycle lifecycle, int itemCount) {
        super(fm, lifecycle);
        this.itemCount = itemCount;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "SCIENCE_BOARD - createFragment: position clicked: " + position);
        switch (position) {
            case 0: {
//                Bundle args = new Bundle();
//                // Our object is just an integer :-P
//                args.putInt(allArticlesTabFragment.ARG_OBJECT, position + 1);
                return AllTabFragment.newInstance();
            }
            case 1:
                return TechTabFragment.newInstance();
            case 2:
                return PhysicsTabFragment.newInstance();
            case 3:
                return SpaceTabFragment.newInstance();
            case 4:
                return new Fragment();
            case 5:
                return new Fragment();
        }

        // TODO: fix, should use "default" as last chance
        return null;
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

//    public void removePage(int id) {
//
//    }

}
