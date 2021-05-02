package com.nocorp.scienceboard.ui.topics;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.transition.MaterialContainerTransform;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentCustomizeTopicsBinding;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.recycler.adapter.RecyclerAdapterCustomizeTopics;

import java.util.ArrayList;
import java.util.List;

public class CustomizeTopicsFragment extends Fragment implements RecyclerAdapterCustomizeTopics.FollowChipListener {
    private final String TAG = this.getClass().getSimpleName();
    private TopicsViewModel topicsViewModel;
    private View view;
    private RecyclerView recyclerView;
    private RecyclerAdapterCustomizeTopics recyclerAdapterCustomizeTopics;
    private FragmentCustomizeTopicsBinding viewBinding;
    private List<Topic> topicsFetched;
    private ExtendedFloatingActionButton floatingActionButton;
    private Toast toast;

    //
    private List<Topic> topicsToUpdate;



    //-------------------------------------------------------------------------------------------- CONSTRUCTORS

    public static CustomizeTopicsFragment newInstance() {
        return new CustomizeTopicsFragment();
    }




    //-------------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setEnterTransition();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentCustomizeTopicsBinding.inflate(getLayoutInflater());
        view = viewBinding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        floatingActionButton.show();
        floatingActionButton.setOnClickListener(v -> {
            topicsViewModel.updateTopicsFollowStatus(topicsToUpdate);
            Navigation.findNavController(view).navigateUp();
        });
        observeTopicsFetched();
    }








    //-------------------------------------------------------------------------------------------- METHODS

    private void observeTopicsFetched() {
        topicsViewModel.getObservableTopicsList().observe(getViewLifecycleOwner(), topics -> {
            if(topics!=null && !topics.isEmpty()) {
                // TODO
                this.topicsFetched = new ArrayList<>(topics);
                recyclerAdapterCustomizeTopics.loadNewData(topics);
            }
        });
    }



    private void initView() {
        topicsViewModel = new ViewModelProvider(requireActivity()).get(TopicsViewModel.class);
        floatingActionButton = viewBinding.floatingActionButtonTopicsFragment;
        recyclerView = viewBinding.recyclerViewTopicsFragment;
        topicsToUpdate = new ArrayList<>();
        initRecycleView(recyclerView);
    }

    private void initRecycleView(RecyclerView recyclerView) {
        // defining Recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAdapterCustomizeTopics = new RecyclerAdapterCustomizeTopics(new ArrayList<>(), this);
        recyclerView.setAdapter(recyclerAdapterCustomizeTopics);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
//        SnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(recyclerView);
    }

    private void setEnterTransition() {
        // TRANSITION
        MaterialContainerTransform transform = new MaterialContainerTransform();
//        transform.setInterpolator(new FastOutSlowInInterpolator());
        transform.setDrawingViewId(R.id.nav_host_fragment);
//        transform.setContainerColor(Color.WHITE);
//        transform.setFadeMode(MaterialContainerTransform.FADE_MODE_THROUGH);
        transform.setDuration(300);
        transform.setAllContainerColors(getResources().getColor(R.color.white));

        setSharedElementEnterTransition(transform);
//        setSharedElementReturnTransition(transform);
    }



    @Override
    public void onFollowChipChecked(int position, Chip chip) {
        Topic topic = recyclerAdapterCustomizeTopics.getItem(position);
        if(topic!=null) {
            topicsToUpdate.remove(topic);
            topic.setFollowed(true);
            topicsToUpdate.add(topic);
        }
        else {
            Log.e(TAG, "onFollowChipChecked: cannot follow topic, cause: topic is null");
        }
    }

    @Override
    public void onFollowChipUnchecked(int position, Chip chip) {
        Topic topic = recyclerAdapterCustomizeTopics.getItem(position);

        if(topic!=null) {
            topicsToUpdate.remove(topic); // remove old topic state, if any
            topic.setFollowed(false); // set new topic state
            topicsToUpdate.add(topic);
        }
        else {
            Log.e(TAG, "onFollowChipUnchecked: cannot unfollow topic, cause: topic is null");
        }
    }

//    private boolean atLeastOneTopicIsFollowed(List<Topic> topics) {
//        boolean result = false;
//        if(topics==null || topics.isEmpty()) return result;
//
//        for (Topic current)
//
//        return result;
//    }

    private void showToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }


}// end TopicsFragment