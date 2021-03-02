package com.maherhanna.cheeta;


public class Piece {
    public Type type;
    public Color color;
    PlayerPiece playerPiece;

    public Piece(Type type, Color color,PlayerPiece playerPiece) {
        this.type = type;
        this.color = color;
        this.playerPiece = playerPiece;


    }

    public Piece(Piece p) {
        this.type = p.type;
        this.color = p.color;
        this.playerPiece = p.playerPiece;

    }


    public enum Type {PAWN, ROOK, KNIGHT,BISHOP,QUEEN,KING }

    public enum Color {BLACK,WHITE}

}


