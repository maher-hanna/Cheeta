package com.maherhanna.cheeta;


public class Piece {
    public Type type;
    public Color color;

    public Piece(Type type, Color color) {
        this.type = type;
        this.color = color;


    }

    public Piece(Piece p) {
        this.type = p.type;
        this.color = p.color;

    }


    public enum Type {PAWN, ROOK, KNIGHT,BISHOP,QUEEN,KING }

    public enum Color {BLACK,WHITE}

}


