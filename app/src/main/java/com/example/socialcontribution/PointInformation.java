package com.example.socialcontribution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PointInformation extends AppCompatActivity implements OnMapReadyCallback {

    TextView textView1,textView2,textView3,eventdatetimetxt,addedontxt,infotxt;
    ImageView back,fav,image;
    DatabaseReference reference,ref,suggestionsref;
    FirebaseDatabase firebaseDatabase;
    SharedPreferences preferences;
    int seen,sugseen=0;
    Boolean isfav=false;
    String category, sub,pointname,pid, suggid,addedon,date,time;
    CardView scrollup,calendarcard;
    LinearLayout infolinear,eventlinear;
    boolean visible=false;
    CalendarView calendarView;
    Points point;
    StorageReference storageReference2;
    FirebaseStorage storage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_information);
        textView1=findViewById(R.id.textView19); //point name
        textView2=findViewById(R.id.textView20); //address
        textView3=findViewById(R.id.textView22); //category & subcategory
        back=findViewById(R.id.imageView19); //back button
        fav= findViewById(R.id.imageView20); //favourite button
        pid=getIntent().getStringExtra("pId");
        for(Points points:FirebaseData.p){
            if(points.getPId().equals(pid))
                point=points;
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        category= point.getCategory();
        sub=point.getSubcategory();
        pointname=point.getPointname();
        addedon=point.getDateAdded();
        suggid=category.substring(0,1)+sub.substring(0,1);
        scrollup=findViewById(R.id.scrollup);
        calendarcard=findViewById(R.id.calendarviewcard);
        eventlinear=findViewById(R.id.eventlinear);
        infolinear=findViewById(R.id.infolinear);
        eventdatetimetxt=findViewById(R.id.eventdatetime);
        addedontxt=findViewById(R.id.dateaaded);
        calendarView=findViewById(R.id.calendarview2);
        infotxt=findViewById(R.id.info);
        image=findViewById(R.id.imageView);

        storage = FirebaseStorage.getInstance();
        try{
            storageReference2 = storage.getReferenceFromUrl(point.getImageUri());
            storageReference2.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this )
                        .load(uri)
                        .into(image);
            });
        }catch (Exception e){
            Log.i("THE EXCEPTION IS", e.toString());
        }

        firebaseDatabase= FirebaseDatabase.getInstance();
        reference= firebaseDatabase.getReference("user/"+SplashActivity.user.getuId()+"/favourites");
        suggestionsref= firebaseDatabase.getReference("user/"+SplashActivity.user.getuId()+"/suggestions/"+suggid);
        ref= firebaseDatabase.getReference("points/"+category+"/"+sub+"/"+pid);

        if(preferences.getBoolean("isSignedIn",false)) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                        if(dataSnapshot.getValue().equals(pid)){
                            fav.setImageResource(R.drawable.favourites2);
                            isfav=true;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            suggestionsref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Object tmp=snapshot.child("seen").getValue();
                    if(tmp!=null)
                        sugseen=Integer.parseInt(String.valueOf(tmp));


                    sugseen++;
                    suggestionsref.child("seen").setValue(sugseen);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        //seen counter to find popular
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Object tmp= snapshot.child("seen").getValue();
                seen= Integer.parseInt(String.valueOf(tmp));

                seen++;
                ref.child("seen").setValue(seen);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //add to suggestions counter

        textView1.setText(pointname);
        textView2.setText(point.getAddress()+", "+point.getZipcode());
        infotxt.setText(point.getPointsinfo());
        textView3.setText(category+", "+sub);
        addedontxt.setText(getString(R.string.addedon)+addedon);
        if(category.equals("Events")){
            date=point.getEventdate();
            time=point.getTime();
            eventdatetimetxt.setText(getString(R.string.eventdate) +": "+date+" "+time);
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            try {
                Date d1= df.parse(date);
                calendarView.setDate(d1.getTime());

            } catch (ParseException e) {

            }
            TransitionManager.beginDelayedTransition(findViewById(R.id.infocardview));
            visible= !visible;
            eventlinear.setVisibility(visible? View.VISIBLE : View.GONE);
            calendarcard.setVisibility(visible? View.VISIBLE : View.GONE);

        }


        //link to map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapinfo); //link map to view
        mapFragment.getMapAsync(this);
        back.setOnClickListener(v->this.finish());

        //add to faves
        fav.setOnClickListener(v->{
            if(preferences.getBoolean("isSignedIn",false))
            {
                if(isfav){
                    reference.child(getIntent().getStringExtra("pId")).removeValue();
                    isfav=false;
                    fav.setImageResource(R.drawable.favourites);

                }
                else {
                    reference.child(getIntent().getStringExtra("pId")).setValue(getIntent().getStringExtra("pId"));
                    isfav=true;
                    fav.setImageResource(R.drawable.favourites2);
                }
            }
            else{
                GuestDialog dialog = new GuestDialog(this);
                dialog.show();
            }



        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng=new LatLng(point.getLatitude(),point.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

    }
}