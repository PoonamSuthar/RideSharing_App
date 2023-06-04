package com.example.ridesharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Dashboard extends AppCompatActivity {
    private Button driver_button_;
    private Button rider_button_;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        rider_button_ =(Button) findViewById(R.id.rider_button);
        driver_button_ =(Button) findViewById(R.id.driver_button);

        rider_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginRiderIntent =new Intent(Dashboard.this,Rider_page.class);
                startActivity(loginRiderIntent);

            }
        });
        driver_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginDriverIntent =new Intent(Dashboard.this,Driver_page.class);
                startActivity(loginDriverIntent);

            }
        });

    }

}