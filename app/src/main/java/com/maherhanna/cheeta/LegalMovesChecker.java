package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Iterator;

public class LegalMovesChecker {

    public static LegalMoves getBlackLegalMoves(ChessBoard chessBoard) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> blackPositions = chessBoard.getBlackPositions();

        for (int i = 0; i < blackPositions.size(); i++) {
            int square = blackPositions.get(i);
            Piece piece = chessBoard.getPieceAt(square);

            ArrayList<Move> pieceLegalMoves = getPieceMoves(chessBoard, piece);
            removeMovesThatExposeKing(chessBoard, pieceLegalMoves, piece);
            legalMoves.addMovesFor(square, pieceLegalMoves);

        }
        checkCastling(chessBoard, legalMoves, Piece.Color.BLACK);

        return legalMoves;
    }

    public static LegalMoves getWhiteLegalMoves(ChessBoard chessBoard) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> whitePositions = chessBoard.getWhitePositions();
        for (int i = 0; i < whitePositions.size(); i++) {
            int position = whitePositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);
            ArrayList<Move> pieceLegalMoves = getPieceMoves(chessBoard, piece);
            removeMovesThatExposeKing(chessBoard, pieceLegalMoves, piece);

            legalMoves.addMovesFor(position, pieceLegalMoves);
        }
        checkCastling(chessBoard, legalMoves, Piece.Color.WHITE);
        return legalMoves;
    }

    public static LegalMoves getLegalMovesFor(ChessBoard chessBoard, Piece.Color color){
        if(color == Piece.Color.WHITE){
            return getWhiteLegalMoves(chessBoard);
        } else{
            return getBlackLegalMoves(chessBoard);
        }
    }


    private static void checkCastling(ChessBoard chessBoard, LegalMoves legalMoves,
                                      Piece.Color color) {

        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        int initialRookKingSidePosition = getInitialRookKingSide(chessBoard, color);
        int initialRookQueenSidePosition = getInitialRookQueenSide(chessBoard, color);
        int kingTarget = 0;
        if (canCastleKingSide(chessBoard, color, chessBoard.isKingInCheck(color))) {
            kingTarget = initialKingPosition + 2;
            Piece king = new Piece(Piece.Type.KING, color, initialKingPosition);
            Move move = new Move(king, initialKingPosition, kingTarget);
            move.setCastling(true, Move.CastlingType.CASTLING_kING_SIDE);

            legalMoves.addMoveFor(initialKingPosition, move);
        }
        if (canCastleQueenSide(chessBoard, color, chessBoard.isKingInCheck(color))) {
            kingTarget = initialKingPosition - 2;
            Piece king = new Piece(Piece.Type.KING, color, initialKingPosition);
            Move move = new Move(king, initialKingPosition, kingTarget);
            move.setCastling(true, Move.CastlingType.CASTLING_QUEEN_SIDE);
            legalMoves.addMoveFor(initialKingPosition, move);

        }

    }

    public static int getInitialRookKingSide(ChessBoard chessBoard, Piece.Color rookColor) {
        if (rookColor == Piece.Color.WHITE) {
            return 7;
        } else {
            return 63;
        }

    }

    public static int getInitialRookQueenSide(ChessBoard chessBoard, Piece.Color rookColor) {
        if (rookColor == Piece.Color.WHITE) {
            return 0;
        } else {
            return 56;
        }
    }

    public static int getInitialKingPosition(ChessBoard chessBoard, Piece.Color kingColor) {
        if (kingColor == Piece.Color.WHITE) {
            return 4;
        } else {
            return 60;
        }
    }


    public static boolean isSquareAttacked(ChessBoard chessBoard, int square, Piece.Color opponentColor) {
        Piece.Color color = opponentColor.getOpposite();

        // attacked by a pawn
        int opposingToRightPawnSquare = 0;
        int opposingToLeftPawnSquare = 0;
        if (color == Piece.Color.WHITE) {
            opposingToRightPawnSquare = ChessBoard.offset(square, 1, 1);
            opposingToLeftPawnSquare = ChessBoard.offset(square, -1, 1);

        } else {
            opposingToRightPawnSquare = ChessBoard.offset(square, -1, -1);
            opposingToLeftPawnSquare = ChessBoard.offset(square, 1, -1);

        }
        if (chessBoard.isPieceAt(opposingToLeftPawnSquare, Piece.Type.PAWN, opponentColor)) {
            return true;
        }
        if (chessBoard.isPieceAt(opposingToRightPawnSquare, Piece.Type.PAWN, opponentColor)) {
            return true;
        }
        //--------------------------


        int attackingSquare = ChessBoard.OUT_OF_BOARD;


        // attacked by a bishop or diagonal queen
        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)) {
                fileOffset++;
                rankOffset++;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.BISHOP ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }


        }


        //check the diagonal line along the upper left direction
        fileOffset = -1;
        rankOffset = 1;
        while (true) {
            attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)) {
                fileOffset--;
                rankOffset++;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.BISHOP ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the diagonal line along the lower right direction
        fileOffset = 1;
        rankOffset = -1;
        while (true) {
            attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)) {
                fileOffset++;
                rankOffset--;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.BISHOP ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the diagonal line along the lower left direction
        fileOffset = -1;
        rankOffset = -1;
        while (true) {
            attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)) {
                fileOffset--;
                rankOffset--;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.BISHOP ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }
        //--------------------------


        //attacked by a rook or queen
        //check the rank
        //to the right of the rook
        int offset = 1;
        while (true) {
            attackingSquare = ChessBoard.offsetFile(square, offset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)) {
                offset++;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.ROOK ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the rank to the left

        offset = -1;
        while (true) {
            attackingSquare = ChessBoard.offsetFile(square, offset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)) {
                offset--;

                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.ROOK ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the file
        //to the top of the rook
        offset = 1;
        while (true) {
            attackingSquare = ChessBoard.offsetRank(square, offset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)) {
                offset++;

                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.ROOK ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the file
        //to the bottom of the rook
        offset = -1;
        while (true) {
            attackingSquare = ChessBoard.offsetRank(square, offset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)) {
                offset--;

                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.ROOK ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }

        //------------------------------

        //attacked by king
        fileOffset = 0;
        rankOffset = 0;

        for (fileOffset = -1; fileOffset <= 1; fileOffset++) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset++) {
                attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
                if (attackingSquare == ChessBoard.OUT_OF_BOARD) continue;
                if (chessBoard.isSquareEmpty(attackingSquare)) continue;

                if (chessBoard.getPieceColor(attackingSquare) == opponentColor) {
                    if (chessBoard.getPieceType(attackingSquare) == Piece.Type.KING) {
                        return true;
                    } else {
                        continue;
                    }
                }

            }
        }
        //--------------------------

        //attacked by knight
        fileOffset = 0;
        rankOffset = 0;

        for (fileOffset = -2; fileOffset <= 2; fileOffset += 4) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset += 2) {
                attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
                if (attackingSquare == ChessBoard.OUT_OF_BOARD) continue;
                if (chessBoard.isSquareEmpty(attackingSquare)) continue;

                if (chessBoard.getPieceColor(attackingSquare) == opponentColor) {
                    if (chessBoard.getPieceType(attackingSquare) == Piece.Type.KNIGHT) {
                        return true;
                    } else {
                        continue;
                    }
                }

            }
        }

        for (rankOffset = -2; rankOffset <= 2; rankOffset += 4) {
            for (fileOffset = -1; fileOffset <= 1; fileOffset += 2) {
                attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
                if (attackingSquare == ChessBoard.OUT_OF_BOARD) continue;
                if (chessBoard.isSquareEmpty(attackingSquare)) continue;

                if (chessBoard.getPieceColor(attackingSquare) == opponentColor) {
                    if (chessBoard.getPieceType(attackingSquare) == Piece.Type.KNIGHT) {
                        return true;
                    } else {
                        continue;
                    }
                }
            }
        }
        //--------------------------


        return false;
    }


    private static void removeMovesThatExposeKing(ChessBoard chessBoard, ArrayList<Move> pieceLegalMoves, Piece piece) {

        Iterator<Move> itr = pieceLegalMoves.iterator();
        while (itr.hasNext()) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);

            Move move = itr.next();
            chessBoardAfterMove.movePiece(move);
            if (isSquareAttacked(chessBoardAfterMove,
                    chessBoardAfterMove.getKingPosition(piece.getColor()), piece.getColor().getOpposite())) {
                itr.remove();
            }
        }

    }


    public static ArrayList<Move> getPieceMoves(ChessBoard chessBoard, Piece piece) {
        ArrayList<Move> pieceLegalMoves = new ArrayList<Move>();

        switch (piece.getType()) {
            case PAWN:
                pieceLegalMoves = getPawnMoves(chessBoard, piece);
                break;
            case ROOK:
                pieceLegalMoves = getRookMoves(chessBoard, piece);
                break;
            case KNIGHT:
                pieceLegalMoves = getKnightMoves(chessBoard, piece);
                break;
            case BISHOP:
                pieceLegalMoves = getBishopMoves(chessBoard, piece);

                break;
            case QUEEN:
                pieceLegalMoves = getQueenMoves(chessBoard, piece);

                break;
            case KING:
                pieceLegalMoves = getKingMoves(chessBoard, piece);
                break;
        }
        return pieceLegalMoves;
    }


    private static boolean canCastleKingSide(ChessBoard chessBoard, Piece.Color color, boolean kingInCheck) {
        if (kingInCheck) {
            return false;
        }

        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        if (chessBoard.isSquareEmpty(initialKingPosition)) {
            return false;
        }

        if (chessBoard.moves.hasPieceMoved(initialKingPosition)) {
            return false;
        }


        int initialRookKingSidePosition = getInitialRookKingSide(chessBoard, color);

        if (chessBoard.isSquareEmpty(initialRookKingSidePosition)) {
            return false;
        }

        if (chessBoard.moves.hasPieceMoved(initialRookKingSidePosition)) {
            return false;
        }

        if (!chessBoard.isSquareEmpty(initialKingPosition + 1) ||
                !chessBoard.isSquareEmpty(initialKingPosition + 2)) {
            return false;
        }


        return !isSquareAttacked(chessBoard, initialKingPosition + 1, color.getOpposite()) &&
                !isSquareAttacked(chessBoard, initialKingPosition + 2, color.getOpposite());
    }

    private static boolean canCastleQueenSide(ChessBoard chessBoard, Piece.Color color, boolean kingInCheck) {
        if (kingInCheck) {
            return false;
        }
        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        if (chessBoard.isSquareEmpty(initialKingPosition)) {
            return false;
        }
        if (chessBoard.moves.hasPieceMoved(initialKingPosition)) {
            return false;
        }

        int initialRookQueenSidePosition = getInitialRookQueenSide(chessBoard, color);
        if (chessBoard.isSquareEmpty(initialRookQueenSidePosition)) {
            return false;
        }
        if (chessBoard.moves.hasPieceMoved(initialRookQueenSidePosition)) {
            return false;
        }


        if (!chessBoard.isSquareEmpty(initialKingPosition - 1) ||
                !chessBoard.isSquareEmpty(initialKingPosition - 2) ||
                !chessBoard.isSquareEmpty(initialKingPosition - 3)) {
            return false;
        }

        return !isSquareAttacked(chessBoard, initialKingPosition - 1, color.getOpposite()) &&
                !isSquareAttacked(chessBoard, initialKingPosition - 2, color.getOpposite());
    }

    private static ArrayList<Move> getKnightMoves(ChessBoard chessBoard, Piece piece) {
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
                    knightLegalMoves.add(new Move(piece, piece.getPosition(), targetSquare));
                } else {
                    //check take
                    Piece targetPiece = chessBoard.getPieceAt(targetSquare);
                    if (piece.getColor() != targetPiece.getColor()) {
                        knightLegalMoves.add(new Move(piece, piece.getPosition(), targetSquare, true,
                                targetPiece.getType()));
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
                    knightLegalMoves.add(new Move(piece, piece.getPosition(), targetSquare));
                } else {
                    //check take
                    Piece targetPiece = chessBoard.getPieceAt(targetSquare);
                    if (piece.getColor() != targetPiece.getColor()) {
                        knightLegalMoves.add(new Move(piece, piece.getPosition(), targetSquare, true,
                                targetPiece.getType()));
                    }
                }
            }
        }

        return knightLegalMoves;

    }

    private static ArrayList<Move> getKingMoves(ChessBoard chessBoard, Piece piece) {
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
                    kingLegalMoves.add(new Move(piece, piece.getPosition(), adjacentSquare));
                } else {
                    //check take
                    Piece targetPiece = chessBoard.getPieceAt(adjacentSquare);
                    if (piece.getColor() != targetPiece.getColor()) {
                        kingLegalMoves.add(new Move(piece, piece.getPosition(), adjacentSquare, true,
                                targetPiece.getType()));
                    }
                }
            }
        }


        return kingLegalMoves;
    }

    private static ArrayList<Move> getQueenMoves(ChessBoard chessBoard, Piece piece) {
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
                queenLegalMoves.add(new Move(piece, piece.getPosition(), upperRightSquare));
            } else {
                //check take
                Piece targetPiece = chessBoard.getPieceAt(upperRightSquare);
                if (piece.getColor() != targetPiece.getColor()) {
                    queenLegalMoves.add(new Move(piece, piece.getPosition(), upperRightSquare, true,
                            targetPiece.getType()));
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
                queenLegalMoves.add(new Move(piece, piece.getPosition(), upperLeftSquare));
            } else {
                //check take
                Piece targetPiece = chessBoard.getPieceAt(upperLeftSquare);
                if (piece.getColor() != targetPiece.getColor()) {
                    queenLegalMoves.add(new Move(piece, piece.getPosition(), upperLeftSquare, true,
                            targetPiece.getType()));
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
                queenLegalMoves.add(new Move(piece, piece.getPosition(), lowerRightSquare));
            } else {
                //check take
                Piece targetPiece = chessBoard.getPieceAt(lowerRightSquare);
                if (piece.getColor() != targetPiece.getColor()) {
                    queenLegalMoves.add(new Move(piece, piece.getPosition(), lowerRightSquare, true,
                            targetPiece.getType()));
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
                queenLegalMoves.add(new Move(piece, piece.getPosition(), lowerLeftSquare));
            } else {
                //check take
                Piece targetPiece = chessBoard.getPieceAt(lowerLeftSquare);
                if (piece.getColor() != targetPiece.getColor()) {
                    queenLegalMoves.add(new Move(piece, piece.getPosition(), lowerLeftSquare, true,
                            targetPiece.getType()));
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
                queenLegalMoves.add(new Move(piece, piece.getPosition(), toRightSquare));
            } else {
                //check take
                Piece targetPiece = chessBoard.getPieceAt(toRightSquare);
                if (piece.getColor() != targetPiece.getColor()) {
                    queenLegalMoves.add(new Move(piece, piece.getPosition(), toRightSquare, true,
                            targetPiece.getType()));
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
                queenLegalMoves.add(new Move(piece, piece.getPosition(), toLeftSquare));
            } else {
                //check take
                Piece targetPiece = chessBoard.getPieceAt(toLeftSquare);
                if (piece.getColor() != targetPiece.getColor()) {
                    queenLegalMoves.add(new Move(piece, piece.getPosition(), toLeftSquare, true,
                            targetPiece.getType()));
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
                queenLegalMoves.add(new Move(piece, piece.getPosition(), toTopSquare));
            } else {
                //check take
                Piece targetPiece = chessBoard.getPieceAt(toTopSquare);
                if (piece.getColor() != targetPiece.getColor()) {
                    queenLegalMoves.add(new Move(piece, piece.getPosition(), toTopSquare, true,
                            targetPiece.getType()));
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
                queenLegalMoves.add(new Move(piece, piece.getPosition(), toBottomSquare));
            } else {
                //check take
                Piece targetPiece = chessBoard.getPieceAt(toBottomSquare);
                if (piece.getColor() != targetPiece.getColor()) {
                    queenLegalMoves.add(new Move(piece, piece.getPosition(), toBottomSquare, true,
                            targetPiece.getType()));
                }

                break;

            }
            offset--;
        }
        //-----------------

        return queenLegalMoves;
    }


    private static ArrayList<Move> getPawnMoves(ChessBoard chessBoard, Piece piece) {
        ArrayList<Move> pawnLegalMoves = new ArrayList<Move>();

        //white pawn
        if (piece.getColor() == Piece.Color.WHITE) {

            //check if the upper square is empty
            int oneSquareUp = piece.offsetRank(1);
            if (chessBoard.isSquareEmpty(oneSquareUp)) {
                if (ChessBoard.GetRank(oneSquareUp) == ChessBoard.RANK_8) {
                    Move move = new Move(piece, piece.getPosition(), oneSquareUp);
                    move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
                    pawnLegalMoves.add(move);
                } else {
                    pawnLegalMoves.add(new Move(piece, piece.getPosition(), oneSquareUp));

                }
                //first move for the pawn can be tow squares up
                if (piece.getRank() == ChessBoard.RANK_2) {
                    int twoSquaresUp = piece.offsetRank(2);
                    if (chessBoard.isSquareEmpty(twoSquaresUp)) {
                        pawnLegalMoves.add(new Move(piece, piece.getPosition(), twoSquaresUp));
                    }
                }
            }

            //check for en passant
            if (piece.getRank() == ChessBoard.RANK_5) {
                if (chessBoard.moves.getToPlayNow() == piece.getColor()) {
                    Move lastMove = chessBoard.moves.getLastMove();

                    if (chessBoard.isPieceAt(piece.offsetFile(-1), Piece.Type.PAWN, Piece.Color.BLACK)) {
                        if (lastMove.getTo() == piece.offsetFile(-1) && lastMove.isPawnDoubleMove()) {
                            int enPassantTarget = piece.offset(-1, 1);
                            Move enPassant = new Move(piece, piece.getPosition(), enPassantTarget);
                            enPassant.setEnPasant(true);
                            pawnLegalMoves.add(enPassant);
                        }

                    }
                    if (chessBoard.isPieceAt(piece.offsetFile(1), Piece.Type.PAWN, Piece.Color.BLACK)) {
                        if (lastMove.getTo() == piece.offsetFile(1) && lastMove.isPawnDoubleMove()) {
                            int enPassantTarget = piece.offset(1, 1);
                            Move enPassant = new Move(piece, piece.getPosition(), enPassantTarget);
                            enPassant.setEnPasant(true);
                            pawnLegalMoves.add(enPassant);
                        }

                    }

                }


            }
            //----------------------------


            //check for takes
            int upperRightSquare = piece.offset(1, 1);
            if (upperRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperRightSquare)) {
                if (chessBoard.getPieceAt(upperRightSquare).getColor() != piece.getColor()) {
                    if (ChessBoard.GetRank(upperRightSquare) == ChessBoard.RANK_8) {
                        Move move = new Move(piece, piece.getPosition(), upperRightSquare);
                        move.setTakes(true, chessBoard.getPieceAt(upperRightSquare).getType());
                        move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
                        pawnLegalMoves.add(move);
                    } else {
                        pawnLegalMoves.add(new Move(piece, piece.getPosition(), upperRightSquare, true,
                                chessBoard.getPieceAt(upperRightSquare).getType()));

                    }

                }
            }
            int upperLeftSquare = piece.offset(-1, 1);
            if (upperLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperLeftSquare)) {
                if (chessBoard.getPieceAt(upperLeftSquare).getColor() != piece.getColor()) {
                    if (ChessBoard.GetRank(upperLeftSquare) == ChessBoard.RANK_8) {
                        Move move = new Move(piece, piece.getPosition(), upperLeftSquare);
                        move.setTakes(true, chessBoard.getPieceAt(upperLeftSquare).getType());
                        move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
                        pawnLegalMoves.add(move);
                    } else {
                        pawnLegalMoves.add(new Move(piece, piece.getPosition(), upperLeftSquare, true,
                                chessBoard.getPieceAt(upperLeftSquare).getType()));

                    }
                }
            }
        }
        //--------------------------


        //black pawn
        if (piece.getColor() == Piece.Color.BLACK) {

            //check if the lower square is empty
            int oneSquareDown = piece.offsetRank(-1);
            if (chessBoard.isSquareEmpty(oneSquareDown)) {
                if (ChessBoard.GetRank(oneSquareDown) == ChessBoard.RANK_1) {
                    Move move = new Move(piece, piece.getPosition(), oneSquareDown);
                    move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
                    pawnLegalMoves.add(move);
                } else {
                    pawnLegalMoves.add(new Move(piece, piece.getPosition(), oneSquareDown));

                }
                //first move for the pawn can be tow squares up
                if (piece.getRank() == ChessBoard.RANK_7) {
                    int twoSquaresDown = piece.offsetRank(-2);
                    if (chessBoard.isSquareEmpty(twoSquaresDown)) {
                        pawnLegalMoves.add(new Move(piece, piece.getPosition(), twoSquaresDown));
                    }
                }
            }


            //check for en passant
            if (piece.getRank() == ChessBoard.RANK_4) {
                if (chessBoard.moves.getToPlayNow() == piece.getColor()) {
                    Move lastMove = chessBoard.moves.getLastMove();
                    if (chessBoard.isPieceAt(piece.offsetFile(-1), Piece.Type.PAWN, Piece.Color.WHITE)) {
                        if (lastMove.getTo() == piece.offsetFile(-1) && lastMove.isPawnDoubleMove()) {
                            int enPassantTarget = piece.offset(-1, -1);
                            Move enPassant = new Move(piece, piece.getPosition(), enPassantTarget);
                            enPassant.setEnPasant(true);
                            pawnLegalMoves.add(enPassant);
                        }

                    }
                    if (chessBoard.isPieceAt(piece.offsetFile(1), Piece.Type.PAWN, Piece.Color.WHITE)) {
                        if (lastMove.getTo() == piece.offsetFile(1) && lastMove.isPawnDoubleMove()) {
                            int enPassantTarget = piece.offset(1, -1);
                            Move enPassant = new Move(piece, piece.getPosition(), enPassantTarget);
                            enPassant.setEnPasant(true);
                            pawnLegalMoves.add(enPassant);
                        }

                    }

                }


            }
            //----------------------------


            //check for takes
            int lowerRightSquare = piece.offset(1, -1);
            if (lowerRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerRightSquare)) {
                if (chessBoard.getPieceAt(lowerRightSquare).getColor() != piece.getColor()) {
                    if (ChessBoard.GetRank(lowerRightSquare) == ChessBoard.RANK_1) {
                        Move move = new Move(piece, piece.getPosition(), lowerRightSquare);
                        move.setTakes(true, chessBoard.getPieceAt(lowerRightSquare).getType());
                        move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
                        pawnLegalMoves.add(move);
                    } else {
                        pawnLegalMoves.add(new Move(piece, piece.getPosition(), lowerRightSquare, true,
                                chessBoard.getPieceAt(lowerRightSquare).getType()));

                    }

                }
            }
            int lowerLeftSquare = piece.offset(-1, -1);
            if (lowerLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerLeftSquare)) {
                if (chessBoard.getPieceAt(lowerLeftSquare).getColor() != piece.getColor()) {
                    if (ChessBoard.GetRank(lowerLeftSquare) == ChessBoard.RANK_1) {
                        Move move = new Move(piece, piece.getPosition(), lowerLeftSquare);
                        move.setTakes(true, chessBoard.getPieceAt(lowerLeftSquare).getType());
                        move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
                        pawnLegalMoves.add(move);
                    } else {
                        pawnLegalMoves.add(new Move(piece, piece.getPosition(), lowerLeftSquare, true,
                                chessBoard.getPieceAt(lowerLeftSquare).getType()));

                    }
                }
            }
        }


        return pawnLegalMoves;

    }

    private static ArrayList<Move> getRookMoves(ChessBoard chessBoard, Piece piece) {
        ArrayList<Move> rookLegalMoves = new ArrayList<Move>();
        int offset = 0;

        //check the rank
        //to the right of the rook
        offset = 1;
        while (true) {
            int toRightSquare = piece.offsetFile(offset);
            if (toRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toRightSquare)) {
                rookLegalMoves.add(new Move(piece, piece.getPosition(), toRightSquare));
            } else {
                //check take
                if (piece.getColor() != chessBoard.getPieceAt(toRightSquare).getColor()) {
                    rookLegalMoves.add(new Move(piece, piece.getPosition(), toRightSquare, true,
                            chessBoard.getPieceType(toRightSquare)));
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
                rookLegalMoves.add(new Move(piece, piece.getPosition(), toLeftSquare));
            } else {
                //check take
                if (piece.getColor() != chessBoard.getPieceAt(toLeftSquare).getColor()) {
                    rookLegalMoves.add(new Move(piece, piece.getPosition(), toLeftSquare, true,
                            chessBoard.getPieceType(toLeftSquare)));
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
                rookLegalMoves.add(new Move(piece, piece.getPosition(), toTopSquare));
            } else {
                //check take
                if (piece.getColor() != chessBoard.getPieceAt(toTopSquare).getColor()) {
                    rookLegalMoves.add(new Move(piece, piece.getPosition(), toTopSquare, true,
                            chessBoard.getPieceType(toTopSquare)));
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
                rookLegalMoves.add(new Move(piece, piece.getPosition(), toBottomSquare));
            } else {
                //check take
                if (piece.getColor() != chessBoard.getPieceAt(toBottomSquare).getColor()) {
                    rookLegalMoves.add(new Move(piece, piece.getPosition(), toBottomSquare, true,
                            chessBoard.getPieceType(toBottomSquare)));
                }

                break;

            }
            offset--;
        }
        //-----------------


        return rookLegalMoves;

    }

    private static ArrayList<Move> getBishopMoves(ChessBoard chessBoard, Piece piece) {
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
                bishopLegalMoves.add(new Move(piece, piece.getPosition(), upperRightSquare));
            } else {
                //check take
                if (piece.getColor() != chessBoard.getPieceAt(upperRightSquare).getColor()) {
                    bishopLegalMoves.add(new Move(piece, piece.getPosition(), upperRightSquare, true,
                            chessBoard.getPieceType(upperRightSquare)));
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
                bishopLegalMoves.add(new Move(piece, piece.getPosition(), upperLeftSquare));
            } else {
                //check take
                if (piece.getColor() != chessBoard.getPieceAt(upperLeftSquare).getColor()) {
                    bishopLegalMoves.add(new Move(piece, piece.getPosition(), upperLeftSquare, true,
                            chessBoard.getPieceType(upperLeftSquare)));
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
                bishopLegalMoves.add(new Move(piece, piece.getPosition(), lowerRightSquare));
            } else {
                //check take
                if (piece.getColor() != chessBoard.getPieceAt(lowerRightSquare).getColor()) {
                    bishopLegalMoves.add(new Move(piece, piece.getPosition(), lowerRightSquare, true,
                            chessBoard.getPieceType(lowerRightSquare)));
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
                bishopLegalMoves.add(new Move(piece, piece.getPosition(), lowerLeftSquare));
            } else {
                //check take
                if (piece.getColor() != chessBoard.getPieceAt(lowerLeftSquare).getColor()) {
                    bishopLegalMoves.add(new Move(piece, piece.getPosition(), lowerLeftSquare, true,
                            chessBoard.getPieceType(lowerLeftSquare)));
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
