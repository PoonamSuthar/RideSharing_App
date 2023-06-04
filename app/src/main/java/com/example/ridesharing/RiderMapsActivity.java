package com.example.ridesharing;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.ridesharing.databinding.ActivityRiderMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RiderMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityRiderMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Button Rider_logout_button;
    private Button Book_Cab_Button;
    private String RiderID;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUsers;
    private DatabaseReference RiderDatabaseRef;
    private Location lastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRiderMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth=FirebaseAuth.getInstance();
        currentUsers =mAuth.getCurrentUser();
        RiderID =FirebaseAuth.getInstance().getCurrentUser().getUid();
        Rider_logout_button = (Button) findViewById(R.id.Log_out_Button);
        Book_Cab_Button = (Button) findViewById(R.id.book_cab);
        RiderDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Rider Requests");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Book_Cab_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastLocation != null) {
                    double pickupLatitude = lastLocation.getLatitude();
                    double pickupLongitude = lastLocation.getLongitude();

                    // Save the customer's pickup location to the database
                    saveCustomerPickupLocation(pickupLatitude, pickupLongitude);
                } else {
                    // Handle the case when the lastLocation is not available
                }
            }
        });





//        Book_Cab_Button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (lastLocation != null) {
//                    double pickupLatitude = lastLocation.getLatitude();
//                    double pickupLongitude = lastLocation.getLongitude();
//
//                    // Save the pickup location to the database or perform any other desired action
//                    savePickupLocationToDatabase(pickupLatitude, pickupLongitude);
//
//                    // Perform any additional operations related to booking a cab
//                    // ...
//                } else {
//                    // Handle the case when the lastLocation is not available
//                }
//            }
//        });

    }
    private void saveCustomerPickupLocation(double latitude, double longitude) {
        // Create a map to hold the pickup location data
        HashMap<String, Object> pickupLocationMap = new HashMap<>();
        pickupLocationMap.put("latitude", latitude);
        pickupLocationMap.put("longitude", longitude);

        // Save the pickup location to the database
        RiderDatabaseRef.setValue(pickupLocationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // The pickup location was saved successfully
                            // Perform any additional actions or navigate to the next screen
                        } else {
                            // An error occurred while saving the pickup location
                            // Handle the error appropriately
                        }
                    }
                });
    }






    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Request location updates every second
        createLocationRequest();
        createLocationCallback();

        // Start location updates
        startLocationUpdates();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Get the latitude and longitude of the clicked location
                double pickupLatitude = latLng.latitude;
                double pickupLongitude = latLng.longitude;

                // Save the pickup location to the database or perform any other desired action
                savePickupLocationToDatabase(pickupLatitude, pickupLongitude);

                // Update map marker and move the camera to the pickup location
                mMap.clear(); // Clear existing markers
                mMap.addMarker(new MarkerOptions().position(latLng).title("Pickup Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }

    private void savePickupLocationToDatabase(double pickupLatitude, double pickupLongitude) {
        // Assuming you have a "Customers" node in your Firebase database
        DatabaseReference customersRef = FirebaseDatabase.getInstance().getReference().child("Customers");

        // Get the current user's ID
        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a new child node with the customer's ID under "Customers" node
        DatabaseReference customerRef = customersRef.child(customerId);

        // Save the pickup location (latitude and longitude) under the "pickupLocation" child node
        customerRef.child("pickupLocation").child("latitude").setValue(pickupLatitude);
        customerRef.child("pickupLocation").child("longitude").setValue(pickupLongitude);

        // Perform any other desired actions with the pickup location
        // ...
    }



    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // Update interval in milliseconds
        locationRequest.setFastestInterval(1000); // Fastest update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult: Location updates received");
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update current location
                    updateCurrentLocation(location);
                }
            }
        };
    }


    private void startLocationUpdates() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateCurrentLocation(Location location) {
        // Get current latitude and longitude
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Update map marker and move the camera
        LatLng currentLocation = new LatLng(latitude, longitude);
        mMap.clear(); // Clear existing markers
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }


    private void LogoutRider() {
        Intent welcomeIntent =new Intent(RiderMapsActivity.this, Dashboard.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }

}
