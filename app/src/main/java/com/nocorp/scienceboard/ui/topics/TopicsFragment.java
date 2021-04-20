package com.nocorp.scienceboard.ui.topics;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.transition.MaterialContainerTransform;
import com.nocorp.scienceboard.R;

public class TopicsFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private TopicsViewModel topicsViewModel;





    //-------------------------------------------------------------------------------------------- CONSTRUCTORS

    public static TopicsFragment newInstance() {
        return new TopicsFragment();
    }




    //-------------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition();
    }

    private void setEnterTransition() {
        // TRANSITION
        MaterialContainerTransform transform = new MaterialContainerTransform();
//        transform.setInterpolator(new FastOutSlowInInterpolator());
        transform.setDrawingViewId(R.id.nav_host_fragment);
//        transform.setContainerColor(Color.WHITE);
//        transform.setFadeMode(MaterialContainerTransform.FADE_MODE_THROUGH);
        transform.setDuration(1000);
        transform.setAllContainerColors(getResources().getColor(R.color.white));

        setSharedElementEnterTransition(transform);
//        setSharedElementReturnTransition(transform);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_topics, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        topicsViewModel = new ViewModelProvider(this).get(TopicsViewModel.class);
        // TODO: Use the ViewModel
    }





    //-------------------------------------------------------------------------------------------- METHODS






}// end TopicsFragment