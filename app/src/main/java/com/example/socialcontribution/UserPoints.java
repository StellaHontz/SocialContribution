package com.example.socialcontribution;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.socialcontribution.SplashActivity.user;

public class UserPoints extends AppCompatActivity {
    DrawerLayout drawerLayout;
    LinearLayout linearpoints,heartlayout;
    View backall,backfav, nodataview, nodataviewfav;
    TextView textall,textfav,pointTitle, filtertxt,number;
    String type;
    View cardView;
    ImageView image,infobutton,favouritesbutton,imageall,imageallback;
    boolean isfav=false;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    List<Points> added,favs;
    LinearLayout f1,f2,f3;
    CardView allpoints;
    static ImageView favouritescardview;
    ViewGroup filterlayoutlinear;
    boolean visible=false,visible2=false,visible3=false,resumed=false;
    List<CheckBox> tmpanimals,tmpevents,tmppeople;
    CheckBox animalscb,peoplecb,eventcb;
    FirebaseStorage storage;
    StorageReference storageReference2;

    @Override
    protected void onStop() {
        super.onStop();
        resumed=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(resumed){
            linearpoints.removeAllViews();
            getData();
            selectType();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_points);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference("user/"+user.getuId()+"/favourites");
        drawerLayout=findViewById(R.id.filterdrawer);
        filtertxt=drawerLayout.findViewById(R.id.typetxt);
        added=new ArrayList<>();
        favs=new ArrayList<>();
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
        number= findViewById(R.id.number);
        storage=FirebaseStorage.getInstance();

        linearpoints=findViewById(R.id.linearpoints);
        backall=findViewById(R.id.backaddedbyyou);
        backfav=findViewById(R.id.backfav);
        textall=findViewById(R.id.addedbyyoutxt);
        textfav=findViewById(R.id.textfav);
        type= getIntent().getStringExtra("type");

        //list of checkboxes
        tmpanimals =new ArrayList<>();
        tmpevents =new ArrayList<>();
        tmppeople =new ArrayList<>();

        //check which button clicked, more favourites or see all
        if(type.equals("favourites"))
            changeColors(textfav,backfav,textall,backall);

        else if(type.equals("all"))
            changeColors(textall,backall,textfav,backfav);

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

        if(FirebaseData.p!=null && FirebaseData.p.size()>0) {
            filterlayoutlinear.findViewById(R.id.button7).setOnClickListener(this::getFilterCards); //filter cards
            filterlayoutlinear.findViewById(R.id.button8).setOnClickListener(this::clearCards); //clear all cards
            getData();
            selectType();

        }
        else{
            nodataview=getLayoutInflater().inflate(R.layout.nodata, null, false);
            linearpoints.addView(nodataview);
            number.setText("0");
        }

            //See all clicked
            textall.setOnClickListener(v -> {
                changeColors(textall, backall, textfav, backfav);
                type = "all";
                linearpoints.removeAllViews();
                getData();
                selectType();
            });

            //see favourites
            textfav.setOnClickListener(v -> {
                changeColors(textfav, backfav, textall, backall);
                type = "favourites";
                linearpoints.removeAllViews();
                getData();
                selectType();
            });

            allpoints.setOnClickListener(v -> {
                if (imageallback.getVisibility() == View.INVISIBLE) {
                    imageallback.setVisibility(View.VISIBLE);
                    imageall.setImageResource(R.drawable.allmarkers);
                    linearpoints.removeAllViews();
                    number.setText("0");
                } else if (imageallback.getVisibility() == View.VISIBLE) {
                    imageallback.setVisibility(View.INVISIBLE);
                    imageall.setImageResource(R.drawable.allmarkers2);

                    if (type.equals("all")) {
                        for (Points p : added) {
                            addCards(p);
                        }
                    } else if (type.equals("favourites")) {
                        for (Points p : favs) {
                            addCards(p);
                        }
                    }
                    number.setText(String.valueOf(linearpoints.getChildCount()));
                }
            });
    }

    public void filterCards(String subcategory,String category){
        if(type.equals("all")){
            for(Points p : added){
                if(p.getCategory().equals(category) && p.getSubcategory().equals(subcategory))
                    addCards(p);
            }
        }
        else if(type.equals("favourites")){
            for(Points p : favs){
                if(p.getCategory().equals(category) && p.getSubcategory().equals(subcategory))
                    addCards(p);
            }
        }
        number.setText(String.valueOf(linearpoints.getChildCount()));
    }
    public void clearCards(View view){
        linearpoints.removeAllViews();
        for(CheckBox cb: tmppeople){
            cb.setChecked(false);
        }
        for(CheckBox cb: tmpanimals){
            cb.setChecked(false);
        }
        for(CheckBox cb: tmpevents){
            cb.setChecked(false);
        }
        number.setText("0");
    }

    public void getFilterCards(View view){
        linearpoints.removeAllViews();
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

    public void getData(){
        favs.clear();
        added.clear();
        //added by you points
        if(user.getPointsadded()!=null && user.getPointsadded().size()>0){
            for(String s : user.getPointsadded()){
                for(Points p: FirebaseData.p){
                    if(p.getPId().equals(s))
                        added.add(p);
                }
            }
        }
        //favs
        if(user.getFavourites()!=null && user.getFavourites().size()>0){
            for(String s : user.getFavourites()){
                for(Points p: FirebaseData.p){
                    if(p.getPId().equals(s))
                        favs.add(p);
                }
            }
        }
    }
    public void changeColors(TextView chosentxt,View chosenview,TextView othertxt,View otherview){
        chosentxt.setBackgroundColor(Color.parseColor("#ff7844"));
        chosenview.setBackgroundColor(Color.parseColor("#ff7844"));
        othertxt.setBackgroundColor(Color.parseColor("#00FF7844"));
        otherview.setBackgroundColor(Color.parseColor("#00FF7844"));
    }

    public void selectType(){
        if(FirebaseData.p!=null && FirebaseData.p.size()>0){
            //if list chosen is all
            if (type.equals("all")) {
                if (added.size() > 0) {
                    for (Points p : added) {
                        addCards(p);
                    }
                    number.setText(String.valueOf(linearpoints.getChildCount()));
                }
                else {
                    nodataview = getLayoutInflater().inflate(R.layout.nodata, null, false);
                    linearpoints.addView(nodataview);
                    nodataview.findViewById(R.id.nodatabutton).setOnClickListener(v -> {
                        Intent intent = new Intent(this, addnewpoint.class);
                        startActivity(intent);
                    });
                    number.setText("0");

                }
            } else if (type.equals("favourites")) {
                if (favs.size() > 0) {
                    for (Points p : favs) {
                        addCards(p);
                    }
                    number.setText(String.valueOf(linearpoints.getChildCount()));
                } else {
                    linearpoints.setGravity(Gravity.CENTER);
                    nodataviewfav = getLayoutInflater().inflate(R.layout.nodatafav, null, false);
                    linearpoints.addView(nodataviewfav);
                    nodataviewfav.findViewById(R.id.nondatafavbutton).setOnClickListener(v -> {
                        Intent intent = new Intent(this, ListView.class);
                        startActivity(intent);
                    });
                    number.setText("0");
                }
            }

        }
        else {
            nodataview=getLayoutInflater().inflate(R.layout.nodata, null, false);
            linearpoints.addView(nodataview);
            number.setText("0");

        }
    }

    public void addCards(Points points) {
        cardView = getLayoutInflater().inflate(R.layout.card, null, false);
        pointTitle = cardView.findViewById(R.id.eventTitle);
        pointTitle.setText(points.getPointname());
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
        if(user.getFavourites()!=null){
            if(user.getFavourites().contains(points.getPId())){
                isfav=true;
                favouritesbutton.setImageResource(R.drawable.favourites2);
            }}

        //add to faves
        heartlayout.setOnClickListener(v->{
            if(isfav){
                databaseReference.child(points.getPId()).removeValue();
                isfav =false;
                favouritesbutton=v.findViewById(R.id.favouritebutton);
                favouritesbutton.setImageResource(R.drawable.favourites);
                linearpoints.removeAllViews();
                getData();
                selectType();

            }
            else {
                databaseReference.child(points.getPId()).setValue(points.getPId());
                isfav=true;
                favouritesbutton=v.findViewById(R.id.favouritebutton);
                favouritesbutton.setImageResource(R.drawable.favourites2);
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