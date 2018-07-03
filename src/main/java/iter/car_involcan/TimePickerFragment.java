package iter.car_involcan;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    private TimePickerDialog.OnTimeSetListener listener;

    public void init(TimePickerDialog.OnTimeSetListener listener){
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), listener, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }
}
