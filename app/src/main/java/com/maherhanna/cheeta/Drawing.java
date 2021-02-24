package com.maherhanna.cheeta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

public class Drawing {
    private final MainActivity activity;
    private final ChessboardView chessboardView;
    private final Bitmap black_pawn;
    private final Bitmap white_pawn;
    private final Bitmap black_bishop;
    private final Bitmap white_bishop;
    private final Bitmap black_knight;
    private final Bitmap white_knight;
    private final Bitmap black_rook;
    private final Bitmap white_rook;
    private final Bitmap black_queen;
    private final Bitmap white_queen;
    private final Bitmap black_king;
    private final Bitmap white_king;


    public Drawing(MainActivity activity) {
        this.activity = activity;
        this.chessboardView = this.activity.findViewById(R.id.chessboardView);
        this.black_pawn = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_pawn);
        this.white_pawn = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_pawn);
        this.black_bishop = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_bishop);
        this.white_bishop = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_bishop);
        this.black_knight = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_knight);
        this.white_knight = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_knight);
        this.black_rook = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_rook);
        this.white_rook = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_rook);
        this.black_queen = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_queen);
        this.white_queen = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_queen);
        this.black_king = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_king);
        this.white_king = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_king);
    }
}
