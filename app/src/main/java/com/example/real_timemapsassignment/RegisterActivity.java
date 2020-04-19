package com.example.real_timemapsassignment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private Intent intent;
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private CheckBox isAdminCheckXox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
        setProgressBar(R.id.pBar);

        mEmailField = findViewById(R.id.fEmail);
        mPasswordField = findViewById(R.id.fPassword);
        isAdminCheckXox = findViewById(R.id.isadmin);
        findViewById(R.id.createAccountButton).setOnClickListener(this);
        findViewById(R.id.goBack).setOnClickListener(this);
        findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void createAccount(String email, String password, final Boolean isAdmin) {
//        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressBar();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            String role = isAdmin ? "admin" : "client";
                            final User user1 = new User(user.getEmail(), role);
                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                            if(database.getReference("users").getKey() == null){
                                List<User> users = new ArrayList<>();
                                users.add(user1);
                                database.getReference("users/").setValue(users);
                            }else{
                                database.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {
                                            List<User> users = new ArrayList<>();
                                            for (DataSnapshot dss: dataSnapshot.getChildren()) {
                                                User user2 = dss.getValue(User.class);
                                                users.add(user2);
                                            }
                                            users.add(user1);
                                            database.getReference("users/").setValue(users);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

//                            String key =
//                            database.getReference().child("users/").child(key).setValue(user1);
                            intent = new Intent(getApplicationContext(), SignedInActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed." + task.getResult().toString(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.createAccountButton) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString(), isAdminCheckXox.isChecked());
        } else if(i == R.id.goBack) {
            isAdminCheckXox.setChecked(false);
            mEmailField.setText("");
            mPasswordField.setText("");
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
