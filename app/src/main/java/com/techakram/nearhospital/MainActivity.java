package com.techakram.nearhospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Spinner spType;
    Button btFind;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0, currentLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spType = findViewById(R.id.sp_type);
        btFind = findViewById(R.id.bt_find);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager( )
                .findFragmentById(R.id.google_map);

        //Initialize array of plce type..
        final String[] placetypeList = {"atm", "bank", "hospital", "movie_theater", "restaurant"};
        //initialize array of place name..
        final String[] placeNameList = {"ATM", "Bank", "Hospital", "Movie Theater", "Restaurant"};
        //set adapter on spinner..
        spType.setAdapter(new ArrayAdapter<>(MainActivity.this
                , android.R.layout.simple_spinner_dropdown_item, placeNameList));
        //initialize fused location provide..
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //check permission..
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //call method current location.
            getCurrentLocation( );
        }
        else {
            //when permission denied..
            //request permission..
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},66);

        }
        btFind.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                //get selected position on spinner..
                int i=spType.getSelectedItemPosition();
                //initialize url..

                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +//url
                            "?location=" + currentLat + "," + currentLong +//location latitude and longitude
                            "&radius=6000" +//nearby radius
                            "&types=" + placetypeList[i] +//place type
                            "&sensor=true" + //Sensor
                            "key=" + getResources( ).getString(R.string.google_map_key);//Google map key
                    //execute place task method download json data.
                    new PlaceTask( ).execute(url);
                Toast.makeText(MainActivity.this, "Show Near Places", Toast.LENGTH_SHORT).show( );
            }
        });
    }

    private void getCurrentLocation() {
        //initialize task location..
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation( );
        task.addOnSuccessListener(new OnSuccessListener<Location>( ) {
            @Override
            public void onSuccess(Location location) {
                if(location!=null) {
                    currentLat = location.getLatitude( );
                    currentLong = location.getLongitude( );
                    //sync map
                    supportMapFragment.getMapAsync(new OnMapReadyCallback( ) {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLong), 10));
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==66)
        {
           if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
           {
               //when permission granted then call method
               getCurrentLocation();
           }

        }
    }

    private class PlaceTask extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... strings) {
            String data=null;
            try {
                //initialize data..
                 data=downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace( );
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private String downloadUrl(String string) throws IOException
    {
        // initialize url
       URL url=new URL(string);
       // initialize connection..
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
         //connect connection.
        connection.connect();
        // initialize inputStream.
        InputStream stream=connection.getInputStream();
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(stream));
        // initialize string builder.
        StringBuilder stringBuilder=new StringBuilder();
        String line="";
        while((line=bufferedReader.readLine())!=null)
        {
            //append line
            stringBuilder.append(line);
        }
        //get append data
        String data=stringBuilder.toString();
        //close reader data.
        bufferedReader.close();
        //return data.
        return data;
    }
    private class ParserTask extends AsyncTask<String,Integer, List<HashMap<String,String>>>
    {

        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //crea tion json parser class
            JsonParser jsonParser=new JsonParser();
            //initialize hash map list
            List<HashMap<String,String>> mapList=null;
            JSONObject jsonObject=null;
            //Initialize json Object
            try {
                 jsonObject=new JSONObject(strings[0]);
                 //parse json object
                mapList=jsonParser.parseresult(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace( );
            }
            //Return maplist
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            //clear map
            map.clear();
            //use for loop
            for(int i=0;i<hashMaps.size();i++)
            {
              //initialize hash map
                HashMap<String,String> hashMapList=hashMaps.get(i);
                //get latitude
                double latitude= Double.parseDouble(hashMapList.get("lat"));
                double longitude= Double.parseDouble(hashMapList.get("lng"));
                String name=hashMapList.get("name");
                //concate latitude and longitude
                LatLng latLng=new LatLng(latitude,latitude);
                //initialize Marker Option
                MarkerOptions markerOptions=new MarkerOptions();
                //set position
                markerOptions.position(latLng);
                //set title
                markerOptions.title(name);
                //add marker on map
                map.addMarker(markerOptions);

            }
        }
    }
}
