package com.maherhanna.cheeta;

public class Move {

    private long bitValue;


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
                [(int)BitMath.getBitsValue(bitValue,CASTLING_TYPE_START,CASTLING_TYPE_MASK)];
    }

    public boolean isTake(){
        return BitMath.getBitsValue(bitValue,TAKE_START
                ,TAKE_MASK) == 1;
    }
    public Piece.Type getTakenPieceType(){
        return Piece.Type.values()
                [(int)BitMath.getBitsValue(bitValue,TAKE_TYPE_START,TAKE_TYPE_MASK)];
    }

    public boolean isPromote() {
        return BitMath.getBitsValue(bitValue,PROMOTE_START
                ,PROMOTE_MASK) == 1;
    }
    public Piece.Type getPromotionPieceType(){
        Piece.Type type = Piece.Type.QUEEN;
        PromoteToPieceType promotionPieceType = PromoteToPieceType.values()[
                (int)BitMath.getBitsValue(bitValue,PROMOTE_TYPE_START,PROMOTE_TYPE_MASK)];
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
        return BitMath.isBitSet(bitValue,PAWN_DOUBLE_PUSH_START);
    }

    public int getPreviousFiftyMoves(){return (int)BitMath.getBitsValue(bitValue, PREVIOUS_FIFTY_MOVES_START,
            PREVIOUS_FIFTY_MOVES_MASK);}

    public int getPreviousWCastlingRights(){return (int) BitMath.getBitsValue(bitValue, PREVIOUS_WCASTLING_RIGHTS_START,
            PREVIOUS_WCASTLING_RIGHTS_MASK);}

    public int getPreviousBCastlingRights(){return (int) BitMath.getBitsValue(bitValue, PREVIOUS_BCASTLING_RIGHTS_START,
            PREVIOUS_BCASTLING_RIGHTS_MASK);}

    public int getFrom(){
        return (int)BitMath.getBitsValue(bitValue,FROM_START,FROM_MASK);
    }
    public void setFrom(int position){
        bitValue =  BitMath.setBitsValue(bitValue,FROM_START,FROM_MASK,position);
    }
    public int getTo(){return (int)BitMath.getBitsValue(bitValue,TO_START,TO_MASK);}
    public void setTo(int position){
        bitValue =  BitMath.setBitsValue(bitValue,TO_START,TO_MASK,position);
    }

    public Piece.Color getColor(){
        return Piece.Color.values()[(int)BitMath.getBitsValue(bitValue,COLOR_START,COLOR_MASK)];
    }
    public Piece.Type getPieceType(){
        return Piece.Type.values()[(int)BitMath.getBitsValue(bitValue,TYPE_START,TYPE_MASK)];

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
    public void setPawnDoublePush(boolean pawnDoublePush){
        int value = 0;
        if(pawnDoublePush) value = 1;
        bitValue = BitMath.setBitsValue(bitValue,PAWN_DOUBLE_PUSH_START,PAWN_DOUBLE_PUSH_MASK,value);
    }
    public void setPreviousFiftyMoves(int fiftyMoves){
        bitValue = BitMath.setBitsValue(bitValue, PREVIOUS_FIFTY_MOVES_START, PREVIOUS_FIFTY_MOVES_MASK,fiftyMoves);
    }

    public void setPreviousWCastlingRights(int whiteCastlingRights){bitValue =  BitMath.setBitsValue(bitValue, PREVIOUS_WCASTLING_RIGHTS_START,
            PREVIOUS_WCASTLING_RIGHTS_MASK,whiteCastlingRights);}
    public void setPreviousBCastlingRights(int blackCastlingRights){bitValue =  BitMath.setBitsValue(bitValue, PREVIOUS_BCASTLING_RIGHTS_START,
            PREVIOUS_BCASTLING_RIGHTS_MASK,blackCastlingRights);}



    public enum CastlingType {CASTLING_kING_SIDE,CASTLING_QUEEN_SIDE}

    public enum PromoteToPieceType{QUEEN,KNIGHT,ROOK,BISHOP}

    private static final long TYPE_MASK = 7;
    private static final long TYPE_START = 0;

    private static final long COLOR_MASK = 8;
    private static final long COLOR_START = 3;

    private static final long FROM_MASK = 1008;
    private static final long FROM_START = 4;

    private static final long TO_MASK = 64512;
    private static final long TO_START = 10;

    private static final long CASTLING_MASK = 65536;
    private static final long CASTLING_START = 16;

    private static final long CASTLING_TYPE_MASK = 131072;
    private static final long CASTLING_TYPE_START = 17;

    private static final long TAKE_MASK = 262144;
    private static final long TAKE_START = 18;

    private static final long TAKE_TYPE_MASK = 3670016;
    private static final long TAKE_TYPE_START = 19;

    private static final long PROMOTE_MASK = 4194304;
    private static final long PROMOTE_START = 22;

    private static final long PROMOTE_TYPE_MASK = 58720256;
    private static final long PROMOTE_TYPE_START = 23;

    private static final long ENPASSANT_MASK = 67108864;
    private static final long ENPASSANT_START = 26;

    private static final long PAWN_DOUBLE_PUSH_MASK = 67108864;
    private static final long PAWN_DOUBLE_PUSH_START = 27;

    private static final long PREVIOUS_FIFTY_MOVES_MASK = 4227858432L;
    private static final long PREVIOUS_FIFTY_MOVES_START = 28;

    private static final long PREVIOUS_BCASTLING_RIGHTS_MASK = 51539607552L;
    private static final long PREVIOUS_BCASTLING_RIGHTS_START = 34;

    private static final long PREVIOUS_WCASTLING_RIGHTS_MASK = 206158430208L;
    private static final long PREVIOUS_WCASTLING_RIGHTS_START = 36;




}
