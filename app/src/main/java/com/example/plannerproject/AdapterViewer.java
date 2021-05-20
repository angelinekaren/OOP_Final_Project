package com.example.plannerproject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class AdapterViewer extends RecyclerView.Adapter<AdapterViewer.MyViewHolder> {

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private List<TaskModel> taskModelList;
    MainActivity activity;
    private OnTaskListener onTaskListener;


    public AdapterViewer(MainActivity activity, List<TaskModel> taskModelList, OnTaskListener onTaskListener){
        this.activity = activity;
        this.taskModelList = taskModelList;
        this.onTaskListener = onTaskListener;
    }

    public void deleteTask(int position) {
        TaskModel taskModel = taskModelList.get(position);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userUid = user.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query deleteTaskQuery = ref.child("Tasks").child(userUid).orderByChild("taskId").equalTo(taskModel.getTaskId());

        deleteTaskQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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

        taskModelList.remove(position);
        notifyItemRangeRemoved(0, getItemCount());
    }

    public void editTask(int position) {
        TaskModel taskModel = taskModelList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("dateTime", taskModel.getDateTime());
        bundle.putString("task", taskModel.getTask());
        bundle.putString("id", taskModel.getTaskId());


        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(activity.getSupportFragmentManager(), addNewTask.getTag());

    }

    public Context getContext() {
        return activity;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_task_view, parent, false);
        return new MyViewHolder(view, onTaskListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewer.MyViewHolder holder, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userUid = user.getUid();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Tasks").child(userUid);

        TaskModel taskModel = taskModelList.get(position);

        holder.setDate(taskModel.getDateTime());
        holder.checkBox.setText(taskModel.getTask());
        holder.checkStatus(taskModel.getStatus());

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("status", 1);

                    ref.child(taskModel.getTaskId()).updateChildren(result);
                }
                else {
                    HashMap<String, Object> resultFalse = new HashMap<>();
                    resultFalse.put("status", 0);

                    ref.child(taskModel.getTaskId()).updateChildren(resultFalse);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        TextView getDate;
        CheckBox checkBox;
        ImageView timer;
        OnTaskListener onTaskListener;

        public MyViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            view = itemView;

            getDate = view.findViewById(R.id.due_date);
            checkBox = view.findViewById(R.id.materialCheckBox);
            timer = view.findViewById(R.id.timer);
            this.onTaskListener = onTaskListener;

            // Refer to the interface View.OnClickListener
            timer.setOnClickListener(this);

        }

        public void setDate(String date) {
            getDate.setText(date);
        }

        public void checkStatus(int status) {
            checkBox.setChecked(toBoolean(status));
        }

        public boolean toBoolean(int status) {
            return status != 0;
        }

        @Override
        public void onClick(View v) {
            onTaskListener.onTaskListener(getAdapterPosition());
        }
    }

    public interface OnTaskListener {
        void onTaskListener(int position);
    }

}
