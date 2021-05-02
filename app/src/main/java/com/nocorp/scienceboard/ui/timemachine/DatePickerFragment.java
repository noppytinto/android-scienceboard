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
    private Calendar givenCalendar;





    //------------------------------------------------------------------------------------- CONSTRUCTORS


    //------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        DatePickerDialog dialog = null;
        Bundle arguments = getArguments();

        if(arguments!=null) {
            Log.d(TAG, "initView: onCreateDialog: " + (long)arguments.get("givenDialogCalendarDate"));

            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis((long)arguments.get("givenDialogCalendarDate"));
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            Log.d(TAG, "onCreateDialog: " + day + "/" + month + "/" + year + "/" );

            // Create a new instance of DatePickerDialog and return it
            dialog = new DatePickerDialog(requireContext(), /*R.style.Theme_AppCompat_Dialog,*/ this, year, month, day);
            setupDatePickerDialog(dialog);
        }

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        TimeMachineViewModel timeMachineViewModel =
                new ViewModelProvider(requireActivity()).get(TimeMachineViewModel.class);

        Log.d(TAG, "onDateSet: " + day + "/" + month + "/" + year + "/" );


        final Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        long pickedDate = c.getTimeInMillis();

        Log.d(TAG, "onDateSet: " + pickedDate);
        timeMachineViewModel.setPickedDate(pickedDate);
    }





    //------------------------------------------------------------------------------------- METHODS

    private void setupDatePickerDialog(DatePickerDialog dialog) {
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.getDatePicker().setMinDate(1618869600000L); // 15 April 2021

//        dialog.setTitle(getString(R.string.date_picker_label));
    }

}// end DatePickerFragment

