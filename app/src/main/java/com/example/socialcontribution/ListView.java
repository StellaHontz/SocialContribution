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
import android.os.Parcelable;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.socialcontribution.SplashActivity.user;

public class ListView extends AppCompatActivity{

    View cardView,nodata;
    TextView eventTitle,allpointstxt,number,usernametxt;
    ImageView image,infobutton,favouritesbutton;
    LinearLayout cardList,heartlayout;
    DrawerLayout drawerLayout;
    static ImageView listcardview;
    LinearLayout mapviewb,listviewb,f1,f2,f3,allpoints;
    ViewGroup filterlayoutlinear;
    boolean visible=false,visible2=false,visible3=false;
    List<CheckBox> tmpanimals,tmpevents,tmppeople;
    CheckBox animalscb,peoplecb,eventcb;
    boolean isfav=false;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    private static SharedPreferences preferences;
    boolean resumed=false;
    static CardView notificationscardview;
    static TextView notificationstextview;
    FirebaseStorage storage;
    StorageReference storageReference,storageReference2;


    @Override
    protected void onResume() {
        super.onResume();
        if(resumed && FirebaseData.p!=null &&FirebaseData.p.size()>0){
            cardList.removeAllViews();
            allpoints.performClick();
        }
        else if(resumed &&(FirebaseData.p==null || FirebaseData.p.size()==0)) nodatacheck();
    }

    @Override
    protected void onStop() {
        super.onStop();
        resumed=true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        cardList = findViewById(R.id.cardlistview);
        drawerLayout = findViewById(R.id.drawerlayout);
        listcardview=drawerLayout.findViewById(R.id.listviewcardview);
        f1=findViewById(R.id.f1);
        f2=findViewById(R.id.f2);
        f3=findViewById(R.id.f3);
        filterlayoutlinear=findViewById(R.id.filterlayoutlinear);
        animalscb= filterlayoutlinear.findViewById(R.id.checkBox2);
        peoplecb= filterlayoutlinear.findViewById(R.id.checkBox);
        eventcb= filterlayoutlinear.findViewById(R.id.checkBox3);
        allpoints =findViewById(R.id.allpoints);
        image= findViewById(R.id.imageView12);
        allpointstxt =findViewById(R.id.textView15);
        firebaseDatabase= FirebaseDatabase.getInstance();
        number=findViewById(R.id.number);
        usernametxt=drawerLayout.findViewById(R.id.textView4);
        storage = FirebaseStorage.getInstance();

        if(preferences.getBoolean("isSignedIn",false)) {
            usernametxt.setText(SplashActivity.user.getUsername());
            //storage reference and userlogoimage

            storageReference = storage.getReference(SplashActivity.user.getImagelink());
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this )
                        .load(uri)
                        .into((ImageView) findViewById(R.id.imageView15));

            });


            if(user.getuId()==null)
                user.setuId(preferences.getString("uId","guest"));
            databaseReference= firebaseDatabase.getReference("user/"+user.getuId()+"/favourites");

        }


        notificationscardview=drawerLayout.findViewById(R.id.notificationsnumber);
        notificationstextview=drawerLayout.findViewById(R.id.numberofnotifications);


        listcardview.setVisibility(View.VISIBLE);
        listviewb =findViewById(R.id.listviewbutton);
        mapviewb=findViewById(R.id.mapview);

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

        if(FirebaseData.p!=null &&FirebaseData.p.size()>0){
            filterlayoutlinear.findViewById(R.id.button7).setOnClickListener(this::getFilterCards); //filter cards
            filterlayoutlinear.findViewById(R.id.button8).setOnClickListener(this::clearCards); //clear all cards

            allpoints.setOnClickListener(v->{
                if(allpointstxt.getText().equals(getResources().getString(R.string.allpoints))){
                    for (Points points: FirebaseData.p){
                        addCards(points);
                    }
                    image.setImageResource(R.drawable.allmarkers2);
                    allpointstxt.setText(getResources().getString(R.string.clear));
                    number.setText(String.valueOf(FirebaseData.p.size()));
                }
                else if(allpointstxt.getText().equals(getResources().getString(R.string.clear))){
                    image.setImageResource(R.drawable.allmarkers);
                    clearCards(v);
                    allpointstxt.setText(getResources().getString(R.string.allpoints));
                    number.setText("0");

                }
            });
            allpoints.performClick(); //show all cards
        }
        else nodatacheck();

        listviewb.setOnClickListener(v->{
            Intent intent=new Intent(this,ListView.class);
            startActivity(intent);
            listcardview.setVisibility(View.INVISIBLE);
        });
        mapviewb.setOnClickListener(v->{
            Intent intent=new Intent(this,MainMap.class);
            startActivity(intent);
            listcardview.setVisibility(View.INVISIBLE);
        });

    }
    public void nodatacheck(){
        nodata=getLayoutInflater().inflate(R.layout.nodata, null, false);
        cardList.addView(nodata);
        nodata.findViewById(R.id.nodatabutton).setOnClickListener(v->{
            Intent intent=new Intent(this,addnewpoint.class);
            startActivity(intent);
        });
        number.setText("0");

    }

    public void addCards(Points points) {
        cardView = getLayoutInflater().inflate(R.layout.card, null, false);
        eventTitle = cardView.findViewById(R.id.eventTitle);
        eventTitle.setText(points.getPointname());
        infobutton =cardView.findViewById(R.id.infobutton);
        heartlayout=cardView.findViewById(R.id.heartlayout);
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
            listcardview.setVisibility(View.INVISIBLE);
        });
        cardList.addView(cardView);

        favouritesbutton=cardView.findViewById(R.id.favouritebutton);

        if(preferences.getBoolean("isSignedIn",false)) {
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
            heartlayout.setOnClickListener(v->{
                GuestDialog dialog = new GuestDialog(this);
                dialog.show();
            });
        }
    }

    public void filterCards(String subcategory,String category){
        for(Points p : FirebaseData.p){
            if(p.getCategory().equals(category) && p.getSubcategory().equals(subcategory)){
                addCards(p);
            }
        }
        number.setText(String.valueOf(cardList.getChildCount()));

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
        number.setText("0");

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



    //navigation events
    public static void redirectActivity(Activity activity, Class theClass) {
        //Initialize intent
        Intent intent = new Intent(activity, theClass);
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
        listcardview.setVisibility(View.INVISIBLE);
    }
    public void clickHome(View view){
        redirectActivity(this, MainActivity.class);
    }
    public void clickMapView(View view){
        redirectActivity(this, MainMap.class);
    }
    public void clickListView(View view){
        recreate();
    }
    public void clickEvents(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","Events");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        listcardview.setVisibility(View.INVISIBLE);
    }

    public void clickAnimals(View view){
        Intent intent = new Intent(this, AllPoints.class);
        intent.putExtra("type","Animals");
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        this.startActivity(intent);
        listcardview.setVisibility(View.INVISIBLE);

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
        listcardview.setVisibility(View.INVISIBLE);

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
    public void openFilter(View view){
        openDrawer2(drawerLayout);
    }
    public static void openDrawer2(DrawerLayout drawerLayout) {
        //Open the menu drawer layout

        drawerLayout.openDrawer(GravityCompat.END);

    }

    public void clickAddNewPoint(View view) {
        redirectActivity(this, addnewpoint.class);
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