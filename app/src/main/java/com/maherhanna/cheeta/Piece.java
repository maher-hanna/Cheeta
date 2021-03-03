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

    public int getPosition(){return playerPiece.getPosition();}
    public int getFile(){return playerPiece.getFile();}
    public int getRank(){return playerPiece.getRank();}
    public int offset(int file, int rank){
        return ChessBoard.GetPosition(getFile() + file, getRank() + rank);
    }
    public int offsetFile(int file){
        return ChessBoard.GetPosition(getFile() + file, getRank());
    }
    public int offsetRank(int rank){
        return ChessBoard.GetPosition(getFile(), getRank() + rank);
    }



    public enum Type {PAWN, ROOK, KNIGHT,BISHOP,QUEEN,KING }

    public enum Color {BLACK,WHITE}

}


