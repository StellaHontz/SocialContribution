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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.socialcontribution.FirebaseData.p;
import static com.example.socialcontribution.SplashActivity.user;

public class AllPoints extends AppCompatActivity {

    String type;
    LinearLayout pointlinear;
    LinearLayout popular, favourites;
    CardView seeall,favmore, menubutton;
    View cardView;
    DrawerLayout drawerLayout;
    TextView title, quote,writter, usernametxt;
    List<Points> tmp,favouritestmp;
    boolean resumed=false;
    static ImageView thiscardview;
    private static SharedPreferences preferences;
    static CardView notificationscardview;
    static TextView notificationstextview;
    FirebaseStorage storage;
    StorageReference storageReference,storageReference2;


    @Override
    protected void onResume() {
        super.onResume();
        if(resumed && FirebaseData.p!=null && FirebaseData.p.size()>0) {
            if(preferences.getBoolean("isSignedIn",false) && user.getFavourites().size()>0)
                favourites.removeAllViews();
            popular.removeAllViews();
            populateactivity();

        }
        thiscardview.setVisibility(View.VISIBLE);
   }

    @Override
    protected void onStop() {
        super.onStop();
        resumed=true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_points);
        drawerLayout=findViewById(R.id.drawerlayout);
        pointlinear=findViewById(R.id.pointslinear);
        seeall=findViewById(R.id.seeall);
        popular=findViewById(R.id.popularpoints);
        favourites=findViewById(R.id.favouritespoints);
        type= getIntent().getStringExtra("type");
        title=findViewById(R.id.textView12);
        quote=findViewById(R.id.textView27);
        writter=findViewById(R.id.textView28);
        favmore=findViewById(R.id.favmore);
        menubutton=findViewById(R.id.menu);
        usernametxt=drawerLayout.findViewById(R.id.textView4);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        notificationscardview=drawerLayout.findViewById(R.id.notificationsnumber);
        notificationstextview=drawerLayout.findViewById(R.id.numberofnotifications);
        storage = FirebaseStorage.getInstance();

        if(preferences.getBoolean("isSignedIn",false)){
            //storage reference and userlogoimage
            storageReference = storage.getReference(SplashActivity.user.getImagelink());
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this )
                        .load(uri)
                        .into((ImageView) findViewById(R.id.imageView15));

            });
            usernametxt.setText(user.getUsername());
        }



        Intent intent=new Intent(this,CategoriesPoints.class);
        intent.putExtra("category",type);
        if(type.equals("People")){
            thiscardview=drawerLayout.findViewById(R.id.peoplecardview);
            thiscardview.setVisibility(View.VISIBLE);
            title.setText(getText(R.string.People));
            quote.setText(getText(R.string.quotepeople));
            writter.setText(" ");
        }
        else if(type.equals("Animals")){

            thiscardview=drawerLayout.findViewById(R.id.animalscardview);
            thiscardview.setVisibility(View.VISIBLE);
            title.setText(getText(R.string.Animals));
            quote.setText(getText(R.string.quoteanimals));
            writter.setText(getText(R.string.quoteanimals2));
        }
        else if(type.equals("Events")){
            thiscardview=drawerLayout.findViewById(R.id.eventscardview);
            thiscardview.setVisibility(View.VISIBLE);
            title.setText(getText(R.string.Events));
            quote.setText(getText(R.string.quoteevents));
            writter.setText(getText(R.string.quoteevents2));
        }

        tmp= new ArrayList<>();
        favouritestmp= new ArrayList<>();


        //populate activity according to type
        populateactivity();

        seeall.setOnClickListener(v->{
            intent.putExtra("type","all");
            startActivity(intent);

        });

        if(preferences.getBoolean("isSignedIn",false))
            favmore.setOnClickListener(v->{
                intent.putExtra("type","favourites");
                startActivity(intent);

            });
        else {
            favmore.setOnClickListener(v->{
                GuestDialog dialog = new GuestDialog(this);
                dialog.show();
            });
        }


    }
    public void populateactivity(){
        if(resumed){
            tmp.clear();
        }

        if(FirebaseData.p!=null && FirebaseData.p.size()>0) {
            for (Points p : FirebaseData.p) {
                if (p.getCategory().equals(type))
                    tmp.add(p);
            }

            //see if less than 5 points add all
            if (tmp.size() != 0) {
                popular.removeAllViews();
                if (tmp.size() <= 5) {
                    for (int i = 0; i <= tmp.size() - 1; i++) {
                        addCards(tmp.get(i), popular);
                    }
                }
                //if more than five points get the most seen
                if (tmp.size() > 5) {
                    for (int i = 0; i <= 5; i++) {
                        addCards(tmp.get(i), popular);
                    }
                }
            }
            if(preferences.getBoolean("isSignedIn",false))
                getfavouritesslide();
        }

    }
    public void getfavouritesslide(){
        if(resumed){
            favourites.removeAllViews();
            favouritestmp.clear();
        }
        //get all favourites points that are of selected type
        if (user.getFavourites() != null) {
            for (String s : user.getFavourites()) {
                for (Points points : p) {
                    if (s.equals(points.getPId()) && points.getCategory().equals(type))
                        favouritestmp.add(points);
                }
            }
        }


        if (favouritestmp.size() != 0) {
            favourites.removeAllViews();
            if (favouritestmp.size() <= 5) {
                for (int i = 0; i <= favouritestmp.size() - 1; i++)
                    addCards(favouritestmp.get(i), favourites);
            } else {
                favouritestmp.size();
                for (int i = 0; i <= 5; i++) {
                    addCards(favouritestmp.get(i), favourites);
                }
            }
        }
        else if(favouritestmp.size()==0 && resumed) {
            View nodata=getLayoutInflater().inflate(R.layout.nodataslide, null, false);
            favourites.addView(nodata);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
        thiscardview.setVisibility(View.INVISIBLE);
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
        thiscardview.setVisibility(View.INVISIBLE);
    }

    public void clickAnimals(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","Animals");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        thiscardview.setVisibility(View.INVISIBLE);


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
        thiscardview.setVisibility(View.INVISIBLE);


    }
    public void clickAround(View view){
        redirectActivity(this,Nearby.class);
    }

    public void clickLogout(View view){
        if(preferences.getBoolean("isSignedIn",false))
            logout(this);
        else{
            GuestDialog dialog = new GuestDialog(this);
            dialog.show();
        }

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

    public void clickAddNewPoint(View view) {
        redirectActivity(this, addnewpoint.class);
    }


}