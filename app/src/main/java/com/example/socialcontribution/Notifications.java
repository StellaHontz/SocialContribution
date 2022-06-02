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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.example.socialcontribution.Nearby.nearbycardview;
import static com.example.socialcontribution.SplashActivity.user;

public class Notifications extends AppCompatActivity {

    LinearLayout cardlist;
    View cardView;
    static ImageView notificationscardview;
    DrawerLayout drawerLayout;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private static SharedPreferences preferences;
    TextView usernametxt;
    static CardView notificationscard;
    static TextView notificationstextview;
    FirebaseStorage storage;
    StorageReference storageReference;
    boolean resumed=false;

    @Override
    protected void onResume() {
        super.onResume();
        if(resumed && FirebaseData.p!=null && FirebaseData.p.size()>0 && user.getNotificationsid()!=null && SplashActivity.user.getNotificationsid().size()>0){
            cardlist.removeAllViews();
            fillList();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        resumed=true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        cardlist=findViewById(R.id.notificationslinear);
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("user/"+SplashActivity.user.getuId()+"/notificationsid");
        drawerLayout=findViewById(R.id.drawerlayout);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        notificationscardview=findViewById(R.id.notificationscardview);
        notificationscardview.setVisibility(View.VISIBLE);

        notificationscard=drawerLayout.findViewById(R.id.notificationsnumber);
        notificationstextview=drawerLayout.findViewById(R.id.numberofnotifications);

        //storage reference and userlogoimage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(SplashActivity.user.getImagelink());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this )
                    .load(uri)
                    .into((ImageView) findViewById(R.id.imageView15));

        });

        //username at menu
        usernametxt=drawerLayout.findViewById(R.id.textView4);
        try {
            usernametxt.setText(SplashActivity.user.getUsername());
        }catch (Exception ignored){ }



        if(FirebaseData.p!=null && FirebaseData.p.size()>0 && user.getNotificationsid()!=null && SplashActivity.user.getNotificationsid().size()>0 ){
            fillList();
        }
    }
    public void fillList(){
        for(String s:SplashActivity.user.getNotificationsid()){
            for(Points points:FirebaseData.p){
                if(points.getPId().equals(s))
                    addCards(points);
            }
        }
    }
    public void addCards(Points points) {
        cardView = getLayoutInflater().inflate(R.layout.notificationcard, null, false);
        TextView pointname = cardView.findViewById(R.id.pointname);
        pointname.setText(points.getPointname());
        TextView categorysub = cardView.findViewById(R.id.categorysub);
        categorysub.setText(points.getCategory()+", "+points.getSubcategory());
        TextView address = cardView.findViewById(R.id.address);
        address.setText(points.getAddress());
        CardView seemorebutton =cardView.findViewById(R.id.seemore);

        seemorebutton.setOnClickListener(v->{
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

        CardView deletecard=cardView.findViewById(R.id.deletecard);

        //delete notification
        deletecard.setOnClickListener(v->{
            SplashActivity.user.getNotificationsid().remove(points.getPId());
            databaseReference.child(points.getPId()).removeValue();
            cardlist.removeAllViews();
            fillList();

        });
        cardlist.addView(cardView);
    }
    public void clickNotifications(View view){
        recreate();
    }

    public void ClickMenu(View View) {
        //call method to open drawer
        openDrawer(drawerLayout);
        if(SplashActivity.user.getNotificationsid().size()>0){
            notificationscard.setVisibility(android.view.View.VISIBLE);
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
        notificationscardview.setVisibility(View.INVISIBLE);
    }

    public void clickHome(View view) {
        redirectActivity(this, MainActivity.class);
    }

    public void clickMapView(View view) {
        redirectActivity(this, MainMap.class);
    }

    public void clickListView(View view) {
        redirectActivity(this, ListView.class);
    }

    public void clickEvents(View view) {
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type", "Events");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        notificationscardview.setVisibility(View.INVISIBLE);

    }

    public void clickAnimals(View view) {
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type", "Animals");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        notificationscardview.setVisibility(View.INVISIBLE);

    }

    public void clickProfile(View view) {
        if(preferences.getBoolean("isSignedIn",false))
            redirectActivity(this, Profile.class);
        else{
            GuestDialog dialog = new GuestDialog(this);
            dialog.show();
        }


    }

    public void clickPeople(View view) {
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type", "People");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        notificationscardview.setVisibility(View.INVISIBLE);

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

            FirebaseData.mostrecentpoints.clear();
            FirebaseData.usersuggestions.clear();
            FirebaseData.sugids.clear();

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


    public void clickFavourites(View view) {
        if(preferences.getBoolean("isSignedIn",false))
            redirectActivity(this, Favourites.class);
        else{
            GuestDialog dialog = new GuestDialog(this);
            dialog.show();
        }

    }
    public void clickAddNewPoint(View view) {
        redirectActivity(this, addnewpoint.class);
    }
    public void clickAround(View view){redirectActivity(this,Nearby.class);

    }

}