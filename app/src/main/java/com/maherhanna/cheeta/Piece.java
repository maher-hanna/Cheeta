package com.maherhanna.cheeta;


public class Piece {
    public Type type;
    public Color color;
    public int position;

    public Piece(Type type, Color color, int position) {
        this.type = type;
        this.color = color;
        this.position = position;


    }
    public Piece(Type type, Color color) {
        this.type = type;
        this.color = color;
        this.position = ChessBoard.OUT_OF_BOARD;

    }

    public Piece(Piece p) {
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

    public enum Color {
        BLACK(0),
        WHITE(1);

        private final int color;
        private Color(final int color){this.color = color;}
        public Color getOpposite(){
            if(color == 0){
                return WHITE;
            }
            else {
                return BLACK;
            }
        }

    }
    public static final Piece BLACK_PAWN = new Piece(Type.PAWN,Color.BLACK,-1);
    public static final Piece BLACK_ROOK = new Piece(Type.ROOK,Color.BLACK,-1);
    public static final Piece BLACK_KNIGHT = new Piece(Type.KNIGHT,Color.BLACK,-1);
    public static final Piece BLACK_BISHOP = new Piece(Type.BISHOP,Color.BLACK,-1);
    public static final Piece BLACK_QUEEN = new Piece(Type.QUEEN,Color.BLACK,-1);
    public static final Piece BLACK_KING = new Piece(Type.KING,Color.BLACK,-1);
    public static final Piece WHITE_PAWN = new Piece(Type.PAWN,Color.WHITE,-1);
    public static final Piece WHITE_ROOK = new Piece(Type.ROOK,Color.WHITE,-1);
    public static final Piece WHITE_KNIGHT = new Piece(Type.KNIGHT,Color.WHITE,-1);
    public static final Piece WHITE_BISHOP = new Piece(Type.BISHOP,Color.WHITE,-1);
    public static final Piece WHITE_QUEEN = new Piece(Type.QUEEN,Color.WHITE,-1);
    public static final Piece WHITE_KING = new Piece(Type.KING,Color.WHITE,-1);

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



}


