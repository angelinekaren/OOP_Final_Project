package com.example.plannerproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        bundle.putString("clockTime", taskModel.getClockTime());
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
        holder.setTaskTitle(taskModel.getTask());
        holder.setClock(taskModel.getClockTime());
        holder.checkStatus(taskModel.getStatus());

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("status", 1);

                    ref.child(taskModel.getTaskId()).updateChildren(result);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setMessage("Is it completed?").setTitle("Completed Task")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                    DatabaseReference reference = firebaseDatabase.getReference().child("Completed").child(userUid);

                                    ref.child(taskModel.getTaskId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            reference.child(taskModel.getTaskId()).setValue(snapshot.getValue(), new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                    deleteTask(position);
                                                    if (error != null) {
                                                        Toast.makeText(getContext(), "Successfully moved to completed task!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else {
                                                        Toast.makeText(getContext(), "Process failed!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    notifyItemChanged(position);
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
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
        TextView getTaskTitle;
        TextView getDate;
        TextView getClock;
        CheckBox checkBox;
        ImageView timer;
        OnTaskListener onTaskListener;

        public MyViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            view = itemView;

            getTaskTitle = view.findViewById(R.id.taskTitle);
            getDate = view.findViewById(R.id.due_date);
            getClock = view.findViewById(R.id.due_time);
            checkBox = view.findViewById(R.id.materialCheckBox);
            timer = view.findViewById(R.id.timer);
            this.onTaskListener = onTaskListener;

            // Refer to the interface View.OnClickListener
            timer.setOnClickListener(this);

        }

        public void setTaskTitle(String taskTitle) {
            getTaskTitle.setText(taskTitle);
        }

        public void setDate(String date) {
            getDate.setText(date);
        }

        public void setClock(String clock) {
            getClock.setText(clock);
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
