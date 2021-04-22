package com.nocorp.scienceboard.ui.timemachine;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentAllTabBinding;
import com.nocorp.scienceboard.databinding.FragmentTimeMachineBinding;
import com.nocorp.scienceboard.rss.repository.SourceViewModel;
import com.nocorp.scienceboard.ui.tabs.all.AllTabViewModel;
import com.nocorp.scienceboard.ui.topics.TopicsViewModel;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

public class TimeMachineFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    private TimeMachineViewModel timeMachineViewModel;
    private View view;
    private FragmentTimeMachineBinding viewBinding;


    //----------------------------------------------------------------------------------------- CONSTRUCTORS

    public static TimeMachineFragment newInstance() {
        return new TimeMachineFragment();
    }




    //----------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentTimeMachineBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }



    //----------------------------------------------------------------------------------------- METHODS

    private void initView() {

    }





}// end TimeMachineFragment