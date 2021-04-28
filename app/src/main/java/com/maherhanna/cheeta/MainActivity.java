package com.maherhanna.cheeta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btn_start;
    RadioButton rb_white;
    RadioButton rb_black;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        rb_white = findViewById(R.id.rb_white);
        rb_black = findViewById(R.id.rb_black);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(MainActivity.this,GameActivity.class);
                int humanPiecesColor = Piece.WHITE;
                if(rb_black.isSelected())humanPiecesColor = Piece.BLACK;
                gameIntent.putExtra("humanPiecesColor",humanPiecesColor);
                startActivity(gameIntent);
            }
        });



    }


}