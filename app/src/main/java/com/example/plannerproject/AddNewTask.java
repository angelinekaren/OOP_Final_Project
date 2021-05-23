package com.example.plannerproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.RenderScript;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddNewTask extends BottomSheetDialogFragment implements View.OnClickListener {
    public static final String TAG = "addNewTask";
    private EditText getTaskInput;
    private TextView getSetDate;
    private TextView getSetPriority;
    private TextView getSetClock;
    private Calendar calendar;
    private Button getSaveButton;
    private int selectedButtonId;
    private RadioButton selectedRadioButton;
    private ImageView getCalBtn, getClockBtn, getFlagBtn;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private Context context;
    private String getDate;
    private String getTime;
    private String dbDate, dbTask;
    private String id;
    private String myPriority;
    private String taskId;
    private boolean isUpdated = false;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getTaskInput = view.findViewById(R.id.addTaskTextInput);
        getSetDate = view.findViewById(R.id.setDate);
        getSetClock = view.findViewById(R.id.setTime);
        getSaveButton = view.findViewById(R.id.saveButton);
        getCalBtn = view.findViewById(R.id.calendar_button);
        getClockBtn = view.findViewById(R.id.clock_button);
        getFlagBtn = view.findViewById(R.id.flag_button);

        getSetPriority = view.findViewById(R.id.setPriority);

        calendar = Calendar.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userUid = user.getUid();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Tasks").child(userUid);

        getTaskInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if EditText is empty/not
                if(s.toString().equals("")) {
                    getSaveButton.setEnabled(false);
                    getSaveButton.setBackgroundColor(Color.GRAY);
                } else {
                    getSaveButton.setEnabled(true);
                    getSaveButton.setBackgroundColor(getResources().getColor(R.color.register_background));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        updatedTask();

        getCalBtn.setOnClickListener(this);
        getSaveButton.setOnClickListener(this);
        getClockBtn.setOnClickListener(this);
        getFlagBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case(R.id.calendar_button): {
                viewCalendar();
                break;
            }
            case(R.id.saveButton) : {
                setNewTask();
                break;
            }
            case(R.id.clock_button) : {
                viewTime();
                break;
            }
            case(R.id.flag_button) : {
                setPriority();
                break;
            }
        }
    }

    private void updatedTask() {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdated = true;
            String task = bundle.getString("task");
            taskId = bundle.getString("id");
            String date = bundle.getString("dateTime");
            String clock = bundle.getString("clockTime");
            String priority = bundle.getString("priority");

            getSetClock.setText(clock);
            getSetDate.setText(date);
            getTaskInput.setText(task);
            getSetPriority.setText(priority);

            if (task.length() > 0) {
                getSaveButton.setEnabled(false);
                getSaveButton.setBackgroundColor(Color.GRAY);
            }

        }
    }



    private void setNewTask() {
        String taskName = getTaskInput.getText().toString();
        id = ref.push().getKey();

        if (isUpdated) {

            HashMap<String, Object> result = new HashMap<>();
            result.put("task", taskName);
            result.put("dateTime", getDate);
            result.put("clockTime", getTime);
            result.put("priority", myPriority);


            ref.child(taskId).updateChildren(result)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Successfully updated
                            Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed
                            Toast.makeText(getActivity(), "Error updating", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            if (taskName.isEmpty()) {
                Toast.makeText(context, "Required to fill in new task!", Toast.LENGTH_SHORT).show();
            }
            else {
                TaskModel taskModel = new TaskModel(id, taskName, getDate, getTime, 0, myPriority);

                ref.child(id).setValue(taskModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Task successfully saved!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Failed to saved task", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
        dismiss();
    }

    private void setPriority() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_priority, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(dialogView).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
                int choose = radioGroup.getCheckedRadioButtonId();
                selectedRadioButton = dialogView.findViewById(choose);
                myPriority = selectedRadioButton.getText().toString();
                getSetPriority.setText(myPriority);
                dialog.cancel();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void viewCalendar() {

        int year, month, day;

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month += 1;
                getDate = dayOfMonth + "/" + month + "/" + year;
                getSetDate.setText(getDate);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    private void viewTime() {
        final long date = System.currentTimeMillis();

        SimpleDateFormat timeSdf = new SimpleDateFormat("hh : mm a"); // 12:08 PM
        String timeString = timeSdf.format(date);
        getSetClock.setText(timeString);

        calendar.setTimeInMillis(System.currentTimeMillis());

        int hour, minute;

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                @SuppressLint("DefaultLocale") String minTime = String.format("%02d", minute);
                if (hourOfDay >= 0 && hourOfDay < 12) {
                    getTime = hourOfDay + " : " + minTime + " AM";
                } else {
                    if (hourOfDay != 12) {
                        hourOfDay = hourOfDay - 12;
                    }
                    getTime = hourOfDay + " : " + minTime + " PM";
                }
                getSetClock.setText(getTime);
                calendar.set(Calendar.HOUR, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
            }
        }, hour, minute, false);

        timePickerDialog.show();
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


