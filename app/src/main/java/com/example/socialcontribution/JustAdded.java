package com.example.socialcontribution;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.TransitionManager;
import android.util.Log;
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

public class JustAdded extends AppCompatActivity {
    View nodataview;
    CardView backb,filterbutton,allpoints;
    View cardView;
    LinearLayout justaddedlinear;
    boolean isfav, resumed=false;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ImageView favouritesbutton,imageallback,imageall;
    DrawerLayout drawerLayout;
    LinearLayout f1,f2,f3;
    ViewGroup filterlayoutlinear;
    boolean visible=false,visible2=false,visible3=false;
    List<CheckBox> tmpanimals,tmpevents,tmppeople;
    CheckBox animalscb,peoplecb,eventcb;
    FirebaseStorage storage;
    StorageReference storageReference2;
    SharedPreferences preferences;

    @Override
    protected void onResume() {
        super.onResume();
        if(resumed && FirebaseData.p!=null && FirebaseData.p.size()>0 && FirebaseData.mostrecentpoints.size()>0) {
            justaddedlinear.removeAllViews();
            populateScrollView();
        }
        else if(!resumed && (FirebaseData.p==null || FirebaseData.p.size()==0 || FirebaseData.mostrecentpoints.size()==0))
            nodatacheck();
    }

    @Override
    protected void onStop() {
        super.onStop();
        resumed=true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_just_added);

        backb= findViewById(R.id.backbutton);
        justaddedlinear=findViewById(R.id.justaddedlinear);
        filterbutton=findViewById(R.id.filterbutton);
        firebaseDatabase =FirebaseDatabase.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        storage=FirebaseStorage.getInstance();

        if(preferences.getBoolean("isSignedIn",false))
            databaseReference=firebaseDatabase.getReference("user/"+user.getuId()+"/favourites");
        drawerLayout=findViewById(R.id.drawerlayout);
        allpoints=findViewById(R.id.allpoints);
        f1=findViewById(R.id.f1);
        f2=findViewById(R.id.f2);
        f3=findViewById(R.id.f3);
        filterlayoutlinear=findViewById(R.id.filterlayoutlinear);
        animalscb= filterlayoutlinear.findViewById(R.id.checkBox2);
        peoplecb= filterlayoutlinear.findViewById(R.id.checkBox);
        eventcb= filterlayoutlinear.findViewById(R.id.checkBox3);
        imageallback=findViewById(R.id.imageviewall);
        imageall= findViewById(R.id.imageall);

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
        backb.setOnClickListener(v->{
            this.finish();
        });

        if(FirebaseData.p!=null && FirebaseData.p.size()>0 && FirebaseData.mostrecentpoints.size()>0) {
            filterlayoutlinear.findViewById(R.id.button7).setOnClickListener(this::getFilterCards); //filter cards
            filterlayoutlinear.findViewById(R.id.button8).setOnClickListener(this::clearCards); //clear all cards

            allpoints.setOnClickListener(v -> {
                if (imageallback.getVisibility() == View.INVISIBLE) {
                    imageallback.setVisibility(View.VISIBLE);
                    imageall.setImageResource(R.drawable.allmarkers);
                    justaddedlinear.removeAllViews();
                } else if (imageallback.getVisibility() == View.VISIBLE) {
                    imageallback.setVisibility(View.INVISIBLE);
                    imageall.setImageResource(R.drawable.allmarkers2);
                    for (Points p : FirebaseData.mostrecentpoints) {
                        addCards(p);
                    }
                }

            });
            populateScrollView();
        }
        else nodatacheck();
    }
    public void nodatacheck(){
        nodataview=getLayoutInflater().inflate(R.layout.nodata, null, false);
        justaddedlinear.addView(nodataview);
        nodataview.findViewById(R.id.nodatabutton).setOnClickListener(v->{
            Intent intent=new Intent(this,addnewpoint.class);
            startActivity(intent);
        });
    }

    public void filterCards(String subcategory,String category){
        for(Points p : FirebaseData.mostrecentpoints){
            if(p.getCategory().equals(category) && p.getSubcategory().equals(subcategory))addCards(p);
        }
    }
    public void clearCards(View view){
        justaddedlinear.removeAllViews();
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
        justaddedlinear.removeAllViews();
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


    public void populateScrollView(){
        for(Points p:FirebaseData.mostrecentpoints){
            addCards(p);
        }
    }

    public void addCards(Points points) {
        cardView = getLayoutInflater().inflate(R.layout.card, null, false);
        ImageView image=cardView.findViewById(R.id.imageView4);
        try{
            storageReference2= storage.getReferenceFromUrl(points.getImageUri());
            storageReference2.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this )
                        .load(uri)
                        .into(image);
            });
        }catch (Exception e){
            Log.i("THE EXCEPTION IS", e.toString());
            e.printStackTrace();
        }
        TextView eventTitle = cardView.findViewById(R.id.eventTitle);
        eventTitle.setText(points.getPointname());
        ImageView infobutton =cardView.findViewById(R.id.infobutton);
        LinearLayout heartlayout=cardView.findViewById(R.id.heartlayout);
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
        justaddedlinear.addView(cardView);

        if(preferences.getBoolean("isSignedIn",false)) {
            favouritesbutton = cardView.findViewById(R.id.favouritebutton);
            if (user.getFavourites() != null) {
                if (user.getFavourites().contains(points.getPId())) {
                    isfav = true;
                    favouritesbutton.setImageResource(R.drawable.favourites2);
                }
            }

            //add to faves
            heartlayout.setOnClickListener(v -> {
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

            });
        }
        else{
            heartlayout.setOnClickListener(v -> {
                GuestDialog dialog = new GuestDialog(this);
                dialog.show();
            });
    }
    }
    
    public void clickFilter(View View) {
        //call method to open drawer
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open the menu drawer layout
        drawerLayout.openDrawer(GravityCompat.END);

    }
}