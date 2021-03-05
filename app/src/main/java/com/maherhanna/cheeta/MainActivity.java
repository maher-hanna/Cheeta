package com.maherhanna.cheeta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    Button btn_start;
    Spinner spinner_gameType;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        spinner_gameType = findViewById(R.id.spinner_gameType);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(MainActivity.this,GameActivity.class);
                int gameType = spinner_gameType.getSelectedItemPosition();
                gameIntent.putExtra("game_type",gameType);
                startActivity(gameIntent);
            }
        });



    }


}