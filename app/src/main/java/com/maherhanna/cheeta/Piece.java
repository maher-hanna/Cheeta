package com.maherhanna.cheeta;


public class Piece {
    private int bitValue;

    public Piece(Type type, Color color, int position) {
        this.setType(type);
        this.setColor(color);
        this.setPosition(position);


    }
    public Piece(Type type, Color color) {
        this.setType(type);
        this.setColor(color);

    }

    public Piece(Piece p) {
        this.bitValue = p.bitValue;

    }

    public int getFile(){return getPosition() % 8;}
    public int getRank(){return getPosition() / 8;}
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

    public Type getType() {
        int typeInt = BitMath.getBitsValue(bitValue,Piece.TYPE_BITS_START,Piece.TYPE_BITS_END);
        return Piece.Type.values()[typeInt];
    }

    public void setType(Type type) {
        int typeInt = type.ordinal();
        bitValue =  BitMath.setBitsValue(bitValue,TYPE_BITS_START,TYPE_BITS_END,typeInt);
    }

    public Color getColor() {
        int colorInt = BitMath.getBitsValue(bitValue,COLOR_BITS_START,COLOR_BITS_END);
        return Piece.Color.values()[colorInt];
    }

    public void setColor(Color color) {
        int colorInt = color.ordinal();
        bitValue =  BitMath.setBitsValue(bitValue,COLOR_BITS_START,COLOR_BITS_END,colorInt);
    }

    public void setPosition(int position) {
        bitValue =  BitMath.setBitsValue(bitValue,POSITION_BITS_START,POSITION_BITS_END,position);

    }
    public int getPosition(){
        return BitMath.getBitsValue(bitValue,POSITION_BITS_START,POSITION_BITS_END);

    }

    public boolean isOutOfBoard(){
        int out = BitMath.getBitsValue(bitValue,OUT_OF_BOARD_BITS_START,
                OUT_OF_BOARD_BITS_END);
        return out == 1;
    }

    public void setOutOfBoard(boolean isOut){
        int out = 0;
        if(isOut) out = 1;
        BitMath.setBitsValue(bitValue,OUT_OF_BOARD_BITS_START,
                OUT_OF_BOARD_BITS_END,out);

    }




    public enum Type {PAWN, ROOK, KNIGHT,BISHOP,QUEEN,KING }

    public enum Color {
        BLACK(0),
        WHITE(1);

        private final int color;
        Color(final int color){this.color = color;}
        public Color getOpposite(){
            if(color == 0){
                return WHITE;
            }
            else {
                return BLACK;
            }
        }

    }



    public static final Piece BLACK_PAWN = new Piece(Type.PAWN,Color.BLACK,0);
    public static final Piece BLACK_ROOK = new Piece(Type.ROOK,Color.BLACK,0);
    public static final Piece BLACK_KNIGHT = new Piece(Type.KNIGHT,Color.BLACK,0);
    public static final Piece BLACK_BISHOP = new Piece(Type.BISHOP,Color.BLACK,0);
    public static final Piece BLACK_QUEEN = new Piece(Type.QUEEN,Color.BLACK,0);
    public static final Piece BLACK_KING = new Piece(Type.KING,Color.BLACK,0);
    public static final Piece WHITE_PAWN = new Piece(Type.PAWN,Color.WHITE,0);
    public static final Piece WHITE_ROOK = new Piece(Type.ROOK,Color.WHITE,0);
    public static final Piece WHITE_KNIGHT = new Piece(Type.KNIGHT,Color.WHITE,0);
    public static final Piece WHITE_BISHOP = new Piece(Type.BISHOP,Color.WHITE,0);
    public static final Piece WHITE_QUEEN = new Piece(Type.QUEEN,Color.WHITE,0);
    public static final Piece WHITE_KING = new Piece(Type.KING,Color.WHITE,0);

    public static final Piece Pawn(Color color){
        return new Piece(Type.PAWN,color);
    }

    public static final Piece Rook(Color color){
        return new Piece(Type.ROOK,color);
    }

    public static final Piece Knight(Color color){
        return new Piece(Type.KNIGHT,color);
    }
    public static final Piece Bishop(Color color){
        return new Piece(Type.BISHOP,color);
    }
    public static final Piece Queen(Color color){
        return new Piece(Type.QUEEN,color);
    }
    public static final Piece King(Color color){
        return new Piece(Type.KING,color);
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

    private static final int  OUT_OF_BOARD_BITS_START = 11;
    private static final int OUT_OF_BOARD_BITS_END = 11;




}


