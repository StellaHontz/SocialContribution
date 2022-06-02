package com.example.socialcontribution;

import java.util.List;

public class User {

    String email,username,uId;
    List<String> favourites,pointsadded,notificationsid;
    String imagelink;
    public User(){}

    public User(String email,String username,String uId){
        this.email=email;
        this.username=username;
        this.uId=uId;
    }


    public void setFavourites(List<String> favourites) {
        this.favourites = favourites;
    }
    public void setPointsadded(List<String> pointsadded) {
        this.pointsadded = pointsadded;
    }
    public void setNotificationsid(List<String> notificationsid){this.notificationsid=notificationsid;}

    public String getImagelink() {
        return imagelink;
    }

    public void setImagelink(String imagelink) {
        this.imagelink = imagelink;
    }

    public List<String> getNotificationsid() {
        return notificationsid;
    }
    public String getEmail(){return email;}
    public void setuId(String uId) {
        this.uId = uId;
    }
    public String getUsername() {
        return username;
    }
    public String getuId() {return uId;}
    public List<String> getFavourites(){return favourites;}
    public List<String> getPointsadded(){return pointsadded;}
}
