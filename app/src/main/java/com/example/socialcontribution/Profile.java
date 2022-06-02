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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Random;

public class Profile extends AppCompatActivity {
    TextView usernametxt,emailtxt,usernametxt2;
    LinearLayout youadded,favourites,suggestions;
    CardView youmore,favouritesmore,sugmore;
    static ImageView profilecardview;
    DrawerLayout drawerLayout;
    View cardView;
    private static SharedPreferences preferences;
    static CardView notificationscardview;
    static TextView notificationstextview;
    boolean resumed=false;

    FirebaseStorage storage;
    StorageReference storageReference, storageReference2;

    @Override
    protected void onStop() {
        super.onStop();
        resumed=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(resumed){
            displayData();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        usernametxt= findViewById(R.id.username);
        drawerLayout=findViewById(R.id.drawerlayout);
        profilecardview=drawerLayout.findViewById(R.id.profilecardview);
        profilecardview.setVisibility(View.VISIBLE);
        emailtxt= findViewById(R.id.email);
        youadded=findViewById(R.id.addedbyyou);
        favourites=findViewById(R.id.favourites);
        youmore=findViewById(R.id.youmore);
        favouritesmore=findViewById(R.id.allfavourites);
        usernametxt.setText(SplashActivity.user.getUsername());
        emailtxt.setText(SplashActivity.user.getEmail());
        Intent intent=new Intent(this,UserPoints.class);
        sugmore=findViewById(R.id.sugmore);
        suggestions=findViewById(R.id.suggestionslinear);
        //storage reference and userlogoimage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(SplashActivity.user.getImagelink());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this )
                    .load(uri)
                    .into((ImageView) findViewById(R.id.image));
            Glide.with(this )
                    .load(uri)
                    .into((ImageView) findViewById(R.id.imageView15));

        });

        //for notifications
        notificationscardview=drawerLayout.findViewById(R.id.notificationsnumber);
        notificationstextview=drawerLayout.findViewById(R.id.numberofnotifications);

        //username at menu
        usernametxt2=drawerLayout.findViewById(R.id.textView4);
        try {
            usernametxt2.setText(SplashActivity.user.getUsername());
        }catch (Exception ignored){ }

        displayData();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        youmore.setOnClickListener(v->{
            intent.putExtra("type","all");
            startActivity(intent);
        });
        favouritesmore.setOnClickListener(v->{
            intent.putExtra("type","favourites");
            startActivity(intent);
        });
        sugmore.setOnClickListener(v->{
            Intent intent2 = new Intent(this,Suggestions.class);
            startActivity(intent2);
        });



    }
    public void displayData(){

        if(FirebaseData.p!=null && FirebaseData.p.size()>0) {
            if (SplashActivity.user.getPointsadded().size()>0)
                populateSliders(SplashActivity.user.getPointsadded(), youadded);
            if (SplashActivity.user.getFavourites().size() > 0)
                populateSliders(SplashActivity.user.getFavourites(), favourites);
            else if (SplashActivity.user.getFavourites().size()==0 && resumed){
                favourites.removeAllViews();
                View nodata=getLayoutInflater().inflate(R.layout.nodataslide, null, false);
                favourites.addView(nodata);
            }
            if(!resumed)
                populateSuggSlider(FirebaseData.userpointssugg, suggestions);
            else if(resumed && (FirebaseData.userpointssugg==null || FirebaseData.userpointssugg.size()==0) ){
                suggestions.removeAllViews();
                View nodata=getLayoutInflater().inflate(R.layout.nodataslide, null, false);
                suggestions.addView(nodata);
            }
        }
    }
    public void populateSuggSlider(List<Points> list,LinearLayout linearLayout){
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

    public void populateSliders(List<String> list, LinearLayout linearLayout){
        linearLayout.removeAllViews();
        //populate slider
        if(list!=null){
            if(list.size()<5){ //add all points if less than 5
                for(String s:list){
                    for(Points p: FirebaseData.p){
                        if(p.getPId().equals(s))
                            addCards(p,linearLayout);
                    }
                }
            }
            else{
                for(int i=0;i<5;i++){ //add 5 points
                    for(Points p: FirebaseData.p){
                        if(p.getPId().equals(list.get(i)))
                            addCards(p,linearLayout);
                    }
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
        }
        TextView eventTitle = cardView.findViewById(R.id.pointtitle);
        eventTitle.setText(points.getPointname());
        ImageView infobutton =cardView.findViewById(R.id.infob);
        infobutton.setOnClickListener(v->{
            Intent info=new Intent(this,PointInformation.class);
            info
                    .putExtra("pointname", points.getPointname())
                    .putExtra("category",points.getCategory())
                    .putExtra("subcategory",points.getSubcategory())
                    .putExtra("address",points.getAddress())
                    .putExtra("longitude",points.getLongitude())
                    .putExtra("latitude",points.getLatitude())
                    .putExtra("pId",points.getPId())
                    .putExtra("info",points.getPointsinfo());
            if(points.getCategory().equals("Events"))
                info.putExtra("eventdate",points.getEventdate())
                        .putExtra("eventtime",points.getTime());

            startActivity(info);
            profilecardview.setVisibility(View.INVISIBLE);
        });
        linearLayout.addView(cardView);

    }
    public void ClickMenu(View View) {
        //call method to open drawer
        openDrawer(drawerLayout);
        if(SplashActivity.user.getNotificationsid().size()>0){
            notificationscardview.setVisibility(android.view.View.VISIBLE);
            notificationstextview.setText(String.valueOf(SplashActivity.user.getNotificationsid().size()));
        }


        //menuimage.setText(preferences.getString("userNode", FirebaseAuth.getInstance().getCurrentUser().getEmail()));
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
        profilecardview.setVisibility(View.INVISIBLE);
    }

    public void clickHome(View view){
        redirectActivity(this,MainActivity.class);
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
        profilecardview.setVisibility(View.INVISIBLE);
    }

    public void clickAnimals(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","Animals");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        profilecardview.setVisibility(View.INVISIBLE);

    }
    public void clickProfile(View view){
        recreate();

    }
    public void clickPeople(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","People");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        profilecardview.setVisibility(View.INVISIBLE);
    }
    public void clickAround(View view){
        redirectActivity(this,Nearby.class);
    }

    public void clickLogout(View view){
        logout(this);
    }

    public void clickAddNewPoint(View view) {
        redirectActivity(this, addnewpoint.class);
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
            FirebaseData.splaskok=false;
            FirebaseData.mostrecentpoints.clear();
            FirebaseData.usersuggestions.clear();
            FirebaseData.sugids.clear();
            SplashActivity.splash=0;

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


    public void clickFavourites(View view){
        redirectActivity(this, Favourites.class);

    }
    public void clickNotifications(View view){
        redirectActivity(this, Notifications.class);

    }


}