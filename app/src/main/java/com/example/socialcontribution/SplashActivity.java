package com.example.socialcontribution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.socialcontribution.FirebaseData.fetchFavourites;
import static com.example.socialcontribution.FirebaseData.fetchNotifications;
import static com.example.socialcontribution.FirebaseData.fetchPointsAdded;
import static com.example.socialcontribution.FirebaseData.getPointsInfo;

public class SplashActivity extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    String username, uId;
    static User user;
    Animation top_anim, bottom_anim;
    ImageView logo, slogan, image;
    Intent intent;
    DatabaseReference pointref,allref;
    static Button button;
    static int splash=0;
    int categoriescount=0;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        top_anim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottom_anim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        image = findViewById(R.id.imageView21);
        logo = findViewById(R.id.imageView23);
        slogan = findViewById(R.id.imageView22);
        firebaseDatabase = FirebaseDatabase.getInstance();
        button = findViewById(R.id.button6);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //put animations to views
        image.setAnimation(top_anim);
        logo.setAnimation(bottom_anim);
        slogan.setAnimation(bottom_anim);

        if(preferences.getBoolean("isSignedIn",false)){
            uId = getIntent().getStringExtra("uId");
            makeUserObj();
        }

        intent = new Intent(this, MainActivity.class);
        pointref = firebaseDatabase.getReference("points");

        //when data is fetched this button gets clicked from FirebaseData class
                button.setOnClickListener(b -> {
                    final Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        // Do something after 5s = 5000ms
                        startActivity(intent);
                        FirebaseData.splaskok=true;
                        this.finish();
                    }, 3000);
                    splash=1;


                });

        allref = firebaseDatabase.getReference("points"); //get # of categories children
        allref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriescount= (int) snapshot.getChildrenCount();
                if(categoriescount!=0){
                    getPointsInfo("People",preferences.getBoolean("isSignedIn",false));
                    getPointsInfo("Animals",preferences.getBoolean("isSignedIn",false));
                    getPointsInfo("Events",preferences.getBoolean("isSignedIn",false));
                }
                else button.performClick();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void makeUserObj() {

        DatabaseReference reference = firebaseDatabase.getReference("user/" + uId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                username = (String) snapshot.child("username").getValue();
                String mail = (String) snapshot.child("email").getValue();
                user = new User(mail, username, uId);
                user.setImagelink((String)snapshot.child("imagelink").getValue());
;
                if (snapshot.child("favourites").exists())
                    fetchFavourites(uId);
                else {
                    List<String> tmp=new ArrayList<>();
                    user.setFavourites(tmp);
                }
                if(snapshot.child("pointsadded").exists())
                    fetchPointsAdded(uId);
                else {
                    List<String> tmp=new ArrayList<>();
                    user.setPointsadded(tmp);
                }
                if(snapshot.child("notificationsid").exists())
                    fetchNotifications(uId);
                else {
                    List<String> templist=new ArrayList<>();
                    user.setNotificationsid(templist);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

