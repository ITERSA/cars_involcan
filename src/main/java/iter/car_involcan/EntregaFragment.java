package iter.car_involcan;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EntregaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EntregaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EntregaFragment extends Fragment implements ManageData, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Spinner project_spinner, plate_spinner, driver_spinner;
    private TextView tvDate, tvTime;
    private EditText et_km, et_reason, et_trip, et_comment;

    private OnFragmentInteractionListener mListener;

    public EntregaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EntregaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EntregaFragment newInstance(String param1, String param2) {
        EntregaFragment fragment = new EntregaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recogida_fragment, container, false);
        plate_spinner =  (Spinner)view.findViewById(R.id.plate_spinner);
        project_spinner =  (Spinner)view.findViewById(R.id.project_spinner);
        driver_spinner =  (Spinner)view.findViewById(R.id.driver_spinner);

        et_km = (EditText)view.findViewById(R.id.et_km);
        et_km.setHint(getString(R.string.hint_km_final));
        et_comment = (EditText)view.findViewById(R.id.et_comment);
        et_reason = (EditText)view.findViewById(R.id.et_motivo);
        et_trip = (EditText)view.findViewById(R.id.edit_trayecto);

        Button button = (Button)view.findViewById(R.id.btSetDateTime);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDateTime();
            }
        });
        tvDate = (TextView)view.findViewById(R.id.tv_Date);
        tvTime = (TextView)view.findViewById(R.id.tv_Time);

        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        currentDateTime();
        String items[]  = {"item1" ,"item2," ,"item3", "item4"};
        updateSpinner(items, plate_spinner, getString(R.string.label_plate));
        updateSpinner(items, project_spinner, getString(R.string.label_project));
        updateSpinner(items, driver_spinner, getString(R.string.label_driver));
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public JSONObject getData() {
        JSONObject object = new JSONObject();
        try {
            object.put(getString(R.string.tag_json_matricula), plate_spinner.getSelectedItem().toString());
            object.put(getString(R.string.tag_json_conductor), driver_spinner.getSelectedItem().toString());
            object.put(getString(R.string.tag_json_proyecto), project_spinner.getSelectedItem().toString());
            object.put(getString(R.string.tag_json_trayecto), et_trip.getText().toString());
            object.put(getString(R.string.tag_json_motivo), et_reason.getText().toString());
            object.put(getString(R.string.tag_json_FechaInicial), ""+tvDate.getText().toString() + " " + tvTime.getText().toString());
            object.put(getString(R.string.tag_json_kminicial), et_km.getText().toString());
            object.put(getString(R.string.tag_json_Comentarios), et_comment.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public void setData(JSONObject json) {
        if (plate_spinner != null){
            updateSpinner(setJsonData(json, getString(R.string.tag_json_matricula)), plate_spinner, getString(R.string.label_plate));
        }
        if (project_spinner != null){
            updateSpinner(setJsonData(json, getString(R.string.tag_json_proyecto)), project_spinner, getString(R.string.label_project));
        }
        if (driver_spinner != null){
            updateSpinner(setJsonData(json, getString(R.string.tag_json_conductor)), driver_spinner, getString(R.string.label_driver));
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM MM dd, yyyy");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        tvDate.setText(dateFormat.format(cal.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker,  int hourOfDay, int minute) {
        SimpleDateFormat hourFormat = new SimpleDateFormat("h:mm a");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        tvTime.setText(hourFormat.format(cal.getTime()));
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void updateSpinner(String items[], Spinner spinner, CharSequence sequence){
        if (spinner != null){
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            if (sequence != null)
                spinner.setPrompt(sequence);
        }
    }
    public void showTimePickerDialog() {
        TimePickerFragment dialog = new TimePickerFragment();
        dialog.init(this);
        dialog.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog() {
        DatePickerFragment datePickerDialog = new DatePickerFragment();
        datePickerDialog.init(this);
        datePickerDialog.show(getFragmentManager(), "datePicker");
    }

    public void currentDateTime() {
        long date = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM MM dd, yyyy");
        String dateString = dateFormat.format(date);
        SimpleDateFormat hourFormat = new SimpleDateFormat("h:mm a");
        String hourString = hourFormat.format(date);
        tvDate.setText(dateString);
        tvTime.setText(hourString);
    }

    private String[] setJsonData(JSONObject json, String tag){
        String[] strings = null;
        try {
            JSONArray jsonArray = json.getJSONArray(tag);
            int length = jsonArray.length();
            if (length > 0){
                strings = new String[length];
                for (int i = 0; i < length; i++){
                    try {
                        strings[i] = jsonArray.getString(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strings;
    }
}
