package com.example.plannerproject;

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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SetTime extends BottomSheetDialogFragment {
    public static final String TAG = "setTimeDialog";
    private EditText getTimeText;
    public static Button setTime;
    private Context context;

    public static SetTime newInstance() {
        return new SetTime();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_set_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getTimeText = view.findViewById(R.id.timeInput);
        setTime = view.findViewById(R.id.setButton);

        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputTime = getTimeText.getText().toString();
                if (inputTime.isEmpty()) {
                    Toast.makeText(context, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert minutes to milliseconds: * 60000
                long millisInput = Long.parseLong(inputTime) * 60000;
                if (millisInput == 0) {
                    Toast.makeText(context, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                TimerActivity.setTime(millisInput);
                getTimeText.setText("");
                dismiss();
            }
        });

    }
    
    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Activity activity = getActivity();
        if(activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener)activity).onDialogClose(dialog);
        }

    }
}
