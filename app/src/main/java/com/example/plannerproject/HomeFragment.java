package com.example.plannerproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plannerproject.Adapter.AdapterClass;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private FirebaseRecyclerAdapter<TaskModel, AddNewTask.MyViewHolder> adapter;
    private FirebaseDatabase database;
    private DatabaseReference ref;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);
        floatingActionButton = v.findViewById(R.id.addButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userUid = user.getUid();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Tasks").child(userUid);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getActivity().getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<TaskModel> options = new FirebaseRecyclerOptions.Builder<TaskModel>()
                .setQuery(ref, TaskModel.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<TaskModel, AddNewTask.MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AddNewTask.MyViewHolder holder, int position, @NonNull TaskModel model) {
                holder.setDate(model.getDateTime());
                holder.checkBox.setText(model.getTask());
                holder.checkStatus(model.getStatus());

                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("status", 1);

                            ref.child(model.getTaskId()).updateChildren(result);
                        }
                        else {
                            HashMap<String, Object> resultFalse = new HashMap<>();
                            resultFalse.put("status", 0);

                            ref.child(model.getTaskId()).updateChildren(resultFalse);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public AddNewTask.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_task_view, parent, false);
                return new AddNewTask.MyViewHolder(view);
            }
        };
    }
}
