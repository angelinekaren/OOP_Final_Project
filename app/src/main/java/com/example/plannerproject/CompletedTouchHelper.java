package com.example.plannerproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plannerproject.Adapter.CompletedAdapter;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

// Class for completed task onSwipe
// ItemTouchHelper.SimpleCallback: simple wrapper to the default Callback which you can construct
// with drag and swipe directions and this class will handle the flag callbacks
public class CompletedTouchHelper extends ItemTouchHelper.SimpleCallback{
    private CompletedAdapter adapter;

    // Constructor
    public CompletedTouchHelper(CompletedAdapter adapter) {
        super(0, ItemTouchHelper.RIGHT);
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

        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());

        // Set the message, title, create positive button and negative button
        builder.setMessage("Are you sure you want to delete? Action cannot be undone").setTitle("Delete")
                // If positive button is clicked,
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Call the deleteCompletedTask function to delete this certain task by its position
                        adapter.deleteCompletedTask(position);
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

    // onChildDraw(): called by ItemTouchHelper on RecyclerView's onDraw callback
    // Override to customize how view's respond to user interactions (swipe)
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeRightActionIcon(R.drawable.ic_delete) // set icon for swipe right action
                .addSwipeRightBackgroundColor(Color.RED) // set red color for swipe right action
                .create()
                .decorate();
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
