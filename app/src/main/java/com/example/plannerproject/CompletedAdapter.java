package com.example.plannerproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CompletedAdapter extends RecyclerView.Adapter<CompletedAdapter.CompletedViewHolder> {
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private List<TaskModel> taskModelCompletedList;
    MainActivity activity;


    public CompletedAdapter(MainActivity activity, List<TaskModel> taskModelCompletedList){
        this.activity = activity;
        this.taskModelCompletedList = taskModelCompletedList;
    }

    @NonNull
    @Override
    public CompletedAdapter.CompletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_completed_view, parent, false);
        return new CompletedAdapter.CompletedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedAdapter.CompletedViewHolder holder, int position) {
        TaskModel taskModel = taskModelCompletedList.get(position);

        holder.setDate(taskModel.getDateTime());
        holder.setTask(taskModel.getTask());
        holder.setClock(taskModel.getClockTime());
        holder.checkPriority(taskModel.getPriority());
    }

    @Override
    public int getItemCount() {
        return taskModelCompletedList.size();
    }

    public Context getContext() {
        return activity;
    }

    public void deleteCompletedTask(int position) {
        TaskModel taskModel = taskModelCompletedList.get(position);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userUid = user.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query deleteCompletedTaskQuery = ref.child("Completed").child(userUid).orderByChild("taskId").equalTo(taskModel.getTaskId());

        deleteCompletedTaskQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appleSnapshot: snapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println(error.toException());
            }
        });

        taskModelCompletedList.remove(position);
        notifyItemRangeRemoved(0, getItemCount());
    }

    public class CompletedViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView getDate;
        TextView getTask;
        TextView getClock;
        ImageView getFlag;

        public CompletedViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            getTask = view.findViewById(R.id.taskCompleted);
            getDate = view.findViewById(R.id.dateCompleted);
            getClock = view.findViewById(R.id.timeCompleted);
            getFlag = view.findViewById(R.id.flagPriorityCompleted);

        }

        public void setDate(String date) {
            getDate.setText(date);
        }

        public void setTask(String task) {
            getTask.setText(task);
        }

        public void setClock(String clock) {
            getClock.setText(clock);
        }

        public void checkPriority(String priority) {
            if (priority == null) {
                return;
            }
            if(priority.equals("HIGH")) {
                getFlag.setColorFilter(Color.RED);
            }
            if(priority.equals("MEDIUM")) {
                getFlag.setColorFilter(Color.YELLOW);
            }
            if(priority.equals("LOW")) {
                getFlag.setColorFilter(Color.GREEN);
            }
        }
    }
}
