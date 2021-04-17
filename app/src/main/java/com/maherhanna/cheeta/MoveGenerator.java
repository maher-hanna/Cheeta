package com.maherhanna.cheeta;

import java.util.ArrayList;

public class MoveGenerator {
    //move operations
    //----------------------------------------------------------------------------
    private static long notAFile = 0xfefefefefefefefeL;
    private static long notHFile = 0x7f7f7f7f7f7f7f7fL;
    private static long rank4 = 0xff000000L;
    private static long rank5 = 0xff00000000L;
    private static long rank8 = 0xff00000000000000L;
    private static long rank1 = 0xffL;


    private static long south (long square) {return  square >>> 8;}
    private static long north (long square) {return  square << 8;}
    private static long east (long square) {return (square & notHFile) << 1;}
    private static long west (long square) {return (square & notAFile) >>> 1;}
    private static long southWest (long square) {return (square & notAFile) >>> 9;}
    private static long southEast (long square) {return (square & notHFile) >>> 7;}
    private static long northWest (long square) {return (square & notAFile) << 7;}
    private static long northEast (long square) {return (square & notHFile) << 9;}

    //*****************************************************************************

    public MoveGenerator(){

    }

    //pawn moves
    //-------------------------------------------------------------------------------
    private static long whitePawnsSinglePush(long whitePawns,long empty){
        return north(whitePawns) & empty;
    }

    private static long whitePawnsDoublePush(long whitePawns,long empty){
        long singlePush = north(whitePawns) & empty;
        return north(singlePush) & rank4 & empty;
    }

    private static long whitePawnsAttackWest(long whitePawns,long blackPieces){
        return northWest(whitePawns) & blackPieces;
    }
    private static long whitePawnsAttackEast(long whitePawns,long blackPieces){
        return northEast(whitePawns) & blackPieces;
    }
    private ArrayList<Move> getWhitePawnsPushes(long whitePawns, long empty){
        ArrayList<Move> moves = new ArrayList<>();
        long singlePushes = whitePawnsSinglePush(whitePawns,empty);
        long doublePushes = whitePawnsDoublePush(whitePawns,empty);

        int count = BitMath.count1Bits(singlePushes);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(singlePushes);
            singlePushes = BitMath.popBit(singlePushes,index);
            Move move = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE,index - 8),
                    index - 8,index);
            if(ChessBoard.GetRank(index) == ChessBoard.RANK_8){
                move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            moves.add(move);
        }
        count = BitMath.count1Bits(doublePushes);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(doublePushes);
            doublePushes = BitMath.popBit(doublePushes,index);
            Move move = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE,index - 16),
                    index - 16,index);

            moves.add(move);
        }


        return moves;

    }

    private ArrayList<Move> getWhitePawnsAttacks(ChessBoard chessBoard) {
        ArrayList<Move> attacks = new ArrayList<>();
        long attacksWest = whitePawnsAttackWest(chessBoard.whitePawns,chessBoard.allBlackPieces);
        long attacksEast = whitePawnsAttackEast(chessBoard.whitePawns,chessBoard.allBlackPieces);

        int count = BitMath.count1Bits(attacksWest);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(attacksWest);
            attacksWest = BitMath.popBit(attacksWest,index);
            Move attack = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE,index - 7),
                    index - 7,index);

            attack.setTakes(true,chessBoard.getPieceType(index));
            if(ChessBoard.GetRank(index) == ChessBoard.RANK_8){
                attack.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            attacks.add(attack);

        }

        count = BitMath.count1Bits(attacksEast);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(attacksEast);
            attacksEast = BitMath.popBit(attacksEast,index);
            Move attack = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE,index - 9),
                    index - 9,index);
            attack.setTakes(true,chessBoard.getPieceType(index));
            if(ChessBoard.GetRank(index) == ChessBoard.RANK_8){
                attack.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            attacks.add(attack);

        }
        return  attacks;
    }

    private static long blackPawnsSinglePush(long blackPawns,long empty){
        return south(blackPawns) & empty;
    }

    private static long blackPawnsDoublePush(long blackPawns,long empty){
        long singlePush = south(blackPawns) & empty;
        return south(singlePush) & rank5 & empty;
    }
    private static long blackPawnsAttackWest(long blackPawns,long whitePieces){
        return southWest(blackPawns) & whitePieces;
    }
    private static long blackPawnsAttackEast(long blackPawns,long whitePieces){
        return southEast(blackPawns) & whitePieces;
    }
    private ArrayList<Move> getBlackPawnsPushes(long blackPawns, long empty, long whitePieces){
        ArrayList<Move> moves = new ArrayList<>();
        long singlePushes = blackPawnsSinglePush(blackPawns,empty);
        long doublePushes = blackPawnsDoublePush(blackPawns,empty);

        int count = BitMath.count1Bits(singlePushes);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(singlePushes);
            singlePushes = BitMath.popBit(singlePushes,index);
            Move move = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK,index + 8),
                    index + 8,index);
            if(ChessBoard.GetRank(index) == ChessBoard.RANK_1){
                move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            moves.add(move);
        }

        count = BitMath.count1Bits(doublePushes);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(doublePushes);
            Move move = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK,index + 16),
                    index + 16,index);
            doublePushes = BitMath.popBit(doublePushes,index);
            moves.add(move);
        }



        return moves;

    }

    private ArrayList<Move> getBlackPawnsAttacks(ChessBoard chessBoard) {
        ArrayList<Move> attacks = new ArrayList<>();
        long attacksWest = blackPawnsAttackWest(chessBoard.blackPawns,chessBoard.allWhitePieces);
        long attacksEast = blackPawnsAttackEast(chessBoard.blackPawns,chessBoard.allWhitePieces);


        int count = BitMath.count1Bits(attacksWest);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(attacksWest);
            attacksWest = BitMath.popBit(attacksWest,index);
            Move attack = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK,index + 9),
                    index + 9,index);
            attack.setTakes(true,chessBoard.getPieceType(index));

            if(ChessBoard.GetRank(index) == ChessBoard.RANK_1){
                attack.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            attacks.add(attack);

        }


        count = BitMath.count1Bits(attacksEast);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(attacksEast);
            attacksEast = BitMath.popBit(attacksEast,index);
            Move attack = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK,index + 7),
                    index + 7,index);
            attack.setTakes(true,chessBoard.getPieceType(index));
            if(ChessBoard.GetRank(index) == ChessBoard.RANK_1){
                attack.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            attacks.add(attack);

        }
        return  attacks;
    }


    //*******************************************************************************



    public ArrayList<Move> getWhitepseudoLegalMoves(ChessBoard chessBoard){
        ArrayList<Move> moves = new ArrayList<>();
        long attacksWest = whitePawnsAttackWest(chessBoard.whitePawns,chessBoard.allBlackPieces);
        moves.addAll(getWhitePawnsPushes(chessBoard.whitePawns,chessBoard.emptySquares));
        moves.addAll(getWhitePawnsAttacks(chessBoard));
        return moves;
    }



    public ArrayList<Move> getBlackpseudoLegalMoves(ChessBoard chessBoard){
        ArrayList<Move> moves = new ArrayList<>();
        moves.addAll(getBlackPawnsPushes(chessBoard.blackPawns,chessBoard.emptySquares,
                chessBoard.allWhitePieces));
        moves.addAll(getBlackPawnsAttacks(chessBoard));
        return moves;
    }
}
