package com.example.ridesharing;
//package com.example.codingcafe.cab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomersMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, AdapterView.OnItemSelectedListener{

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location LastLocation;
    LocationRequest locationRequest;

    private RatingBar ratingBar;
    private Button Logout;
    private Button SettingsButton;
    private Button CallCabCarButton;
    private Button ShareRideButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference CustomerDatabaseRef;
    private DatabaseReference DriverAvailableRef, DriverLocationRef;
    private DatabaseReference DriversRef;
    private LatLng CustomerPickUpLocation;
    private double fare;
    private int radius = 1;

    private String selectedDestination;
    private Boolean driverFound = false, requestType = false;
    private String driverFoundID;
    private String customerID;
    Marker DriverMarker, PickUpMarker;
    GeoQuery geoQuery;
    private Spinner destinationSpinner;

    private ValueEventListener DriverLocationRefListner;
    private boolean fareConfirmationShown = false;

    private TextView txtName, txtPhone, shareFare, totalFare, textDestination, txtName1, txtPhone1;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;

    private RelativeLayout relativeLayout1;


    String[] destination = {"Airport", "Railway Station", "Paota", "Mandore"};
    LatLng iitJodhpurLocation = new LatLng(26.4710, 73.1134);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        ratingBar = findViewById(R.id.rating_bar);
        Logout = (Button) findViewById(R.id.logout_customer_btn);
        SettingsButton = (Button) findViewById(R.id.settings_customer_btn);
        CallCabCarButton =  (Button) findViewById(R.id.call_a_car_button);
        ShareRideButton = (Button) findViewById(R.id.Share_a_Ride) ;
        txtName = findViewById(R.id.name_driver);
        txtPhone = findViewById(R.id.phone_driver);
        relativeLayout = findViewById(R.id.rel1);
        txtName1 = findViewById(R.id.name_ShareCustomer);
        txtPhone1 = findViewById(R.id.phone_shareCustomer);
        relativeLayout1 = findViewById(R.id.rel4);
        textDestination = findViewById(R.id.destination_textview);
        shareFare = findViewById(R.id.share_fare_textview);
        shareFare.setVisibility(View.GONE);
        // Replace with your TextView's ID

        // Dropdown list
        destinationSpinner = findViewById(R.id.destination_spinner);
        destinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDestination = destination[position];
                    LatLng destinationLocation = getDestinationLocation(selectedDestination);

                    if (destinationLocation != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15f));

                        float distance = (float) calculateDistance(iitJodhpurLocation, destinationLocation);
                        double fare = calculateFare(distance);
                        Toast.makeText(getApplicationContext(), "Distance to " + selectedDestination + ": " + distance + " km", Toast.LENGTH_LONG).show();
                        TextView fareTextView = findViewById(R.id.total_fare_textview);
                        fareTextView.setText("Total Fare: \u20B9" + fare);
                        fareTextView.setVisibility(View.VISIBLE);

                        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);

                        customerRef.child("destination").setValue(selectedDestination)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Destination updated successfully
                                        String groupKey = FirebaseDatabase.getInstance().getReference().child("Customers").child("Share Cab Customers").push().getKey();
                                        findCustomersForCabSharing(selectedDestination);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to update destination
                                        Toast.makeText(getApplicationContext(), "Failed to update destination", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, destination);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(adapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Setting Button Activity
        SettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(CustomersMapActivity.this, SettingActivity.class);
                intent.putExtra("type", "Customers");
                startActivity(intent);
            }
        });

        // Logout button Activity
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mAuth.signOut();
                LogOutUser();
            }
        });

        ShareRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the customer's information in the database
//                totalFare.setVisibility(View.GONE);
                DatabaseReference customersRef = FirebaseDatabase.getInstance().getReference("Customers").child("Share Cab Customers");
                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                customersRef.child(customerId).child("wantsToShareCab").setValue(true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Sharing information updated successfully
                                Toast.makeText(CustomersMapActivity.this, "Cab sharing enabled", Toast.LENGTH_SHORT).show();

                                // Start the ShareRideMapsActivity
                                ShareRideButton.setVisibility(View.GONE);
                                CallCabCarButton.setVisibility(View.GONE);
                                findCustomersForCabSharing(selectedDestination);
                            }
                        })

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to update sharing information
//                                Toast.makeText(CustomersMapActivity.this, "Failed to enable cab sharing", Toast.LENGTH_SHORT).show();
                            }
                        });

            }


        });



        // CAll cab button Activity
        CallCabCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (requestType)
                {
                    requestType = false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListner);

                    if (driverFound != null)
                    {
                        DriversRef = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Drivers").child(driverFoundID).child("CustomerRideID");

                        DriversRef.removeValue();
                        driverFoundID = null;
                    }

                    driverFound = false;
                    radius = 1;

                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.removeLocation(customerId);

                    if (PickUpMarker != null)
                    {
                        PickUpMarker.remove();
                    }
                    if (DriverMarker != null)
                    {
                        DriverMarker.remove();
                    }

                    CallCabCarButton.setText("Call a Cab");
                    relativeLayout.setVisibility(View.GONE);
                }
                else
                {
                    requestType = true;
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                    geoFire.setLocation(customerId, new GeoLocation(LastLocation.getLatitude(), LastLocation.getLongitude()));

                    CustomerPickUpLocation = new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude());
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                    CallCabCarButton.setText("Getting your Driver...");
                    getClosetDriverCab();

                }
            }
        });

    }

    private void markFareAsPaid() {
        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Update the fare payment status in the database
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Customers").child(customerId);
        customerRef.child("farePaid").setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CustomersMapActivity.this, "Fare payment marked as paid.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CustomersMapActivity.this, "Failed to mark fare payment as paid.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getClosetDriverCab() {
        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude, CustomerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();

        final List<String> availableDrivers = new ArrayList<>();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // Anytime a driver is available nearby, this method will be called
                // Key = driverID and the location

                // Make sure the driver is not already assigned to another customer
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && !dataSnapshot.hasChild("CustomerRideID")) {
                            availableDrivers.add(key);
                        }

                        // Check if all nearby drivers have been considered
                        if (availableDrivers.size() == 0) {
                            // No available drivers found nearby
                            // Handle this scenario or increase the radius and call getClosetDriverCab() again
                        } else {
                            // Sort the availableDrivers list based on driver ratings
                            Collections.sort(availableDrivers, new Comparator<String>() {
                                @Override
                                public int compare(String driverId1, String driverId2) {
                                    // Assuming you have a driver ratings field in the database
                                    Double rating1 = dataSnapshot.child(driverId1).child("ratings").getValue(Double.class);
                                    Double rating2 = dataSnapshot.child(driverId2).child("ratings").getValue(Double.class);

                                    // Check if both drivers have a rating of 5 or 4
                                    boolean isRating1High = (rating1 != null && (rating1 == 5 || rating1 == 4));
                                    boolean isRating2High = (rating2 != null && (rating2 == 5 || rating2 == 4));

                                    // If driver ratings are not available, assign a default high rating
                                    if (rating1 == null) {
                                        rating1 = isRating1High ? rating1 : 5.0;
                                    }
                                    if (rating2 == null) {
                                        rating2 = isRating2High ? rating2 : 5.0;
                                    }

                                    // Compare based on ratings
                                    if (isRating1High && !isRating2High) {
                                        return -1; // driverId1 has a higher rating, so it should come first
                                    } else if (!isRating1High && isRating2High) {
                                        return 1; // driverId2 has a higher rating, so it should come first
                                    } else {
                                        // Sort in descending order of ratings (highest-rated first)
                                        return Double.compare(rating2, rating1);
                                    }
                                }
                            });



                            // Get the highest-rated driver's ID
                            String highestRatedDriverId = availableDrivers.get(0);

                            // Assign the highest-rated driver to the customer
                            driverFound = true;
                            driverFoundID = highestRatedDriverId;
                            DriversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                            HashMap<String, Object> driversMap = new HashMap<>();
                            driversMap.put("CustomerRideID", customerID);
                            DriversRef.updateChildren(driversMap);

                            // Show driver location on customerMapActivity
                            GettingDriverLocation();
                            CallCabCarButton.setText("Looking for Driver Location...");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle the error if needed
                    }
                });
            }



            @Override
            public void onKeyExited(String key) {
                // Handle the driver exit event if needed
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // Handle the driver movement event if needed
            }

            @Override
            public void onGeoQueryReady() {
                // This method will be called when all the initial data has been loaded and all
                // entered events have been fired. You can use this method to check if any drivers
                // are available nearby and proceed accordingly.
                if (availableDrivers.isEmpty()) {
                    // No available drivers found nearby
                    // Handle this scenario or increase the radius and call getClosetDriverCab() again
                } else {
                    // Some available drivers found nearby
                    // You can implement additional logic here if needed
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                // Handle the error if needed
            }
        });
    }

    //and then we get to the driver location - to tell customer where is the driver
    private void GettingDriverLocation()
    {
        String selectedDestination = destinationSpinner.getSelectedItem().toString();
        LatLng destinationLocation = getDestinationLocation(selectedDestination);

        float distance_from_destination = (float) calculateDistance(iitJodhpurLocation, destinationLocation);
        double fare = calculateFare(distance_from_destination);

        DriverLocationRefListner = DriverLocationRef.child(driverFoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists()  &&  requestType)
                        {
                            List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                            double LocationLat = 0;
                            double LocationLng = 0;
                            CallCabCarButton.setText("Driver Found :Fare - " + fare);


                            relativeLayout.setVisibility(View.VISIBLE);
                            getAssignedDriverInformation();


                            if(driverLocationMap.get(0) != null)
                            {
                                LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if(driverLocationMap.get(1) != null)
                            {
                                LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                            }

                            //adding marker - to pointing where driver is - using this lat lng
                            LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                            if(DriverMarker != null)
                            {
                                DriverMarker.remove();
                            }


                            Location location1 = new Location("");
                            location1.setLatitude(CustomerPickUpLocation.latitude);
                            location1.setLongitude(CustomerPickUpLocation.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatLng.latitude);
                            location2.setLongitude(DriverLatLng.longitude);

                            float Distance = location1.distanceTo(location2);

                            if (Distance < 90  && !fareConfirmationShown)
                            {
                                fareConfirmationShown = true;
                                CallCabCarButton.setText("Driver's Reached");
//                                fare = calculateFare(destinationLocation);

                                // Show the fare and ask for payment confirmation
                                AlertDialog.Builder builder = new AlertDialog.Builder(CustomersMapActivity.this);
                                builder.setTitle("Fare Details");
                                builder.setMessage("Fare: ₹" + String.format("%.0f", fare));
                                builder.setPositiveButton("Confirm Payment", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Mark fare as paid
                                        markFareAsPaid();

                                        // Continue with the rest of the code
                                        // ...
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                            else
                            {
                                CallCabCarButton.setText("Driver Found: " + String.valueOf(Distance));
                            }

                            DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("your driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void findCustomersForCabSharing(String selectedDestination) {
        DatabaseReference shareCustomersRef = FirebaseDatabase.getInstance().getReference("Customers").child("Share Cab Customers");
        double totalFare = 250.0; // Total fare for the trip
        double customerFare = 101.0; // Fare to be paid by the customer

        String totalFareText = "Total Fare: \u20B9" + totalFare;
        String customerFareText = "You have to pay: \u20B9" + customerFare;

        // Query customers who want to share a cab
        Query query = shareCustomersRef.orderByChild("wantsToShareCab").equalTo(true);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> customerIds = new ArrayList<>();

                // Iterate through the customers who want to share a cab
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String customerId = snapshot.getKey();
                    customerIds.add(customerId);
                }

                // Check if there are at least two customers willing to share a cab
                if (customerIds.size() >= 2) {
                    // Assign two customers to a group
                    relativeLayout1.setVisibility(View.VISIBLE);
                    String groupKey = FirebaseDatabase.getInstance().getReference().child("Groups").push().getKey();
                    DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupKey);

                    for (String customerId : customerIds) {
                        groupRef.child(customerId).setValue(true);
                    }

                    DatabaseReference customersRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
                    for (String customerId : customerIds) {
                        customersRef.child(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Retrieve the customer information
                                String customerName = dataSnapshot.child("name").getValue(String.class);
                                String customerPhone = dataSnapshot.child("phone").getValue(String.class);
                                String customerDestination = dataSnapshot.child("destination").getValue(String.class);

                                // Update the corresponding TextViews with customer information
                                txtName1.setText(customerName);
                                txtPhone1.setText(customerPhone);
                                textDestination.setText(customerDestination);
                                shareFare.setText(totalFareText + "\n" + customerFareText);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle the error if needed
                            }
                        });
                    }

                    // Make the "Call Cab" button visible
                    CallCabCarButton.setVisibility(View.VISIBLE);
                    shareFare.setVisibility(View.VISIBLE);


                } else {
                    Toast.makeText(CustomersMapActivity.this, "Not enough customers for cab sharing", Toast.LENGTH_SHORT).show();
                    relativeLayout1.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if needed
            }
        });

        DatabaseReference CustomersRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");

        }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // now let set user location enable
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //

            return;
        }
        //it will handle the refreshment of the location
        //if we dont call it we will get location only once
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        //getting the updated location
        LastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }


    //create this method -- for useing apis
    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }


    @Override
    protected void onStop()
    {
        super.onStop();
    }


    public void LogOutUser()
    {
        Intent startPageIntent = new Intent(CustomersMapActivity.this, Dashboard.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }



    private void getAssignedDriverInformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverFoundID);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                DatabaseReference driverRatingRef = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Drivers").child(driverFoundID).child("rating");

                driverRatingRef.setValue(rating);
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()  &&  dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
//                    String car = dataSnapshot.child("car").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private double calculateDistance(LatLng origin, LatLng destination) {
        if (origin == null || destination == null) {
            // Handle the case where either origin or destination is null
            // Return a default distance or throw an exception, depending on your requirements
            return 0.0;
        }

        double earthRadius = 6371; // in kilometers

        double lat1 = Math.toRadians(origin.latitude);
        double lon1 = Math.toRadians(origin.longitude);
        double lat2 = Math.toRadians(destination.latitude);
        double lon2 = Math.toRadians(destination.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = earthRadius * c;

        return distance;
    }

    private double calculateFare(float distance) {
        // Implement the logic to calculate the fare based on the distance
        // You can define your own fare calculation algorithm or use a predefined formula

        // For example, let's assume a simple fare calculation of $2 per kilometer
        double farePerKm = 11.0;
        double fare = distance * farePerKm;
        fare = Math.round(fare);
        return fare;
    }



    private LatLng getDestinationLocation(String destination) {
        switch (destination) {
            case "Airport":
                return new LatLng(26.2644, 73.0505);
            case "Railway Station":
                return new LatLng(26.2838, 73.0226);
            case "Paota":
                return new LatLng(26.2975, 73.0375);
            case "Mandore":
                return new LatLng(26.3427, 73.0443);
            default:
                return null;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}


////
//Query newQuery = customersRef.orderByChild("name").equalTo("poonam");
//            newQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//@Override
//public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//        // Check if the query result has any matching customers
//        if (dataSnapshot.exists()) {
//        // Loop through the matching customers (should be only one in this case)
//        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//        String customerId = snapshot.getKey();
//
//        // Get the customer details
//        String customerName = snapshot.child("name").getValue(String.class);
//        String customerPhone = snapshot.child("phone").getValue(String.class);
//        String destination = "Mandore"; // Set the destination as Mandore
//
//        // Update the TextView with the customer details
//        textView.setText("Name: " + customerName + "\nPhone: " + customerPhone + "\nDestination: " + destination);
//        }
//        }
//        }
//
//@Override
//public void onCancelled(@NonNull DatabaseError databaseError) {
//        // Handle the error if needed
//        }
//        });