package com.example.socialcontribution;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

public class GuestDialog extends Dialog {
    Activity activity;
    Dialog dialog;
    CardView signin,signup;


    public GuestDialog(@NonNull Activity activity) {
        super(activity);
        this.activity=activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.guestcard);
        signin = findViewById(R.id.signin);
        signup = findViewById(R.id.signup);
        signin.setOnClickListener(v->redirectActivity(activity,SignIn.class));
        signup.setOnClickListener(v->redirectActivity(activity,SignUp.class));

    }

    public void redirectActivity(Activity activity, Class theClass) {
        //Initialize intent
        Intent intent = new Intent(activity, theClass);
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
        activity.finish();

    }
}
