package com.maherhanna.cheeta;

import java.util.ArrayList;

public class LegalMoves {
    private ChessBoard chessBoard;

    public LegalMoves(ChessBoard chessBoard){
        this.chessBoard = chessBoard;
    }

    public ArrayList<Integer> getLegalMoves(int position){
        ArrayList<Integer> pieceMoves = new ArrayList<Integer>();
        Piece piece = chessBoard.getPieceAt(position);

        switch (piece.type) {
            case PAWN:
                pieceMoves = getPawnMoves(position);
                break;
            case ROOK:
                break;
            case KNIGHT:
                break;
            case BISHOP:
                break;
            case QUEEN:
                break;
            case KING:
                break;
        }
        return pieceMoves;
    }

    private ArrayList<Integer> getPawnMoves(int position){
        ArrayList<Integer> pawnMoves = new ArrayList<Integer>();
        if(ChessBoard.GetRank(position) == ChessBoard.RANK_8) return pawnMoves;
        Piece pawn = chessBoard.getPieceAt(position);
        //first move for the pawn can be tow squares top
        if(ChessBoard.GetRank(position) == ChessBoard.RANK_2){
            //check if the top square is empty or contains opponent piece
            if(chessBoard.isSquareEmpty(position + 8) ||
                    pawn.color != chessBoard.getPieceAt(position + 8).color)
            {
                pawnMoves.add(position + 8);
                if(chessBoard.isSquareEmpty(position + 16) ||
                        pawn.color != chessBoard.getPieceAt(position + 16).color){
                    pawnMoves.add(position + 16);
                }

            }

        }
        else{
            int movePosition = position + 8;
            if(chessBoard.isSquareEmpty(movePosition)||
                    pawn.color != chessBoard.getPieceAt(position + 16).color){
                if(movePosition <= ChessBoard.MAX_POSITION){
                    pawnMoves.add(movePosition);
                }
            }


        }
        return pawnMoves;

    }

    private ArrayList<Integer> getRookMoves(Piece rook){
        ArrayList<Integer> rookMoves = new ArrayList<Integer>();

        return rookMoves;

    }


}
