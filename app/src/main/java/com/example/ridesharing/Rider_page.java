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

public class Rider_page extends AppCompatActivity {
    private Button rider_login_button;
    private Button rider_register_button;
    private TextView forgot_password;
    private TextView registerLink;
    private EditText EmailRider;
    private EditText PasswordRider;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_page);

        mAuth = FirebaseAuth.getInstance();
        rider_register_button = (Button) findViewById(R.id.RegistrationDetail);
        rider_login_button = (Button) findViewById(R.id.loginDetail);
        EmailRider = (EditText) findViewById(R.id.EmailAddress);
        PasswordRider = (EditText) findViewById(R.id.Password);
        registerLink = (TextView) findViewById(R.id.acountExistorNot);

        rider_register_button.setVisibility(View.INVISIBLE);
        rider_register_button.setEnabled(false);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rider_login_button.setVisibility(View.INVISIBLE);
                registerLink.setVisibility(View.INVISIBLE);
                rider_register_button.setVisibility(View.VISIBLE);
                rider_register_button.setEnabled(true);

            }
        });

//       **************************** Registration for Rider  *********************************       //
        rider_register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = EmailRider.getText().toString();
                String password = PasswordRider.getText().toString();

                RegisterRider(email, password);
            }
        });

        //  ************************ Login for rider ********************************************* //
        rider_login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                String email = EmailRider.getText().toString();
                String password = PasswordRider.getText().toString();

                Sign_in_Rider(email, password);
            }
        });

    }

    //    Login Details
    private void Sign_in_Rider(String email,String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Rider_page.this, "Please write your Email..", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(Rider_page.this, "Please write your Password..", Toast.LENGTH_SHORT).show();

        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Rider_page.this, "Rider Login Successfully", Toast.LENGTH_SHORT).show();

                        Intent RiderIntent = new Intent(Rider_page.this, RiderMapsActivity.class);
                        RiderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(RiderIntent);



                    } else {
                        Toast.makeText(Rider_page.this, "Rider Login Unsuccessfully", Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }

    //    Registration
    private void RegisterRider(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Rider_page.this, "Please write your Email..", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(Rider_page.this, "Please write your Password..", Toast.LENGTH_SHORT).show();

        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Rider_page.this, "Rider Register Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Rider_page.this, "Rider Register Unsuccessfully", Toast.LENGTH_SHORT).show();
                    }
                }

            });


        }
    }
}