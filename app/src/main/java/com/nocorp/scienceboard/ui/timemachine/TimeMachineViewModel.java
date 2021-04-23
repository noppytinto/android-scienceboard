package com.nocorp.scienceboard.ui.timemachine;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Calendar;

public class TimeMachineViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<Long> pickedDate;
    private static boolean timeMachineEnabled;



    //-------------------------------------------------------------------------------------------- CONSTRUCTORS
    public TimeMachineViewModel() {
        pickedDate = new MutableLiveData<>();
    }



    //-------------------------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<Long> getObservablePickedDate() {
        return pickedDate;
    }

    public Long getPickedDate() {
        return pickedDate.getValue();
    }

    public void setPickedDate(Long pickedDate) {
        if(equalsTheCurrentDate(pickedDate))
            timeMachineEnabled = false;
        else
            timeMachineEnabled = true;
        this.pickedDate.postValue(pickedDate);
    }

    public boolean timeMachineIsEnabled() {
        return timeMachineEnabled;
    }

    public void setTimeMachineEnabled(boolean timeMachineEnabled) {
        TimeMachineViewModel.timeMachineEnabled = timeMachineEnabled;
    }

    //-------------------------------------------------------------------------------------------- METHODS

    /**
     * PRECONDITIONS:
     * the givenDate is guaranteed to be >0
     */
    private boolean equalsTheCurrentDate(Long givenDateInMillis) {
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







}// end TimeMachineViewModel