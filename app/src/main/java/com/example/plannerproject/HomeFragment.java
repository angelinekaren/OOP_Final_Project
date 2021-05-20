package com.example.plannerproject;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment implements OnDialogCloseListener, AdapterViewer.OnTaskListener {
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private List<TaskModel> taskModelList;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private AdapterViewer adapter;



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
        recyclerView.setLayoutManager(new LinearLayoutManager((MainActivity) getActivity()));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getActivity().getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        taskModelList = new ArrayList<>();
        showData();

        adapter = new AdapterViewer((MainActivity) getActivity(), taskModelList, this::onTaskListener );
        recyclerView.setAdapter(adapter);


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);



        return v;
    }

    private void showData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskModelList.clear();
                for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                    TaskModel taskModel = npsnapshot.getValue(TaskModel.class);
                    taskModelList.add(taskModel);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        taskModelList.clear();
        showData();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskListener(int position) {
        taskModelList.get(position);
        TaskModel taskModel = taskModelList.get(position);
        Intent intent = new Intent(getActivity(), TimerActivity.class);
        intent.putExtra("task", taskModel.getTask());
        intent.putExtra("dateTime", taskModel.getDateTime());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}