package com.example.ridesharing;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Driver_page extends AppCompatActivity {

    private Button driver_login_button;
    private Button driver_register_button;
    private TextView driver_forgot_password;
    private TextView driver_registerLink;
    private EditText driveEmail;
    private EditText driverPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference DriverDatabaseRef ;
    private String onlineDriverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_page);

        mAuth = FirebaseAuth.getInstance();

        driver_register_button = (Button) findViewById(R.id.Driver_register);
        driver_login_button = (Button) findViewById(R.id.driver_login);
        driveEmail = (EditText) findViewById(R.id.driver_email);
        driverPassword = (EditText) findViewById(R.id.driver_password);
        driver_registerLink = (TextView) findViewById(R.id.driver_accountExist);

        driver_register_button.setVisibility(View.INVISIBLE);
        driver_register_button.setEnabled(false);

        driver_registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driver_login_button.setVisibility(View.INVISIBLE);
                driver_registerLink.setVisibility(View.INVISIBLE);
                driver_register_button.setVisibility(View.VISIBLE);
                driver_register_button.setEnabled(true);

            }
        });

        driver_register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_driver = driveEmail.getText().toString();
                String password_driver = driverPassword.getText().toString();

                RegisterDriver(email_driver, password_driver);
            }
        });

        driver_login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                String email_driver = driveEmail.getText().toString();
                String password_driver = driverPassword.getText().toString();

                Sign_in_Driver(email_driver, password_driver);
            }
        });
    }

    private void Sign_in_Driver(String email_driver, String password_driver) {
        if (TextUtils.isEmpty(email_driver)) {
            Toast.makeText(Driver_page.this, "Please write your Email..", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password_driver)) {
            Toast.makeText(Driver_page.this, "Please write your Password..", Toast.LENGTH_SHORT).show();

        } else {
            mAuth.signInWithEmailAndPassword(email_driver, password_driver).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Driver_page.this, "Driver Login Successfully", Toast.LENGTH_SHORT).show();


                        Intent DriverIntent = new Intent(Driver_page.this, DriverMapsActivity.class);
                        DriverIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(DriverIntent);



                    } else {
                        Toast.makeText(Driver_page.this, "Driver Login Unsuccessfully", Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }

    private void RegisterDriver(String email_driver, String password_driver) {
        if (TextUtils.isEmpty(email_driver)) {
            Toast.makeText(Driver_page.this, "Please write your Email..", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password_driver)) {
            Toast.makeText(Driver_page.this, "Please write your Password..", Toast.LENGTH_SHORT).show();

        } else {
            mAuth.createUserWithEmailAndPassword(email_driver, password_driver).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        onlineDriverID = mAuth.getCurrentUser().getUid();
                        DriverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(onlineDriverID);

                        DriverDatabaseRef.setValue(true);
                        Intent driverIntent = new Intent(Driver_page.this, DriverMapsActivity.class);
                        startActivity(driverIntent);

                        Toast.makeText(Driver_page.this, "Driver Register Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Driver_page.this, "Driver Register Unsuccessfully", Toast.LENGTH_SHORT).show();
                    }
                }

            });


        }
    }
}
