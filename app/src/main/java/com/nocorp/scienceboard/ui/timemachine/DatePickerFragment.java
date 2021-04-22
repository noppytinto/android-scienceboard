package com.nocorp.scienceboard.ui.timemachine;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private final String TAG = this.getClass().getSimpleName();
    private TimeMachineViewModel timeMachineViewModel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(requireActivity(), this, year, month, day);
        dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        dialog.setTitle("Go back in time!");

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        timeMachineViewModel = new ViewModelProvider(requireActivity()).get(TimeMachineViewModel.class);

        final Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        long pickedDate = c.getTimeInMillis();

        Log.d(TAG, "onDateSet: " + pickedDate);

        timeMachineViewModel.setPickedDate(pickedDate);
    }
}

