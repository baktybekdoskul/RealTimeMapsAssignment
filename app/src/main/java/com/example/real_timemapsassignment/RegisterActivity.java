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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                            FirebaseUser user = mAuth.getCurrentUser();
                            String role = isAdmin ? "admin" : "client";
                            User user1 = new User(user.getEmail(), role);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            String key = database.getReference().child("users/").push().getKey();
                            database.getReference().child("users/").child(key).setValue(user1);
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
