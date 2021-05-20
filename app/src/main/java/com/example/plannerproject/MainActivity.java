package com.example.plannerproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    static final float END_SCALE = 0.7f;
    LinearLayout main_content;
    private FirebaseAuth mFirebaseAuth;
    private ProfileFragment profileFragment;
    private HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar_icon);
        main_content = findViewById(R.id.main_content);
        drawerLayout = findViewById(R.id.drawerLayout);

        // Sets toolbar as the app bar (position the toolbar at the top of the activity's layout)
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setting the NavigationIcon with ic_menu
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        // Bring navigationView to front -> make navigation item clickable
        navigationView.bringToFront();

        // Tie together the functionality of drawerLayout and the framework ActionBar
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Set a listener that will be notified when a menu item is selected
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();

        // Call function
        animateNavigationDrawer();

        mFirebaseAuth = FirebaseAuth.getInstance();

        if(savedInstanceState == null)
        {
            setFragment(homeFragment);
        }


    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }


    // Back Pressed: go back to this MainActivity page
    @Override
    public void onBackPressed() {
        // When the drawerLayout is open, when back pressed, close the drawerLayout (MainActivity page)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        // Else, as initial, back to home screen
        else {
            super.onBackPressed();
        }
    }

    // Function when navigation item is selected
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.home):
                navigationView.setCheckedItem(R.id.home);
                setFragment(homeFragment);
                break;
            case (R.id.profile):
                navigationView.setCheckedItem(R.id.profile);
                setFragment(profileFragment);
                break;
            case (R.id.logout):
                logout();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Function for Navigation View animation
    private void animateNavigationDrawer() {
        // Set faded background in MainActivity page when Navigation is on view
        drawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            // When the drawer layout is sliding
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Scale the view based on current slide offset

                final float difference_scale = slideOffset * (1-END_SCALE);
                final float offset = 1 - difference_scale;

                // Scale window size according to the view's center
                main_content.setScaleX(offset);
                main_content.setScaleY(offset);

                final float x_offset = drawerView.getWidth() * slideOffset;
                final float x_offset_diff = main_content.getWidth() * difference_scale / 2;
                final float x_translation = x_offset - x_offset_diff;

                // Sets the horizontal location of this view relative to its left position
                main_content.setTranslationX(x_translation);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            // User logged in
        }
        else {
            // No one logged in
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void logout() {
        mFirebaseAuth.signOut();
        finish();
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

}