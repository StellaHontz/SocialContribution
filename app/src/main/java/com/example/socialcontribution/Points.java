package com.example.socialcontribution;

import android.accessibilityservice.GestureDescription;
import android.location.Address;
import android.net.Uri;

import java.util.Date;

public class Points {
    String pointname,category,subcategory,address,zipcode,pId,dateAdded,pointsinfo,eventdate,time,imageUri;
    double longitude,latitude;
    int seen;
    long addedtimestamp;


public Points(){}

public Points(String pId,String pointname,String category,String subcategory,double longitude,
              double latitude,String address,String zipcode,String dateAdded,int seen,String pointsinfo,long addedtimestamp,String imageUri){
    this.pointname=pointname;
    this.address=address;
    this.category=category;
    this.subcategory=subcategory;
    this.longitude=longitude;
    this.latitude=latitude;
    this.zipcode=zipcode;
    this.pId=pId;
    this.dateAdded=dateAdded;
    this.seen=seen;
    this.pointsinfo=pointsinfo;
    this.addedtimestamp=addedtimestamp;
    this.imageUri=imageUri;
}

    public Points(String pId,String pointname,String category,String subcategory,double longitude,
                  double latitude,String address,String zipcode,String dateAdded,long addedtimestamp,int seen,String pointsinfo,String eventdate,String time,String imageUri){
        this.pointname=pointname;
        this.address=address;
        this.category=category;
        this.subcategory=subcategory;
        this.longitude=longitude;
        this.latitude=latitude;
        this.zipcode=zipcode;
        this.pId=pId;
        this.dateAdded=dateAdded;
        this.seen=seen;
        this.pointsinfo=pointsinfo;
        this.eventdate=eventdate;
        this.time=time;
        this.addedtimestamp=addedtimestamp;
        this.imageUri=imageUri;
    }

    public String getTime() {
        return time;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public long getAddedtimestamp() {
        return addedtimestamp;
    }

    public String getCategory(){return category;}
    public String getSubcategory(){return subcategory;}
    public String getPointname(){return pointname;}
    public String getAddress(){return address;}
    public String getZipcode(){return zipcode;}
    public double getLongitude(){return longitude;}
    public double getLatitude(){return latitude;}
    public String getPId(){return pId;}
    public String getDateAdded(){return dateAdded;}
    public int getSeen(){return seen;}

    public String getEventdate() {
        return eventdate;
    }

    public void setEventdate(String eventdate) {
        this.eventdate = eventdate;
    }

    public String getPointsinfo() {
        return pointsinfo;
    }

    public void setPointsinfo(String pointsinfo) {
        this.pointsinfo = pointsinfo;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }
}
