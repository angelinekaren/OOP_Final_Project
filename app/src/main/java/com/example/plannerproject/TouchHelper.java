package com.example.plannerproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


// Class for task onSwipe
// ItemTouchHelper.SimpleCallback: simple wrapper to the default Callback which you can construct
// with drag and swipe directions and this class will handle the flag callbacks
public class TouchHelper extends ItemTouchHelper.SimpleCallback {
    private AdapterViewer adapter;

    // Constructor
    public TouchHelper(AdapterViewer adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    // Set onMove to false (not allow dragging items)
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    // onSwiped for swiping action
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Get the adapter's position of the holder. It always have the updated position
        final int position = viewHolder.getAdapterPosition();
        // If swipe right,
        if(direction == ItemTouchHelper.RIGHT) {
            // Create alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());

            // Set the message, title, create positive button and negative button
            builder.setMessage("Are you sure you want to delete?").setTitle("Delete")
                    // If positive button is clicked,
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Call the deleteTask function to delete this certain task by its position
                            adapter.deleteTask(position);
                        }
                    })
                    // If negative button is clicked,
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // notifyItemChanged(): notify any registered observers that the item at position has changed
                            // revert swipe animation
                            adapter.notifyItemChanged(position);
                        }
                    });
            // Create and show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            // Else, if swipe right,
            // Call the editTask function to edit this certain task by its position
            adapter.editTask(position);
        }
    }

    // onChildDraw(): called by ItemTouchHelper on RecyclerView's onDraw callback
    // Override to customize how view's respond to user interactions (swipe)
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeRightActionIcon(R.drawable.ic_delete) // set icon for swipe right action
                .addSwipeRightBackgroundColor(Color.RED) // set red color for swipe right action
                .addSwipeLeftActionIcon(R.drawable.ic_edit) // set icon for swipe left action
                .addSwipeLeftBackgroundColor(Color.GREEN) // set green color for swipe right action
                .create()
                .decorate();
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
