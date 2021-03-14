package com.maherhanna.cheeta;


public class Square {
    public Type type;
    public Color color;
    public int position;

    public Square(Type type, Color color, int position) {
        this.type = type;
        this.color = color;
        this.position = position;


    }

    public Square(Square p) {
        this.type = p.type;
        this.color = p.color;
        this.position = p.position;

    }

    public int getPosition(){return position;}
    public int getFile(){return position % 8;}
    public int getRank(){return position / 8;}
    public int offset(int file, int rank){
        int newFile = getFile() + file;
        int newRank = getRank() + rank;
        if(newFile < ChessBoard.FILE_A || newFile > ChessBoard.FILE_H) return ChessBoard.OUT_OF_BOARD;
        if(newRank < ChessBoard.RANK_1 || newRank > ChessBoard.RANK_8) return ChessBoard.OUT_OF_BOARD;

        return (newRank * 8 )  + newFile;
    }
    public int offsetFile(int file){
        return offset(file,0);
    }
    public int offsetRank(int rank){
        return offset(0,rank);
    }



    public enum Type {PAWN, ROOK, KNIGHT,BISHOP,QUEEN,KING }

    public enum Color {BLACK,WHITE}



}


