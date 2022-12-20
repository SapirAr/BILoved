package com.example.datingappmine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
            finish();
        }

        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.pass);
        Button loginBtn = findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString();
                String userPass = password.getText().toString();

                mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (task.isComplete()) {
                                        String deviceToken = task.getResult();
                                        usersRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(MainActivity.this, getString(R.string.sign_in_compl), Toast.LENGTH_SHORT).show();
                                                    FirebaseMessaging.getInstance().subscribeToTopic(currentUserId);
                                                    Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.sign_in_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        Button registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString();
                String userPass = password.getText().toString();

                mAuth.createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (task.isComplete()) {
                                        String deviceToken = task.getResult();
                                        usersRef.child(currentUserId).child(currentUserId).child("device_token").setValue(deviceToken);
                                    }
                                }
                            });

                            Toast.makeText(MainActivity.this, getString(R.string.register_compl), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                            startActivity(intent);
                            finish();
                        } else
                            Toast.makeText(MainActivity.this, getString(R.string.register_fail), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button guestBtn = findViewById(R.id.guest);
        guestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, getString(R.string.welcome_guest), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    new MainActivity();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}