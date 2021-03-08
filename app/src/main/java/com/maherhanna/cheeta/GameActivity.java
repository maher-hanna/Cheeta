package com.maherhanna.cheeta;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
        int gameType =  getIntent().getIntExtra("game_type",Game.COMPUTER_HUMAN);
        int computerPlayerDelay = getIntent().getIntExtra("computer_player_delay",0);
        Game game = new Game(drawing,gameType,computerPlayerDelay);

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
                drawing.show();
            }
        }, 100);
        //------------------------------

    }

    @Override
    public void onBackPressed() {
        finishGame(getString(R.string.finish_game_message));
    }

    private void finishGame(String alertMessage) {
        DialogInterface.OnClickListener dialogClickListner = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage(alertMessage)
                .setPositiveButton("Yes",dialogClickListner)
                .setNegativeButton("No" ,dialogClickListner).show();
    }
}