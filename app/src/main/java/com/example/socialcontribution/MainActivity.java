package com.example.socialcontribution;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.example.socialcontribution.FirebaseData.findRecent;
import static com.example.socialcontribution.FirebaseData.p;
import static com.example.socialcontribution.FirebaseData.points;
import static com.example.socialcontribution.FirebaseData.userpointssugg;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    LinearLayout recent,popular,suggestions;
    CardView people,animals,events, ppmore,jamore,sugmore;
    static ImageView homecardview;
    static CardView notificationscardview;
    static TextView notificationstextview;
    View cardView;
    DrawerLayout drawerLayout;
    TextView usernametxt;
    boolean resumed=false;
    private static SharedPreferences preferences;
    ImageView userlogoimage;

    FirebaseStorage storage;
    StorageReference storageReference, storageReference2;



    @Override
    protected void onResume() {
        super.onResume();
        if(resumed && FirebaseData.p!=null && FirebaseData.p.size()>0){
                //gets the most recent data displayed
                FirebaseData.sortPopular();
                popular.removeAllViews();
                populateSliders(FirebaseData.p,popular);
                if(FirebaseData.mostrecentpoints!=null && FirebaseData.mostrecentpoints.size()>0){
                    findRecent();
                    recent.removeAllViews();
                    populateSliders(FirebaseData.mostrecentpoints,recent);
                }
                else if((FirebaseData.mostrecentpoints==null || FirebaseData.mostrecentpoints.size()==0) && resumed){
                    View nodata=getLayoutInflater().inflate(R.layout.nodataslide, null, false);
                    recent.addView(nodata);
                }
                if(userpointssugg!=null && userpointssugg.size()>0){
                    suggestions.removeAllViews();
                    populateSliders(userpointssugg,suggestions);
                }
                else if(preferences.getBoolean("isSignedIn",false) && (userpointssugg==null || userpointssugg.size()==0) && resumed){
                    View nodata=getLayoutInflater().inflate(R.layout.nodataslide, null, false);
                    suggestions.addView(nodata);
                }
            }
            homecardview.setVisibility(View.VISIBLE);
        }

    @Override
    protected void onStop() {
        super.onStop();
        resumed=true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        people= findViewById(R.id.view14);
        animals= findViewById(R.id.view13);
        events= findViewById(R.id.view12);
        recent=findViewById(R.id.justadded);
        popular=findViewById(R.id.popularpoints);
        ppmore=findViewById(R.id.ppmore);
        jamore=findViewById(R.id.jamore);
        drawerLayout = findViewById(R.id.drawerlayout);
        homecardview=drawerLayout.findViewById(R.id.homecardview);
        notificationscardview=drawerLayout.findViewById(R.id.notificationsnumber);
        notificationstextview=drawerLayout.findViewById(R.id.numberofnotifications);
        userlogoimage=drawerLayout.findViewById(R.id.imageView15);
        homecardview.setVisibility(View.VISIBLE);
        usernametxt=drawerLayout.findViewById(R.id.textView4);
        sugmore=findViewById(R.id.sugmore);
        suggestions=findViewById(R.id.suggestionslinear);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        storage = FirebaseStorage.getInstance();

        if(preferences.getBoolean("isSignedIn",false)){
            //storage reference and userlogoimage
            storageReference = storage.getReference(SplashActivity.user.getImagelink());
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this )
                        .load(uri)
                        .into((ImageView) findViewById(R.id.imageView15));

            });
            usernametxt.setText(SplashActivity.user.getUsername());
            sugmore.setOnClickListener(v->{
                Intent intent = new Intent(this,Suggestions.class);
                startActivity(intent);
            });

        }
        else {
            sugmore.setOnClickListener(v->{
                GuestDialog dialog = new GuestDialog(this);
                dialog.show();
            });
        }


        if(FirebaseData.p!=null && FirebaseData.p.size()>0){
            populateSliders(FirebaseData.p,popular);
            if(FirebaseData.mostrecentpoints!=null && FirebaseData.mostrecentpoints.size()>0)
                populateSliders(FirebaseData.mostrecentpoints,recent);
            if(preferences.getBoolean("isSignedIn",false) && userpointssugg!=null && userpointssugg.size()>0)
                populateSliders(userpointssugg,suggestions);
        }

        people.setOnClickListener(v->{
            Intent intent = new Intent(this, AllPoints.class);
            intent.putExtra("type","People");
            //Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //start activity
            startActivity(intent);
            homecardview.setVisibility(View.INVISIBLE);
        });

        animals.setOnClickListener(v->{
            Intent intent = new Intent(this, AllPoints.class);
            intent.putExtra("type","Animals");
            //Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //start activity
            this.startActivity(intent);
            homecardview.setVisibility(View.INVISIBLE);
        });

        events.setOnClickListener(v->{
            Intent intent = new Intent(this, AllPoints.class);
            intent.putExtra("type","Events");
            //Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //start activity
            this.startActivity(intent);
            homecardview.setVisibility(View.INVISIBLE);
        });

        ppmore.setOnClickListener(v->{
            redirectActivity(this,ListView.class);
        });

        //show just added
        jamore.setOnClickListener(v->{
            Intent intent = new Intent(this,JustAdded.class);
            startActivity(intent);
        });


    }

    public void populateSliders(List<Points> list,LinearLayout linearLayout){

        //check if data received
        if(list.size()>0){
            linearLayout.removeAllViews();
            //if less than 5 total points take them all
            if(list.size()<5){
                for(Points points:list){
                    addCards(points,linearLayout);
                }
            }
            //if more than five points get the most seen
            if(list.size()>=5){
                for(int i=0;i<5;i++){
                    addCards(list.get(i),linearLayout);
                }
            }
        }
    }

    public void addCards(Points points,LinearLayout linearLayout) {
        cardView = getLayoutInflater().inflate(R.layout.slidecards, null, false);
        ImageView image=cardView.findViewById(R.id.imageView4);
        try{
         storageReference2 = storage.getReferenceFromUrl(points.getImageUri());
         storageReference2.getDownloadUrl().addOnSuccessListener(uri -> {
             Glide.with(this )
                     .load(uri)
                     .into(image);
            });
        }catch (Exception e){
            Log.i("THE EXCEPTION IS", e.toString());
            e.printStackTrace();
        }

        if(points.getCategory().equals("Events")){
            cardView.findViewById(R.id.eventdatetime).setVisibility(View.VISIBLE);
            TextView date = cardView.findViewById(R.id.eventdatetime).findViewById(R.id.eventdatetxt);
            date.setText(points.getEventdate());
            TextView time = cardView.findViewById(R.id.eventdatetime).findViewById(R.id.eventtimetxt);
            time.setText(points.getTime());

        }
        TextView eventTitle = cardView.findViewById(R.id.pointtitle);
        eventTitle.setText(points.getPointname());
        ImageView infobutton =cardView.findViewById(R.id.infob);
        infobutton.setOnClickListener(v->{
            Intent info=new Intent(this,PointInformation.class);
            info
                    .putExtra("pId",points.getPId());
            startActivity(info);
            homecardview.setVisibility(View.INVISIBLE);
        });
        linearLayout.addView(cardView);

    }

    public void ClickMenu(View View) {
        //call method to open drawer
        openDrawer(drawerLayout);
        if(preferences.getBoolean("isSignedIn",false) && SplashActivity.user.getNotificationsid().size()>0){
           notificationscardview.setVisibility(android.view.View.VISIBLE);
           notificationstextview.setText(String.valueOf(SplashActivity.user.getNotificationsid().size()));
        }
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open the menu drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    //navigation events
    public static void redirectActivity(Activity activity, Class theClass) {
        //Initialize intent
        Intent intent = new Intent(activity, theClass);
        //Set flag
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
       homecardview.setVisibility(View.INVISIBLE);
    }

    public void clickHome(View view){
        recreate();
    }
    public void clickMapView(View view){
        redirectActivity(this, MainMap.class);
    }
    public void clickListView(View view){
        redirectActivity(this, ListView.class);
    }
    public void clickEvents(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","Events");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        homecardview.setVisibility(View.INVISIBLE);

    }

    public void clickAnimals(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","Animals");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        homecardview.setVisibility(View.INVISIBLE);

    }
    public void clickProfile(View view){
        if(preferences.getBoolean("isSignedIn",false))
            redirectActivity(this, Profile.class);
        else{
            GuestDialog dialog = new GuestDialog(this);
            dialog.show();
        }
    }

    public void clickPeople(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","People");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        homecardview.setVisibility(View.INVISIBLE);

    }
    public void clickAround(View view){
        redirectActivity(this,Nearby.class);
    }

    public void clickLogout(View view){
            logout(this);

    }
    public static void logout(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");

        //Positive answer
        builder.setPositiveButton("YES", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isSignedIn", false).apply();
            editor.putString("uId", null).apply();
            if(preferences.getBoolean("isSignedIn",false)) {
                FirebaseData.mostrecentpoints.clear();
                FirebaseData.usersuggestions.clear();
                FirebaseData.sugids.clear();
            }
            SplashActivity.splash=0;

            FirebaseData.splaskok=false;
            //Logout
            redirectActivity(activity, SignIn.class);
            activity.finish();
        });

        //Negative answer
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Dismiss dialog and continue
                dialog.dismiss();

            }
        });

        //show dialog
        builder.show();
    }


    public void clickAddNewPoint(View view) {
        redirectActivity(this, addnewpoint.class);
    }

    public void clickFavourites(View view){
        if(preferences.getBoolean("isSignedIn",false))
            redirectActivity(this, Favourites.class);
        else{
            GuestDialog dialog = new GuestDialog(this);
            dialog.show();
        }
    }
    public void clickNotifications(View view){
        if(preferences.getBoolean("isSignedIn",false))
            redirectActivity(this, Notifications.class);
        else{
            GuestDialog dialog = new GuestDialog(this);
            dialog.show();
        }
    }

}