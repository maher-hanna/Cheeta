package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Random;

public class ComputerAi {
    public Move getMove(ArrayList<Piece> pieces){
        Random random = new Random();
        int numPieces = pieces.size();
        int numOfLegalMoves = 0;
        int randomPieceIndex = 0;
        int randomPiecePosition = 0;
        do {
            randomPieceIndex = random.nextInt(numPieces);
            randomPiecePosition = pieces.get(randomPieceIndex).getPosition();
            numOfLegalMoves = pieces.get(randomPieceIndex).legalMoves.size();

        } while (numOfLegalMoves == 0);

        int randomLegalMove = random.nextInt(numOfLegalMoves);
        int randomMove = pieces.get(randomPieceIndex).legalMoves.get(randomLegalMove);
        return new Move(randomPiecePosition,randomMove);
    }
}
