package com.nocorp.scienceboard.ui.timemachine;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.time.Instant;

public class TimeMachineViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<Long> pickedDate;



    //-------------------------------------------------------------------------------------------- CONSTRUCTORS
    public TimeMachineViewModel() {
        pickedDate = new MutableLiveData<>();
    }



    //-------------------------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<Long> getObservablePickedDate() {
        return pickedDate;
    }

    public void setPickedDate(Long pickedDate) {
        this.pickedDate.postValue(pickedDate);
    }



    //-------------------------------------------------------------------------------------------- METHODS








}// end TimeMachineViewModel