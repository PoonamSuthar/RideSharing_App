package com.example.ridesharing;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private String getType;

    private CircleImageView profileImageView;
    private EditText nameEditText, phoneEditText, driverCarName;
    private ImageView closeButton, saveButton;
    private TextView profileChangeBtn;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getType = getIntent().getStringExtra("type");
        Toast.makeText(this, getType, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);

        profileImageView = findViewById(R.id.profile_image);

        nameEditText = findViewById(R.id.name);
        phoneEditText = findViewById(R.id.phone_number);
        driverCarName = findViewById(R.id.driver_car_name);

//        if (getType.equals("Drivers")) {
//            driverCarName.setVisibility(VISIBLE);
//        }

        closeButton = findViewById(R.id.close_button);
        saveButton = findViewById(R.id.save_button);

        profileChangeBtn = findViewById(R.id.change_picture_btn);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getType.equals("Drivers")) {
                    startActivity(new Intent(SettingActivity.this, DriverMapsActivity.class));
                } else {
//                    startActivity(new Intent(SettingActivity.this, RiderMapsActivity.class));
                    startActivity(new Intent(SettingActivity.this, CustomersMapActivity.class));

                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndSaveOnlyInformation();
            }
        });

        profileChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add code for selecting a new profile picture
            }
        });

        getUserInformation();
    }

    private void validateAndSaveOnlyInformation() {
        if (TextUtils.isEmpty(nameEditText.getText().toString())) {
            Toast.makeText(this, "Please provide your name.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneEditText.getText().toString())) {
            Toast.makeText(this, "Please provide your phone number.", Toast.LENGTH_SHORT).show();
        }  else {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", mAuth.getCurrentUser().getUid());
            userMap.put("name", nameEditText.getText().toString());
            userMap.put("phone", phoneEditText.getText().toString());

//            if (getType.equals("Drivers")) {
//                userMap.put("car", driverCarName.getText().toString());
//            }

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);

            if (getType.equals("Drivers")) {
                startActivity(new Intent(SettingActivity.this, DriverMapsActivity.class));
            } else {
//                startActivity(new Intent(SettingActivity.this, RiderMapsActivity.class));
                startActivity(new Intent(SettingActivity.this, CustomersMapActivity.class));

            }
        }
    }

    private void getUserInformation() {
        String userId = mAuth.getCurrentUser().getUid();
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = "";
                    String phone = "";
//                    String car = "";

                    if (dataSnapshot.hasChild("name")) {
                        name = dataSnapshot.child("name").getValue(String.class);
                    }
                    if (dataSnapshot.hasChild("phone")) {
                        phone = dataSnapshot.child("phone").getValue(String.class);
                    }
//                    if (getType.equals("Drivers") && dataSnapshot.hasChild("car")) {
//                        car = dataSnapshot.child("car").getValue(String.class);
//                        driverCarName.setText(car);
//                    }

                    nameEditText.setText(name);
                    phoneEditText.setText(phone);

                    // Load profile picture if available
//                    if (dataSnapshot.hasChild("image")) {
//                        String imageUrl = dataSnapshot.child("image").getValue(String.class);
//                        // Load the image into the profileImageView using your preferred image loading library
//                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle onCancelled() event if needed
            }
        });
    }

}
