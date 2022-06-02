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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class SignUp extends AppCompatActivity {


    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    EditText userNametxt, passwordtxt, checkpasswordtxt, emailtxt;
    static final String TAG ="SignUp";
    SharedPreferences preferences;
    User user;
    String imageViewlink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user");
        mAuth = FirebaseAuth.getInstance();
        emailtxt=findViewById(R.id.editTextTextPersonName2);
        userNametxt= findViewById(R.id.editTextTextPersonName3);
        passwordtxt= findViewById(R.id.editTextTextPassword2);
        checkpasswordtxt= findViewById(R.id.editTextTextPassword3);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


    }


    public void registerUser(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(SignUp.this, "Welcome! You are now signed up.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            personalizedNode(firebaseUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication failure!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
    //creates unique node for each user with his data
    public void personalizedNode(FirebaseUser currentUser){
        String initial=userNametxt.getText().toString().substring(0,1).toLowerCase();
        imageViewlink="usernamelogos/"+initial+".png";

        user = new User(emailtxt.getText().toString(),userNametxt.getText().toString(),currentUser.getUid());
        user.setImagelink(imageViewlink);
        myRef.child(currentUser.getUid()).setValue(user); //gets the data from User class and stores them to the new node



        SharedPreferences.Editor editor= preferences.edit();
        editor.putBoolean("isSignedIn",true).apply();
        Intent intent= new Intent(this,SplashActivity.class);
        intent.putExtra("uId",currentUser.getUid());
        startActivity(intent);
    }

    //savePersonalData to User class and register user
    public void savePersonalData(View view)
    {
        if(emailtxt.getText().length()!=0 || passwordtxt.getText().length()!=0  || userNametxt.getText().length()!=0  || checkpasswordtxt.getText().length()!=0 ) {
            if(passwordtxt.getText().toString().equals(checkpasswordtxt.getText().toString())){
                if(passwordtxt.getText().length()>=6)
                    registerUser(emailtxt.getText().toString(),passwordtxt.getText().toString());
            else Toast.makeText(this, "Password should be at least 6 characters!",Toast.LENGTH_SHORT).show();}
            else Toast.makeText(this, "Passwords don't match!",Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(this, "Please fill in all the fields!",Toast.LENGTH_SHORT).show();

    }

}