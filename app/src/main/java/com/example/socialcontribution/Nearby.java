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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.socialcontribution.SplashActivity.user;

public class Nearby extends AppCompatActivity implements LocationListener {
    DrawerLayout drawerLayout;
    TextView locationtxt,usernametxt,eventTitle;
    CardView editcardview, editlocationcardview, checkbutton,editbutton,radiuscardview, plusbutton,minusbutton,allpoints;
    boolean visible = true, visible2 = false, gotlocation = false;
    LocationManager locationManager;
    Location thelocation;
    Geocoder coder;
    String zipcodestr,addressstr;
    List<Points> aroundlocation;
    View cardView,nodataview;
    ImageView infobutton,favouritesbutton,imageall,imageallback;
    LinearLayout cardList,heartlayout;
    boolean isfav=false;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    EditText getlocationtxt,distancetxt;
    double distance=1000; //metres
    LinearLayout f1,f2,f3;
    static ImageView nearbycardview;
    ViewGroup filterlayoutlinear;
    boolean visible3=false,visible4=false,visible5=false,resumed=false;
    List<CheckBox> tmpanimals,tmpevents,tmppeople;
    CheckBox animalscb,peoplecb,eventcb;
    private static SharedPreferences preferences;

    static CardView notificationscardview;
    static TextView notificationstextview;
    FirebaseStorage storage;
    StorageReference storageReference,storageReference2;


    @Override
    protected void onResume() {
        super.onResume();
        if(resumed){
            nearbycardview.setVisibility(View.VISIBLE);
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
        setContentView(R.layout.activity_nearby);
        drawerLayout = findViewById(R.id.drawerlayout);
        editcardview = findViewById(R.id.editcardview);
        editbutton=findViewById(R.id.edit);
        editlocationcardview = findViewById(R.id.locationcardview);
        locationtxt = findViewById(R.id.selectedlocation);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        aroundlocation= new ArrayList<>();
        cardList=findViewById(R.id.cardlist);
        checkbutton=findViewById(R.id.check);
        thelocation= new Location("theLocation");
        coder = new Geocoder(this);
        getlocationtxt=findViewById(R.id.locationtxt);
        radiuscardview=findViewById(R.id.radius);
        plusbutton=findViewById(R.id.plus);
        minusbutton=findViewById(R.id.minus);
        distancetxt=findViewById(R.id.distancetxt);
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
        nearbycardview=drawerLayout.findViewById(R.id.aroundcardview);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        firebaseDatabase= FirebaseDatabase.getInstance();

        //username at menu
        usernametxt=drawerLayout.findViewById(R.id.textView4);
        try {
            usernametxt.setText(SplashActivity.user.getUsername());
        }catch (Exception ignored){ }

        //storage reference and userlogoimage
        storage = FirebaseStorage.getInstance();
        if(preferences.getBoolean("isSignedIn",false))
        {
            storageReference = storage.getReference(SplashActivity.user.getImagelink());
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this )
                        .load(uri)
                        .into((ImageView) findViewById(R.id.imageView15));
            });
            databaseReference= firebaseDatabase.getReference("user/"+user.getuId()+"/favourites");

        }


        notificationscardview=drawerLayout.findViewById(R.id.notificationsnumber);
        notificationstextview=drawerLayout.findViewById(R.id.numberofnotifications);
        nearbycardview.setVisibility(View.VISIBLE);

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
            visible3= !visible3;
            f1.setVisibility(visible3? View.VISIBLE : View.GONE);
            if(f1.getVisibility()== View.GONE)
                peoplecb.setButtonDrawable(R.drawable.forwardbutton);
            else peoplecb.setButtonDrawable(R.drawable.backbutton);
        });
        animalscb.setOnClickListener(v -> {

            TransitionManager.beginDelayedTransition(filterlayoutlinear);
            visible4= !visible4;
            f2.setVisibility(visible4? View.VISIBLE : View.GONE);

            if(f2.getVisibility()== View.GONE)
                animalscb.setButtonDrawable(R.drawable.forwardbutton);
            else animalscb.setButtonDrawable(R.drawable.backbutton);
        });
        eventcb.setOnClickListener(v->{

            TransitionManager.beginDelayedTransition(filterlayoutlinear);
            visible5= !visible5;
            f3.setVisibility(visible5? View.VISIBLE : View.GONE);

            if(f3.getVisibility()== View.GONE)
                eventcb.setButtonDrawable(R.drawable.forwardbutton);
            else eventcb.setButtonDrawable(R.drawable.backbutton);

        });
        minusbutton.setOnClickListener(v->{ //decrease distance
            if(!distancetxt.getText().toString().equals(""))
                distance= Double.parseDouble(distancetxt.getText().toString());
            else distancetxt.setText(String.valueOf(distance));
            if(distance>250)
                distance-=250;
            else distance=0;
            distancetxt.setText(String.valueOf(distance));

        });
        plusbutton.setOnClickListener(v->{

            if(!distancetxt.getText().toString().equals(""))
                distance= Double.parseDouble(distancetxt.getText().toString());
            else distancetxt.setText(String.valueOf(distance));
            distance+= 250;
            distancetxt.setText(String.valueOf(distance));

        }); // increase distance
        editbutton.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(findViewById(R.id.relative));
            visible = !visible;
            editcardview.setVisibility(visible ? View.VISIBLE : View.GONE);

        });

        findViewById(R.id.button13).setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(editcardview);
            visible2 = !visible2;
            editlocationcardview.setVisibility(visible ? View.VISIBLE : View.GONE);
            radiuscardview.setVisibility(visible ? View.VISIBLE : View.GONE);

        });

        if(FirebaseData.p!=null && FirebaseData.p.size()>0) {
            filterlayoutlinear.findViewById(R.id.button7).setOnClickListener(this::getFilterCards); //filter cards
            filterlayoutlinear.findViewById(R.id.button8).setOnClickListener(this::clearCards); //clear all cards


            allpoints.setOnClickListener(v -> {
                if (imageallback.getVisibility() == View.INVISIBLE) {
                    imageallback.setVisibility(View.VISIBLE);
                    imageall.setImageResource(R.drawable.allmarkers);
                    cardList.removeAllViews();
                } else if (imageallback.getVisibility() == View.VISIBLE) {
                    imageallback.setVisibility(View.INVISIBLE);
                    imageall.setImageResource(R.drawable.allmarkers2);
                    for (Points p : aroundlocation) {
                        addCards(p);
                    }
                }

            });

            checkbutton.setOnClickListener(v -> {
                if (!gotlocation) {
                    String addressstr = getlocationtxt.getText().toString();
                    //address to long lat and confirm result
                    List<Address> addzip;
                    try {
                        // May throw an IOException
                        addzip = coder.getFromLocationName(addressstr, 1); //na balw na dialejei tk
                        for (Address a : addzip) {
                            thelocation.setLongitude(a.getLongitude());
                            thelocation.setLatitude(a.getLatitude());

                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                TransitionManager.beginDelayedTransition(findViewById(R.id.relative));
                visible = !visible;
                editcardview.setVisibility(visible ? View.VISIBLE : View.GONE);
                radiuscardview.setVisibility(visible ? View.VISIBLE : View.GONE);
                locationtxt.setText(getlocationtxt.getText());
                if (!distancetxt.getText().toString().equals(""))
                    distance = Double.parseDouble(distancetxt.getText().toString());

                getNear(thelocation, distance);

            });
        }
        else{
            nodataview=getLayoutInflater().inflate(R.layout.nodata, null, false);
            cardList.addView(nodataview);
            nodataview.findViewById(R.id.nodatabutton).setOnClickListener(v->{
                Intent intent=new Intent(this,addnewpoint.class);
                startActivity(intent);
            });
            editcardview.setVisibility(View.GONE);
        }

    }

    public void filterCards(String subcategory,String category){
        for(Points p : aroundlocation){
            if(p.getCategory().equals(category) && p.getSubcategory().equals(subcategory))addCards(p);
        }
    }
    public void clearCards(View view){
        cardList.removeAllViews();
        for(CheckBox cb: tmppeople){
            cb.setChecked(false);
        }
        for(CheckBox cb: tmpanimals){
            cb.setChecked(false);
        }for(CheckBox cb: tmpevents){
            cb.setChecked(false);
        }

    }
    public void getFilterCards(View view){
        cardList.removeAllViews();
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

    public void getNear(Location selectedlocation,double radiuscircle){
        aroundlocation.clear();
        Location pointlocation= new Location("points");
        for(Points points:FirebaseData.p){
            pointlocation.setLatitude(points.getLatitude());
            pointlocation.setLongitude(points.getLongitude());
            if(pointlocation.distanceTo(selectedlocation)<radiuscircle){
                aroundlocation.add(points);
                addCards(points);
            }
        }
    }

    public void addCards(Points points) {
        cardView = getLayoutInflater().inflate(R.layout.card, null, false);
        eventTitle = cardView.findViewById(R.id.eventTitle);
        eventTitle.setText(points.getPointname());
        infobutton =cardView.findViewById(R.id.infobutton);
        heartlayout=cardView.findViewById(R.id.heartlayout);
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
                    .putExtra("addedon", points.getDateAdded())
                    .putExtra("info",points.getPointsinfo());
            if(points.getCategory().equals("Events"))
                info.putExtra("eventdate",points.getEventdate())
                        .putExtra("eventtime",points.getTime());
            startActivity(info);
        });
        cardList.addView(cardView);

        favouritesbutton=cardView.findViewById(R.id.favouritebutton);

        if(preferences.getBoolean("isSignedIn",false)){
            if(user.getFavourites()!=null){
                if(user.getFavourites().contains(points.getPId())){
                    isfav=true;
                    favouritesbutton.setImageResource(R.drawable.favourites2);
                }}
        }

        //add to faves
        heartlayout.setOnClickListener(v->{
            if(preferences.getBoolean("isSignedIn",false)){
                if(isfav){
                    databaseReference.child(points.getPId()).removeValue();
                    isfav =false;
                    favouritesbutton=v.findViewById(R.id.favouritebutton);
                    favouritesbutton.setImageResource(R.drawable.favourites);

                }
                else {
                    databaseReference.child(points.getPId()).setValue(points.getPId());
                    isfav=true;
                    favouritesbutton=v.findViewById(R.id.favouritebutton);
                    favouritesbutton.setImageResource(R.drawable.favourites2);
                }
            }
            else{
                GuestDialog dialog = new GuestDialog(this);
                dialog.show();
            }

        });
    }

    public void ClickMenu(View View) {
        //call method to open drawer
        openDrawer(drawerLayout);
        if(preferences.getBoolean("isSignedIn",false)){

            if(SplashActivity.user.getNotificationsid().size()>0){
            notificationscardview.setVisibility(android.view.View.VISIBLE);
            notificationstextview.setText(String.valueOf(SplashActivity.user.getNotificationsid().size()));
        }
        }
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open the menu drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickFilter(View View) {
        //call method to open drawer
        openDrawer2(drawerLayout);
    }

    public static void openDrawer2(DrawerLayout drawerLayout) {
        //Open the edit drawer layout
        drawerLayout.openDrawer(GravityCompat.END);
    }

    //navigation events
    public static void redirectActivity(Activity activity, Class theClass) {
        //Initialize intent
        Intent intent = new Intent(activity, theClass);
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
        nearbycardview.setVisibility(View.INVISIBLE);
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
        nearbycardview.setVisibility(View.INVISIBLE);

    }

    public void clickAnimals(View view) {
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type", "Animals");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        nearbycardview.setVisibility(View.INVISIBLE);

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
        nearbycardview.setVisibility(View.INVISIBLE);

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
            FirebaseData.splaskok = false;
            if (preferences.getBoolean("isSignedIn", false)) {
                FirebaseData.mostrecentpoints.clear();
                FirebaseData.usersuggestions.clear();
                FirebaseData.sugids.clear();
            }
            SplashActivity.splash = 0;

            //Logout
            redirectActivity(activity, SignIn.class);
            activity.finish();
        }).setNegativeButton("NO", (dialog, which) -> {
            //Dismiss dialog and continue
            dialog.dismiss();
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
    public void clickAround(View view){
        recreate();
    }
    public void clickNotifications(View view){
        if(preferences.getBoolean("isSignedIn",false))
            redirectActivity(this, Notifications.class);
        else{
            GuestDialog dialog = new GuestDialog(this);
            dialog.show();
        }


    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        while (!gotlocation) {
            thelocation.set(location);
            gotlocation = true;
            setLocationInfo(thelocation);
        }
    }


    public void getCurrentLocation(View view) {
        //get location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
            return;
        }
        TransitionManager.beginDelayedTransition(editcardview);
        visible2 = !visible2;
        editlocationcardview.setVisibility(visible ? View.VISIBLE : View.GONE);
        radiuscardview.setVisibility(visible ? View.VISIBLE : View.GONE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    public void setLocationInfo(Location location){
        List<Address> addresses;
        try {
            addresses = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String tmp = addresses.get(0).getAddressLine(0);
            addressstr= tmp.split(",")[0];
            zipcodestr = addresses.get(0).getPostalCode();
            getlocationtxt.setText(addressstr);

        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Problem with finding location. Please enter location!", Toast.LENGTH_LONG).show();
        }


    }
}