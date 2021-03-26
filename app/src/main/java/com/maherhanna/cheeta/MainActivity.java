package com.maherhanna.cheeta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btn_start;
    Spinner spinner_gameType;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        spinner_gameType = findViewById(R.id.spinner_gameType);

        ArrayList<String> gameTypes = new ArrayList<>();
        gameTypes.add(getString(R.string.me_vs_computer));
        gameTypes.add(getString(R.string.computer_vs_computer));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,gameTypes);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_gameType.setAdapter(arrayAdapter);

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