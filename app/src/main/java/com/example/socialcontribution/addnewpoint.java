package com.example.socialcontribution;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class addnewpoint extends AppCompatActivity implements LocationListener {

    Button gcl, nl, add,choosetime;
    LinearLayout locationlayout;
    EditText address, zipcode,name,pointinfo;
    List<String> category,animalcat, peoplecat,eventcat;
    Spinner categoryspinner,category2spinnner;
    ArrayAdapter adapter,animaladapter,peopleadapter,eventadapter;
    LocationManager locationManager;
    Location userlocation;
    String chosencat, chosencat2,pointname,zipcodestr,addressstr, pId,pointinformation,time,eventdate;
    Address position=null;
    Double longitude,latitude;
    DatabaseReference myRef, ref2,ref3;
    FirebaseDatabase firebaseDatabase;
    Geocoder coder;
    boolean gotLocation=false, visible=false,visible2=false;
    Points points;
    String currentDate;
    int seen=0;
    java.text.DateFormat df;
    CalendarView calendarView;
    long timestamp;
    Uri imageUri;
    ImageView image;
    boolean hasImage;
    String tmp="noimage";
    FirebaseStorage storage;
    StorageReference storageReference,storageReference2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewpoint);
        gcl = (Button) findViewById(R.id.button3);
        nl = (Button) findViewById(R.id.button4);
        add = (Button) findViewById(R.id.button5);
        locationlayout= findViewById(R.id.locationlayout);
        address=findViewById(R.id.editTextTextPersonName4);
        zipcode=findViewById(R.id.editTextTextPersonName5);
        choosetime=findViewById(R.id.button12);
        pointinfo=findViewById(R.id.editTextTextMultiLine);
        category= new ArrayList<>();
        animalcat= new ArrayList<>();
        peoplecat= new ArrayList<>();
        eventcat= new ArrayList<>();
        chosencat= getString(R.string.People);
        chosencat2=getString(R.string.shelter);
        name = findViewById(R.id.editTextTextPersonName6);
        coder = new Geocoder(this);
        categoryspinner=findViewById(R.id.spinner);
        category2spinnner=findViewById(R.id.spinner2);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
        calendarView=findViewById(R.id.calendarView2);
        image=findViewById(R.id.imageView);

        //storage reference and userlogoimage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("pointphotos");

        df = new SimpleDateFormat("dd-MM-yyyy");
        timestamp=Calendar.getInstance().getTimeInMillis();
        currentDate = df.format(Calendar.getInstance().getTime());
        calendarView.setDate(Calendar.getInstance().getTimeInMillis());

        userlocation = new Location("userLocation");
        userlocation.setLongitude(0.1);
        userlocation.setLatitude(0.1);

        //connect to firebase
        firebaseDatabase=FirebaseDatabase.getInstance();
        myRef= firebaseDatabase.getReference("points");
        ref2=firebaseDatabase.getReference("user/"+SplashActivity.user.getuId()+"/pointsadded");
        ref3=firebaseDatabase.getReference("user/"+SplashActivity.user.getuId()+"/notificationsid");


        //point categories
        category.add(getString(R.string.People));
        category.add(getString(R.string.Animals));
        category.add(getString(R.string.Events));


        //help animals
        animalcat.add(getString(R.string.food));
        animalcat.add(getString(R.string.Adoption));
        animalcat.add(getString(R.string.shelter));
        animalcat.add(getString(R.string.Vet));

        //help people
        peoplecat.add(getString(R.string.shelter));
        peoplecat.add(getString(R.string.food));
        peoplecat.add(getString(R.string.personalcare));
        peoplecat.add(getString(R.string.money));

        //events
        eventcat.add(getString(R.string.Treeplanting));
        eventcat.add(getString(R.string.Helpaffectedareas));
        eventcat.add(getString(R.string.Litterpicking));
        eventcat.add(getString(R.string.SeaCleaning));

        //populate category spinner
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,category);
        categoryspinner.setAdapter(adapter);

        //animal adapter
        animaladapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,animalcat);
        animaladapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //people adapter
        peopleadapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,peoplecat);
        peopleadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //event adapter
        eventadapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,eventcat);
        eventadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        category2spinnner.setAdapter(peopleadapter);

        //get selected category
        categoryspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosencat =parent.getItemAtPosition(position).toString();
                if(chosencat.equals(getString(R.string.Animals))){
                    category2spinnner.setAdapter(animaladapter);
                    if(!hasImage) image.setImageResource(R.drawable.helpanimals);
                    if(calendarView.getVisibility()==View.VISIBLE && choosetime.getVisibility()==View.VISIBLE){
                        calendarView.setVisibility(View.INVISIBLE);
                        choosetime.setVisibility(View.INVISIBLE);
                    }

                }
                if(chosencat.equals(getString(R.string.People))){
                    category2spinnner.setAdapter(peopleadapter);
                    if(!hasImage) image.setImageResource(R.drawable.helppeople);
                    if(calendarView.getVisibility()==View.VISIBLE && choosetime.getVisibility()==View.VISIBLE){
                        calendarView.setVisibility(View.GONE);
                        choosetime.setVisibility(View.GONE);
                    }}

                if(chosencat.equals(getString(R.string.Events))){
                    category2spinnner.setAdapter(eventadapter);
                    if(!hasImage) image.setImageResource(R.drawable.helpevents);
                    TransitionManager.beginDelayedTransition(calendarView);
                    TransitionManager.beginDelayedTransition(locationlayout);
                    visible2= !visible2;
                    calendarView.setVisibility(visible2? View.VISIBLE : View.GONE);
                    choosetime.setVisibility(visible2? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                chosencat=getString(R.string.People); //default code
                category2spinnner.setAdapter(peopleadapter);

            }
        });
        category2spinnner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosencat2 =parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(chosencat.equals(getString(R.string.People)))
                    chosencat2=getString(R.string.shelter);
                if(chosencat.equals(getString(R.string.Animals)))
                    chosencat2=getString(R.string.Vet);
                if(chosencat.equals(getString(R.string.Events)))
                    chosencat2=getString(R.string.Treeplanting);

            }
        });


        nl.setOnClickListener((view)->{
            TransitionManager.beginDelayedTransition(locationlayout);
            visible= !visible;
            locationlayout.setVisibility(visible? View.VISIBLE : View.GONE);
        });

        //get location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
            return;
        }


        choosetime.setOnClickListener(v->{
            showTimePickerDialog();
        });
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            eventdate= dayOfMonth+"-"+(month+1)+"-"+year;
        });


    }

    public void showTimePickerDialog(){
        TimePickerDialog timePickerDialog =new TimePickerDialog(this, (TimePickerDialog.OnTimeSetListener) (view, hourOfDay, minute) -> {
            time= hourOfDay+ ":" +minute;
            choosetime.setText(time);

        },24,0,true);

        timePickerDialog.show();

    }

    //add point to firebase
    public void addEvent(View view) {
        zipcodestr=zipcode.getText().toString();
        addressstr =address.getText().toString();

        if(name.getText().length()==0) pointname=addressstr;
        else pointname=name.getText().toString();
        if(!zipcodestr.contains(" ")){
            zipcodestr=zipcodestr.substring(0,3)+" "+zipcodestr.substring(3);
        }

        //address to long lat and confirm result
        List<Address> addzip;
        try {
            // May throw an IOException
            addzip = coder.getFromLocationName(addressstr, 5);

            for(Address a:addzip){
                if(a.getPostalCode().equals(zipcodestr)){
                    position=a;
                    longitude=position.getLongitude();
                    latitude=position.getLatitude();}
            }
        }
        catch (Exception ex) {
            Log.i("EXCEPTION",ex.toString());
            Toast.makeText(this,"Please check information !",Toast.LENGTH_LONG).show();
        }
        pId= chosencat.substring(0,1) + chosencat2.substring(0,1)+pointname+Calendar.getInstance().getTimeInMillis();
        if(hasImage){
            uploadPicture(imageUri,pId);
        }
        else{
            switch (chosencat){
                case "People":
                    storageReference2=storage.getReference("pointphotos/helppeople.jpg");
                    storageReference2.getDownloadUrl().addOnSuccessListener(uri ->{
                        tmp= uri.toString();
                        continueaddevent(tmp);
                    });

                    break;
                case "Animals":
                    storageReference2=storage.getReference("pointphotos/helpanimals.jpg");
                    storageReference2.getDownloadUrl().addOnSuccessListener(uri ->{
                        tmp= uri.toString();
                        continueaddevent(tmp);
                    });
                    break;
                case "Events":
                    storageReference2=storage.getReference("pointphotos/helpevents.jpg");
                    storageReference2.getDownloadUrl().addOnSuccessListener(uri -> {
                        tmp= uri.toString();
                        continueaddevent(tmp);

                    });
                    break;
            }
        }
    }

    public void continueaddevent(String uri){
        if(longitude==null || latitude==null)
            Toast.makeText(this,"Please choose another address!",Toast.LENGTH_LONG).show();
        else {
            pointinformation = pointinfo.getText().toString();
            if (pointinformation.length() == 0)
                pointinformation = " ";
            Log.i("Long lat", String.valueOf(longitude) + " " + String.valueOf(latitude));
            if (chosencat.equals(getString(R.string.Events))) {
                if (eventdate.equals(currentDate) || time.equals(""))
                    Toast.makeText(this, "Please select both date and time", Toast.LENGTH_LONG).show();
                else
                    points = new Points(pId, pointname, chosencat, chosencat2, longitude, latitude, addressstr, zipcodestr, currentDate, timestamp, seen, pointinformation, eventdate, time, uri);
            } else
                points = new Points(pId, pointname, chosencat, chosencat2, longitude, latitude, addressstr, zipcodestr, currentDate, seen, pointinformation, timestamp, uri);

            SplashActivity.user.getPointsadded().add(pId);

            //Add values to firebase by category , category2, name
            myRef.child(points.getCategory()).child(points.getSubcategory()).child(points.getPId()).setValue(points);

            ref2.child(points.getPId()).setValue(points.getPId());

            if (FirebaseData.p != null)
                FirebaseData.findRecent();

            Toast.makeText(this, "Successfully Added", Toast.LENGTH_LONG).show();
            final Handler handler = new Handler();
            handler.postDelayed(this::finish, 2000);
        }
    }

    public void getCurrentLocation(View view) {
        //get location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        if(userlocation.getLatitude() != 0.1 && userlocation.getLongitude() != 0.1)
            setLocationInfo();

    }
    @Override
    public void onLocationChanged(@NonNull Location location) {

        if(!gotLocation){
        longitude=location.getLongitude();
        latitude=location.getLatitude();
        //set current location
        userlocation.setLongitude(longitude);
        userlocation.setLatitude(latitude);
        gotLocation=true;

        }


    }
    public void setLocationInfo(){
        List<Address> addresses;
        try {
            addresses = coder.getFromLocation(latitude, longitude, 1);
            String tmp = addresses.get(0).getAddressLine(0);
            addressstr= tmp.split(",")[0];
            zipcodestr = addresses.get(0).getPostalCode();
            locationlayout.setVisibility(View.VISIBLE);
            address.setText(addressstr);
            zipcode.setText(zipcodestr);
            Toast.makeText(this, "If address is wrong, complete the right one", Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Problem with finding location. Please enter information below!", Toast.LENGTH_LONG).show();
            locationlayout.setVisibility(View.VISIBLE);
        }


    }

    //called onClick of the spotImage to upload picture
    public void ImageClick(View view) {
        choosePicture();
    }

    //Allows to choose a picture from the user's gallery
    private void choosePicture() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {
            imageUri = data.getData();
            image.setImageURI(imageUri);
            Snackbar.make(findViewById(android.R.id.content),"Image Uploaded",Snackbar.LENGTH_LONG).show();
            hasImage=true;

        }
    }

    private void uploadPicture(Uri uri, String pointid) {

        // Create a reference
        storageReference = storageReference.child(pointid);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading. . .");
        progressDialog.show();

        storageReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    continueaddevent(uri1.toString());
                    progressDialog.dismiss();
                    //Successful add


                }))
                .addOnFailureListener(e -> {

                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"Failed to Upload",Toast.LENGTH_LONG).show();

                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        progressDialog.setMessage(((int) progress) + "% Uploaded...");
                    }
                });

    }




}