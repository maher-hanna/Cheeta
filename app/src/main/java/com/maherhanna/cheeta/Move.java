package com.maherhanna.cheeta;

public class Move {

    private int bitValue;


    public Move(Piece piece, int from, int to){
        bitValue = 0;
        setPieceType(piece.getType());
        setPieceColor(piece.getColor());
        setFrom(from);
        setTo(to);
    }

    public Move(Piece piece, int from, int to,boolean takes, Piece.Type takenPieceType){
        this(piece,from,to);
        setTakes(takes,takenPieceType);
    }

    public Move(Move copy){
        bitValue = copy.bitValue;
    }


    public boolean isCastling(){
        return BitMath.getBitsValue(bitValue,CASTLING_START
        ,CASTLING_MASK) == 1;
    }
    public CastlingType getCastlingType(){
        return CastlingType.values()
                [BitMath.getBitsValue(bitValue,CASTLING_TYPE_START,CASTLING_TYPE_MASK)];
    }

    public boolean isTake(){
        return BitMath.getBitsValue(bitValue,TAKE_START
                ,TAKE_MASK) == 1;
    }
    public Piece.Type getTakenPieceType(){
        return Piece.Type.values()
                [BitMath.getBitsValue(bitValue,TAKE_TYPE_START,TAKE_TYPE_MASK)];
    }

    public boolean isPromote() {
        return BitMath.getBitsValue(bitValue,PROMOTE_START
                ,PROMOTE_MASK) == 1;
    }
    public Piece.Type getPromotionPieceType(){
        Piece.Type type = Piece.Type.QUEEN;
        PromoteToPieceType promotionPieceType = PromoteToPieceType.values()[
                BitMath.getBitsValue(bitValue,PROMOTE_TYPE_START,PROMOTE_TYPE_MASK)];
        switch (promotionPieceType){
            case ROOK:
                type = Piece.Type.ROOK;
                break;
            case QUEEN:
                type = Piece.Type.QUEEN;
                break;
            case BISHOP:
                type = Piece.Type.BISHOP;
                break;
            case KNIGHT:
                type = Piece.Type.KNIGHT;
                break;
        }
        return type;
    }

    public boolean isEnPasant(){return BitMath.getBitsValue(bitValue,ENPASSANT_START
            ,ENPASSANT_MASK) == 1;
    }

    public boolean isPawnDoubleMove(){
        boolean pawnDoubleMove = false;
        if(getPieceType() == Piece.Type.PAWN &&
                Math.abs(ChessBoard.GetRank(getTo()) - ChessBoard.GetRank(getFrom())) == 2){
            pawnDoubleMove = true;
        }
        return pawnDoubleMove;
    }

    public int getFrom(){
        return BitMath.getBitsValue(bitValue,FROM_START,FROM_MASK);
    }
    public void setFrom(int position){
        bitValue =  BitMath.setBitsValue(bitValue,FROM_START,FROM_MASK,position);
    }
    public int getTo(){return BitMath.getBitsValue(bitValue,TO_START,TO_MASK);}
    public void setTo(int position){
        bitValue =  BitMath.setBitsValue(bitValue,TO_START,TO_MASK,position);
    }

    public Piece.Color getColor(){
        return Piece.Color.values()[BitMath.getBitsValue(bitValue,COLOR_START,COLOR_MASK)];
    }
    public Piece.Type getPieceType(){
        return Piece.Type.values()[BitMath.getBitsValue(bitValue,TYPE_START,TYPE_MASK)];

    }
    public void setPieceType(Piece.Type type){
        int value = type.ordinal();
        bitValue = BitMath.setBitsValue(bitValue,TYPE_START,TYPE_MASK,value);
    }

    public void setPieceColor(Piece.Color color){
        int value = color.ordinal();
        bitValue = BitMath.setBitsValue(bitValue,COLOR_START,COLOR_MASK,value);
    }

    public void setCastling(boolean castling)
    {
        int value = 0;
        if(castling) value = 1;
        bitValue =  BitMath.setBitsValue(bitValue,CASTLING_START,CASTLING_MASK,value);
    }
    public void setCastlingType(CastlingType castlingType)
    {
        int value = castlingType.ordinal();
        bitValue =  BitMath.setBitsValue(bitValue,CASTLING_TYPE_START,CASTLING_TYPE_MASK,value);
    }

    public void setCastling(boolean castling,CastlingType castlingType){
        setCastling(castling);
        setCastlingType(castlingType);
    }
    public void setTakes(boolean takes){
        int value = 0;
        if(takes) value = 1;
        bitValue =  BitMath.setBitsValue(bitValue,TAKE_START,TAKE_MASK,value);
    }

    public void setTakenPieceType(Piece.Type pieceType){
        int value = pieceType.ordinal();
        bitValue =  BitMath.setBitsValue(bitValue,TAKE_TYPE_START,TAKE_TYPE_MASK,value);
    }

    public void setTakes(boolean takes, Piece.Type pieceType){
        setTakes(takes);
        setTakenPieceType(pieceType);
    }

    public void setPromotes(boolean promotes){
        int value = 0;
        if(promotes) value = 1;
        bitValue = BitMath.setBitsValue(bitValue,PROMOTE_START,PROMOTE_MASK,value);
    }

    public void setPromotionPieceType(PromoteToPieceType promotionPieceType){
        int value = promotionPieceType.ordinal();
        bitValue = BitMath.setBitsValue(bitValue,PROMOTE_TYPE_START,PROMOTE_TYPE_MASK,value);
    }

    public void setPromotes(boolean promotes,PromoteToPieceType promotionPieceType){
        setPromotes(promotes);
        setPromotionPieceType(promotionPieceType);
    }

    public void setEnPasant(boolean enPasant){
        int value = 0;
        if(enPasant) value = 1;
        bitValue = BitMath.setBitsValue(bitValue,ENPASSANT_START,ENPASSANT_MASK,value);
        setTakes(true, Piece.Type.PAWN);
    }




    public enum CastlingType {CASTLING_kING_SIDE,CASTLING_QUEEN_SIDE}

    public enum PromoteToPieceType{QUEEN,KNIGHT,ROOK,BISHOP}

    private static final int TYPE_MASK = 7;
    private static final int TYPE_START = 0;

    private static final int COLOR_MASK = 8;
    private static final int COLOR_START = 3;

    private static final int FROM_MASK = 1008;
    private static final int FROM_START = 4;

    private static final int TO_MASK = 64512;
    private static final int TO_START = 10;

    private static final int CASTLING_MASK = 65536;
    private static final int CASTLING_START = 16;

    private static final int CASTLING_TYPE_MASK = 131072;
    private static final int CASTLING_TYPE_START = 17;

    private static final int TAKE_MASK = 262144;
    private static final int TAKE_START = 18;

    private static final int TAKE_TYPE_MASK = 3670016;
    private static final int TAKE_TYPE_START = 19;

    private static final int PROMOTE_MASK = 4194304;
    private static final int PROMOTE_START = 22;

    private static final int PROMOTE_TYPE_MASK = 58720256;
    private static final int PROMOTE_TYPE_START = 23;

    private static final int ENPASSANT_MASK = 67108864;
    private static final int ENPASSANT_START = 26;
}
