package com.maherhanna.cheeta;

import java.util.ArrayList;

public class MoveGenerator {
    //move operations
    //----------------------------------------------------------------------------
    private static long notAFile = 0xfefefefefefefefeL;
    private static long notHFile = 0x7f7f7f7f7f7f7f7fL;
    private static long rank4 = 0xff000000L;
    private static long rank5 = 0xff00000000L;


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
        long singlePush = north(whitePawns);
        return north(singlePush) & rank4 & empty;
    }

    private static long blackPawnsSinglePush(long blackPawns,long empty){
        return south(blackPawns) & empty;
    }

    private static long blackPawnsDoublePush(long blackPawns,long empty){
        long singlePush = south(blackPawns);
        return south(singlePush) & rank5 & empty;
    }

    //*******************************************************************************

    private ArrayList<Move> getWhitePawnsMoves(long whitePawns,long empty){
        ArrayList<Move> moves = new ArrayList<>();
        long singlePush = whitePawnsSinglePush(whitePawns,empty);
        int count = BitMath.count1Bits(singlePush);
        for(int i = 0 ; i < count;i++){
            int index = BitMath.getLSBitIndex(singlePush);
            singlePush = BitMath.popBit(singlePush,index);
            moves.add(new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE,index - 8),
                    index - 8,index));
        }
        return moves;

    }

    public ArrayList<Move> getWhitepseudoLegalMoves(ChessBoard chessBoard){
        ArrayList<Move> moves = new ArrayList<>();
        moves.addAll(getWhitePawnsMoves(chessBoard.whitePawns,chessBoard.emptySquares));
        return moves;
    }
}
