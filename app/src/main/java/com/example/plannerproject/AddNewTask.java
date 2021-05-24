package com.example.plannerproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;



public class AddNewTask extends BottomSheetDialogFragment implements View.OnClickListener {
    public static final String TAG = "addNewTask";
    private EditText getTaskInput;
    private TextView getSetDate;
    private TextView getSetPriority;
    private TextView getSetClock;
    private Calendar calendar;
    private Button getSaveButton;
    private DatabaseReference ref;
    private Context context;
    private String getDate;
    private String getTime;
    private String id;
    private String myPriority;
    private String taskId;
    private boolean isUpdated = false;

    // Constructor
    // AddNewTask class extends Fragment where it needs to have no arguments constructor / no constructor at all
    // This helper function is used to create new instance of AddNewTask class
    public static AddNewTask newInstance() {
        return new AddNewTask();
    }


    // Inflating / adding dialog_add_task.xml view to activity on runtime  (show BottomSheetDialogFragment)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_task, container, false);
    }

    // Initialize view setup -> fragment's root view is non-null
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialization
        getTaskInput = view.findViewById(R.id.addTaskTextInput);
        getSetDate = view.findViewById(R.id.setDate);
        getSetClock = view.findViewById(R.id.setTime);
        getSaveButton = view.findViewById(R.id.saveButton);
        ImageView getCalBtn = view.findViewById(R.id.calendar_button);
        ImageView getClockBtn = view.findViewById(R.id.clock_button);
        ImageView getFlagBtn = view.findViewById(R.id.flag_button);
        getSetPriority = view.findViewById(R.id.setPriority);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Get the current logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get the current logged in user uid
        String userUid = user.getUid();

        // Initialize database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Get reference from the database: Tasks - userUid (current user)
        ref = database.getReference().child("Tasks").child(userUid);

        // Adding text changed listener in EditText input (take input for task title)
        getTaskInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if EditText is empty/not
                if(s.toString().equals("")) {
                    // If it's empty, disabled the save button and set them to gray
                    getSaveButton.setEnabled(false);
                    getSaveButton.setBackgroundColor(Color.GRAY);
                } else {
                    // Else, enabled the save button and set them to the original color
                    getSaveButton.setEnabled(true);
                    getSaveButton.setBackgroundColor(getResources().getColor(R.color.register_background));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Call the updatedTask() function
        updatedTask();

        // Set onClickListener to every view's item
        getCalBtn.setOnClickListener(this);
        getSaveButton.setOnClickListener(this);
        getClockBtn.setOnClickListener(this);
        getFlagBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            // If calendar icon is clicked,
            case(R.id.calendar_button): {
                // Call viewCalendar() function
                viewCalendar();
                break;
            }
            // If save button is clicked,
            case(R.id.saveButton) : {
                // Call setNewTask() function
                setNewTask();
                break;
            }
            // If clock icon is clicked,
            case(R.id.clock_button) : {
                // Call viewTime() function
                viewTime();
                break;
            }
            // If flag icon is clicked,
            case(R.id.flag_button) : {
                // Call setPriority() function
                setPriority();
                break;
            }
        }
    }

    // Function to update the task view
    private void updatedTask() {
        // Retrieve the data
        final Bundle bundle = getArguments();
        // Check whether the passing data is empty
        // If it's not empty,
        if (bundle != null) {
            // Set boolean isUpdated to true
            isUpdated = true;
            // Retrieve the passing data
            String task = bundle.getString("task");
            taskId = bundle.getString("id");
            String date = bundle.getString("dateTime");
            String clock = bundle.getString("clockTime");
            String priority = bundle.getString("priority");

            // Set the new updated data
            getSetClock.setText(clock);
            getSetDate.setText(date);
            getTaskInput.setText(task);
            getSetPriority.setText(priority);

            // Check if task title is empty
            if (task.length() > 0) {
                // If it's empty disable the save button and set them to gray
                getSaveButton.setEnabled(false);
                getSaveButton.setBackgroundColor(Color.GRAY);
            }

        }
    }

    // Function to set new task
    private void setNewTask() {
        // Get the task title inputted by user
        String taskName = getTaskInput.getText().toString();
        // Get the taskId key
        id = ref.push().getKey();

        // Check if current condition is updating an existing task
        if (isUpdated) {
            // Create and put updated data in a HashMap
            // Data have different data types, hence we use HashMap<String, Object>
            HashMap<String, Object> result = new HashMap<>();
            result.put("task", taskName);
            result.put("dateTime", getDate);
            result.put("clockTime", getTime);
            result.put("priority", myPriority);

            // Pass this data at reference: Tasks - userUid (current user) - taskId and update
            ref.child(taskId).updateChildren(result)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Show success message: Task updated
                            Toast.makeText(context, "Task updated!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Show failed message: Error updating
                            Toast.makeText(getActivity(), "Error updating!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        // Else, if the current condition is creating new task
        else {
            // If task title input in EditText is empty
            if (taskName.isEmpty()) {
                // Create a message requiring user to fill it in
                Toast.makeText(context, "Required to fill in new task!", Toast.LENGTH_SHORT).show();
            }
            // Else, if it's not empty
            else {
                // Create new task with its fields: id, taskName, date, time, status, and priority
                TaskModel taskModel = new TaskModel(id, taskName, getDate, getTime, 0, myPriority);

                // Add and set the value in the database by reference Tasks - userUid - taskId
                ref.child(id).setValue(taskModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    // If the action is completed,
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // If setting new task is successful,
                        if (task.isSuccessful()) {
                            // Create success message: Task successfully saved!
                            Toast.makeText(context, "Task successfully saved!", Toast.LENGTH_SHORT).show();
                        }
                        // Else, if it failed to set new task
                        else {
                            // Create failed message: Failed to save task!
                            Toast.makeText(context, "Failed to save task!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    // Else, if it failed, create error message
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
        // Dismiss the BottomSheetDialogFragment
        dismiss();
    }

    // Function to set priority
    private void setPriority() {
        // Inflating / adding dialog_set_priority.xml view to activity on runtime
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_priority, null);

        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the view, create positive button and negative button
        builder.setView(dialogView).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            // If positive button is clicked,
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Initialize item to dialogView
                RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
                // Get the unique id of the selected radio button
                int choose = radioGroup.getCheckedRadioButtonId();
                // Initialize item to dialogView
                RadioButton selectedRadioButton = dialogView.findViewById(choose);
                // Get the text of selected radio button
                myPriority = selectedRadioButton.getText().toString();
                // Set text
                getSetPriority.setText(myPriority);
                // Cancel the dialog
                dialog.cancel();
            }
            // If negative button is clicked,
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Function to view calendar picker dialog
    private void viewCalendar() {
        // Define variable name with integer data type
        int year, month, day;

        // Get current year, month, and day. Assign it to the variables defined
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);

        // Create DatePickerDialog, setting context, implementation of the listener, and start year, month, and day
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Since it starts counting months from 0, it'll return month value - 1
                // For example, we picked September (the 9th month), it returns 9-1 = 8 -> August
                // Hence, we need to add 1 to the month value
                month += 1;
                // Format the date -> 09/04/2021
                getDate = dayOfMonth + "/" + month + "/" + year;
                // Set the TextView with date
                getSetDate.setText(getDate);
            }
        }, year, month, day);

        // Disabled all the passed date in current month and year
        // Sets today's date as minimum date, hence all the past dates are disabled
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    // Function to view time picker dialog
    private void viewTime() {
        // Get the current time in milliseconds
        final long date = System.currentTimeMillis();

        // Set format date
        SimpleDateFormat timeSdf = new SimpleDateFormat("hh : mm a"); // 12:08 PM
        // Get the current time in the format
        String timeString = timeSdf.format(date);
        // Set the TextView with time
        getSetClock.setText(timeString);

        // Set the default time with current time
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Define variables with integer data type
        int hour, minute;

        // Get current hour and minute. Assign it to variables defined
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        // Create TimePickerDialog, setting context, implementation of the listener,
        // and start hour, minute, and boolean is24HourView
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Since it is not a 24 hour view, we define for AM/PM
                // Format minute to 2 digits
                @SuppressLint("DefaultLocale") String minTime = String.format("%02d", minute);
                // Set for AM from 0 - 11
                if (hourOfDay >= 0 && hourOfDay < 12) {
                    getTime = hourOfDay + " : " + minTime + " AM";
                } else {
                    // If not 12, 13-23 decrement it by 12
                    // For example, 13 means 1 PM
                    if (hourOfDay != 12) {
                        hourOfDay = hourOfDay - 12;
                    }
                    // Set the format -> 01:00 PM
                    getTime = hourOfDay + " : " + minTime + " PM";
                }

                // Set the TextView with time
                getSetClock.setText(getTime);
                // Set the values for calendar fields hour_of_day, minute, and second
                calendar.set(Calendar.HOUR, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
            }
        }, hour, minute, false);

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    // Called when a fragment is connected to an activity
    // HomeFragment is attached to MainActivity
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