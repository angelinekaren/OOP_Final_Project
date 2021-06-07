package com.example.plannerproject.ObjectHandlers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.plannerproject.Activities.TimerActivity;
import com.example.plannerproject.Listener.OnDialogCloseListener;
import com.example.plannerproject.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class SetTime extends BottomSheetDialogFragment {
    public static final String TAG = "setTimeDialog";
    private EditText getTimeText;
    public static Button setTime;
    private Context context;

    // Constructor
    // SetTime class extends Fragment where it needs to have no arguments constructor / no constructor at all
    // This helper function is used to create new instance of SetTime class
    public static SetTime newInstance() {
        return new SetTime();
    }

    // Inflating / adding dialog_set_timer.xml view to activity on runtime  (show BottomSheetDialogFragment)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_set_timer, container, false);
    }

    // Initialize view setup -> fragment's root view is non-null
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialization
        getTimeText = view.findViewById(R.id.timeInput);
        setTime = view.findViewById(R.id.setButton);

        // Set onClick listener to setTime button
        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When clicked,

                // Get the inputted time
                String inputTime = getTimeText.getText().toString();

                // If there is no input / empty
                if (inputTime.isEmpty()) {
                    // Create a toast message
                    Toast.makeText(context, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert minutes to milliseconds: * 60000
                long millisInput = Long.parseLong(inputTime) * 60000;
                // If millisInput == 0, create toast message
                if (millisInput == 0) {
                    Toast.makeText(context, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Set the time
                TimerActivity.setTime(millisInput);
                getTimeText.setText("");
                // Dismiss the dialog
                dismiss();
            }
        });

    }

    // Called when a fragment is connected to an activity
    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        this.context = context;
    }

    // Dismiss the dialog
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Activity activity = getActivity();
        if(activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener)activity).onDialogClose(dialog);
        }

    }
}
