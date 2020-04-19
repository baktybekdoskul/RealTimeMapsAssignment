package com.example.real_timemapsassignment;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SignedInActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, ValueEventListener {

    private DrawerLayout drawerLayout;
    private Boolean isAdm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.signed_in_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        hideAdminMenuItem();
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MapsFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_maps);
            TextView emailInfo = findViewById(R.id.email_navheader);
            String email = getIntent().getStringExtra("email");
            if(emailInfo != null)
                emailInfo.setText(email);
        }
    }

    private void hideAdminFragment( NavigationView navigationView){
        navigationView.getMenu().findItem(R.id.nav_admin).setVisible(false);
    }

    private void hideAdminMenuItem() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("users/");
        isAdm = false;
        reference.addListenerForSingleValueEvent(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void signout() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_admin:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AdminFragment()).commit();
                break;
            case R.id.nav_maps:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MapsFragment()).commit();
                break;
            case R.id.signout:
                signout();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(dataSnapshot.exists()) {
            for (DataSnapshot dss: dataSnapshot.getChildren()) {
                User user2 = dss.getValue(User.class);
                if(user2.getEmail().equals(user.getEmail()) && user2.getRole().equals("admin")){
                    isAdm = true;
                    break;
                }
            }
        }
        if(!isAdm) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            hideAdminFragment(navigationView);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
