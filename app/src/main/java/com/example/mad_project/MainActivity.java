package com.example.mad_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    Intent menu_display = new Intent(MainActivity.this, main_menu.class);
                    startActivity(menu_display);
                    finish();
                }
            }
        };
        timer.start();
    }
}

