package com.example.plannerproject;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


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
    private DatabaseReference ref;
    private List<TaskModel> taskModelList;
    MainActivity activity;
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

        // Initialize database and get reference
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

        // Pass data (key-value pair) to HomeFragment
        Bundle bundle = new Bundle();
        bundle.putString("dateTime", taskModel.getDateTime());
        bundle.putString("clockTime", taskModel.getClockTime());
        bundle.putString("task", taskModel.getTask());
        bundle.putString("id", taskModel.getTaskId());
        bundle.putString("priority", taskModel.getPriority());

        // Create object from AddNewTask class
        AddNewTask addNewTask = new AddNewTask();
        // Supply the construction arguments for this HomeFragment
        addNewTask.setArguments(bundle);
        // getSupportFragmentManager:
        // return the FragmentManager for interacting with fragments associated with MainActivity (HomeFragment)
        // Show updated object
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
        ref = database.getReference().child("Tasks").child(userUid);

        // Get the object taskModel by its index/position inside taskModelList
        TaskModel taskModel = taskModelList.get(position);

        // holder: the ViewHolder which should be updated to represent the contents of the item at the given position
        // in the data set
        // Set all the contents of the item at given position: date, taskTitle, setClock, status, priority
        holder.setDate(taskModel.getDateTime());
        holder.setTaskTitle(taskModel.getTask());
        holder.setClock(taskModel.getClockTime());
        holder.checkStatus(taskModel.getStatus());
        holder.checkPriority(taskModel.getPriority());

        // When checkBox is checked
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("status", 1);

                    // Update the status to 1 inside the database
                    ref.child(taskModel.getTaskId()).updateChildren(result);

                    // Create an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    // Set message, title, positive button, and negative button
                    builder.setMessage("Is it completed?").setTitle("Completed Task")
                            // If positive button is click,
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Instantiate firebase database
                                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                    // Create a reference into the database Completed - userUid - all the completed tasks
                                    DatabaseReference reference = firebaseDatabase.getReference().child("Completed").child(userUid);

                                    // Delete this completed task from the view
                                    deleteTask(position);

                                    // Get this certain completed task
                                    ref.child(taskModel.getTaskId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            // Add it to new reference
                                            reference.child(taskModel.getTaskId()).setValue(snapshot.getValue(), new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                    Toast.makeText(getContext(), "Successfully moved to completed task!", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
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
                else {
                    // If not checked, status is 0
                    HashMap<String, Object> resultFalse = new HashMap<>();
                    resultFalse.put("status", 0);

                    ref.child(taskModel.getTaskId()).updateChildren(resultFalse);
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
        View view;
        TextView getTaskTitle;
        ImageView getFlagPriority;
        TextView getDate;
        TextView getClock;
        CheckBox checkBox;
        ImageView timer;
        OnTaskListener onTaskListener;

        public MyViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            view = itemView;

            // Initialization
            getTaskTitle = view.findViewById(R.id.taskTitle);
            getDate = view.findViewById(R.id.due_date);
            getClock = view.findViewById(R.id.due_time);
            checkBox = view.findViewById(R.id.materialCheckBox);
            timer = view.findViewById(R.id.timer);
            getFlagPriority = view.findViewById(R.id.flagPriority);
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
            if (priority == null) {
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
