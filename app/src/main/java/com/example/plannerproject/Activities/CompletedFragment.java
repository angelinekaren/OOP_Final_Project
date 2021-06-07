package com.example.plannerproject.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.plannerproject.Adapter.CompletedAdapter;
import com.example.plannerproject.Helpers.CompletedTouchHelper;
import com.example.plannerproject.Model.TaskModel;
import com.example.plannerproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

// Fragment to view all completed tasks
public class CompletedFragment extends Fragment {
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private List<TaskModel> taskModelCompletedList;
    private Query completedTask;
    private CompletedAdapter adapter;

    // onCreateView(): called to have the fragment instantiate its user interface view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflating / adding fragment_completed.xml view layout for this fragment
        View v = inflater.inflate(R.layout.fragment_completed, container, false);

        // Initialization
        recyclerView = v.findViewById(R.id.recyclerCompleted);
        floatingActionButton = v.findViewById(R.id.deleteAllCompletedButton);

        // Get the current logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get the current logged in user uid
        String userUid = user.getUid();

        // Initialize database and create reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        // Query the database by Tasks - userUid - order the child by status
        // Search this object taskModel inside the database by its status equal to 1 (completed tasks)
        completedTask = ref.child("Tasks").child(userUid).orderByChild("status").equalTo(1);

        // Avoid unnecessary layout passes
        // Changing the contents of the adapter doesn't change it's height or the width
        recyclerView.setHasFixedSize(true);
        // Set the layout of the contents -> list of repeating views in the recycler view
        // LinearLayoutManager: create a vertical LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager((MainActivity) getActivity()));

        // Create an arraylist to store the completed task
        taskModelCompletedList = new ArrayList<>();
        // Call showData() function
        showData();

        // Create new instance of CompletedAdapter to set and display completed data in List
        adapter = new CompletedAdapter((MainActivity) getActivity(), taskModelCompletedList);
        // Set adapter to the recycler view
        recyclerView.setAdapter(adapter);

        // Create new instance of ItemTouchHelper to add swipe-to-dismiss in each of the data in the recyclerview
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CompletedTouchHelper(adapter));
        // Attach it to the recycler view
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // onClick floating action button, call deleteAllCompletedTask() function
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllCompletedTask();
            }
        });

        // Call hideFab() function
        hideFab();

        return v;
    }

    // Function to show / display all completed data inside the list
    private void showData() {
        // ValueEventListener to a list of data will return the entire list of data as a single DataSnapshot,
        // which you can then loop over to access individual children
        // Create a value event listener to the database reference
        completedTask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear or initialize list repeatedly before adding new list from firebase
                taskModelCompletedList.clear();
                for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                    // Get value
                    TaskModel taskModel = npsnapshot.getValue(TaskModel.class);
                    // Add it to taskModelCompletedList
                    taskModelCompletedList.add(taskModel);
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
                if (dy > 0 ||dy < 0 && floatingActionButton.isShown())
                {
                    // Hide the floating action button
                    floatingActionButton.hide();
                }
            }
        });
    }

    // Function to delete all completed task
    private void deleteAllCompletedTask() {
        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the message, title , create positive button and negative button
        builder.setMessage("Are you sure you want to delete all your completed task?").setTitle("Delete all task")
                // If positive button is clicked,
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete all value of this completed tasks by the query
                        completedTask.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // set value to null
                                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                    dataSnapshot.getRef().setValue(null);
                                }
                                // Create success message
                                Toast.makeText(getActivity(), "Successfully delete all task!", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                })
                // Else, if negative button is clicked,
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}