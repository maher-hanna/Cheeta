package com.maherhanna.cheeta;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {
    Drawing drawing = null;
    Game game = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int gameType =  getIntent().getIntExtra("game_type",Game.COMPUTER_HUMAN);

        drawing = new Drawing(this);

        game = new Game(drawing,gameType,500);
        drawing.game = game;

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(game.paused){
            game.paused = false;
            game.resume();
            return;
        }

        //delay drawing of board because at the start
        //of activity the chessboardView is not active yet
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                drawing.clearBoard();
                drawing.drawAllPieces();
                drawing.show();
            }
        },100);

        game.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        game.paused = true;
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