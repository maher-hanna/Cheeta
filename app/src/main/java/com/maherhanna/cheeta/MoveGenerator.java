package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Iterator;

public class MoveGenerator {
    //move operations
    //----------------------------------------------------------------------------
    private static long notAFile = 0xfefefefefefefefeL;
    private static long notHFile = 0x7f7f7f7f7f7f7f7fL;
    private static long notABFile = 0xfcfcfcfcfcfcfcfcL;
    private static long notGHFile = 0x3f3f3f3f3f3f3f3fL;
    private static long rank4 = 0xff000000L;
    private static long rank5 = 0xff00000000L;
    private static long rank8 = 0xff00000000000000L;
    private static long rank1 = 0xffL;


    private static long south(long square) {
        return square >>> 8;
    }

    private static long north(long square) {
        return square << 8;
    }

    private static long east(long square) {
        return (square & notHFile) << 1;
    }

    private static long west(long square) {
        return (square & notAFile) >>> 1;
    }

    private static long southWest(long square) {
        return (square & notAFile) >>> 9;
    }

    private static long southEast(long square) {
        return (square & notHFile) >>> 7;
    }

    private static long northWest(long square) {
        return (square & notAFile) << 7;
    }

    private static long northEast(long square) {
        return (square & notHFile) << 9;
    }

    //*****************************************************************************

    //calculated at start
    //-----------------------------------------------------------------------------
    //knight attacks
    long[] knightAttacksMask = new long[64];

    //rook and bishop tables

    //bishop rays constants
    private static final int NORTH_WEST = 0;
    private static final int NORTH_EAST = 1;
    private static final int SOUTH_WEST = 2;
    private static final int SOUTH_EAST = 3;

    //rook rays constants
    private static final int SOUTH = 0;
    private static final int NORTH = 1;
    private static final int EAST = 2;
    private static final int WEST = 3;

    long[][] rookAttacksMask = new long[4][64];
    long[][] bishopAttacksMask = new long[4][64];

    //*****************************************************************************

    public MoveGenerator() {
        //initialize knight attacks table
        long knight = 0;
        for (int square = ChessBoard.MIN_POSITION; square <= ChessBoard.MAX_POSITION; square++) {
            knight = 0;
            knight = BitMath.setBit(knight, square);
            long northNorthEast = (knight << 17) & notAFile;
            long northEastEast = (knight << 10) & notABFile;
            long southEastEast = (knight >>> 6) & notABFile;
            long southSouthEast = (knight >>> 15) & notAFile;
            long northNorthWest = (knight << 15) & notHFile;
            long northWestWest = (knight << 6) & notGHFile;
            long southWestWest = (knight >>> 10) & notGHFile;
            long southSouthWest = (knight >>> 17) & notHFile;
            long attacks = northNorthEast | northEastEast | southEastEast | southSouthEast |
                    northNorthWest | northWestWest | southWestWest | southSouthWest;
            knightAttacksMask[square] = attacks;


        }

        //initialize rook attacks table
        long rook = 0;
        for (int square = ChessBoard.MIN_POSITION; square <= ChessBoard.MAX_POSITION; square++) {
            rook = 0;
            rook = BitMath.setBit(knight, square);
            long westAttackMask = 0;
            long eastAttackMask = 0;
            long northAttackMask = 0;
            long southAttackMask = 0;

            //west ray
            int currentRank = ChessBoard.GetRank(square);
            for (int file = ChessBoard.GetFile(square) - 1; file >= ChessBoard.FILE_A; file--) {
                westAttackMask = BitMath.setBit(westAttackMask, ChessBoard.Square(file, currentRank));
            }
            rookAttacksMask[WEST][square] = westAttackMask;

            //east ray
            for (int file = ChessBoard.GetFile(square) + 1; file <= ChessBoard.FILE_H; file++) {
                eastAttackMask = BitMath.setBit(eastAttackMask, ChessBoard.Square(file, currentRank));
            }
            rookAttacksMask[EAST][square] = eastAttackMask;

            //north ray
            int currentFile = ChessBoard.GetFile(square);
            for (int rank = ChessBoard.GetRank(square) + 1; rank <= ChessBoard.RANK_8; rank++) {
                northAttackMask = BitMath.setBit(northAttackMask, ChessBoard.Square(currentFile, rank));
            }
            rookAttacksMask[NORTH][square] = northAttackMask;

            //south ray
            for (int rank = ChessBoard.GetRank(square) - 1; rank >= ChessBoard.RANK_1; rank--) {
                southAttackMask = BitMath.setBit(southAttackMask, ChessBoard.Square(currentFile, rank));
            }
            rookAttacksMask[SOUTH][square] = southAttackMask;

        }

        //initialize bishop attacks table
        long bishop = 0;
        for (int square = ChessBoard.MIN_POSITION; square <= ChessBoard.MAX_POSITION; square++) {
            bishop = 0;
            bishop = BitMath.setBit(bishop, square);
            long northWestAttackMask = 0;
            long northEastAttackMask = 0;
            long southWestAttackMask = 0;
            long southEastAttackMask = 0;

            //north west ray
            int rank = ChessBoard.GetRank(square) + 1;
            for (int file = ChessBoard.GetFile(square) - 1; file >= ChessBoard.FILE_A && rank <= ChessBoard.RANK_8; file--, rank++) {
                northWestAttackMask = BitMath.setBit(northWestAttackMask, ChessBoard.Square(file, rank));
            }
            bishopAttacksMask[NORTH_WEST][square] = northWestAttackMask;

            //north east ray
            rank = ChessBoard.GetRank(square) + 1;
            for (int file = ChessBoard.GetFile(square) + 1; file <= ChessBoard.FILE_H && rank <= ChessBoard.RANK_8; file++, rank++) {
                northEastAttackMask = BitMath.setBit(northEastAttackMask, ChessBoard.Square(file, rank));
            }
            bishopAttacksMask[NORTH_EAST][square] = northEastAttackMask;

            //south west ray
            rank = ChessBoard.GetRank(square) - 1;
            for (int file = ChessBoard.GetFile(square) - 1; file >= ChessBoard.FILE_A && rank >= ChessBoard.RANK_1; file--, rank--) {
                southWestAttackMask = BitMath.setBit(southWestAttackMask, ChessBoard.Square(file, rank));
            }
            bishopAttacksMask[SOUTH_WEST][square] = southWestAttackMask;

            //south east ray
            rank = ChessBoard.GetRank(square) - 1;
            for (int file = ChessBoard.GetFile(square) + 1; file <= ChessBoard.FILE_H && rank >= ChessBoard.RANK_1; file++, rank--) {
                southEastAttackMask = BitMath.setBit(southEastAttackMask, ChessBoard.Square(file, rank));
            }
            bishopAttacksMask[SOUTH_EAST][square] = southEastAttackMask;
        }
    }


    //pawn moves
    //-------------------------------------------------------------------------------
    private static long whitePawnsSinglePush(long whitePawns, long empty) {
        return north(whitePawns) & empty;
    }

    private static long whitePawnsDoublePush(long whitePawns, long empty) {
        long singlePush = north(whitePawns) & empty;
        return north(singlePush) & rank4 & empty;
    }

    private static long whitePawnsAttackWest(long whitePawns, long blackPieces) {
        return northWest(whitePawns) & blackPieces;
    }

    private static long whitePawnsAttackEast(long whitePawns, long blackPieces) {
        return northEast(whitePawns) & blackPieces;
    }

    public ArrayList<Move> getWhitePawnsPushes(long whitePawns, long empty) {
        ArrayList<Move> moves = new ArrayList<>();
        long singlePushes = whitePawnsSinglePush(whitePawns, empty);
        long doublePushes = whitePawnsDoublePush(whitePawns, empty);

        int count = BitMath.countSetBits(singlePushes);
        long singlePushesCopy = singlePushes;
        for (int i = 0; i < count; i++) {
            int index = BitMath.getLSBitIndex(singlePushesCopy);
            singlePushesCopy = BitMath.popBit(singlePushesCopy, index);
            Move move = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE, index - 8),
                    index - 8, index);
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_8) {
                move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            moves.add(move);
        }
        count = BitMath.countSetBits(doublePushes);
        long doublePushesCopy = doublePushes;
        for (int i = 0; i < count; i++) {
            int index = BitMath.getLSBitIndex(doublePushesCopy);
            doublePushesCopy = BitMath.popBit(doublePushesCopy, index);
            Move move = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE, index - 16),
                    index - 16, index);
            move.setPawnDoublePush(true);

            moves.add(move);
        }


        return moves;

    }

    public ArrayList<Move> getWhitePawnsCaptures(ChessBoard chessBoard) {
        ArrayList<Move> attacks = new ArrayList<>();
        long attacksWest = whitePawnsAttackWest(chessBoard.whitePawns, chessBoard.allBlackPieces);
        long attacksEast = whitePawnsAttackEast(chessBoard.whitePawns, chessBoard.allBlackPieces);

        int count = BitMath.countSetBits(attacksWest);
        long attacksWestCopy = attacksWest;

        for (int i = 0; i < count; i++) {
            int index = BitMath.getLSBitIndex(attacksWestCopy);
            attacksWestCopy = BitMath.popBit(attacksWestCopy, index);
            Move attack = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE, index - 7),
                    index - 7, index);

            attack.setTakes(true, chessBoard.getPieceType(index));
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_8) {
                attack.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            attacks.add(attack);

        }

        count = BitMath.countSetBits(attacksEast);
        long attacksEastCopy = attacksEast;
        for (int i = 0; i < count; i++) {
            int index = BitMath.getLSBitIndex(attacksEastCopy);
            attacksEastCopy = BitMath.popBit(attacksEastCopy, index);
            Move attack = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE, index - 9),
                    index - 9, index);
            attack.setTakes(true, chessBoard.getPieceType(index));
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_8) {
                attack.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            attacks.add(attack);

        }
        if (chessBoard.enPassantTarget != ChessBoard.NO_SQUARE &&
                ChessBoard.GetRank(chessBoard.enPassantTarget) == ChessBoard.RANK_6) {
            long enPassantWest = whitePawnsAttackWest(chessBoard.whitePawns, 1L << chessBoard.enPassantTarget);
            long enPassantEast = whitePawnsAttackEast(chessBoard.whitePawns, 1L << chessBoard.enPassantTarget);

            //en passant west
            if (enPassantWest != 0) {
                int index = chessBoard.enPassantTarget;
                Move enPassantWestMove = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE, index - 7),
                        index - 7, index);
                enPassantWestMove.setTakes(true, Piece.Type.PAWN);
                enPassantWestMove.setEnPasant(true);
                attacks.add(enPassantWestMove);

            }

            if (enPassantEast != 0) {
                //en passant east
                int index = chessBoard.enPassantTarget;
                Move enPassantEastMove = new Move(new Piece(Piece.Type.PAWN, Piece.Color.WHITE, index - 9),
                        index - 9, index);
                enPassantEastMove.setTakes(true, Piece.Type.PAWN);
                enPassantEastMove.setEnPasant(true);
                attacks.add(enPassantEastMove);

            }
        }
        return attacks;
    }

    private static long blackPawnsSinglePush(long blackPawns, long empty) {
        return south(blackPawns) & empty;
    }

    private static long blackPawnsDoublePush(long blackPawns, long empty) {
        long singlePush = south(blackPawns) & empty;
        return south(singlePush) & rank5 & empty;
    }

    private static long blackPawnsAttackWest(long blackPawns, long whitePieces) {
        return southWest(blackPawns) & whitePieces;
    }

    private static long blackPawnsAttackEast(long blackPawns, long whitePieces) {
        return southEast(blackPawns) & whitePieces;
    }

    public ArrayList<Move> getBlackPawnsPushes(long blackPawns, long empty) {
        ArrayList<Move> moves = new ArrayList<>();
        long singlePushes = blackPawnsSinglePush(blackPawns, empty);
        long doublePushes = blackPawnsDoublePush(blackPawns, empty);

        int count = BitMath.countSetBits(singlePushes);
        long singlePushesCopy = singlePushes;
        for (int i = 0; i < count; i++) {
            int index = BitMath.getLSBitIndex(singlePushesCopy);
            singlePushesCopy = BitMath.popBit(singlePushesCopy, index);
            Move move = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK, index + 8),
                    index + 8, index);
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_1) {
                move.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            moves.add(move);
        }

        count = BitMath.countSetBits(doublePushes);
        long doublePushesCopy = doublePushes;
        for (int i = 0; i < count; i++) {
            int index = BitMath.getLSBitIndex(doublePushesCopy);
            doublePushesCopy = BitMath.popBit(doublePushesCopy, index);

            Move move = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK, index + 16),
                    index + 16, index);
            move.setPawnDoublePush(true);
            moves.add(move);
        }


        return moves;

    }

    public ArrayList<Move> getBlackPawnsCaptures(ChessBoard chessBoard) {
        ArrayList<Move> attacks = new ArrayList<>();
        long attacksWest = blackPawnsAttackWest(chessBoard.blackPawns, chessBoard.allWhitePieces);
        long attacksEast = blackPawnsAttackEast(chessBoard.blackPawns, chessBoard.allWhitePieces);


        int count = BitMath.countSetBits(attacksWest);
        long attacksWestCopy = attacksWest;
        for (int i = 0; i < count; i++) {
            int index = BitMath.getLSBitIndex(attacksWestCopy);
            attacksWestCopy = BitMath.popBit(attacksWestCopy, index);
            Move attack = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK, index + 9),
                    index + 9, index);
            attack.setTakes(true, chessBoard.getPieceType(index));

            if (ChessBoard.GetRank(index) == ChessBoard.RANK_1) {
                attack.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            attacks.add(attack);

        }


        count = BitMath.countSetBits(attacksEast);
        long attacksEastCopy = attacksEast;
        for (int i = 0; i < count; i++) {
            int index = BitMath.getLSBitIndex(attacksEastCopy);
            attacksEastCopy = BitMath.popBit(attacksEastCopy, index);
            Move attack = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK, index + 7),
                    index + 7, index);
            attack.setTakes(true, chessBoard.getPieceType(index));
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_1) {
                attack.setPromotes(true, Move.PromoteToPieceType.QUEEN);
            }
            attacks.add(attack);

        }

        if (chessBoard.enPassantTarget != ChessBoard.NO_SQUARE &&
                ChessBoard.GetRank(chessBoard.enPassantTarget) == ChessBoard.RANK_3) {
            long enPassantWest = blackPawnsAttackWest(chessBoard.blackPawns, 1L << chessBoard.enPassantTarget);
            long enPassantEast = blackPawnsAttackEast(chessBoard.blackPawns, 1L << chessBoard.enPassantTarget);
            if (enPassantWest != 0) {

                //en passant west
                int index = chessBoard.enPassantTarget;
                Move enPassantWestMove = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK, index + 9),
                        index + 9, index);
                enPassantWestMove.setTakes(true, Piece.Type.PAWN);
                enPassantWestMove.setEnPasant(true);
                attacks.add(enPassantWestMove);
            }
            if (enPassantEast != 0) {

                //en passant east
                int index = chessBoard.enPassantTarget;
                Move enPassantEastMove = new Move(new Piece(Piece.Type.PAWN, Piece.Color.BLACK, index + 7),
                        index + 7, index);
                enPassantEastMove.setTakes(true, Piece.Type.PAWN);
                enPassantEastMove.setEnPasant(true);
                attacks.add(enPassantEastMove);
            }
        }

        return attacks;
    }


    //*******************************************************************************


    //king moves
    //-------------------------------------------------------------------------------
    private long kingMovesQuite(long king, long emptySquares) {
        long quietTargets = 0;
        quietTargets |= northWest(king) & emptySquares;
        quietTargets |= north(king) & emptySquares;
        quietTargets |= northEast(king) & emptySquares;
        quietTargets |= west(king) & emptySquares;
        quietTargets |= east(king) & emptySquares;
        quietTargets |= southWest(king) & emptySquares;
        quietTargets |= south(king) & emptySquares;
        quietTargets |= southEast(king) & emptySquares;
        return quietTargets;
    }

    private long kingMovesCapture(long king, long enemyPieces) {
        long captureTargets = 0;
        captureTargets |= northWest(king) & enemyPieces;
        captureTargets |= north(king) & enemyPieces;
        captureTargets |= northEast(king) & enemyPieces;
        captureTargets |= west(king) & enemyPieces;
        captureTargets |= east(king) & enemyPieces;
        captureTargets |= southWest(king) & enemyPieces;
        captureTargets |= south(king) & enemyPieces;
        captureTargets |= southEast(king) & enemyPieces;
        return captureTargets;
    }


    public ArrayList<Move> getKingMoves(ChessBoard chessBoard, int color) {
        long quietTargets = 0;
        long captureTargets = 0;
        int kingPosition = ChessBoard.OUT;
        ArrayList<Move> moves = new ArrayList<>();

        if (color == Piece.BLACK) {
            quietTargets = kingMovesQuite(chessBoard.blackKing, chessBoard.emptySquares);
            captureTargets = kingMovesCapture(chessBoard.blackKing, chessBoard.allWhitePieces);
            kingPosition = BitMath.getLSBitIndex(chessBoard.blackKing);
        } else {
            quietTargets = kingMovesQuite(chessBoard.whiteKing, chessBoard.emptySquares);
            captureTargets = kingMovesCapture(chessBoard.whiteKing, chessBoard.allBlackPieces);
            kingPosition = BitMath.getLSBitIndex(chessBoard.whiteKing);

        }

        int count = BitMath.countSetBits(quietTargets);
        for (int i = 0; i < count; i++) {
            int target = BitMath.getLSBitIndex(quietTargets);
            quietTargets = BitMath.popBit(quietTargets, target);
            Move move = new Move(new Piece(Piece.Type.KING, Piece.Color.values()[color],
                    kingPosition), kingPosition, target);

            moves.add(move);

        }


        count = BitMath.countSetBits(captureTargets);
        for (int i = 0; i < count; i++) {
            int captureTarget = BitMath.getLSBitIndex(captureTargets);
            captureTargets = BitMath.popBit(captureTargets, captureTarget);
            Move move = new Move(new Piece(Piece.Type.KING, Piece.Color.values()[color],
                    kingPosition), kingPosition, captureTarget);
            move.setTakes(true, chessBoard.getPieceType(captureTarget));

            moves.add(move);

        }
        return moves;

    }
    //*******************************************************************************

    //knight moves
    //-------------------------------------------------------------------------------
    private long knightMovesQuite(int knightIndex, long emptySquares) {

        return knightAttacksMask[knightIndex] & emptySquares;
    }

    private long knightMovesCapture(int knightIndex, long enemyPieces) {

        return knightAttacksMask[knightIndex] & enemyPieces;
    }


    public ArrayList<Move> getKnightMoves(ChessBoard chessBoard, int color) {
        long quietTargets = 0;
        long captureTargets = 0;
        long knights = 0;
        int currentKnightPosition = ChessBoard.OUT;

        ArrayList<Move> moves = new ArrayList<>();

        if (color == Piece.BLACK) {
            knights = chessBoard.blackKnights;
        } else {
            knights = chessBoard.whiteKnights;

        }

        int knightsCount = BitMath.countSetBits(knights);
        long knightsCopy = knights;
        for (int i = 0; i < knightsCount; i++) {
            currentKnightPosition = BitMath.getLSBitIndex(knightsCopy);
            knightsCopy = BitMath.popBit(knightsCopy, currentKnightPosition);
            quietTargets = knightMovesQuite(currentKnightPosition, chessBoard.emptySquares);
            if (color == Piece.BLACK) {
                captureTargets = knightMovesCapture(currentKnightPosition, chessBoard.allWhitePieces);
            } else {
                captureTargets = knightMovesCapture(currentKnightPosition, chessBoard.allBlackPieces);

            }
            int count = BitMath.countSetBits(quietTargets);
            long quietTargetsCopy = quietTargets;
            for (int index = 0; index < count; index++) {
                int target = BitMath.getLSBitIndex(quietTargetsCopy);
                quietTargetsCopy = BitMath.popBit(quietTargetsCopy, target);
                Move move = new Move(new Piece(Piece.Type.KNIGHT, Piece.Color.values()[color],
                        currentKnightPosition), currentKnightPosition, target);

                moves.add(move);

            }
            count = BitMath.countSetBits(captureTargets);
            long captureTargetsCopy = captureTargets;
            for (int index = 0; index < count; index++) {
                int captureTarget = BitMath.getLSBitIndex(captureTargetsCopy);
                captureTargetsCopy = BitMath.popBit(captureTargetsCopy, captureTarget);
                Move move = new Move(new Piece(Piece.Type.KNIGHT, Piece.Color.values()[color],
                        currentKnightPosition), currentKnightPosition, captureTarget);
                move.setTakes(true, chessBoard.getPieceType(captureTarget));

                moves.add(move);

            }

        }


        return moves;

    }
    //*******************************************************************************

    //rook moves
    //-------------------------------------------------------------------------------
    private long rookAttacks(int rayDirection, int square, long occupied) {
        long attacks = rookAttacksMask[rayDirection][square];
        long blocker = rookAttacksMask[rayDirection][square] & occupied;

        if (blocker != 0) {
            int blockerIndex = ChessBoard.OUT;
            if (rayDirection == EAST || rayDirection == NORTH) {
                blockerIndex = BitMath.getLSBitIndex(blocker);
            } else {
                blockerIndex = BitMath.getMSBitIndex(blocker);

            }
            attacks ^= rookAttacksMask[rayDirection][blockerIndex];
        }
        return attacks;
    }


    public ArrayList<Move> getRooksMoves(ChessBoard chessBoard, long rooks, int color, int pieceType) {
        ArrayList<Move> moves = new ArrayList<>();
        long quietTargets = 0;
        long captureTarget = 0;
        long northTargets = 0;
        long southTargets = 0;
        long eastTargets = 0;
        long westTargets = 0;

        //use pieceType to get queen moves
        Piece.Type type = Piece.Type.values()[pieceType - 1];


        int rookPosition = ChessBoard.OUT;

        int rooksCount = BitMath.countSetBits(rooks);


        for (int rook = 0; rook < rooksCount; rook++) {
            rookPosition = BitMath.getLSBitIndex(rooks);
            rooks = BitMath.popBit(rooks, rookPosition);

            northTargets = rookAttacks(NORTH, rookPosition, chessBoard.allPieces);
            southTargets = rookAttacks(SOUTH, rookPosition, chessBoard.allPieces);
            eastTargets = rookAttacks(EAST, rookPosition, chessBoard.allPieces);
            westTargets = rookAttacks(WEST, rookPosition, chessBoard.allPieces);


            Move move;
            int captureTargetIndex = ChessBoard.OUT;

            //north moves
            quietTargets = 0;
            captureTarget = 0;
            captureTargetIndex = ChessBoard.OUT;
            if (color == Piece.BLACK) {
                captureTarget = northTargets & chessBoard.allWhitePieces;

            } else {
                captureTarget = northTargets & chessBoard.allBlackPieces;

            }
            quietTargets = northTargets & chessBoard.emptySquares;


            if (captureTarget != 0) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        rookPosition), rookPosition, captureTargetIndex);
                move.setTakes(true, chessBoard.getPieceType(captureTargetIndex));
                moves.add(move);
            }

            int quietTargetsCount = BitMath.countSetBits(quietTargets);
            for (int i = 0; i < quietTargetsCount; i++) {
                int target = BitMath.getLSBitIndex(quietTargets);
                quietTargets = BitMath.popBit(quietTargets, target);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        rookPosition), rookPosition, target);

                moves.add(move);
            }


            //south moves
            quietTargets = 0;
            captureTarget = 0;
            captureTargetIndex = ChessBoard.OUT;
            if (color == Piece.BLACK) {
                captureTarget = southTargets & chessBoard.allWhitePieces;

            } else {
                captureTarget = southTargets & chessBoard.allBlackPieces;

            }
            quietTargets = southTargets & chessBoard.emptySquares;

            if (captureTarget != 0) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        rookPosition), rookPosition, captureTargetIndex);
                move.setTakes(true, chessBoard.getPieceType(captureTargetIndex));
                moves.add(move);
            }

            quietTargetsCount = BitMath.countSetBits(quietTargets);
            for (int i = 0; i < quietTargetsCount; i++) {
                int target = BitMath.getLSBitIndex(quietTargets);
                quietTargets = BitMath.popBit(quietTargets, target);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        rookPosition), rookPosition, target);

                moves.add(move);
            }

            //west moves
            quietTargets = 0;
            captureTarget = 0;
            captureTargetIndex = ChessBoard.OUT;
            if (color == Piece.BLACK) {
                captureTarget = westTargets & chessBoard.allWhitePieces;

            } else {
                captureTarget = westTargets & chessBoard.allBlackPieces;

            }
            quietTargets = westTargets & chessBoard.emptySquares;

            if (captureTarget != 0) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        rookPosition), rookPosition, captureTargetIndex);
                move.setTakes(true, chessBoard.getPieceType(captureTargetIndex));
                moves.add(move);
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets);
            for (int i = 0; i < quietTargetsCount; i++) {
                int target = BitMath.getLSBitIndex(quietTargets);
                quietTargets = BitMath.popBit(quietTargets, target);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        rookPosition), rookPosition, target);

                moves.add(move);
            }

            //east moves
            quietTargets = 0;
            captureTarget = 0;
            captureTargetIndex = ChessBoard.OUT;
            if (color == Piece.BLACK) {
                captureTarget = eastTargets & chessBoard.allWhitePieces;

            } else {
                captureTarget = eastTargets & chessBoard.allBlackPieces;

            }
            quietTargets = eastTargets & chessBoard.emptySquares;

            if (captureTarget != 0) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        rookPosition), rookPosition, captureTargetIndex);
                move.setTakes(true, chessBoard.getPieceType(captureTargetIndex));
                moves.add(move);
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets);
            for (int i = 0; i < quietTargetsCount; i++) {
                int target = BitMath.getLSBitIndex(quietTargets);
                quietTargets = BitMath.popBit(quietTargets, target);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        rookPosition), rookPosition, target);

                moves.add(move);
            }
        }


        return moves;

    }
    //*******************************************************************************


    //-------------------------------------------------------------------------------
    //bishop moves

    private long bishopAttacks(int rayDirection, int square, long occupied) {
        long attacks = bishopAttacksMask[rayDirection][square];
        long blocker = bishopAttacksMask[rayDirection][square] & occupied;

        if (blocker != 0) {
            int blockerIndex = ChessBoard.OUT;
            if (rayDirection == NORTH_EAST || rayDirection == NORTH_WEST) {
                blockerIndex = BitMath.getLSBitIndex(blocker);
            } else {
                blockerIndex = BitMath.getMSBitIndex(blocker);

            }
            attacks ^= bishopAttacksMask[rayDirection][blockerIndex];
        }
        return attacks;
    }


    public ArrayList<Move> getBishopsMoves(ChessBoard chessBoard, long bishops, int color, int pieceType) {
        ArrayList<Move> moves = new ArrayList<>();
        long quietTargets = 0;
        long captureTarget = 0;
        long northWestTargets = 0;
        long southWestTargets = 0;
        long northEastTargets = 0;
        long southEastTargets = 0;

        //use pieceType to get queen moves
        Piece.Type type = Piece.Type.values()[pieceType - 1];


        int bishopPosition = ChessBoard.OUT;

        int bishopsCount = BitMath.countSetBits(bishops);


        for (int bishop = 0; bishop < bishopsCount; bishop++) {
            bishopPosition = BitMath.getLSBitIndex(bishops);
            bishops = BitMath.popBit(bishops, bishopPosition);

            northWestTargets = bishopAttacks(NORTH_WEST, bishopPosition, chessBoard.allPieces);
            southWestTargets = bishopAttacks(SOUTH_WEST, bishopPosition, chessBoard.allPieces);
            northEastTargets = bishopAttacks(NORTH_EAST, bishopPosition, chessBoard.allPieces);
            southEastTargets = bishopAttacks(SOUTH_EAST, bishopPosition, chessBoard.allPieces);


            Move move;
            int captureTargetIndex = ChessBoard.OUT;

            //north west moves
            quietTargets = 0;
            captureTarget = 0;
            captureTargetIndex = ChessBoard.OUT;
            if (color == Piece.BLACK) {
                captureTarget = northWestTargets & chessBoard.allWhitePieces;

            } else {
                captureTarget = northWestTargets & chessBoard.allBlackPieces;

            }
            quietTargets = northWestTargets & chessBoard.emptySquares;


            if (captureTarget != 0) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        bishopPosition), bishopPosition, captureTargetIndex);
                move.setTakes(true, chessBoard.getPieceType(captureTargetIndex));
                moves.add(move);
            }

            int quietTargetsCount = BitMath.countSetBits(quietTargets);
            for (int i = 0; i < quietTargetsCount; i++) {
                int target = BitMath.getLSBitIndex(quietTargets);
                quietTargets = BitMath.popBit(quietTargets, target);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        bishopPosition), bishopPosition, target);

                moves.add(move);
            }


            //south west moves
            quietTargets = 0;
            captureTarget = 0;
            captureTargetIndex = ChessBoard.OUT;
            if (color == Piece.BLACK) {
                captureTarget = southWestTargets & chessBoard.allWhitePieces;

            } else {
                captureTarget = southWestTargets & chessBoard.allBlackPieces;

            }
            quietTargets = southWestTargets & chessBoard.emptySquares;

            if (captureTarget != 0) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        bishopPosition), bishopPosition, captureTargetIndex);
                move.setTakes(true, chessBoard.getPieceType(captureTargetIndex));
                moves.add(move);
            }

            quietTargetsCount = BitMath.countSetBits(quietTargets);
            for (int i = 0; i < quietTargetsCount; i++) {
                int target = BitMath.getLSBitIndex(quietTargets);
                quietTargets = BitMath.popBit(quietTargets, target);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        bishopPosition), bishopPosition, target);

                moves.add(move);
            }

            //south east moves
            quietTargets = 0;
            captureTarget = 0;
            captureTargetIndex = ChessBoard.OUT;
            if (color == Piece.BLACK) {
                captureTarget = southEastTargets & chessBoard.allWhitePieces;

            } else {
                captureTarget = southEastTargets & chessBoard.allBlackPieces;

            }
            quietTargets = southEastTargets & chessBoard.emptySquares;

            if (captureTarget != 0) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        bishopPosition), bishopPosition, captureTargetIndex);
                move.setTakes(true, chessBoard.getPieceType(captureTargetIndex));
                moves.add(move);
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets);
            for (int i = 0; i < quietTargetsCount; i++) {
                int target = BitMath.getLSBitIndex(quietTargets);
                quietTargets = BitMath.popBit(quietTargets, target);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        bishopPosition), bishopPosition, target);

                moves.add(move);
            }

            //north east moves
            quietTargets = 0;
            captureTarget = 0;
            captureTargetIndex = ChessBoard.OUT;
            if (color == Piece.BLACK) {
                captureTarget = northEastTargets & chessBoard.allWhitePieces;

            } else {
                captureTarget = northEastTargets & chessBoard.allBlackPieces;

            }
            quietTargets = northEastTargets & chessBoard.emptySquares;

            if (captureTarget != 0) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        bishopPosition), bishopPosition, captureTargetIndex);
                move.setTakes(true, chessBoard.getPieceType(captureTargetIndex));
                moves.add(move);
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets);
            for (int i = 0; i < quietTargetsCount; i++) {
                int target = BitMath.getLSBitIndex(quietTargets);
                quietTargets = BitMath.popBit(quietTargets, target);
                move = new Move(new Piece(type, Piece.Color.values()[color],
                        bishopPosition), bishopPosition, target);

                moves.add(move);
            }
        }


        return moves;


    }
    //*******************************************************************************


    //-------------------------------------------------------------------------------
    //queen moves

    public ArrayList<Move> getQueensMoves(ChessBoard chessBoard, long queens, int color, int pieceType) {
        ArrayList<Move> moves = new ArrayList<>();
        moves.addAll(getBishopsMoves(chessBoard, queens, color, pieceType));
        moves.addAll(getRooksMoves(chessBoard, queens, color, pieceType));
        return moves;
    }
    //*******************************************************************************

    public long getAllAttackedSquaresFor(ChessBoard chessBoard, Piece.Color color){
        long attackedSquares = 0;

        long pawns = 0;
        long rooks = 0;
        long bishops = 0;
        long knights = 0;
        long queens = 0;
        long king =  0;

        if(color == Piece.Color.WHITE){
            pawns = chessBoard.whitePawns;
            rooks = chessBoard.whiteRooks;
            bishops = chessBoard.whiteBishops;
            knights = chessBoard.whiteKnights;
            queens = chessBoard.whiteQueens;
            king = chessBoard.whiteKing;

            attackedSquares |= (northWest(pawns) | northEast(pawns)) & chessBoard.emptySquares;
        } else {
            pawns = chessBoard.blackPawns;
            rooks = chessBoard.blackRooks;
            bishops = chessBoard.blackBishops;
            knights = chessBoard.blackKnights;
            queens = chessBoard.blackQueens;
            king = chessBoard.blackKing;

            attackedSquares |= (southWest(pawns) | southEast(pawns)) & chessBoard.emptySquares;

        }

        //check rooks and queens
        long rooksAndQueens = rooks | queens;
        int rooksAndQueensCount = BitMath.countSetBits(rooksAndQueens);
        int rookPosition = 0;
        for (int rook = 0; rook < rooksAndQueensCount; rook++) {
            rookPosition = BitMath.getLSBitIndex(rooksAndQueens);
            rooksAndQueens = BitMath.popBit(rooksAndQueens, rookPosition);

            long northTargets = rookAttacks(NORTH, rookPosition, chessBoard.allPieces) & chessBoard.emptySquares;
            long southTargets = rookAttacks(SOUTH, rookPosition, chessBoard.allPieces)& chessBoard.emptySquares;;
            long eastTargets = rookAttacks(EAST, rookPosition, chessBoard.allPieces)& chessBoard.emptySquares;;
            long westTargets = rookAttacks(WEST, rookPosition, chessBoard.allPieces)& chessBoard.emptySquares;;
            attackedSquares |= northTargets | southTargets | eastTargets | westTargets;
        }

        //check bishops and queens
        long bishopsAndQueens = bishops | queens;
        int bishopsAndQueensCount = BitMath.countSetBits(bishopsAndQueens);
        int bishopPosition = 0;
        for (int bishop = 0; bishop < bishopsAndQueensCount; bishop++) {
            bishopPosition = BitMath.getLSBitIndex(bishopsAndQueens);
            bishopsAndQueens = BitMath.popBit(bishopsAndQueens, bishopPosition);

            long northWestTargets = bishopAttacks(NORTH_WEST, bishopPosition, chessBoard.allPieces) & chessBoard.emptySquares;
            long southWestTargets = bishopAttacks(SOUTH_WEST, bishopPosition, chessBoard.allPieces)& chessBoard.emptySquares;;
            long northEastTargets = bishopAttacks(NORTH_EAST, bishopPosition, chessBoard.allPieces)& chessBoard.emptySquares;;
            long southEastTargets = bishopAttacks(SOUTH_EAST, bishopPosition, chessBoard.allPieces)& chessBoard.emptySquares;;
            attackedSquares |= northWestTargets | southWestTargets | northEastTargets | southEastTargets;
        }

        //check knights
        int knightsCount = BitMath.countSetBits(knights);
        int knightPosition = 0;
        for (int knight = 0; knight < knightsCount; knight++) {
            knightPosition = BitMath.getLSBitIndex(knights);
            knights = BitMath.popBit(knights, knightPosition);

            attackedSquares |= knightAttacksMask[knightPosition] & chessBoard.emptySquares;

        }

        //check king
        attackedSquares |= northWest(king) & chessBoard.emptySquares;
        attackedSquares |= north(king) & chessBoard.emptySquares;
        attackedSquares |= northEast(king) & chessBoard.emptySquares;
        attackedSquares |= west(king) & chessBoard.emptySquares;
        attackedSquares |= east(king) & chessBoard.emptySquares;
        attackedSquares |= southWest(king) & chessBoard.emptySquares;
        attackedSquares |= south(king) & chessBoard.emptySquares;
        attackedSquares |= southEast(king) & chessBoard.emptySquares;


        return attackedSquares;
    }

    public ArrayList<Move> getWhitePseudoLegalMoves(ChessBoard chessBoard) {
        ArrayList<Move> moves = new ArrayList<>();

        //add pawns moves
        moves.addAll(getWhitePawnsPushes(chessBoard.whitePawns, chessBoard.emptySquares));
        moves.addAll(getWhitePawnsCaptures(chessBoard));

        //add king moves
        moves.addAll(getKingMoves(chessBoard, Piece.WHITE));

        //add knights moves
        moves.addAll(getKnightMoves(chessBoard, Piece.WHITE));

        //add rooks moves
        moves.addAll(getRooksMoves(chessBoard, chessBoard.whiteRooks, Piece.WHITE, Piece.ROOK));

        //add bishops moves
        moves.addAll(getBishopsMoves(chessBoard, chessBoard.whiteBishops, Piece.WHITE, Piece.BISHOP));

        //add queens moves
        moves.addAll(getQueensMoves(chessBoard, chessBoard.whiteQueens, Piece.WHITE, Piece.QUEEN));

        return moves;
    }


    public ArrayList<Move> getBlackPseudoLegalMoves(ChessBoard chessBoard) {
        ArrayList<Move> moves = new ArrayList<>();

        //add pawns moves
        moves.addAll(getBlackPawnsPushes(chessBoard.blackPawns, chessBoard.emptySquares));
        moves.addAll(getBlackPawnsCaptures(chessBoard));

        //add king moves
        moves.addAll(getKingMoves(chessBoard, Piece.BLACK));

        //add knights moves
        moves.addAll(getKnightMoves(chessBoard, Piece.BLACK));

        //add rooks moves
        moves.addAll(getRooksMoves(chessBoard, chessBoard.blackRooks, Piece.BLACK, Piece.ROOK));

        //add bishops moves
        moves.addAll(getBishopsMoves(chessBoard, chessBoard.blackBishops, Piece.BLACK, Piece.BISHOP));

        //add queens moves
        moves.addAll(getQueensMoves(chessBoard, chessBoard.blackQueens, Piece.BLACK, Piece.QUEEN));

        return moves;
    }

    boolean isSquareAttacked(ChessBoard chessBoard, int square,Piece.Color attackingColor){
        if(attackingColor == Piece.Color.WHITE){
            return (getAllAttackedSquaresFor(chessBoard, Piece.Color.WHITE) & (1L << square)) != 0;

        } else {
            return (getAllAttackedSquaresFor(chessBoard, Piece.Color.BLACK) & (1L << square)) != 0;

        }
    }

    boolean isKingAttacked(ChessBoard chessBoard, Piece.Color attackingColor){
        ChessBoard chessBoardWithoutKing = new ChessBoard(chessBoard);
        int kingPosition = getKingPosition(chessBoard, attackingColor.getOpposite());
        chessBoardWithoutKing.setPieceAt(kingPosition,null);
        if(attackingColor == Piece.Color.WHITE){
            return (getAllAttackedSquaresFor(chessBoardWithoutKing, Piece.Color.WHITE) & (1L << kingPosition)) != 0;

        } else {
            return (getAllAttackedSquaresFor(chessBoardWithoutKing, Piece.Color.BLACK) & (1L << kingPosition)) != 0;

        }
    }
    public int getKingPosition(ChessBoard chessBoard, Piece.Color kingColor) {
        int kingPosition = ChessBoard.OUT;
        if(kingColor == Piece.Color.WHITE){
            kingPosition = BitMath.getLSBitIndex(chessBoard.whiteKing);
        } else{
            kingPosition = BitMath.getLSBitIndex(chessBoard.blackKing);

        }
        return kingPosition;
    }

    public ArrayList<Integer> getBlackPositions(ChessBoard chessBoard) {
        ArrayList<Integer> blackPositions = new ArrayList<>();
        long blackPieces = chessBoard.allBlackPieces;
        int blackPiecesCount = BitMath.countSetBits(blackPieces);
        int currentPiecePosition = 0;
        for (int i = 0; i < blackPiecesCount; i++) {
            currentPiecePosition = BitMath.getLSBitIndex(blackPieces);
            blackPositions.add(currentPiecePosition);
            blackPieces = BitMath.popBit(blackPieces, currentPiecePosition);

        }

        return blackPositions;
    }

    public ArrayList<Integer> getWhitePositions(ChessBoard chessBoard) {
        ArrayList<Integer> whitePositions = new ArrayList<>();
        long whitePieces = chessBoard.allWhitePieces;
        int whitePiecesCount = BitMath.countSetBits(whitePieces);
        int currentPiecePosition = 0;
        for (int i = 0; i < whitePiecesCount; i++) {
            currentPiecePosition = BitMath.getLSBitIndex(whitePieces);
            whitePositions.add(currentPiecePosition);
            whitePieces = BitMath.popBit(whitePieces, currentPiecePosition);

        }

        return whitePositions;
    }



    public ChessBoard movePiece(ChessBoard chessBoard,Move move){
        ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        chessBoardAfterMove.setPieceAt(toSquare, chessBoardAfterMove.getPieceAt(fromSquare));
        chessBoardAfterMove.setPieceAt(fromSquare, null);
        chessBoardAfterMove.getPieceAt(toSquare).setPosition(toSquare);

        if (move.isEnPasant()) {
            if (move.getColor() == Piece.Color.WHITE) {
                chessBoardAfterMove.setPieceAt(ChessBoard.offsetRank(move.getTo(), -1), null);
            } else {
                chessBoardAfterMove.setPieceAt(ChessBoard.offsetRank(move.getTo(), 1), null);
            }
        }
        return chessBoardAfterMove;

    }

    private void removeMovesThatExposeKing(ChessBoard chessBoard, ArrayList<Move> pieceLegalMoves, Piece.Color kingColor) {
        Iterator<Move> itr = pieceLegalMoves.iterator();
        ChessBoard chessBoardAfterMove;
        while (itr.hasNext()) {

            Move move = itr.next();
            chessBoardAfterMove = movePiece(chessBoard,move);
            if(Game.moveGenerator.isKingAttacked(chessBoardAfterMove,kingColor.getOpposite())){
                itr.remove();
            }


        }

    }

    public LegalMoves getWhiteLegalMoves(ChessBoard chessBoard) {
        ArrayList<Move> moves = new ArrayList<>();

        //add pawns moves
        moves.addAll(getWhitePawnsPushes(chessBoard.whitePawns, chessBoard.emptySquares));
        moves.addAll(getWhitePawnsCaptures(chessBoard));

        //add king moves
        moves.addAll(getKingMoves(chessBoard, Piece.WHITE));

        //add knights moves
        moves.addAll(getKnightMoves(chessBoard, Piece.WHITE));

        //add rooks moves
        moves.addAll(getRooksMoves(chessBoard, chessBoard.whiteRooks, Piece.WHITE, Piece.ROOK));

        //add bishops moves
        moves.addAll(getBishopsMoves(chessBoard, chessBoard.whiteBishops, Piece.WHITE, Piece.BISHOP));

        //add queens moves
        moves.addAll(getQueensMoves(chessBoard, chessBoard.whiteQueens, Piece.WHITE, Piece.QUEEN));

        removeMovesThatExposeKing(chessBoard,moves, Piece.Color.WHITE);
        LegalMoves legalMoves = new LegalMoves();
        legalMoves.addAll(moves);
        checkCastling(chessBoard,legalMoves, Piece.Color.WHITE);

        return legalMoves;
    }


    public LegalMoves getBlackLegalMoves(ChessBoard chessBoard) {
        ArrayList<Move> moves = new ArrayList<>();

        //add pawns moves
        moves.addAll(getBlackPawnsPushes(chessBoard.blackPawns, chessBoard.emptySquares));
        moves.addAll(getBlackPawnsCaptures(chessBoard));

        //add king moves
        moves.addAll(getKingMoves(chessBoard, Piece.BLACK));

        //add knights moves
        moves.addAll(getKnightMoves(chessBoard, Piece.BLACK));

        //add rooks moves
        moves.addAll(getRooksMoves(chessBoard, chessBoard.blackRooks, Piece.BLACK, Piece.ROOK));

        //add bishops moves
        moves.addAll(getBishopsMoves(chessBoard, chessBoard.blackBishops, Piece.BLACK, Piece.BISHOP));

        //add queens moves
        moves.addAll(getQueensMoves(chessBoard, chessBoard.blackQueens, Piece.BLACK, Piece.QUEEN));


        removeMovesThatExposeKing(chessBoard,moves, Piece.Color.BLACK);
        LegalMoves legalMoves = new LegalMoves();
        legalMoves.addAll(moves);
        checkCastling(chessBoard,legalMoves, Piece.Color.BLACK);


        return legalMoves;
    }
    public LegalMoves getLegalMovesFor(ChessBoard chessBoard,  Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return getWhiteLegalMoves(chessBoard);
        } else {
            return getBlackLegalMoves(chessBoard);
        }
    }

    private void checkCastling(ChessBoard chessBoard, LegalMoves legalMoves,
                                      Piece.Color color) {

        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        int kingTarget = 0;
        if (canCastleKingSide(chessBoard, color, chessBoard.isKingInCheck(color))) {
            kingTarget = initialKingPosition + 2;
            Piece king = new Piece(Piece.Type.KING, color, initialKingPosition);
            Move move = new Move(king, initialKingPosition, kingTarget);
            move.setCastling(true, Move.CastlingType.CASTLING_kING_SIDE);

            legalMoves.add(move);
        }
        if (canCastleQueenSide(chessBoard, color, chessBoard.isKingInCheck(color))) {
            kingTarget = initialKingPosition - 2;
            Piece king = new Piece(Piece.Type.KING, color, initialKingPosition);
            Move move = new Move(king, initialKingPosition, kingTarget);
            move.setCastling(true, Move.CastlingType.CASTLING_QUEEN_SIDE);
            legalMoves.add(move);

        }

    }

    private boolean canCastleKingSide(ChessBoard chessBoard, Piece.Color color, boolean kingInCheck) {
        if (kingInCheck) {
            return false;
        }
        if (color == Piece.Color.WHITE) {
            if (chessBoard.whiteCastlingRights == ChessBoard.NO_CASTLING) {
                return false;
            }
            if (chessBoard.whiteCastlingRights == ChessBoard.CASTLING_QUEEN_SIDE) {
                return false;
            }

        } else {
            if (chessBoard.blackCastlingRights == ChessBoard.NO_CASTLING) {
                return false;
            }
            if (chessBoard.blackCastlingRights == ChessBoard.CASTLING_QUEEN_SIDE) {
                return false;
            }

        }

        int initialKingPosition = getInitialKingPosition(chessBoard, color);

        if (!chessBoard.isSquareEmpty(initialKingPosition + 1) ||
                !chessBoard.isSquareEmpty(initialKingPosition + 2)) {
            return false;
        }


        return !isSquareAttacked(chessBoard, initialKingPosition + 1, color.getOpposite()) &&
                !isSquareAttacked(chessBoard, initialKingPosition + 2, color.getOpposite());
    }

    private boolean canCastleQueenSide(ChessBoard chessBoard, Piece.Color color, boolean kingInCheck) {
        if (kingInCheck) {
            return false;
        }
        if (color == Piece.Color.WHITE) {
            if (chessBoard.whiteCastlingRights == ChessBoard.NO_CASTLING) {
                return false;
            }
            if (chessBoard.whiteCastlingRights == ChessBoard.CASTLING_KING_SIDE) {
                return false;
            }

        } else {
            if (chessBoard.blackCastlingRights == ChessBoard.NO_CASTLING) {
                return false;
            }
            if (chessBoard.blackCastlingRights == ChessBoard.CASTLING_KING_SIDE) {
                return false;
            }

        }
        int initialKingPosition = getInitialKingPosition(chessBoard, color);


        if (!chessBoard.isSquareEmpty(initialKingPosition - 1) ||
                !chessBoard.isSquareEmpty(initialKingPosition - 2) ||
                !chessBoard.isSquareEmpty(initialKingPosition - 3)) {
            return false;
        }

        return !isSquareAttacked(chessBoard, initialKingPosition - 1, color.getOpposite()) &&
                !isSquareAttacked(chessBoard, initialKingPosition - 2, color.getOpposite());
    }


    public int getInitialKingPosition(ChessBoard chessBoard, Piece.Color kingColor) {
        if (kingColor == Piece.Color.WHITE) {
            return 4;
        } else {
            return 60;
        }
    }


    public static int getInitialRookKingSide( Piece.Color rookColor) {
        if (rookColor == Piece.Color.WHITE) {
            return 7;
        } else {
            return 63;
        }

    }

    public static int getInitialRookQueenSide( Piece.Color rookColor) {
        if (rookColor == Piece.Color.WHITE) {
            return 0;
        } else {
            return 56;
        }
    }


}
