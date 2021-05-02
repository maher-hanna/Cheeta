package com.maherhanna.cheeta;


public class Piece {

    public static final int NONE = 0;
    public static final int PAWN = 1;
    public static final int ROOK = 2;
    public static final int KNIGHT = 3;
    public static final int BISHOP = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;


    public static final int WHITE = 0;
    public static final int BLACK = 1;

    //bit representation positions
    private static final int TYPE_BITS_START = 1;
    private static final int TYPE_BITS_END = 3;

    private static final int COLOR_BITS_START = 4;
    private static final int COLOR_BITS_END = 4;

    private static final int POSITION_BITS_START = 5;
    private static final int POSITION_BITS_END = 10;

    private static final int OUT_OF_BOARD_BITS_START = 11;
    private static final int OUT_OF_BOARD_BITS_END = 11;

    private int position;



    public Piece(Piece p) {
        this.position = p.position;

    }



    public int getFile() {
        return getPosition() % 8;
    }

    public int getRank() {
        return getPosition() / 8;
    }

    public int offset(int file, int rank) {
        int newFile = getFile() + file;
        int newRank = getRank() + rank;
        if (newFile < ChessBoard.FILE_A || newFile > ChessBoard.FILE_H)
            return ChessBoard.OUT;
        if (newRank < ChessBoard.RANK_1 || newRank > ChessBoard.RANK_8)
            return ChessBoard.OUT;
        return (newRank * 8) + newFile;
    }


    public int offsetFile(int file) {
        return offset(file, 0);
    }

    public int offsetRank(int rank) {
        return offset(0, rank);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }


    public static int QUEEN_VALUE = 900;
    public static int ROOK_VALUE = 500;
    public static int BISHOP_VALUE = 330;
    public static int KNIGHT_VALUE = 320;
    public static int PAWN_VALUE = 100;
    public static int KING_VALUE = 20000;




    public static int GetOppositeColor(int color){
        return color ^= 1;
    }



}


