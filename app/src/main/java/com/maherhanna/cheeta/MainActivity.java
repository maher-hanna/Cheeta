package com.maherhanna.cheeta;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_start;
    Drawing drawing;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawing == null){
                    drawing = new Drawing(MainActivity.this);

                }

                Game game = new Game(drawing);
                btn_start.setVisibility(View.INVISIBLE);
                drawing.drawAllPieces();
                game.start();
            }
        });



    }


}