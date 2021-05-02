package com.nocorp.scienceboard.ui.about;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nocorp.scienceboard.NavGraphDirections;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentAboutBinding;
import com.nocorp.scienceboard.databinding.FragmentHomeBinding;

public class AboutFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private View view;
    private FragmentAboutBinding viewBinding;
    private AboutViewModel mViewModel;
    private Button privacyButton;
    private Button termsButton;



    //---------------------------------------------------------------------------------------- CONSTRUCTORS

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }





    //---------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentAboutBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        return view;
    }





    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AboutViewModel.class);
        privacyButton = viewBinding.buttonAboutFragmentPrivacy;
        termsButton = viewBinding.buttonAboutFragmentTerms;

        privacyButton.setOnClickListener(v -> showPrivacy());
        termsButton.setOnClickListener(v -> showTerms());

    }



    private void showPrivacy() {
        AboutFragmentDirections.ActionAboutFragmentToAboutWebviewFragment action =
                AboutFragmentDirections.actionAboutFragmentToAboutWebviewFragment("privacy");
        Navigation.findNavController(view).navigate(action);
    }

    private void showTerms() {
        AboutFragmentDirections.ActionAboutFragmentToAboutWebviewFragment action =
                AboutFragmentDirections.actionAboutFragmentToAboutWebviewFragment("terms");
        Navigation.findNavController(view).navigate(action);
    }


}// end AboutFragment