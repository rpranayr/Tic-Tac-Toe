package com.example.mad_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class main_menu extends AppCompatActivity {

    Intent menu_intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void go_to_search(View view)
    {
        menu_intent = new Intent(main_menu.this, web_search.class);
        startActivity(menu_intent);
        finish();
    }

    public void go_to_singlePlayer(View view)
    {
        menu_intent = new Intent(main_menu.this, single_player.class);
        startActivity(menu_intent);
        finish();
    }
}
