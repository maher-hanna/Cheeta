package com.maherhanna.cheeta;


public class Piece {
    public PieceType pieceType;
    public PieceColor pieceColor;
    private int position;

    public Piece(PieceType pieceType, PieceColor pieceColor,int position) {
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
        setPosition(position);

    }

    public void setPosition(int newPosition){
        if(newPosition < ChessBoard.MIN_POSITION || newPosition > ChessBoard.MAX_POSITION){
            throw new IndexOutOfBoundsException("Trying to put a piece outside of chess board");
        }
        this.position = newPosition;
    }
    public int getPosition(){
        return this.position;
    }


    public enum PieceType {PAWN, ROOK, KNIGHT,BISHOP,QUEEN,KING }

    public enum PieceColor {BLACK,WHITE}

}


