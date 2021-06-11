package com.example.plannerproject.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.plannerproject.ObjectHandlers.AddNewTask;
import com.example.plannerproject.Activities.MainActivity;
import com.example.plannerproject.Model.TaskModel;
import com.example.plannerproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

// AdapterViewer class: retrieve data from the data set and for generating View objects based on that data
public class AdapterViewer extends RecyclerView.Adapter<AdapterViewer.MyViewHolder> {
    private List<TaskModel> taskModelList;
    private MainActivity activity;
    private OnTaskListener onTaskListener;

    // Constructor: activity, List objects of TaskModel, OnTaskListener (for each object view onClick)
    public AdapterViewer(MainActivity activity, List<TaskModel> taskModelList, OnTaskListener onTaskListener){
        this.activity = activity;
        this.taskModelList = taskModelList;
        this.onTaskListener = onTaskListener;
    }

    // Function to delete a specific task (define by position/index)
    public void deleteTask(int position) {
        // Get the object taskModel by its index/position inside taskModelList
        TaskModel taskModel = taskModelList.get(position);

        // Get the current logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get the current logged in user uid
        String userUid = user.getUid();

        // Initialize database and reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        // Query the database by Tasks - userUid - order the child by taskId
        // Search this object taskModel inside the database by its taskId
        Query deleteTaskQuery = ref.child("Tasks").child(userUid).orderByChild("taskId").equalTo(taskModel.getTaskId());

        // Delete all value of this object taskModel inside the database
        deleteTaskQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appleSnapshot: snapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Remove this object taskModel from taskModelList
        taskModelList.remove(position);
        // Notify any registered observers that the itemCount items previously
        // located at positionStart have been removed(deleted) from the data set
        notifyItemRangeRemoved(0, getItemCount());
    }

    // Function to edit a specific task (define by position/index)
    public void editTask(int position) {
        // Get the object taskModel by its index/position inside taskModelList
        TaskModel taskModel = taskModelList.get(position);

        // Pass data (key-value pair)
        Bundle bundle = new Bundle();
        bundle.putString("dateTime", taskModel.getDateTime());
        bundle.putString("clockTime", taskModel.getClockTime());
        bundle.putString("task", taskModel.getTask());
        bundle.putString("id", taskModel.getTaskId());
        bundle.putString("priority", taskModel.getPriority());

        // Create an instance of AddNewTask class
        AddNewTask addNewTask = new AddNewTask();
        // Supply the construction arguments for the instance
        addNewTask.setArguments(bundle);
        // Display dialog
        addNewTask.show(activity.getSupportFragmentManager(), addNewTask.getTag());

    }

    // getContext(): returns the Context which is linked to the MainActivity
    public Context getContext() {
        return activity;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // onCreateViewHolder() called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item
        // new View with XML layout -> each_task_view
        // Apply onTaskListener to allow each view clickable to another activity
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_task_view, parent, false);
        return new MyViewHolder(view, onTaskListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewer.MyViewHolder holder, int position) {
        // onBindViewHolder() is called by RecyclerView to display the data at the specified position

        // Get current logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get current logged in user uid
        String userUid = user.getUid();

        // Instantiate database and get all tasks made by this user
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("Tasks").child(userUid);

        // Get the object taskModel by its index/position inside taskModelList
        TaskModel taskModel = taskModelList.get(position);

        // holder: the ViewHolder which should be updated to represent the contents of the item at the given position
        // in the data set
        // Set all the contents of the item at given position: date, taskTitle, setClock, status, priority
        holder.setDate(taskModel.getDateTime());
        holder.setTaskTitle(taskModel.getTask());
        holder.checkStatus(taskModel.getStatus());
        holder.setClock(taskModel.getClockTime());
        holder.checkPriority(taskModel.getPriority());

        // Create an alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // When check box is checked,
                if (isChecked) {
                    // Set message, title, positive button, and negative button
                    builder.setMessage("Is it completed?").setTitle("Completed Task")
                            // If positive button is click,
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    HashMap<String, Object> result = new HashMap<>();
                                    result.put("status", 1);

                                    // Update the status to 1 inside the database
                                    reference.child(taskModel.getTaskId()).updateChildren(result);
                                }
                            })
                            // If negative button is clicked,
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // the item at position retains the same identity
                                    notifyItemChanged(position);
                                }
                            });
                    // Create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    // getItemCount() returns the total number of items in the data set held by the adapter
    @Override
    public int getItemCount() {
        return taskModelList.size();
    }

    // Class for each View objects
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView getTaskTitle;
        private final ImageView getFlagPriority;
        private final TextView getDate;
        private final TextView getClock;
        private final CheckBox checkBox;
        private final OnTaskListener onTaskListener;

        public MyViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);

            // Initialization
            getTaskTitle = itemView.findViewById(R.id.taskTitle);
            getDate = itemView.findViewById(R.id.due_date);
            getClock = itemView.findViewById(R.id.due_time);
            checkBox = itemView.findViewById(R.id.materialCheckBox);
            ImageView timer = itemView.findViewById(R.id.timer);
            getFlagPriority = itemView.findViewById(R.id.flagPriority);
            this.onTaskListener = onTaskListener;

            // Refer to the interface View.OnClickListener
            timer.setOnClickListener(this);

        }

        // Set task title
        public void setTaskTitle(String taskTitle) {
            getTaskTitle.setText(taskTitle);
        }

        // Set date
        public void setDate(String date) {
            getDate.setText(date);
        }

        // Set clock
        public void setClock(String clock) {
            getClock.setText(clock);
        }

        // Check status: 0/1
        public void checkStatus(int status) {
            checkBox.setChecked(toBoolean(status));
        }

        // If status != 0: return true
        public boolean toBoolean(int status) {
            return status != 0;
        }

        // Check priority set by user
        public void checkPriority(String priority) {
            // If user doesn't set any priority: default color
            if (priority.equals("Priority")) {
                Context context = getFlagPriority.getContext();
                int color = ContextCompat.getColor(context, R.color.register_background);
                getFlagPriority.setColorFilter(color);
                return;
            }
            // If priority: HIGH, set flag imageview color to red
            if(priority.equals("HIGH")) {
                getFlagPriority.setColorFilter(Color.RED);
            }
            // If priority: MEDIUM, set flag imageview color to yellow
            if(priority.equals("MEDIUM")) {
                getFlagPriority.setColorFilter(Color.YELLOW);
            }
            // If priority: LOW, set flag imageview color to green
            if(priority.equals("LOW")) {
                getFlagPriority.setColorFilter(Color.GREEN);
            }
        }

        // Function for certain View object onClick
        @Override
        public void onClick(View v) {
            onTaskListener.onTaskListener(getAdapterPosition());
        }
    }

    public interface OnTaskListener {
        void onTaskListener(int position);
    }
}
