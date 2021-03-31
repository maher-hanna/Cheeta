package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Iterator;

public class LegalMovesChecker {

    public static LegalMoves getBlackLegalMoves(ChessBoard chessBoard, boolean kingInCheck) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> blackPositions = chessBoard.getBlackPositions();


        for (int i = 0; i < blackPositions.size(); i++) {
            int position = blackPositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);

            ArrayList<Move> pieceLegalMoves = getPieceMoves(chessBoard, piece, kingInCheck);
            removeMovesThatExposeKing(chessBoard, pieceLegalMoves, piece);
            legalMoves.addMovesFor(position, pieceLegalMoves);

        }
        checkCastling(chessBoard,legalMoves, Piece.Color.BLACK,kingInCheck);

        return legalMoves;
    }

    public static LegalMoves getWhiteLegalMoves(ChessBoard chessBoard, boolean kingInCheck) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> whitePositions = chessBoard.getWhitePositions();
        for (int i = 0; i < whitePositions.size(); i++) {
            int position = whitePositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);
            ArrayList<Move> pieceLegalMoves = getPieceMoves(chessBoard, piece, kingInCheck);
            removeMovesThatExposeKing(chessBoard, pieceLegalMoves, piece);

            legalMoves.addMovesFor(position, pieceLegalMoves);
        }
        checkCastling(chessBoard,legalMoves, Piece.Color.WHITE,kingInCheck);
        return legalMoves;
    }


    private static void checkCastling(ChessBoard chessBoard, LegalMoves legalMoves,
                                      Piece.Color color, boolean kingInCheck) {

        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        int initialRookKingSidePosition = getInitialRookKingSide(chessBoard, color);
        int initialRookQueenSidePosition = getInitialRookQueenSide(chessBoard,color);
        int kingTarget = 0;
        if (canCastleKingSide(chessBoard, color, kingInCheck) == true) {
            if(chessBoard.bottomPlayerColor == Piece.Color.WHITE){
                kingTarget = initialKingPosition + 2;
            }
            else {
                kingTarget = initialKingPosition -2;
            }
            legalMoves.addMoveFor(initialKingPosition,new Move( initialKingPosition,
                    kingTarget, Move.Type.CASTLING_kING_SIDE));
        }
        if (canCastleQueenSide(chessBoard, color, kingInCheck) == true) {
            if(chessBoard.bottomPlayerColor == Piece.Color.BLACK){
                kingTarget = initialKingPosition + 2;
            }
            else {
                kingTarget = initialKingPosition - 2;
            }
            legalMoves.addMoveFor(initialKingPosition,new Move( initialKingPosition,
                    kingTarget, Move.Type.CASTLING_QUEEN_SIDE));

        }

    }

    public static int getInitialRookKingSide(ChessBoard chessBoard, Piece.Color rookColor) {
        int rookPosition = ChessBoard.OUT_OF_BOARD;
        if (rookColor == chessBoard.bottomPlayerColor) {
            if (rookColor == Piece.Color.WHITE) {
                rookPosition = 7;
            } else {
                rookPosition = 0;
            }
        }
        // for player at top of board
        else {
            if (rookColor == Piece.Color.WHITE) {
                rookPosition = 56;

            } else {
                rookPosition = 63;

            }

        }
        return rookPosition;

    }

    public static int getInitialRookQueenSide(ChessBoard chessBoard, Piece.Color rookColor) {
        int rookPosition = ChessBoard.OUT_OF_BOARD;
        if (rookColor == chessBoard.bottomPlayerColor) {
            if (rookColor == Piece.Color.WHITE) {
                rookPosition = 0;
            } else {
                rookPosition = 7;
            }
        }
        // for player at top of board
        else {
            if (rookColor == Piece.Color.WHITE) {
                rookPosition = 63;

            } else {
                rookPosition = 56;

            }

        }
        return rookPosition;
    }

    public static int getInitialKingPosition(ChessBoard chessBoard, Piece.Color kingColor) {
        int kingPosition = ChessBoard.OUT_OF_BOARD;
        if (kingColor == chessBoard.bottomPlayerColor) {
            if (kingColor == Piece.Color.WHITE) {
                kingPosition = 4;
            } else {
                kingPosition = 3;
            }
        }
        // for player at top of board
        else {
            if (kingColor == Piece.Color.WHITE) {
                kingPosition = 59;

            } else {
                kingPosition = 60;

            }

        }
        return kingPosition;
    }


    private static boolean isKingExposedToCheck(ChessBoard chessBoard, Piece.Color kingColor) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> opponentPositions;
        opponentPositions = chessBoard.getPositionsFor(kingColor.getOpposite());

        for (int i = 0; i < opponentPositions.size(); i++) {
            int position = opponentPositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);
            ArrayList<Move> pieceLegalMoves = getPieceMoves(chessBoard, piece, false);
            legalMoves.addMovesFor(position,
                    getPieceMoves(chessBoard, piece, false));
        }
        if (legalMoves.contains(chessBoard.getKingPosition(kingColor))) {
            return true;
        } else {
            return false;
        }
    }


    private static void removeMovesThatExposeKing(ChessBoard chessBoard, ArrayList<Move> pieceLegalMoves, Piece piece) {

        Iterator<Move> itr = pieceLegalMoves.iterator();
        while (itr.hasNext()) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            int position = itr.next().to;
            chessBoardAfterMove.movePiece(piece.getPosition(), position);
            if (isKingExposedToCheck(chessBoardAfterMove, piece.color)) {
                itr.remove();
            }
        }

    }


    public static ArrayList<Move> getPieceMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Move> pieceLegalMoves = new ArrayList<Move>();

        switch (piece.type) {
            case PAWN:
                pieceLegalMoves = getPawnMoves(chessBoard, piece, kingInCheck);
                break;
            case ROOK:
                pieceLegalMoves = getRookMoves(chessBoard, piece, kingInCheck);
                break;
            case KNIGHT:
                pieceLegalMoves = getKnightMoves(chessBoard, piece, kingInCheck);
                break;
            case BISHOP:
                pieceLegalMoves = getBishopMoves(chessBoard, piece, kingInCheck);

                break;
            case QUEEN:
                pieceLegalMoves = getQueenMoves(chessBoard, piece, kingInCheck);

                break;
            case KING:
                pieceLegalMoves = getKingMoves(chessBoard, piece, kingInCheck);
                break;
        }
        return pieceLegalMoves;
    }




    private static boolean canCastleKingSide(ChessBoard chessBoard, Piece.Color color, boolean kingInCheck) {
        if (kingInCheck) {
            return false;
        }

        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        if(chessBoard.isSquareEmpty(initialKingPosition)){
            return false;
        }

        if(chessBoard.moves.hasPieceMoved(initialKingPosition)){
            return false;
        }


        int initialRookKingSidePosition = getInitialRookKingSide(chessBoard, color);

        if(chessBoard.isSquareEmpty(initialRookKingSidePosition)){
            return false;
        }

        if (chessBoard.moves.hasPieceMoved(initialRookKingSidePosition)) {
            return false;
        }
        if(chessBoard.bottomPlayerColor == Piece.Color.WHITE){
            if(!chessBoard.isSquareEmpty(initialKingPosition + 1) ||
            !chessBoard.isSquareEmpty(initialKingPosition + 2)){
                return false;
            }

            LegalMoves opponentLegalMoves = chessBoard.getLegalMovesFor(color.getOpposite());

            if(opponentLegalMoves.contains(initialKingPosition + 1) ||
                    opponentLegalMoves.contains(initialKingPosition + 2)){
                return false;
            }
        }
        else {
            if(!chessBoard.isSquareEmpty(initialKingPosition - 1) ||
                    !chessBoard.isSquareEmpty(initialKingPosition - 2)){
                return false;
            }

            LegalMoves opponentLegalMoves = chessBoard.getLegalMovesFor(color.getOpposite());

            if(opponentLegalMoves.contains(initialKingPosition - 1) ||
                    opponentLegalMoves.contains(initialKingPosition - 2)){
                return false;
            }

        }



        return true;
    }

    private static boolean canCastleQueenSide(ChessBoard chessBoard, Piece.Color color, boolean kingInCheck) {
        if (kingInCheck) {
            return false;
        }
        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        if(chessBoard.isSquareEmpty(initialKingPosition)){
            return false;
        }
        if (chessBoard.moves.hasPieceMoved(initialKingPosition)) {
            return false;
        }

        int initialRookQueenSidePosition = getInitialRookQueenSide(chessBoard, color);
        if(chessBoard.isSquareEmpty(initialRookQueenSidePosition)){
            return false;
        }
        if (chessBoard.moves.hasPieceMoved( initialRookQueenSidePosition)) {
            return false;
        }

        if(chessBoard.bottomPlayerColor == Piece.Color.WHITE){
            if(!chessBoard.isSquareEmpty(initialKingPosition - 1) ||
                    !chessBoard.isSquareEmpty(initialKingPosition - 2) ||
                    !chessBoard.isSquareEmpty(initialKingPosition - 3)){
                return false;
            }

            LegalMoves opponentLegalMoves = chessBoard.getLegalMovesFor(color.getOpposite());

            if(opponentLegalMoves.contains(initialKingPosition - 1) ||
                    opponentLegalMoves.contains(initialKingPosition - 2)){
                return false;
            }
        }
        else {
            if(!chessBoard.isSquareEmpty(initialKingPosition + 1) ||
                    !chessBoard.isSquareEmpty(initialKingPosition + 2) ||
                    !chessBoard.isSquareEmpty(initialKingPosition + 3)){
                return false;
            }

            LegalMoves opponentLegalMoves = chessBoard.getLegalMovesFor(color.getOpposite());

            if(opponentLegalMoves.contains(initialKingPosition + 1) ||
                    opponentLegalMoves.contains(initialKingPosition + 2)){
                return false;
            }

        }

        return true;
    }

    private static ArrayList<Move> getKnightMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Move> knightLegalMoves = new ArrayList<Move>();

        int fileOffset = 0;
        int rankOffset = 0;

        for (fileOffset = -2; fileOffset <= 2; fileOffset += 4) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset += 2) {
                int targetSquare = piece.offset(fileOffset, rankOffset);
                if (targetSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(targetSquare)) {
                    knightLegalMoves.add(new Move(piece.position,targetSquare));
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(targetSquare).color) {
                        knightLegalMoves.add(new Move(piece.position,targetSquare,true));
                    }
                }
            }
        }

        for (rankOffset = -2; rankOffset <= 2; rankOffset += 4) {
            for (fileOffset = -1; fileOffset <= 1; fileOffset += 2) {
                int targetSquare = piece.offset(fileOffset, rankOffset);
                if (targetSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(targetSquare)) {
                    knightLegalMoves.add(new Move(piece.position,targetSquare));
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(targetSquare).color) {
                        knightLegalMoves.add(new Move(piece.position,targetSquare,true));
                    }
                }
            }
        }

        return knightLegalMoves;

    }

    private static ArrayList<Move> getKingMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Move> kingLegalMoves = new ArrayList<Move>();

        int fileOffset = 0;
        int rankOffset = 0;

        for (fileOffset = -1; fileOffset <= 1; fileOffset++) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset++) {
                int adjacentSquare = piece.offset(fileOffset, rankOffset);
                if (adjacentSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(adjacentSquare)) {
                    kingLegalMoves.add(new Move(piece.position,adjacentSquare));
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(adjacentSquare).color) {
                        kingLegalMoves.add(new Move(piece.position,adjacentSquare,true));
                    }
                }
            }
        }


        return kingLegalMoves;
    }

    private static ArrayList<Move> getQueenMoves(ChessBoard chessBoard, Piece piece,
                                                    boolean kingInCheck) {
        ArrayList<Move> queenLegalMoves = new ArrayList<Move>();

        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            int upperRightSquare = piece.offset(fileOffset, rankOffset);
            if (upperRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperRightSquare)) {
                queenLegalMoves.add(new Move(piece.position,upperRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperRightSquare).color) {
                    queenLegalMoves.add(new Move(piece.position,upperRightSquare,true));
                }
                break;

            }
            fileOffset++;
            rankOffset++;
        }
        //--------------------

        //check the diagonal line along the upper left direction
        fileOffset = -1;
        rankOffset = 1;
        while (true) {
            int upperLeftSquare = piece.offset(fileOffset, rankOffset);
            if (upperLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperLeftSquare)) {
                queenLegalMoves.add(new Move(piece.position,upperLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperLeftSquare).color) {
                    queenLegalMoves.add(new Move(piece.position,upperLeftSquare,true));
                }
                break;

            }
            fileOffset--;
            rankOffset++;
        }
        //--------------------


        //check the diagonal line along the lower right direction
        fileOffset = 1;
        rankOffset = -1;
        while (true) {
            int lowerRightSquare = piece.offset(fileOffset, rankOffset);
            if (lowerRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerRightSquare)) {
                queenLegalMoves.add(new Move(piece.position,lowerRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerRightSquare).color) {
                    queenLegalMoves.add(new Move(piece.position,lowerRightSquare,true));
                }
                break;

            }
            fileOffset++;
            rankOffset--;
        }
        //--------------------

        //check the diagonal line along the lower left direction
        fileOffset = -1;
        rankOffset = -1;
        while (true) {
            int lowerLeftSquare = piece.offset(fileOffset, rankOffset);
            if (lowerLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerLeftSquare)) {
                queenLegalMoves.add(new Move(piece.position,lowerLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerLeftSquare).color) {
                    queenLegalMoves.add(new Move(piece.position,lowerLeftSquare,true));
                }
                break;

            }
            fileOffset--;
            rankOffset--;
        }
        //--------------------


        //check the rank
        //to the right of the queen
        int offset = 1;
        while (true) {
            int toRightSquare = piece.offsetFile(offset);
            if (toRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toRightSquare)) {
                queenLegalMoves.add(new Move(piece.position,toRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toRightSquare).color) {
                    queenLegalMoves.add(new Move(piece.position,toRightSquare,true));
                }
                break;

            }
            offset++;
        }
        //to the left of the queen
        offset = -1;
        while (true) {
            int toLeftSquare = piece.offsetFile(offset);
            if (toLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toLeftSquare)) {
                queenLegalMoves.add(new Move(piece.position,toLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toLeftSquare).color) {
                    queenLegalMoves.add(new Move(piece.position,toLeftSquare,true));

                }
                break;

            }
            offset--;
        }
        //-----------------


        //check the file
        //to the top of the queen
        offset = 1;
        while (true) {
            int toTopSquare = piece.offsetRank(offset);
            if (toTopSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toTopSquare)) {
                queenLegalMoves.add(new Move(piece.position,toTopSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toTopSquare).color) {
                    queenLegalMoves.add(new Move(piece.position,toTopSquare,true));
                }
                break;

            }
            offset++;
        }
        //to the bottom of the queen
        offset = -1;
        while (true) {
            int toBottomSquare = piece.offsetRank(offset);
            if (toBottomSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toBottomSquare)) {
                queenLegalMoves.add(new Move(piece.position,toBottomSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toBottomSquare).color) {
                    queenLegalMoves.add(new Move(piece.position,toBottomSquare,true));
                }

                break;

            }
            offset--;
        }
        //-----------------

        return queenLegalMoves;
    }


    private static ArrayList<Move> getPawnMoves(ChessBoard chessBoard, Piece piece,
                                                   boolean kingInCheck) {
        ArrayList<Move> pawnLegalMoves = new ArrayList<Move>();

        //the player at the bottom of chess board
        if (piece.color == chessBoard.bottomPlayerColor) {
            if (piece.getRank() == ChessBoard.RANK_8) return pawnLegalMoves;

            //check if the upper square is empty
            int oneSquareUp = piece.offsetRank(1);
            if (chessBoard.isSquareEmpty(oneSquareUp)) {
                pawnLegalMoves.add(new Move(piece.position,oneSquareUp));
                //first move for the pawn can be tow squares up
                if (piece.getRank() == ChessBoard.RANK_2) {
                    int twoSquaresUp = piece.offsetRank(2);
                    if (chessBoard.isSquareEmpty(twoSquaresUp)) {
                        pawnLegalMoves.add(new Move(piece.position,twoSquaresUp));
                    }
                }
            }


            //check for takes
            int upperRightSquare = piece.offset(1, 1);
            if (upperRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperRightSquare)) {
                if (chessBoard.getPieceAt(upperRightSquare).color != piece.color) {
                    pawnLegalMoves.add(new Move(piece.position,upperRightSquare));
                }
            }
            int upperLeftSquare = piece.offset(-1, 1);
            if (upperLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperLeftSquare)) {
                if (chessBoard.getPieceAt(upperLeftSquare).color != piece.color) {
                    pawnLegalMoves.add(new Move(piece.position,upperLeftSquare));
                }
            }
        }
        //--------------------------


        //the player at the top of chess board
        if (piece.color == chessBoard.topPlayerColor) {
            if (piece.getRank() == ChessBoard.RANK_1) return pawnLegalMoves;

            //check if the lower square is empty
            int oneSquareDown = piece.offsetRank(-1);
            if (chessBoard.isSquareEmpty(oneSquareDown)) {
                pawnLegalMoves.add(new Move(piece.position,oneSquareDown));
                //first move for the pawn can be tow squares up
                if (piece.getRank() == ChessBoard.RANK_7) {
                    int twoSquaresDown = piece.offsetRank(-2);
                    if (chessBoard.isSquareEmpty(twoSquaresDown)) {
                        pawnLegalMoves.add(new Move(piece.position, twoSquaresDown));
                    }
                }
            }


            //check for takes
            int lowerRightSquare = piece.offset(1, -1);
            if (lowerRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerRightSquare)) {
                if (chessBoard.getPieceAt(lowerRightSquare).color != piece.color) {
                    pawnLegalMoves.add(new Move(piece.position, lowerRightSquare,true));
                }
            }
            int lowerLeftSquare = piece.offset(-1, -1);
            if (lowerLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerLeftSquare)) {
                if (chessBoard.getPieceAt(lowerLeftSquare).color != piece.color) {
                    pawnLegalMoves.add(new Move(piece.position, lowerLeftSquare,true));
                }
            }
        }


        return pawnLegalMoves;

    }

    private static ArrayList<Move> getRookMoves(ChessBoard chessBoard, Piece piece,
                                                   boolean kingInCheck) {
        ArrayList<Move> rookLegalMoves = new ArrayList<Move>();
        int offset = 0;

        //check the rank
        //to the right of the rook
        offset = 1;
        while (true) {
            int toRightSquare = piece.offsetFile(offset);
            if (toRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toRightSquare)) {
                rookLegalMoves.add(new Move(piece.position, toRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toRightSquare).color) {
                    rookLegalMoves.add(new Move(piece.position, toRightSquare,true));
                }
                break;

            }
            offset++;
        }
        //to the left of the rook
        offset = -1;
        while (true) {
            int toLeftSquare = piece.offsetFile(offset);
            if (toLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toLeftSquare)) {
                rookLegalMoves.add(new Move(piece.position, toLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toLeftSquare).color) {
                    rookLegalMoves.add(new Move(piece.position, toLeftSquare,true));

                }
                break;

            }
            offset--;
        }
        //-----------------


        //check the file
        //to the top of the rook
        offset = 1;
        while (true) {
            int toTopSquare = piece.offsetRank(offset);
            if (toTopSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toTopSquare)) {
                rookLegalMoves.add(new Move(piece.position, toTopSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toTopSquare).color) {
                    rookLegalMoves.add(new Move(piece.position, toTopSquare,true));
                }
                break;

            }
            offset++;
        }
        //to the bottom of the rook
        offset = -1;
        while (true) {
            int toBottomSquare = piece.offsetRank(offset);
            if (toBottomSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toBottomSquare)) {
                rookLegalMoves.add(new Move(piece.position, toBottomSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toBottomSquare).color) {
                    rookLegalMoves.add(new Move(piece.position, toBottomSquare,true));
                }

                break;

            }
            offset--;
        }
        //-----------------


        return rookLegalMoves;

    }

    private static ArrayList<Move> getBishopMoves(ChessBoard chessBoard, Piece piece,
                                                     boolean kingInCheck) {
        ArrayList<Move> bishopLegalMoves = new ArrayList<Move>();

        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            int upperRightSquare = piece.offset(fileOffset, rankOffset);
            if (upperRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperRightSquare)) {
                bishopLegalMoves.add(new Move(piece.position, upperRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperRightSquare).color) {
                    bishopLegalMoves.add(new Move(piece.position, upperRightSquare,true));
                }
                break;

            }
            fileOffset++;
            rankOffset++;
        }
        //--------------------

        //check the diagonal line along the upper left direction
        fileOffset = -1;
        rankOffset = 1;
        while (true) {
            int upperLeftSquare = piece.offset(fileOffset, rankOffset);
            if (upperLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperLeftSquare)) {
                bishopLegalMoves.add(new Move(piece.position, upperLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperLeftSquare).color) {
                    bishopLegalMoves.add(new Move(piece.position, upperLeftSquare,true));
                }
                break;

            }
            fileOffset--;
            rankOffset++;
        }
        //--------------------


        //check the diagonal line along the lower right direction
        fileOffset = 1;
        rankOffset = -1;
        while (true) {
            int lowerRightSquare = piece.offset(fileOffset, rankOffset);
            if (lowerRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerRightSquare)) {
                bishopLegalMoves.add(new Move(piece.position,lowerRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerRightSquare).color) {
                    bishopLegalMoves.add(new Move(piece.position,lowerRightSquare,true));
                }
                break;

            }
            fileOffset++;
            rankOffset--;
        }
        //--------------------

        //check the diagonal line along the lower left direction
        fileOffset = -1;
        rankOffset = -1;
        while (true) {
            int lowerLeftSquare = piece.offset(fileOffset, rankOffset);
            if (lowerLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerLeftSquare)) {
                bishopLegalMoves.add(new Move(piece.position,lowerLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerLeftSquare).color) {
                    bishopLegalMoves.add(new Move(piece.position,lowerLeftSquare,true));
                }
                break;

            }
            fileOffset--;
            rankOffset--;
        }
        //--------------------


        return bishopLegalMoves;


    }


}
