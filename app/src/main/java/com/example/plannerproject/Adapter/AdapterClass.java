package com.example.plannerproject.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plannerproject.AddNewTask;
import com.example.plannerproject.HomeFragment;
import com.example.plannerproject.R;
import com.example.plannerproject.TaskModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class AdapterClass extends RecyclerView.Adapter<AdapterClass.MyViewHolder> {
    private List<TaskModel> taskModelList;
    private FragmentActivity homeFragment;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    public AdapterClass(FragmentActivity fragment, List<TaskModel> taskModelList) {
        this.taskModelList = taskModelList;
        homeFragment = fragment;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_task_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterClass.MyViewHolder holder, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userUid = user.getUid();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Tasks").child(userUid);

        TaskModel taskModel = taskModelList.get(position);
        holder.getDate.setText(taskModel.getDateTime());
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

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView getDate;
        CheckBox checkBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            getDate = view.findViewById(R.id.due_date);
            checkBox = view.findViewById(R.id.materialCheckBox);

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

    }
}
