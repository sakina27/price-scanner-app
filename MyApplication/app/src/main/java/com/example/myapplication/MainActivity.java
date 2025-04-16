package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationText;
    private Button pickLocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationText = findViewById(R.id.location_text);
        pickLocationBtn = findViewById(R.id.pick_location);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkAndRequestPermissions();

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCNDulGYg0HAWcEYm3emh5mXus7n7msOtw");
        }

        pickLocationBtn.setOnClickListener(v -> openPlacePicker());

        // Initialize TabLayout and ViewPager
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        // Set up ViewPager Adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Food");
                    break;
                case 1:
                    tab.setText("Grocery");
                    break;
                case 2:
                    tab.setText("Cabs");
                    break;
            }
        }).attach();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void checkAndRequestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateLocationUI(location);
                    } else {
                        Log.w("Location", "Last location is null, requesting new location...");
                        requestNewLocation();
                    }
                });
    }


    private void requestNewLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000) // Request location every 3 seconds
                .setFastestInterval(1000) // Accept updates as fast as possible
                .setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    locationText.setText("Unable to fetch location.");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationUI(location);
                        break;
                    }
                }
            }
        }, getMainLooper());
    }


    private void updateLocationUI(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        //locationText.setText("Lat: " + latitude + ", Lng: " + longitude);
        getAddressFromLocation(latitude, longitude);
        Log.d("Location", "Lat: " + latitude + ", Lng: " + longitude);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                locationText.setText("Permission denied. Enable location in settings.");
            }
        }
    }

    private void openPlacePicker() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountry("IN")  // Restrict to India
                .build(this);
        startActivityForResult.launch(intent);
    }

    // Handle the result
    private final ActivityResultLauncher<Intent> startActivityForResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    locationText.setText(place.getName() + "\n" + place.getLatLng().toString());
                }
            });

    private void getAddressFromLocation(double latitude, double longitude) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + latitude + "," + longitude + "&key=AIzaSyCNDulGYg0HAWcEYm3emh5mXus7n7msOtw";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray results = jsonObject.getJSONArray("results");
                        if (results.length() > 0) {
                            String address = results.getJSONObject(0).getString("formatted_address");
                            locationText.setText(address);
                        } else {
                            locationText.setText("No address found");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        locationText.setText("Error parsing address");
                    }
                },
                error -> locationText.setText("Geocoder API request failed"));

        queue.add(stringRequest);
    }


}
