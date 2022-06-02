package com.example.socialcontribution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SignIn extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText email,password;
    FirebaseUser currentUser;
    SharedPreferences preferences;
    FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        email=findViewById(R.id.editTextTextPersonName);
        password=findViewById(R.id.editTextTextPassword);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        if(preferences.getBoolean("isSignedIn",false)){
            Intent intent= new Intent(this,SplashActivity.class);
            intent.putExtra("uId",preferences.getString("uId",null));
            startActivity(intent);
            this.finish();

        }



    }
    public void signUpForm(View view){
        Intent signupIntent= new Intent(getApplicationContext(), SignUp.class);
        startActivity(signupIntent);
        this.finish();


    }
    public void continueasguest(View view){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isSignedIn", false).apply();
        Intent guestintent= new Intent(getApplicationContext(), SplashActivity.class);
        guestintent.putExtra("guest",true);
        startActivity(guestintent);
        this.finish();


    }
    public void login(View view){
        mAuth.signInWithEmailAndPassword(
                email.getText().toString(),password.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        firebaseDatabase=FirebaseDatabase.getInstance();
                        currentUser = mAuth.getCurrentUser();

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isSignedIn", true).apply();
                        editor.putString("uId", currentUser.getUid()).apply();
                        Intent intent= new Intent(this,SplashActivity.class);

                        intent.putExtra("uId",currentUser.getUid());
                        startActivity(intent);
                        this.finish();


                    }else {
                        Toast.makeText(getApplicationContext(),
                                task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

}