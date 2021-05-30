package com.example.plannerproject.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plannerproject.MainActivity;
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
import java.util.List;

// CompletedAdapter class: retrieve data from the data set and for generating View objects based on that data
public class CompletedAdapter extends RecyclerView.Adapter<CompletedAdapter.CompletedViewHolder> {
    private List<TaskModel> taskModelCompletedList;
    private MainActivity activity;

    // Constructor: activity, List objects of TaskModel
    public CompletedAdapter(MainActivity activity, List<TaskModel> taskModelCompletedList){
        this.activity = activity;
        this.taskModelCompletedList = taskModelCompletedList;
    }

    @NonNull
    @Override
    public CompletedAdapter.CompletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // onCreateViewHolder() called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item
        // new View with XML layout -> each_completed_view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_completed_view, parent, false);
        return new CompletedAdapter.CompletedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedAdapter.CompletedViewHolder holder, int position) {
        // onBindViewHolder() is called by RecyclerView to display the data at the specified position

        // Get the object taskModel by its index/position inside taskModelList
        TaskModel taskModel = taskModelCompletedList.get(position);

        // holder: the ViewHolder which should be updated to represent the contents of the item at the given position
        // in the data set
        // Set all the contents of the item at given position: date, taskTitle, setClock, priority
        holder.setDate(taskModel.getDateTime());
        holder.setTask(taskModel.getTask());
        holder.setClock(taskModel.getClockTime());
        holder.checkPriority(taskModel.getPriority());
    }

    // getItemCount() returns the total number of items in the data set held by the adapter
    @Override
    public int getItemCount() {
        return taskModelCompletedList.size();
    }

    // getContext(): returns the Context which is linked to the MainActivity
    public Context getContext() {
        return activity;
    }

    // Function to delete a specific completed task (define by position/index)
    public void deleteCompletedTask(int position) {
        // Get the object taskModel by its index/position inside taskModelCompletedList
        TaskModel taskModel = taskModelCompletedList.get(position);

        // Get the current logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get the current logged in user uid
        String userUid = user.getUid();

        // Initialize database and get reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        // Query the database by Completed - userUid - order the child by taskId
        // Search this object taskModel inside the database by its taskId
        Query deleteCompletedTaskQuery = ref.child("Tasks").child(userUid).orderByChild("taskId").equalTo(taskModel.getTaskId());

        // Delete all value of this object taskModel inside the database
        deleteCompletedTaskQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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

        // Remove this object taskModel from taskModelCompletedList
        taskModelCompletedList.remove(position);

        // Notify any registered observers that the itemCount items previously
        // located at positionStart have been removed(deleted) from the data set
        notifyItemRangeRemoved(0, getItemCount());
    }

    // Class for each View objects
    public static class CompletedViewHolder extends RecyclerView.ViewHolder {
        private final TextView getDate;
        private final TextView getTask;
        private final TextView getClock;
        private final ImageView getFlag;

        public CompletedViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialization
            getTask = itemView.findViewById(R.id.taskCompleted);
            getDate = itemView.findViewById(R.id.dateCompleted);
            getClock = itemView.findViewById(R.id.timeCompleted);
            getFlag = itemView.findViewById(R.id.flagPriorityCompleted);

        }

        // Set date
        public void setDate(String date) {
            getDate.setText(date);
        }

        // Set task title
        public void setTask(String task) {
            getTask.setText(task);
        }

        // Set clock
        public void setClock(String clock) {
            getClock.setText(clock);
        }

        // Check priority set by user
        public void checkPriority(String priority) {
            // If user doesn't set any priority: default color
            if (priority == null) {
                return;
            }
            // If priority: HIGH, set flag imageview color to red
            if(priority.equals("HIGH")) {
                getFlag.setColorFilter(Color.RED);
            }
            // If priority: MEDIUM, set flag imageview color to yellow
            if(priority.equals("MEDIUM")) {
                getFlag.setColorFilter(Color.YELLOW);
            }
            // If priority: LOW, set flag imageview color to green
            if(priority.equals("LOW")) {
                getFlag.setColorFilter(Color.GREEN);
            }
        }
    }
}
