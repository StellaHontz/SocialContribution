package com.example.socialcontribution;

import androidx.annotation.NonNull;
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
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.TransitionManager;
import android.transition.Visibility;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.MapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.socialcontribution.FirebaseData.fetchFavourites;
import static com.example.socialcontribution.FirebaseData.findRecent;
import static com.example.socialcontribution.FirebaseData.p;
import static com.example.socialcontribution.SplashActivity.user;

public class Favourites extends AppCompatActivity {
    LinearLayout favouriteslayout;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    View cardView,nodata,nodatafav;
    TextView pointTitle,number,usernametxt;
    ImageView image,favouritesbutton, imageall,imageallback;
    DrawerLayout drawerLayout;
    LinearLayout f1,f2,f3;
    CardView allpoints;
    static ImageView favouritescardview;
    ViewGroup filterlayoutlinear;
    boolean visible=false,visible2=false,visible3=false,resumed=false;
    List<CheckBox> tmpanimals,tmpevents,tmppeople;
    CheckBox animalscb,peoplecb,eventcb;
    List<Points> favs;
    FirebaseStorage storage;
    StorageReference storageReference,storageReference2;
    private static SharedPreferences preferences;

    static CardView notificationscardview;
    static TextView notificationstextview;


    @Override
    protected void onResume() {
        super.onResume();
        if(resumed && FirebaseData.p!=null && FirebaseData.p.size()>0 && user.getFavourites().size()>0)
            getdata();

        else if (resumed && (FirebaseData.p!=null || FirebaseData.p.size()==0 || user.getFavourites().size()==0))
            nodatacheck();
        favouritescardview.setVisibility(View.VISIBLE);
        }

    @Override
    protected void onStop() {
        super.onStop();
        resumed=true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        favouriteslayout=findViewById(R.id.favouriteslinear);
        drawerLayout = findViewById(R.id.drawerlayout);
        favouritescardview=drawerLayout.findViewById(R.id.favouritescardview);
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("user/"+user.getuId()+"/favourites");
        resumed=false;
        f1=findViewById(R.id.f1);
        f2=findViewById(R.id.f2);
        f3=findViewById(R.id.f3);
        filterlayoutlinear=findViewById(R.id.filterlayoutlinear);
        animalscb= filterlayoutlinear.findViewById(R.id.checkBox2);
        peoplecb= filterlayoutlinear.findViewById(R.id.checkBox);
        eventcb= filterlayoutlinear.findViewById(R.id.checkBox3);
        allpoints =findViewById(R.id.allpoints);
        imageallback=findViewById(R.id.imageviewall);
        imageall= findViewById(R.id.imageall);
        favs=new ArrayList<>();
        favouritescardview.setVisibility(View.VISIBLE);
        number=findViewById(R.id.number);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //username at menu
        usernametxt=drawerLayout.findViewById(R.id.textView4);
        try {
            usernametxt.setText(SplashActivity.user.getUsername());
        }catch (Exception ignored){ }

        //storage reference and userlogoimage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(SplashActivity.user.getImagelink());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this )
                    .load(uri)
                    .into((ImageView) findViewById(R.id.imageView15));

        });


        notificationscardview=drawerLayout.findViewById(R.id.notificationsnumber);
        notificationstextview=drawerLayout.findViewById(R.id.numberofnotifications);


        //list of checkboxes
        tmpanimals =new ArrayList<>();
        tmpevents =new ArrayList<>();
        tmppeople =new ArrayList<>();

        //fill checkboxes list
        for(int i=0;i<=f1.getChildCount()-1;i++){
            tmppeople.add((CheckBox)f1.getChildAt(i));
        }
        for(int i=0;i<=f2.getChildCount()-1;i++){
            tmpanimals.add((CheckBox)f2.getChildAt(i));
        }
        for(int i=0;i<=f3.getChildCount()-1;i++){
            tmpevents.add((CheckBox)f3.getChildAt(i));
        }

        //filter layout transitions
        peoplecb.setOnClickListener(v -> {

            TransitionManager.beginDelayedTransition(filterlayoutlinear);
            visible= !visible;
            f1.setVisibility(visible? View.VISIBLE : View.GONE);
            if(f1.getVisibility()== View.GONE)
                peoplecb.setButtonDrawable(R.drawable.forwardbutton);
            else peoplecb.setButtonDrawable(R.drawable.backbutton);
        });
        animalscb.setOnClickListener(v -> {

            TransitionManager.beginDelayedTransition(filterlayoutlinear);
            visible2= !visible2;
            f2.setVisibility(visible2? View.VISIBLE : View.GONE);

            if(f2.getVisibility()== View.GONE)
                animalscb.setButtonDrawable(R.drawable.forwardbutton);
            else animalscb.setButtonDrawable(R.drawable.backbutton);
        });
        eventcb.setOnClickListener(v->{

            TransitionManager.beginDelayedTransition(filterlayoutlinear);
            visible3= !visible3;
            f3.setVisibility(visible3? View.VISIBLE : View.GONE);

            if(f3.getVisibility()== View.GONE)
                eventcb.setButtonDrawable(R.drawable.forwardbutton);
            else eventcb.setButtonDrawable(R.drawable.backbutton);


        });

        getdata();
        nodatacheck();


    }
    public void getdata(){
        if(FirebaseData.p!=null && FirebaseData.p.size()>0 && user.getFavourites().size()>0){

            filterlayoutlinear.findViewById(R.id.button7).setOnClickListener(this::getFilterCards); //filter cards
            filterlayoutlinear.findViewById(R.id.button8).setOnClickListener(this::clearCards); //clear all cards

            fillList();


            allpoints.setOnClickListener(v->{
                if(imageallback.getVisibility()== View.INVISIBLE){
                    imageallback.setVisibility(View.VISIBLE);
                    imageall.setImageResource(R.drawable.allmarkers);
                    favouriteslayout.removeAllViews();
                    number.setText("0");

                }
                else if(imageallback.getVisibility()==View.VISIBLE){
                    imageallback.setVisibility(View.INVISIBLE);
                    imageall.setImageResource(R.drawable.allmarkers2);
                    for(Points p: favs){
                        addCard(p);
                    }
                    number.setText(String.valueOf(favs.size()));
                }

            });
        }
    }
    public void nodatacheck(){

        if(FirebaseData.p==null || FirebaseData.p.size()==0){
            nodata=getLayoutInflater().inflate(R.layout.nodata, null, false);
            favouriteslayout.addView(nodata);
            nodata.findViewById(R.id.nodatabutton).setOnClickListener(v->{
                Intent intent=new Intent(this,addnewpoint.class);
                startActivity(intent);
            });
        }
        else if(user.getFavourites().size()==0){
            nodatafav=getLayoutInflater().inflate(R.layout.nodatafav, null, false);
            favouriteslayout.addView(nodatafav);
            nodatafav.findViewById(R.id.nondatafavbutton).setOnClickListener(v->{
                Intent intent=new Intent(this,ListView.class);
                startActivity(intent);
            });
            number.setText("0");
        }
    }

    public void fillList(){
        favouriteslayout.removeAllViews();
        favs.clear();
        fetchFavourites(user.getuId());
        if(user.getFavourites()!=null){
            for(String s:user.getFavourites()){
                for(Points points: p){
                    if(s.equals(points.getPId()))
                        favs.add(points);
                }
            }
        }
        for(Points points:favs){
            addCard(points);
        }
        number.setText(String.valueOf(favs.size()));
    }

    public void filterCards(String subcategory,String category){
        for(Points p : favs){
            if(p.getCategory().equals(category) && p.getSubcategory().equals(subcategory)) {
                addCard(p);
            }
        }
        number.setText(String.valueOf(favouriteslayout.getChildCount()));

    }
    public void clearCards(View view){
        favouriteslayout.removeAllViews();
        for(CheckBox cb: tmppeople){
            cb.setChecked(false);
        }
        for(CheckBox cb: tmpanimals){
            cb.setChecked(false);
        }for(CheckBox cb: tmpevents){
            cb.setChecked(false);
        }
        number.setText("0");

    }
    public void getFilterCards(View view){
        favouriteslayout.removeAllViews();
        for(CheckBox c: tmpevents){
            if(c.isChecked()){
                filterCards(c.getText().toString(),"Events");
            }
        }
        for(CheckBox c2: tmpanimals){
            if(c2.isChecked()){
                filterCards(c2.getText().toString(),"Animals");
            }
        }for(CheckBox c3: tmppeople){
            if(c3.isChecked()){
                filterCards(c3.getText().toString(),"People");
            }
        }
    }


    public void addCard(Points points){
        cardView = getLayoutInflater().inflate(R.layout.gridcard, null, false);
        pointTitle = cardView.findViewById(R.id.pointName);
        pointTitle.setText(points.getPointname());
        favouritesbutton=cardView.findViewById(R.id.favbut);
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
        cardView.setOnClickListener(v->{
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
            favouritescardview.setVisibility(View.INVISIBLE);
        });

        //remove from faves
        favouritesbutton.setOnClickListener(v->{
            databaseReference.child(points.getPId()).removeValue();
            user.getFavourites().remove(points.getPId());
            favouriteslayout.removeAllViews();
            fillList();

        });

        favouriteslayout.addView(cardView);

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
    public void openFilter(View view){
        openDrawer2(drawerLayout);
    }

    public static void openDrawer2(DrawerLayout drawerLayout) {
        //Open the menu drawer layout

        drawerLayout.openDrawer(GravityCompat.END);

    }
    public static void redirectActivity(Activity activity, Class theClass) {
        //Initialize intent
        Intent intent = new Intent(activity, theClass);
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
        favouritescardview.setVisibility(View.INVISIBLE);
    }

    public void clickHome(View view){
        redirectActivity(this, MainActivity.class);
    }
    public void clickMapView(View view){
        redirectActivity(this, MapView.class);
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
        favouritescardview.setVisibility(View.INVISIBLE);
    }

    public void clickAnimals(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","Animals");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        favouritescardview.setVisibility(View.INVISIBLE);

    }
    public void clickProfile(View view){
        redirectActivity(this, Profile.class);

    }
    public void clickPeople(View view) {
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type", "People");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        favouritescardview.setVisibility(View.INVISIBLE);
    }

    public void clickAround(View view){
        redirectActivity(this, Nearby.class);

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
        recreate();
    }
    public void clickNotifications(View view){
        redirectActivity(this, Notifications.class);

    }

    public void clickAddNewPoint(View view) {
        redirectActivity(this, addnewpoint.class);
    }


}