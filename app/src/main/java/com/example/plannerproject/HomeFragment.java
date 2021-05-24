package com.example.plannerproject;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

// Fragment where all tasks are viewed
public class HomeFragment extends Fragment implements OnDialogCloseListener, AdapterViewer.OnTaskListener {
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private List<TaskModel> taskModelList;
    private DatabaseReference ref;
    private AdapterViewer adapter;


    // onCreateView(): called to have the fragment instantiate its user interface view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflating / adding fragment_home.xml view layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialization
        recyclerView = v.findViewById(R.id.recyclerView);
        floatingActionButton = v.findViewById(R.id.addButton);

        // Get the current logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get the current logged in user uid
        String userUid = user.getUid();

        // Initialize database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Get reference from the database: Tasks - userUid (current user)
        ref = database.getReference().child("Tasks").child(userUid);

        // Avoid unnecessary layout passes
        // Changing the contents of the adapter doesn't change it's height or the width
        recyclerView.setHasFixedSize(true);
        // Set the layout of the contents -> list of repeating views in the recycler view
        // LinearLayoutManager: create a vertical LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager((MainActivity) getActivity()));

        // onClick floating action button, create new Instance of AddNewTask class
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getActivity().getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        // Call hideFab() function
        hideFab();

        // Create an arraylist to store the task
        taskModelList = new ArrayList<>();
        // Call showData() function
        showData();

        // Create new instance of AdapterViewer to set and display data in List
        adapter = new AdapterViewer((MainActivity) getActivity(), taskModelList, this::onTaskListener);
        // Set adapter to the recycler view
        recyclerView.setAdapter(adapter);

        // Create new instance of ItemTouchHelper to add swipe-to-dismiss in each of the data in the recyclerview
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        // Attach it to the recycler view
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return v;
    }

    // Function to show / display all data inside the list
    private void showData() {
        // ValueEventListener to a list of data will return the entire list of data as a single DataSnapshot,
        // which you can then loop over to access individual children
        // Create a value event listener to the database reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear or initialize list repeatedly before adding new list from firebase
                taskModelList.clear();
                for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                    // Get value
                    TaskModel taskModel = npsnapshot.getValue(TaskModel.class);
                    // Add it to taskModelList
                    taskModelList.add(taskModel);
                }
                // notifyDataSetChanged(): notify any registered observers that the data set has changed
                // Initially, when this adapter is constructed, it holds the reference for the list that was passed in
                // Here, we are going to pass in new task everytime the user wants to. However, adapter is still holding
                // reference to the original list and this adapter doesn't know that we made changes to the list
                // Hence, we need to notify everytime the list is updated (new task is created)
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Function to hide floating action button on scroll
    private void hideFab() {
        // Add on scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            // onScrollStateChanged(): callback method to be invoked when RecyclerView's scroll state changes
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                // If the recyclerview is not on scroll,
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    // Show the floating action button
                    floatingActionButton.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
            // onScrolled(): callback method to be invoked when the RecyclerView has been scrolled
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // dy: on vertical scroll
                // dy > 0: scrolled downwards, dy < 0: scrolled upwards
                // floatingActionButton.isShown(): is currently visible
                if (dy > 0 ||dy<0 && floatingActionButton.isShown())
                {
                    // Hide the floating action button
                    floatingActionButton.hide();
                }
            }
        });
    }

    // When dialog is closed,
    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        // Clear or initialize list
        taskModelList.clear();
        // Call showData() function
        showData();
        // notifyDataSetChanged(): notify any registered observers that the data set has changed
        // Initially, when this adapter is constructed, it holds the reference for the list that was passed in
        // Here, we are going to pass in new task everytime the user wants to. However, adapter is still holding
        // reference to the original list and this adapter doesn't know that we made changes to the list
        // Hence, we need to notify everytime the list is updated (new task is created)
        adapter.notifyDataSetChanged();
    }

    // Function for onTaskListener
    @Override
    public void onTaskListener(int position) {
        taskModelList.get(position);

        // Get the object taskModel by its index/position inside taskModelList
        TaskModel taskModel = taskModelList.get(position);

        // Invoke to start new activity and pass data
        Intent intent = new Intent(getActivity(), TimerActivity.class);
        // Pass values and retrieve them in the other Activity using keyName
        intent.putExtra("task", taskModel.getTask());
        intent.putExtra("dateTime", taskModel.getDateTime());
        intent.putExtra("clockTime", taskModel.getClockTime());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}