package com.example.socialcontribution;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.example.socialcontribution.SplashActivity.user;

public class Suggestions extends AppCompatActivity {
    View morebutton,cardView,nodataview;
    LinearLayout suggestions;
    CardView backbutton;
    ImageView favouritesbutton;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    boolean isfav, resumed=false;
    TextView number;
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
        if(resumed && FirebaseData.p!=null && FirebaseData.p.size()>0 && FirebaseData.userpointssugg.size()!=0 ){
            suggestions.removeAllViews();
            populateScroll();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        suggestions= findViewById(R.id.suggestionslinear);
        backbutton=findViewById(R.id.backbutton);
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("user/"+user.getuId()+"/favourites");
        number=findViewById(R.id.number);
        storage= FirebaseStorage.getInstance();

        backbutton.setOnClickListener(v->{
            this.finish();
        });

        if(FirebaseData.p!=null && FirebaseData.p.size()>0 && FirebaseData.userpointssugg.size()!=0)
            populateScroll();
        else nodatacheck();
    }
    public void nodatacheck(){
        nodataview=getLayoutInflater().inflate(R.layout.nodata, null, false);
        suggestions.addView(nodataview);
        nodataview.findViewById(R.id.nodatabutton).setOnClickListener(v->{
            Intent intent=new Intent(this,addnewpoint.class);
            startActivity(intent);
        });
        number.setText("0");
    }

    public void populateScroll(){
        if(FirebaseData.userpointssugg.size()<=5){
            for(Points p: FirebaseData.userpointssugg){
            addCards(p);
            }
            number.setText(String.valueOf(suggestions.getChildCount()));
        }
        else{
            for(int k=0;k<5;k++){
                addCards(FirebaseData.userpointssugg.get(k));
            }
            morebutton=getLayoutInflater().inflate(R.layout.seemorebutton, null, false);
            suggestions.addView(morebutton);
            number.setText(String.valueOf(suggestions.getChildCount()-1));
            morebutton.setOnClickListener(v->{
                Log.i("SUGG", String.valueOf(FirebaseData.userpointssugg.size()));
                suggestions.removeAllViews();
                for(Points p: FirebaseData.userpointssugg){
                    addCards(p);
                }
                //suggestions.removeView(morebutton);
                number.setText(String.valueOf(suggestions.getChildCount()));
            });

        }
    }

    public void addCards(Points points) {
        cardView = getLayoutInflater().inflate(R.layout.card, null, false);
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
        suggestions.addView(cardView);

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

            }
            else {
                databaseReference.child(points.getPId()).setValue(points.getPId());
                isfav=true;
                favouritesbutton=v.findViewById(R.id.favouritebutton);
                favouritesbutton.setImageResource(R.drawable.favourites2);
            }

        });
    }

}