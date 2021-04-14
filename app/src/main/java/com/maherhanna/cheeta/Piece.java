package com.maherhanna.cheeta;


public class Piece {
    private int position;
    private Color color;
    private Type type;

    public Piece(Type type, Color color, int position) {
        this.setType(type);
        this.setColor(color);
        this.setPosition(position);


    }


    public Piece(Piece p) {
        this.position = p.position;
        this.color = p.color;
        this.type = p.type;

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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }


    public enum Type {PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING}

    public enum Color {
        BLACK(0),
        WHITE(1);

        private final int color;

        Color(final int color) {
            this.color = color;
        }

        public Color getOpposite() {
            if (color == 0) {
                return WHITE;
            } else {
                return BLACK;
            }
        }

    }


    public static int QUEEN_VALUE = 900;
    public static int ROOK_VALUE = 500;
    public static int BISHOP_VALUE = 330;
    public static int KNIGHT_VALUE = 320;
    public static int PAWN_VALUE = 100;
    public static int KING_VALUE = 20000;


    //bit representation positions
    private static final int TYPE_BITS_START = 1;
    private static final int TYPE_BITS_END = 3;

    private static final int COLOR_BITS_START = 4;
    private static final int COLOR_BITS_END = 4;

    private static final int POSITION_BITS_START = 5;
    private static final int POSITION_BITS_END = 10;

    private static final int OUT_OF_BOARD_BITS_START = 11;
    private static final int OUT_OF_BOARD_BITS_END = 11;

    public static int GetValueOf(Type type){
        switch (type){
            case PAWN:
                return Piece.PAWN_VALUE;
            case ROOK:
                return Piece.ROOK_VALUE;
            case KNIGHT:
                return Piece.KNIGHT_VALUE;
            case BISHOP:
                return Piece.BISHOP_VALUE;
            case QUEEN:
                return Piece.QUEEN_VALUE;
            case KING:
                return Piece.KING_VALUE;
            default:
                return 0;
        }
    }


}


