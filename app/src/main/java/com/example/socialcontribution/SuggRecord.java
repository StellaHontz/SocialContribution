package com.example.socialcontribution;

public class SuggRecord {
    String id;
    int seen;

    public SuggRecord(String id,int seen){
        this.id=id;
        this.seen=seen;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
