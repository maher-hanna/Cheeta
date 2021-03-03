package com.maherhanna.cheeta;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {
    Drawing drawing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(drawing == null){
            drawing = new Drawing(this);

        }

        Game game = new Game(drawing);
        game.start();


        /*
        schedule drawing of pieces after a small amount of time
        or the pieces will not draw if called immediately
        after onResume because the chessboardView don't give
        the correct dimensions yet
        */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                drawing.drawAllPieces();
            }
        }, 100);
        //------------------------------


    }
}