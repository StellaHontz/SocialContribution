package com.example.socialcontribution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.socialcontribution.SplashActivity.user;

public class MainMap extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    DrawerLayout drawerLayout;
    private GoogleMap mMap;
    Boolean gotLocation = false;
    Location userLocation;
    Marker cL;
    LocationManager locationManager;
    LatLng myLocation;
    FloatingActionButton addevent,focus;
    TextView usernamenemu,allmarktxt;
    String username;
    Map<Points,Marker> peoplemarkers,eventmarkers,animalmarkers;
    LinearLayout mapviewb,listviewb,f1,f2,f3,markersb;
    CheckBox animalscb,peoplecb,eventcb;
    ViewGroup filterlayoutlinear;
    boolean visible=false,visible2=false,visible3=false, resumed=false;
    List<CheckBox> tmpanimals,tmpevents,tmppeople;
    TextView textView;
    ImageView img;
    static ImageView mapcardview;
    private static SharedPreferences preferences;
    FirebaseStorage storage;
    StorageReference storageReference;

    static CardView notificationscardview;
    static TextView notificationstextview;



    @Override
    protected void onResume() {
        super.onResume();
        if(resumed){
            mapcardview.setVisibility(View.VISIBLE);
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
        setContentView(R.layout.activity_main_map);
        drawerLayout = findViewById(R.id.drawerlayout);
        mapcardview=drawerLayout.findViewById(R.id.mapviewcardview);
        mapcardview.setVisibility(View.VISIBLE);
        addevent=findViewById(R.id.floatingActionButton);
        textView=findViewById(R.id.textView4);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        img=findViewById(R.id.imageView12);
        allmarktxt=findViewById(R.id.textView15);
        focus = findViewById(R.id.floatingActionButton2);

        notificationscardview=drawerLayout.findViewById(R.id.notificationsnumber);
        notificationstextview=drawerLayout.findViewById(R.id.numberofnotifications);

        //storage reference and userlogoimage
        storage = FirebaseStorage.getInstance();
        if(preferences.getBoolean("isSignedIn",false)){
        storageReference = storage.getReference(SplashActivity.user.getImagelink());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this )
                    .load(uri)
                    .into((ImageView) findViewById(R.id.imageView15));

        });

            username = user.getUsername();
            textView.setText(username);
        }


        userLocation = new Location("userLocation");
        userLocation.setLongitude(0.1);
        userLocation.setLatitude(0.1);

        usernamenemu=drawerLayout.findViewById(R.id.textView4);
        f1=findViewById(R.id.f1);
        f2=findViewById(R.id.f2);
        f3=findViewById(R.id.f3);
        filterlayoutlinear=findViewById(R.id.filterlayoutlinear);
        animalscb= filterlayoutlinear.findViewById(R.id.checkBox2);
        peoplecb= filterlayoutlinear.findViewById(R.id.checkBox);
        eventcb= filterlayoutlinear.findViewById(R.id.checkBox3);
        listviewb =findViewById(R.id.listviewbutton);
        mapviewb=findViewById(R.id.mapview);
        markersb= findViewById(R.id.allmarkers);



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //lists to store Checkboxes
        tmpanimals =new ArrayList<>();
        tmpevents =new ArrayList<>();
        tmppeople =new ArrayList<>();

        //markers map
        eventmarkers=new HashMap<>();
        peoplemarkers=new HashMap<>();
        animalmarkers=new HashMap<>();

        //link to map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1); //link map to view
        mapFragment.getMapAsync(this);

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
        //open people filters
        peoplecb.setOnClickListener(v -> {

            TransitionManager.beginDelayedTransition(filterlayoutlinear);
            visible= !visible;
            f1.setVisibility(visible? View.VISIBLE : View.GONE);
            if(f1.getVisibility()== View.GONE)
                peoplecb.setButtonDrawable(R.drawable.forwardbutton);
            else peoplecb.setButtonDrawable(R.drawable.backbutton);
        });

        //open animals filters
        animalscb.setOnClickListener(v -> {

            TransitionManager.beginDelayedTransition(filterlayoutlinear);
            visible2= !visible2;
            f2.setVisibility(visible2? View.VISIBLE : View.GONE);

            if(f2.getVisibility()== View.GONE)
                animalscb.setButtonDrawable(R.drawable.forwardbutton);
            else animalscb.setButtonDrawable(R.drawable.backbutton);
        });

        //open events filters
        eventcb.setOnClickListener(v->{

            TransitionManager.beginDelayedTransition(filterlayoutlinear);
            visible3= !visible3;
            f3.setVisibility(visible3? View.VISIBLE : View.GONE);

            if(f3.getVisibility()== View.GONE)
                eventcb.setButtonDrawable(R.drawable.forwardbutton);
            else eventcb.setButtonDrawable(R.drawable.backbutton);

        });

        //add new event
        addevent.setOnClickListener((view)->{
           // tl.setVisibility(View.VISIBLE);
            Intent intent=new Intent(this,addnewpoint.class);
            startActivity(intent);
            mapcardview.setVisibility(View.INVISIBLE);
        });

        //get location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        focus.setOnClickListener((view) -> {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
                cL.remove();
            } catch (Exception e) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            }
            gotLocation = false;
        });


        //open listview
        listviewb.setOnClickListener(v->{
            Intent intent=new Intent(this,ListView.class);
            startActivity(intent);
            mapcardview.setVisibility(View.INVISIBLE);
        });

        //openmapview
        mapviewb.setOnClickListener(v->{
            recreate();
        });


        if(FirebaseData.p!=null && FirebaseData.p.size()>0) {
            filterlayoutlinear.findViewById(R.id.button7).setOnClickListener(this::getFilters); //filter markers
            filterlayoutlinear.findViewById(R.id.button8).setOnClickListener(this::clearFilter); //clear all cards

            //get all markers
            markersb.setOnClickListener(v -> {
                if (allmarktxt.getText().equals(getResources().getString(R.string.allpoints))) {
                    allMarkers();
                    img.setImageResource(R.drawable.allmarkers2);
                    allmarktxt.setText(getResources().getString(R.string.clear));
                } else if (allmarktxt.getText().equals(getResources().getString(R.string.clear))) {
                    img.setImageResource(R.drawable.allmarkers);
                    clearMarkers(animalmarkers);
                    clearMarkers(peoplemarkers);
                    clearMarkers(eventmarkers);
                    allmarktxt.setText(getResources().getString(R.string.allpoints));

                }
            });
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    //clear all filters
    public void clearFilter(View view){
        for(CheckBox c: tmpevents){
            c.setChecked(false);
        }
        for(CheckBox c: tmpanimals){
            c.setChecked(false);
        }for(CheckBox c: tmppeople){
            c.setChecked(false);
        }

        clearMarkers(animalmarkers);
        clearMarkers(peoplemarkers);
        clearMarkers(eventmarkers);
    }
    
    //make markers invisible
    public void clearMarkers(Map<Points, Marker> markerHashMap){
        for(Map.Entry<Points, Marker> markerMap: markerHashMap.entrySet()){
            markerMap.getValue().setVisible(false);
        }

    }

    //show all markers and check all checkboxes
    public void allMarkers(){
        for(CheckBox cb: tmppeople){
            cb.setChecked(true);
        }
        for(CheckBox cb: tmpanimals){
            cb.setChecked(true);
        }for(CheckBox cb: tmpevents){
            cb.setChecked(true);
        }

        for(Map.Entry<Points, Marker> markerMap: animalmarkers.entrySet()){
            markerMap.getValue().setVisible(true);
        }
        for(Map.Entry<Points, Marker> markerMap: peoplemarkers.entrySet()){
            markerMap.getValue().setVisible(true);
        }
        for(Map.Entry<Points, Marker> markerMap: eventmarkers.entrySet()){
            markerMap.getValue().setVisible(true);
        }
    }

    //get which checkboxes are checked
    public void getFilters(View view){

        //clear all markers to add to a fresh layout
        clearMarkers(animalmarkers);
        clearMarkers(peoplemarkers);
        clearMarkers(eventmarkers);

        //check checkbox lists
        for(CheckBox c: tmpevents){
            if(c.isChecked())
                filterMarkers(c.getText().toString(),"Events"); }
        for(CheckBox c2: tmpanimals){
            if(c2.isChecked())
                filterMarkers(c2.getText().toString(),"Animals"); }
        for(CheckBox c3: tmppeople){
            if(c3.isChecked())
                filterMarkers(c3.getText().toString(),"People"); }

    }

    //apply the filter to points
    public void filterMarkers(String subcategory,String category){
        Map<Points,Marker> pointsMarkerMap =new HashMap<>();
        for(Points point3: FirebaseData.p){
            if(category.equals("Animals") )pointsMarkerMap=animalmarkers;
            if(category.equals("Events") )pointsMarkerMap=eventmarkers;
            if(category.equals("People") )pointsMarkerMap=peoplemarkers;
            for(Map.Entry<Points, Marker> m:pointsMarkerMap.entrySet()){
                if(point3.getPointname().equals(m.getKey().getPointname()) && point3.getSubcategory().equals(subcategory))
                    m.getValue().setVisible(true);
            }
        }

    }

    //add markers on map and markers lists
    public void addMarkers(Points points1) {
        BitmapDescriptor bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
        if(points1.getCategory().equals("People")) bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
        if(points1.getCategory().equals("Animals")) bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        if(points1.getCategory().equals("Events")) bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        //cast firabase entries to markers
        LatLng latLng = new LatLng(points1.getLatitude(), points1.getLongitude());
        Marker marker = mMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .icon(bd)
                        .visible(false)
                        .title(points1.getPointname()));
            marker.setTag(points1.getPId());
            if(points1.getCategory().equals("People")) peoplemarkers.put(points1,marker);
            if(points1.getCategory().equals("Animals")) animalmarkers.put(points1,marker);
            if(points1.getCategory().equals("Events")) eventmarkers.put(points1,marker);
        }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        while (!gotLocation){ //get location once
            //set current location
            userLocation.setLongitude(location.getLongitude());
            userLocation.setLatitude(location.getLatitude());

            //cast location to LatLng
            myLocation=new LatLng(userLocation.getLatitude(),userLocation.getLongitude());
            //add current location marker and animate camera
            cL=mMap.addMarker(new MarkerOptions().position(myLocation).title("You are here"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,15));
            gotLocation=true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(FirebaseData.p!=null && FirebaseData.p.size()>0) {
            for (Points tmp : FirebaseData.p) {
                addMarkers(tmp); //add all markers on map
            }
            allMarkers(); //show all markers
            Intent info=new Intent(this,PointInformation.class);
            googleMap.setOnInfoWindowClickListener(marker -> {
                info.putExtra("pId",(String)marker.getTag());
                startActivity(info);

            });
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},234);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //get location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},234);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    //navigation events
    public static void redirectActivity(Activity activity, Class theClass) {
        //Initialize intent
        Intent intent = new Intent(activity, theClass);
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
        mapcardview.setVisibility(View.INVISIBLE);

    }

    public void clickHome(View view){
        redirectActivity(this, MainActivity.class);
    }
    public void clickMapView(View view){
        recreate();
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
        mapcardview.setVisibility(View.INVISIBLE);
    }

    public void clickAnimals(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","Animals");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        mapcardview.setVisibility(View.INVISIBLE);
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
        mapcardview.setVisibility(View.INVISIBLE);


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
            FirebaseData.splaskok=false;
            if(preferences.getBoolean("isSignedIn",false)) {
                FirebaseData.mostrecentpoints.clear();
                FirebaseData.usersuggestions.clear();
                FirebaseData.sugids.clear();
            }
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

    public void clickAddNewPoint(View view) {
        redirectActivity(this, addnewpoint.class);
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
    public void openFilter(View view){
        openDrawer2(drawerLayout);
    }

    public static void openDrawer2(DrawerLayout drawerLayout) {
        //Open the menu drawer layout

        drawerLayout.openDrawer(GravityCompat.END);

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