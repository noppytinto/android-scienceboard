package com.nocorp.scienceboard.ui.explore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.transition.MaterialElevationScale;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentExploreBinding;
import com.nocorp.scienceboard.ui.timemachine.DatePickerFragment;
import com.nocorp.scienceboard.ui.timemachine.TimeMachineViewModel;
import com.nocorp.scienceboard.viewpager.HomeViewPagerAdapter;
import org.jetbrains.annotations.NotNull;
import java.util.Calendar;


public class ExploreFragment extends Fragment{
    private final String TAG = this.getClass().getSimpleName();
    FragmentExploreBinding viewBinding;
    private View view;
    private FloatingActionButton customizeHomeButton;
    private Chip chipTimeMachine;
    private ExtendedFloatingActionButton timeMachineEnabledIndicator;

    // viewmodels
    private TimeMachineViewModel timeMachineViewModel;

    // animations
    private int androidDefaultShortAnimationDuration;
    private final long ANIMATION_DURATION = 4000L;
    private ObjectAnimator objectAnimator;

    // parameters
    private final int DATE_PICKER_DEFAULT_CHIP_STROKE_WIDTH = 0;
    private final int DATE_PICKER_SET_CHIP_STROKE_WIDTH = 7;
    private final int TABS_OFFSCREEN_PAGE_LIMIT = 1;
    private long datePickerCalendarDateInMillis;
    private int MONTH_OFFSET_CORRECTION = 1;




    //------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setExitTransition(new Hold().setDuration(1000));
        setExitTransition(new MaterialElevationScale(/* growing= */ false));
        setReenterTransition(new MaterialElevationScale(/* growing= */ true));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewBinding = FragmentExploreBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        Log.d(TAG, "SCIENCE_BOARD - onCreateView: called");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "SCIENCE_BOARD - onViewCreated: called");
        initView();
        chipTimeMachine.setOnClickListener(v -> showTimeMachineDatePicker());
        customizeHomeButton.setOnClickListener(v -> showCustomizeHomeFeedFragment());
        //
        observeDatePickedFromTimeMachine();

        //
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
////        adProvider.destroyAds();
//    }







    //------------------------------------------------------------------------------------- METHODS

    private void initView() {
        Toolbar toolbar = viewBinding.toolbarExploreFragment;
        TabLayout tabLayout = viewBinding.tablayoutExploreFragment;
        ViewPager2 viewPager = viewBinding.viewPagerExploreFragment;
        customizeHomeButton = viewBinding.floatingActionButtonExploreFragmentCustomizeTopics;
        chipTimeMachine = viewBinding.chipExploreFragment;
        timeMachineEnabledIndicator = viewBinding.floatingActionButtonExploreFragmentTimeMachine;

        // values
        androidDefaultShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        datePickerCalendarDateInMillis = System.currentTimeMillis();

        Log.d(TAG, "initView: datePickerCalendarDateInMillis: " + datePickerCalendarDateInMillis);

        // viewmodels
        timeMachineViewModel = new ViewModelProvider(requireActivity()).get(TimeMachineViewModel.class);

        // setups
        setupToolbar(toolbar);
        setupTabs(tabLayout, viewPager);
    }

    private void setupToolbar(Toolbar toolbar) {
        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    private void setupTabs(TabLayout tabLayout, ViewPager2 viewPager) {
        FragmentManager fm = getChildFragmentManager();
        Lifecycle lifecycle = getViewLifecycleOwner().getLifecycle();

        HomeViewPagerAdapter viewPagerAdapter = new HomeViewPagerAdapter(fm, lifecycle);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setUserInputEnabled(true); // disable/enable horiz. swipe to scroll tabs gestures
        viewPager.setOffscreenPageLimit(TABS_OFFSCREEN_PAGE_LIMIT);// TODO: this might solve the "blank tab" problem, but needs more investigation, since the default strategy makes more sense

        setupTabsStyle(tabLayout, viewPager);
        defineOnTabChangedBehavior(viewPager);
    }

    private void setupTabsStyle(TabLayout tabLayout, ViewPager2 viewPager) {
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.home_tab_label);
                    break;
                case 1:
                    tab.setText(R.string.tech_tab_label);
                    break;
                case 2:
                    tab.setText(R.string.physics_tab_label);
                    break;
                case 3:
                    tab.setText(R.string.space_tab_label);
                    break;
                case 4:
                    tab.setText(R.string.biology_tab_label);
                    break;
                case 5:
                    tab.setText(R.string.medicine_tab_label);
                    break;
            }
        });
        tabLayoutMediator.attach();
    }

    private void defineOnTabChangedBehavior(ViewPager2 viewPager) {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case 0:
                        Log.d(TAG, "onPageSelected: " + position);
                        customizeHomeButton.show();
                        break;
                    default:
                        customizeHomeButton.hide();

                }
            }
        });
    }

    private void observeDatePickedFromTimeMachine() {
        timeMachineViewModel.getObservablePickedDate().observe(getViewLifecycleOwner(), pickedDateInMillis-> {
            Log.d(TAG, "observeDatePickedFromTimeMachine: called");
            if(timeMachineViewModel.timeMachineIsEnabled()) {
                applyTimeMachineModeLayout(pickedDateInMillis);
            }
            else {
                removeTimeMachineModeLayout();
            }
        });
    }

    private void showTimeMachineDatePicker() {
        final String DATE_PICKER_DIALOG_TAG = "datePickerDialog";

        Bundle bundle = new Bundle();
        bundle.putLong("givenDialogCalendarDate", datePickerCalendarDateInMillis);

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(requireActivity().getSupportFragmentManager(), DATE_PICKER_DIALOG_TAG);
    }

    private void applyTimeMachineModeLayout(Long pickedDateInMillis) {
        datePickerCalendarDateInMillis = pickedDateInMillis;
        Calendar cal = convertMillisInCalendar(pickedDateInMillis);
        blinkView(timeMachineEnabledIndicator);
        String ddmmyyyy_dateFormat = getString(R.string.slash_formatted_date,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH)+MONTH_OFFSET_CORRECTION,
                cal.get(Calendar.YEAR));
        chipTimeMachine.setText(ddmmyyyy_dateFormat);
        chipTimeMachine.setChipStrokeWidth(DATE_PICKER_SET_CHIP_STROKE_WIDTH);
        chipTimeMachine.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.primary_blue)));
    }

    private void removeTimeMachineModeLayout() {
        datePickerCalendarDateInMillis = System.currentTimeMillis();
        chipTimeMachine.setText(R.string.today_label_date_picker);
        chipTimeMachine.setChipStrokeWidth(DATE_PICKER_DEFAULT_CHIP_STROKE_WIDTH);
        stopBlinkingView(timeMachineEnabledIndicator);
    }



    /**
     * PRECONDITIONS:
     * the givenDate is guaranteed to be >0
     */
    private boolean isTheCurrentDate(long givenDateInMillis) {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        Calendar cal = convertMillisInCalendar(givenDateInMillis);
        int year2 = cal.get(Calendar.YEAR);
        int month2 = cal.get(Calendar.MONTH);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);

        return (day==day2) && (month == month2) && (year == year2);
    }

    @NotNull
    private Calendar convertMillisInCalendar(Long pickedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pickedDate);
        return cal;
    }

    private void showCustomizeHomeFeedFragment() {

        // add container transformation animation
        FragmentNavigator.Extras animations = new FragmentNavigator
                .Extras
                .Builder()
                .addSharedElement(customizeHomeButton, customizeHomeButton.getTransitionName())
                .build();

        Navigation.findNavController(view)
                .navigate(R.id.action_navigation_home_to_topicsFragment,null,null, animations);
    }


    private void blinkView(View view) {
        view.setVisibility(View.VISIBLE);
        fadeIn(view);
    }

    private void fadeIn(View view) {
        objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        objectAnimator.setDuration(ANIMATION_DURATION);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        objectAnimator.start();
    }


    private void fadeOut(View view) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        objectAnimator.setDuration(2000L);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeIn(view);
            }
        });
        objectAnimator.start();
    }

    private void stopBlinkingView(View view) {
        if(objectAnimator!=null) {
            objectAnimator.cancel();
            view.setVisibility(View.GONE);
        }
    }

    private void applyCrossfadeEnter(View view, int duration) {
        final float STARTING_ALPHA = 0f;
        final float ENDING_ALPHA = 1f;

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(STARTING_ALPHA);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(ENDING_ALPHA)
                .setDuration(duration)
                .setListener(null);
    }

    private void applyCrossfadeExit(View view, int duration) {
        final float STARTING_ALPHA = 0f;

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        view.animate()
                .alpha(STARTING_ALPHA)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }



    protected boolean isDestroyed() {
        return (this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }



}// end HomeFragment