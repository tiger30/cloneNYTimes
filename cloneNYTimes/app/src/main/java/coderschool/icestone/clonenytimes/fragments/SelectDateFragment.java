package coderschool.icestone.clonenytimes.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by IceStone on 3/23/2016.
 */
public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private DatePickerDialog.OnDateSetListener settingsChangeListener;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            settingsChangeListener = (DatePickerDialog.OnDateSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnDateSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();
        Bundle b = getArguments();
        if (b != null) {
            if (b.containsKey("beginDateTimestamp")) {
                Long beginDateTimestamp = b.getLong("beginDateTimestamp");
                if (beginDateTimestamp != null) {
                    calendar.setTimeInMillis(beginDateTimestamp);
                }
            }
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        settingsChangeListener.onDateSet(view, yy, mm, dd);
    }

}