package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Random;

public class ComputerAi {
    public Move getMove(ChessBoard chessBoard, Piece.Color color){
        Random random = new Random();
        ArrayList<Integer> piecesPositions = chessBoard.getPositionsOf(color);
        int numPieces = piecesPositions.size();
        int numOfLegalMoves = 0;
        int randomPieceIndex = 0;
        int randomPiecePosition = 0;
        do {
            randomPieceIndex = random.nextInt(numPieces);
            randomPiecePosition = piecesPositions.get(randomPieceIndex);
            numOfLegalMoves = chessBoard.getLegalMovesFor(randomPiecePosition).size();

        } while (numOfLegalMoves == 0);

        int randomLegalMove = random.nextInt(numOfLegalMoves);
        int randomMove = chessBoard.getLegalMovesFor(randomPiecePosition).get(randomLegalMove);
        return new Move(randomPiecePosition,randomMove);
    }
}
