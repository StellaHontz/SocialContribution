package com.example.socialcontribution;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.socialcontribution.SplashActivity.user;

public class CategoriesPoints extends AppCompatActivity {

    DrawerLayout drawerLayout;
    LinearLayout linearpoints,heartlayout;
    View backall,backfav, nodataview, nodataviewfav;
    TextView textall,textfav,pointTitle, pointSubCategory, filtertxt,number;
    String type,category;
    View cardView;
    ImageView image,infobutton,favouritesbutton;
    boolean isfav=false, resumed=false;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    CheckBox checkBox1,checkBox2,checkBox3,checkBox4;
    List<CheckBox> checkBoxes;
    SharedPreferences preferences;
    FirebaseStorage storage;
    StorageReference storageReference2;

    @Override
    protected void onResume() {
        super.onResume();
        if(resumed){
            linearpoints.removeAllViews();
            selectType();

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
        setContentView(R.layout.activity_categories_points);
        firebaseDatabase = FirebaseDatabase.getInstance();
        drawerLayout=findViewById(R.id.filterdrawer);
        filtertxt=drawerLayout.findViewById(R.id.typetxt);
        checkBox1=findViewById(R.id.checkBox4);
        checkBox2=findViewById(R.id.checkBox5);
        checkBox3=findViewById(R.id.checkBox6);
        checkBox4=findViewById(R.id.checkBox7);
        checkBoxes=new ArrayList<>();
        checkBoxes.add(checkBox1);
        checkBoxes.add(checkBox2);
        checkBoxes.add(checkBox3);
        checkBoxes.add(checkBox4);
        number=findViewById(R.id.number);
        //storage reference and userlogoimage
        storage = FirebaseStorage.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        linearpoints=findViewById(R.id.linearpoints);
        backall=findViewById(R.id.backall);
        backfav=findViewById(R.id.backfav);
        textall=findViewById(R.id.textall);
        textfav=findViewById(R.id.textfav);
        type= getIntent().getStringExtra("type");
        category= getIntent().getStringExtra("category");
        if(preferences.getBoolean("isSignedIn",false)) {
            databaseReference= firebaseDatabase.getReference("user/"+user.getuId()+"/favourites");
        }
            selectType();

        //check which button clicked, more favourites or see all
        if(type.equals("favourites"))
            changeColors(textfav,backfav,textall,backall);

        else if(type.equals("all"))
            changeColors(textall,backall,textfav,backfav);


        //See all clicked
        textall.setOnClickListener(v->{
            changeColors(textall,backall,textfav,backfav);
            type="all";
            linearpoints.removeAllViews();
            selectType();
        });

        //see favourites
        textfav.setOnClickListener(v->{
            changeColors(textfav,backfav,textall,backall);
            type="favourites";
            linearpoints.removeAllViews();
            if(preferences.getBoolean("isSignedIn",false))
                selectType();
            else {
                GuestDialog dialog = new GuestDialog(this);
                dialog.show();
            }
        });



    }
    public void changeColors(TextView chosentxt,View chosenview,TextView othertxt,View otherview){
        chosentxt.setBackgroundColor(Color.parseColor("#ff7844"));
        chosenview.setBackgroundColor(Color.parseColor("#ff7844"));
        othertxt.setBackgroundColor(Color.parseColor("#00FF7844"));
        otherview.setBackgroundColor(Color.parseColor("#00FF7844"));
    }


    public void selectType(){
        //if list chosen is all
        if(FirebaseData.p!=null){
        if(type.equals("all")){
            for(Points points: FirebaseData.p){
                if(points.getCategory().equals(category))
                    addCards(points);
            }
                number.setText(String.valueOf(linearpoints.getChildCount()));
        }
        else if(type.equals("favourites") && preferences.getBoolean("isSignedIn",false)) {
            if (user.getFavourites() != null) {
                for (String tmp : user.getFavourites()) {
                    for (Points points : FirebaseData.p) {
                        if (tmp.equals(points.getPId()) && points.getCategory().equals(category))
                            addCards(points);
                    }
                }
                number.setText(String.valueOf(linearpoints.getChildCount()));
            }
        }

        }
        else{
            if(type.equals("all")) {
                nodataview=getLayoutInflater().inflate(R.layout.nodata, null, false);
                linearpoints.addView(nodataview);
                nodataview.findViewById(R.id.nodatabutton).setOnClickListener(v->{
                    Intent intent=new Intent(this,addnewpoint.class);
                    startActivity(intent);
                });
            }
            else{
                linearpoints.setGravity(Gravity.CENTER);
                nodataviewfav=getLayoutInflater().inflate(R.layout.nodatafav, null, false);
                linearpoints.addView(nodataviewfav);
                nodataviewfav.findViewById(R.id.nondatafavbutton).setOnClickListener(v->{
                    Intent intent=new Intent(this,ListView.class);
                    startActivity(intent);
                });
            }
            number.setText("0");
        }

        if(linearpoints.getChildCount()==0){
            linearpoints.setGravity(Gravity.CENTER);
            nodataviewfav=getLayoutInflater().inflate(R.layout.nodatafav, null, false);
            linearpoints.addView(nodataviewfav);
        }

        if(category.equals("Animals")){
            populateFilter(category,getString(R.string.food),getString(R.string.Adoption),getString(R.string.Vet),getString(R.string.shelter));
        }
        else if(category.equals("People")){
            populateFilter(category,getString(R.string.food),getString(R.string.shelter),getString(R.string.money),getString(R.string.personalcare));
        }
        else if(category.equals("Events")){
            populateFilter(category,getString(R.string.Treeplanting),getString(R.string.Litterpicking),getString(R.string.SeaCleaning),getString(R.string.Helpaffectedareas));
        }

    }
    public void populateFilter(String cat,String sub1,String sub2,String sub3,String sub4){
        filtertxt.setText(cat);
        checkBox1.setText(sub1);
        checkBox2.setText(sub2);
        checkBox3.setText(sub3);
        checkBox4.setText(sub4);
    }
    public void clearfilter(View view){
        linearpoints.removeAllViews();
        for(CheckBox c:checkBoxes){
            c.setChecked(false);
        }
        selectType();
    }

    public void applyFilter(View view){
        linearpoints.removeAllViews();
        for(CheckBox checkBox:checkBoxes){
            if(checkBox.isChecked()){
                if(type.equals("all")){
                    if(FirebaseData.p!=null){
                        for(Points points: FirebaseData.p){
                            if(points.getCategory().equals(category) && points.getSubcategory().equals(checkBox.getText()))
                                addCards(points);
                        }
                        number.setText(String.valueOf(linearpoints.getChildCount()));
                    }
                    else {
                        nodataview=getLayoutInflater().inflate(R.layout.nodata, null, false);
                        linearpoints.addView(nodataview);
                        number.setText("0");
                    }
                }
                else if(type.equals("favourites")) {
                    if (user.getFavourites() != null) {
                        for (Points points : FirebaseData.p) {
                            for (String tmp : user.getFavourites()) {
                                if (tmp.equals(points.getPId()) && points.getSubcategory().equals(checkBox.getText()))
                                    addCards(points);
                            }
                        }
                        number.setText(String.valueOf(linearpoints.getChildCount()));
                    }
                    else{
                        linearpoints.setGravity(Gravity.CENTER);
                        nodataviewfav=getLayoutInflater().inflate(R.layout.nodatafav, null, false);
                        linearpoints.addView(nodataviewfav);
                        number.setText("0");

                    }
                }

            }
        }

    }

    public void addCards(Points points) {
        cardView = getLayoutInflater().inflate(R.layout.categoriescard, null, false);
        pointTitle = cardView.findViewById(R.id.pointTitle);
        pointTitle.setText(points.getPointname());
        pointSubCategory=cardView.findViewById(R.id.pointSubCategory);
        pointSubCategory.setText(points.getSubcategory());
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
                    .putExtra("info",points.getPointsinfo());
            if(points.getCategory().equals("Events"))
                info.putExtra("eventdate",points.getEventdate())
                        .putExtra("eventtime",points.getTime());
            startActivity(info);
        });
        linearpoints.addView(cardView);

        favouritesbutton=cardView.findViewById(R.id.favouritebutton);
        if(preferences.getBoolean("isSignedIn",false)) {
            if(user.getFavourites()!=null){
                if(user.getFavourites().contains(points.getPId())){
                    isfav=true;
                    favouritesbutton.setImageResource(R.drawable.favourites2);
                }}
        }


        //add to faves
        heartlayout.setOnClickListener(v->{
            if(preferences.getBoolean("isSignedIn",false)) {
                if (isfav) {
                    databaseReference.child(points.getPId()).removeValue();
                    isfav = false;
                    favouritesbutton = v.findViewById(R.id.favouritebutton);
                    favouritesbutton.setImageResource(R.drawable.favourites);

                } else {
                    databaseReference.child(points.getPId()).setValue(points.getPId());
                    isfav = true;
                    favouritesbutton = v.findViewById(R.id.favouritebutton);
                    favouritesbutton.setImageResource(R.drawable.favourites2);
                }
            }
            else {
                GuestDialog dialog = new GuestDialog(this);
                dialog.show();
            }

        });
    }

    public void openDrawer(DrawerLayout drawerLayout) {
        //Open the menu drawer layout
        drawerLayout.openDrawer(GravityCompat.START);

    }
    public void openFilter(View view){
        openDrawer(drawerLayout);
    }


}