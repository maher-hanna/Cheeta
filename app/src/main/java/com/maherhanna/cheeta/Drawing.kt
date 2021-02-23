package com.maherhanna.cheeta

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class Drawing(activity: MainActivity) {
    private val activity:MainActivity
    private val chessboardView:ChessboardView
    private val black_pawn: Bitmap
    private val white_pawn: Bitmap
    private val black_bishop: Bitmap
    private val white_bishop: Bitmap
    private val black_knight: Bitmap
    private val white_knight: Bitmap
    private val black_rook: Bitmap
    private val white_rook: Bitmap
    private val black_queen: Bitmap
    private val white_queen: Bitmap
    private val black_king: Bitmap
    private val white_king: Bitmap

    init{
        this.activity = activity
        chessboardView = this.activity.findViewById(R.id.chessboardView)

        //initialize pieces drawables
        black_pawn = BitmapFactory.decodeResource(activity.resources,R.drawable.black_pawn)
        white_pawn = BitmapFactory.decodeResource(activity.resources,R.drawable.white_pawn)
        black_bishop = BitmapFactory.decodeResource(activity.resources,R.drawable.black_bishop)
        white_bishop = BitmapFactory.decodeResource(activity.resources,R.drawable.white_bishop)
        black_knight = BitmapFactory.decodeResource(activity.resources,R.drawable.black_knight)
        white_knight = BitmapFactory.decodeResource(activity.resources,R.drawable.white_knight)
        black_rook = BitmapFactory.decodeResource(activity.resources,R.drawable.black_rook)
        white_rook = BitmapFactory.decodeResource(activity.resources,R.drawable.white_rook)
        black_queen = BitmapFactory.decodeResource(activity.resources,R.drawable.black_queen)
        white_queen = BitmapFactory.decodeResource(activity.resources,R.drawable.white_queen)
        black_king = BitmapFactory.decodeResource(activity.resources,R.drawable.black_king)
        white_king = BitmapFactory.decodeResource(activity.resources,R.drawable.white_king)
    }

    public fun drawPieces(piece:Piece){

    }
}