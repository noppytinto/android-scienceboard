package com.nocorp.scienceboard.viewpager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.nocorp.scienceboard.ui.tabs.alltab.AllTabFragment;
import com.nocorp.scienceboard.ui.tabs.physicstab.PhysicsTabFragment;
import com.nocorp.scienceboard.ui.tabs.spacetab.SpaceTabFragment;
import com.nocorp.scienceboard.ui.tabs.techtab.TechTabFragment;

public class HomeViewPagerAdapter extends FragmentStateAdapter {
    private final String TAG = this.getClass().getSimpleName();

    public HomeViewPagerAdapter(@NonNull FragmentActivity fragment) {
        super(fragment);
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
        return 6;
    }
}
