package com.maherhanna.cheeta;

public class Move {
    private final Piece.Type pieceType;
    private final Piece.Color pieceColor;
    private final int from;
    private final int to;
    private boolean castling;
    private CastlingType castlingType;
    private boolean takes ;
    private Piece.Type takenPieceType;
    private boolean promotes;
    private PromoteToPieceType promotionPieceType;
    private boolean enPasant;


    public Move(Piece piece, int from, int to){
        this.pieceType = piece.getType();
        pieceColor = piece.getColor();
        this.from = from;
        this.to = to;
        castling = false;
        castlingType = CastlingType.CASTLING_kING_SIDE;
        takes = false;
        takenPieceType = Piece.Type.PAWN;
        promotes = false;
        promotionPieceType = PromoteToPieceType.QUEEN;
    }

    public Move(Piece piece, int from, int to,boolean takes, Piece.Type takenPieceType){
        this(piece,from,to);
        this.takes = true;
        this.takenPieceType = takenPieceType;
    }

    public Move(Move copy){
        pieceType = copy.pieceType;
        pieceColor = copy.pieceColor;
        from = copy.from;
        to = copy.to;
        castling = copy.castling;
        castlingType = copy.castlingType;
        takes = copy.takes;
        takenPieceType = copy.takenPieceType;
        promotes = copy.promotes;
        promotionPieceType = copy.promotionPieceType;
        enPasant = copy.enPasant;
    }


    public boolean isCastling(){
        return castling;
    }
    public CastlingType getCastlingType(){return this.castlingType;}

    public boolean isTake(){return takes;}
    public Piece.Type getTakenPieceType(){return this.takenPieceType;}

    public boolean isPromote() { return promotes;}
    public Piece.Type getPromotionPieceType(){
        Piece.Type type = Piece.Type.QUEEN;
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

    public boolean isEnPasant(){return enPasant;}

    public boolean isPawnDoubleMove(){
        boolean pawnDoubleMove = false;
        if(pieceType == Piece.Type.PAWN &&
                Math.abs(ChessBoard.GetRank(to) - ChessBoard.GetRank(from)) == 2){
            pawnDoubleMove = true;
        }
        return pawnDoubleMove;
    }

    public int getFrom(){return from;}
    public int getTo(){return to;}
    public Piece.Color getColor(){return pieceColor;}
    public Piece.Type getPieceType(){return pieceType;}

    public void setCastling(boolean castling) {
        this.castling = castling;
    }
    public void setCastlingType(CastlingType castlingType){
        this.castlingType = castlingType;
    }

    public void setCastling(boolean castling,CastlingType castlingType){
        this.castling = castling;
        this.castlingType = castlingType;
    }
    public void setTakes(boolean takes){
        this.takes = takes;
    }

    public void setTakenPieceType(Piece.Type pieceType){
        this.takenPieceType = pieceType;
    }

    public void setTakes(boolean takes, Piece.Type pieceType){
        this.takes = takes;
        this.takenPieceType = pieceType;
    }

    public void setPromotes(boolean promotes){
        this.promotes = promotes;
    }

    public void setPromotionPieceType(PromoteToPieceType promotionPieceType){
        this.promotionPieceType = promotionPieceType;
    }

    public void setPromotes(boolean promotes,PromoteToPieceType promotionPieceType){
        this.promotes = promotes;
        this.promotionPieceType = promotionPieceType;
    }

    public void setEnPasant(boolean enPasant){
        this.enPasant = enPasant;
        setTakes(true, Piece.Type.PAWN);
    }




    public enum CastlingType {CASTLING_kING_SIDE,CASTLING_QUEEN_SIDE}

    public enum PromoteToPieceType{QUEEN,KNIGHT,ROOK,BISHOP}
}
