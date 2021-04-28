package com.nocorp.scienceboard.ui.timemachine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nocorp.scienceboard.databinding.FragmentTimeMachineBinding;


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