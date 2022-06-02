package com.example.socialcontribution;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FirebaseData{

    static FirebaseDatabase firebaseDatabase;
    static String name, category, subcategory, pId;
    static List<Points> p, mostrecentpoints, userpointssugg;
    static List<SuggRecord> usersuggestions;
    static DatabaseReference pointref, tempref,notificationreference;
    static Double longitude, latitude;
    static Points points;
    static String dateAdded, currentdate;
    static SimpleDateFormat df;
    static Date d1;
    static int timesseen=0, count,childcount, dataok=0,alldata=0,allsuggestions,checksuggestions;
    static List<String> pids, sugids;
    static SuggRecord suggRecord;
    static boolean splaskok=false;



    public static void getPointsInfo(String type,Boolean guest) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        mostrecentpoints = new ArrayList<>(); //list to store most recent points
        p = new ArrayList<>(); //list to store all points
        pids= new ArrayList<>(); //list of all point ids in order not to enter point again when database is changed

        //is called for the different types
        pointref = firebaseDatabase.getReference("points/" + type); //category level
        pointref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> children = snapshot.getChildren();

                for (DataSnapshot temp : children) {
                    String key = temp.getKey();
                    tempref = firebaseDatabase.getReference("points/" + type + "/" + key); //subcategory level
                    tempref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            count=0;
                            childcount=0;
                            Iterable<DataSnapshot> children1 = snapshot2.getChildren();
                            childcount= (int) snapshot2.getChildrenCount(); //int that holds count of children in each subcategory
                            alldata+=childcount; //int that holds number of all data
                            for (DataSnapshot tmp2 : children1) {
                                count++; //int for each point
                                DatabaseReference tempref2 = firebaseDatabase.getReference("points/" + type + "/" + key + "/" + tmp2.getKey()); //point level
                                tempref2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        name = (String) snapshot1.child("pointname").getValue();
                                        category = (String) snapshot1.child("category").getValue();
                                        subcategory = (String) snapshot1.child("subcategory").getValue();
                                        longitude = (Double) snapshot1.child("longitude").getValue();
                                        latitude = (Double) snapshot1.child("latitude").getValue();
                                        String add = (String) snapshot1.child("address").getValue();
                                        String zip = (String) snapshot1.child("zipcode").getValue();
                                        pId = (String) snapshot1.child("pid").getValue();
                                        dateAdded=(String) snapshot1.child("dateAdded").getValue();
                                        long timeseen= (long) snapshot1.child("seen").getValue();
                                        timesseen = Integer.parseInt(String.valueOf(timeseen));
                                        String pointsinfo= (String) snapshot1.child("pointsinfo").getValue();
                                        long timestamp= (long) snapshot1.child("addedtimestamp").getValue();
                                        String imageUri= (String) snapshot1.child("imageUri").getValue();

                                        //see if point is already in points list
                                        if(!pids.contains(pId)) {
                                            pids.add(pId); //keep a list of pids to check if exist
                                            if(category.equals("Events")) {
                                                String tmp= (String) snapshot1.child("eventdate").getValue();
                                                String tmp2= (String) snapshot1.child("time").getValue();
                                                points = new Points(pId, name, category, subcategory, longitude, latitude, add, zip, dateAdded,timestamp, timesseen, pointsinfo,tmp ,tmp2,imageUri);

                                            }
                                            else
                                                points = new Points(pId, name, category, subcategory, longitude, latitude, add, zip,dateAdded,timesseen,pointsinfo,timestamp,imageUri);

                                            p.add(points);
                                            dataok++; //counts count of all children
                                            checkNcall(guest); //this checks if data is done received
                                            if(splaskok && SplashActivity.user.getNotificationsid()!=null && guest) {
                                                notifyFornewEntry(pId);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void notifyFornewEntry(String pId){
        firebaseDatabase = FirebaseDatabase.getInstance();
        notificationreference= firebaseDatabase.getReference("user/"+SplashActivity.user.getuId()+"/notificationsid");
        for(SuggRecord suggRecord:usersuggestions){
            if (suggRecord.getId().equals(pId.substring(0, 2)))
                 notificationreference.child(points.getPId()).setValue(points.getPId());
            }
    }
    public static void checkNcall(Boolean guest){
        //check if all data from each category is fetched
        if (dataok == alldata) {
            sortPopular(); //sort the list
            getRecent(guest);
        }

    }
    public static void getRecent(Boolean guest){
        //check if all data is fetched so that recent list can be created
            findRecent();
            if(guest)
                fetchSuggestions(SplashActivity.user.getuId());
    }

    public static void findRecent(){
        mostrecentpoints.clear();
        int tmp=0;
        df = new SimpleDateFormat("dd-MM-yyyy");
        currentdate = df.format(Calendar.getInstance().getTime()); //current date
        try{
        d1= df.parse(currentdate);
        for(Points point:p){
            Date d= df.parse(point.getDateAdded()); //date of each point
            long diff= d1.getTime()-d.getTime(); //calculate the difference between two dates
            if(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)<=7) //check if dates difference is under 7 days
                mostrecentpoints.add(point);
            tmp++; //for counting if all points are examined
            if (tmp==p.size()){
                if(SplashActivity.splash==0) //all points are examined
                    SplashActivity.button.performClick(); //it clicks an invisible button that creates new activity
                //it means that all data is examined and app can start
        }
            mostrecentpoints.sort((o1, o2) -> Long.compare(o2.getAddedtimestamp(),o1.getAddedtimestamp()));
        }

        }
        catch(Exception e) {

        }
    }

    public static void sortPopular(){
        //sorts at desc order by how popular is a point
        p.sort((o1, o2) -> Integer.compare(o2.getSeen(), o1.getSeen()));

    }


    //fetches user's favourites points
    public static void fetchFavourites(String uid){
        List<String> favs=new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference df= firebaseDatabase.getReference("user/"+uid+"/favourites");

        df.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot d:snapshot.getChildren()){
                    favs.add((String) d.getValue());
                }
                SplashActivity.user.setFavourites(favs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public static void fetchSuggestions(String uid){
        userpointssugg= new ArrayList<>();
        allsuggestions=0;
        checksuggestions=0;
        usersuggestions= new ArrayList<>();
        sugids = new ArrayList<>();

        DatabaseReference databaseReference=firebaseDatabase.getReference("user/"+uid+"/suggestions");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allsuggestions = (int) snapshot.getChildrenCount();

                for(DataSnapshot tmp: snapshot.getChildren()){
                    String suggid=tmp.getKey();
                    String tmp2= String.valueOf(tmp.child("seen").getValue());
                    int suggseen= Integer.parseInt(tmp2);

                    //see if sugg is already in suggestions list
                    if(sugids.contains(suggid)) {
                        for (SuggRecord sug : usersuggestions) {
                            if (sug.getId().equals(suggid))
                                suggRecord.setSeen(suggseen);
                        }
                    }
                    else {
                        checksuggestions++;
                        sugids.add(suggid); //keep a list of suggestions id to see if exist
                        suggRecord =new SuggRecord(suggid,suggseen);
                        usersuggestions.add(suggRecord);

                    }

                    if(allsuggestions==checksuggestions)
                        sortSuggestions();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public static void sortSuggestions(){
            usersuggestions.sort((o1, o2) -> Integer.compare(o2.getSeen(), o1.getSeen())); //sort desc the categories and subcat
            for(SuggRecord suggRecord:usersuggestions){
                for(Points points:p){
                    if(suggRecord.getId().equals(points.getCategory().substring(0,1)+points.getSubcategory().substring(0,1)) && !userpointssugg.contains(points))
                        userpointssugg.add(points);
                }
            }
    }


    public static void fetchPointsAdded(String uid){
        List<String> pointsadded=new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference df= firebaseDatabase.getReference("user/"+uid+"/pointsadded");

        df.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot d:snapshot.getChildren()){
                    pointsadded.add((String) d.getValue());
                }
                SplashActivity.user.setPointsadded(pointsadded);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public static void fetchNotifications(String uid){
        List<String> notificationsid=new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference df= firebaseDatabase.getReference("user/"+uid+"/notificationsid");

        df.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot d:snapshot.getChildren()){
                    if(!notificationsid.contains(d.getValue()))
                        notificationsid.add((String) d.getValue());
                }
                SplashActivity.user.setNotificationsid(notificationsid);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}