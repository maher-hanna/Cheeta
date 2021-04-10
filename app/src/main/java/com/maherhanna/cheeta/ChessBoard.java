package com.maherhanna.cheeta;

import android.util.Log;

import java.util.ArrayList;

public class ChessBoard {
    public static final int MIN_POSITION = 0;
    public static final int MAX_POSITION = 63;
    public static final int OUT_OF_BOARD = -1;

    public static final int RANK_1 = 0;
    public static final int RANK_2 = 1;
    public static final int RANK_3 = 2;
    public static final int RANK_4 = 3;
    public static final int RANK_5 = 4;
    public static final int RANK_6 = 5;
    public static final int RANK_7 = 6;
    public static final int RANK_8 = 7;

    public static final int FILE_A = 0;
    public static final int FILE_B = 1;
    public static final int FILE_C = 2;
    public static final int FILE_D = 3;
    public static final int FILE_E = 4;
    public static final int FILE_F = 5;
    public static final int FILE_G = 6;
    public static final int FILE_H = 7;


    private final Piece[] pieces;
    public ChessboardMoves moves;

    LegalMoves blackLegalMoves;
    LegalMoves whiteLegalMoves;

    public ChessBoard(ChessBoard copy) {
        this.pieces = copy.pieces.clone();
        this.moves = new ChessboardMoves(copy.moves);
    }


    public ChessBoard(Drawing drawing) {
        pieces = new Piece[64];

        for (int i = 0; i < 64; ++i) {
            pieces[i] = null;
        }

        moves = new ChessboardMoves();
        blackLegalMoves = new LegalMoves();
        whiteLegalMoves = new LegalMoves();


    }

    public void setUpBoard() {
        setupWhitePieces();
        setupBlackPieces();

        updateWhiteLegalMoves(false);
        updateBlackLegalMoves(false);

    }

    private void setupWhitePieces() {

        Piece square;
        Piece piece;
        for (int i = 0; i < 8; ++i) {
            int position = GetPosition(i, 1);
            setPieceAt(position, Piece.Type.PAWN, Piece.Color.WHITE);
        }

        setPieceAt(0, Piece.Type.ROOK, Piece.Color.WHITE);
        setPieceAt(1, Piece.Type.KNIGHT, Piece.Color.WHITE);
        setPieceAt(2, Piece.Type.BISHOP, Piece.Color.WHITE);
        setPieceAt(3, Piece.Type.QUEEN, Piece.Color.WHITE);
        setPieceAt(4, Piece.Type.KING, Piece.Color.WHITE);
        setPieceAt(5, Piece.Type.BISHOP, Piece.Color.WHITE);
        setPieceAt(6, Piece.Type.KNIGHT, Piece.Color.WHITE);
        setPieceAt(7, Piece.Type.ROOK, Piece.Color.WHITE);


    }

    private void setupBlackPieces() {
        for (int i = 0; i < 8; ++i) {
            setPieceAt(GetPosition(i, 6), Piece.Type.PAWN, Piece.Color.BLACK);

        }

        setPieceAt(GetPosition(0, 7), Piece.Type.ROOK, Piece.Color.BLACK);
        setPieceAt(GetPosition(1, 7), Piece.Type.KNIGHT, Piece.Color.BLACK);
        setPieceAt(GetPosition(2, 7), Piece.Type.BISHOP, Piece.Color.BLACK);
        setPieceAt(GetPosition(3, 7), Piece.Type.QUEEN, Piece.Color.BLACK);
        setPieceAt(GetPosition(4, 7), Piece.Type.KING, Piece.Color.BLACK );
        setPieceAt(GetPosition(5, 7), Piece.Type.BISHOP, Piece.Color.BLACK );
        setPieceAt(GetPosition(6, 7), Piece.Type.KNIGHT, Piece.Color.BLACK );
        setPieceAt(GetPosition(7, 7), Piece.Type.ROOK, Piece.Color.BLACK );

    }





    public void updateBlackLegalMoves(boolean kingInCheck) {
        blackLegalMoves = LegalMovesChecker.getBlackLegalMoves(this);
    }


    public void updateWhiteLegalMoves(boolean kingInCheck) {
        whiteLegalMoves = LegalMovesChecker.getWhiteLegalMoves(this);

    }

    public void updateLegalMovesFor(Piece.Color playerColor, boolean kingInCheck) {
        if (playerColor == Piece.Color.WHITE) {
            updateWhiteLegalMoves(kingInCheck);
        } else {
            updateBlackLegalMoves(kingInCheck);
        }
    }

    public ArrayList<Integer> getBlackPositions() {
        ArrayList<Integer> blackPositions = new ArrayList<>();
        for (int i = MIN_POSITION; i <= MAX_POSITION; i++) {
            if (isSquareEmpty(i)) continue;
            if (isPieceBlackAt(i)) blackPositions.add(i);
        }
        return blackPositions;
    }

    public ArrayList<Integer> getWhitePositions() {
        ArrayList<Integer> whitePositions = new ArrayList<>();
        for (int i = MIN_POSITION; i <= MAX_POSITION; i++) {
            if (isSquareEmpty(i)) continue;
            if (isPieceWhiteAt(i)) whitePositions.add(i);
        }
        return whitePositions;
    }

    public ArrayList<Integer> getPositionsFor(Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return getWhitePositions();
        } else {
            return getBlackPositions();
        }
    }

    public LegalMoves getLegalMovesFor(Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return whiteLegalMoves;
        } else {
            return blackLegalMoves;
        }
    }


    public ArrayList<Integer> getLegalTargetsFor(int position) {
        if (getPieceAt(position).getColor() == Piece.Color.WHITE) {
            return whiteLegalMoves.getLegalTargetsFor(position);
        } else {
            return blackLegalMoves.getLegalTargetsFor(position);
        }
    }




    public boolean isKingInCheck(Piece.Color kingColor) {
        return LegalMovesChecker.isSquareAttacked(this, getKingPosition(kingColor), kingColor.getOpposite());
    }



    public int getKingPosition(Piece.Color kingColor) {
        int kingPosition = OUT_OF_BOARD;
        for (int i = MIN_POSITION; i <= MAX_POSITION; i++) {
            Piece piece = getPieceAt(i);
            if (piece != null && piece.getType() == Piece.Type.KING && piece.getColor() == kingColor) {
                kingPosition = i;
                break;
            }
        }
        return kingPosition;
    }

    public boolean canMove(int fromSquare, int toSquare) {
        boolean isLegal = false;
        if (isPieceBlackAt(fromSquare)) {
            isLegal = blackLegalMoves.canMove(fromSquare, toSquare);

        } else {
            isLegal = whiteLegalMoves.canMove(fromSquare, toSquare);
        }
        return isLegal;

    }



    public void movePiece(Move move) {
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        setPieceAt(toSquare, getPieceAt(fromSquare));
        setPieceAt(fromSquare, null);
        getPieceAt(toSquare).setPosition(toSquare);


        if (move.isCastling()) {
            int rookPosition;
            int rookCastlingTarget;
            Piece.Color moveColor = move.getColor();

            if (move.getCastlingType() == Move.CastlingType.CASTLING_kING_SIDE) {
                rookPosition = LegalMovesChecker.getInitialRookKingSide(this,
                        moveColor);
                rookCastlingTarget = move.getFrom() + 1;

            } else {
                rookPosition = LegalMovesChecker.getInitialRookQueenSide(this,
                        moveColor);
                rookCastlingTarget = move.getFrom() -1;
            }
            setPieceAt(rookCastlingTarget, getPieceAt(rookPosition) );
            setPieceAt(rookPosition, null);
            getPieceAt(rookCastlingTarget).setPosition(rookCastlingTarget);
        }
        if(move.isPromote()){
            getPieceAt(toSquare).setType(move.getPromotionPieceType());
        }
        if(move.isEnPasant()){
            if(move.getColor() == Piece.Color.WHITE){
                setPieceAt(ChessBoard.offsetRank(move.getTo(),-1),null);
            }
            else {
                setPieceAt(ChessBoard.offsetRank(move.getTo(),1),null);
            }
        }

        moves.add(move);

    }

    //get and set a square info
    public Piece getPieceAt(int position) {

        return pieces[position];
    }

    public Piece getPieceAt(int file, int rank) {
        return pieces[GetPosition(file, rank)];
    }

    public void setPieceAt(int position, Piece.Type pieceType, Piece.Color pieceColor) {
        pieces[position] = new Piece(pieceType, pieceColor, position);
    }


    public Game.GameStatus checkStatus(){
        Piece.Color lastPlayed = moves.getLastPlayed();
        Game.GameStatus gameStatus = Game.GameStatus.NOT_FINISHED;
        Piece.Color currentToPlayColor = lastPlayed.getOpposite();
        boolean isKingInCheck = isKingInCheck(currentToPlayColor);

        LegalMoves currentToPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(this,
                currentToPlayColor);

        if (isKingInCheck) {
            if (currentToPlayLegalMoves.getNumberOfMoves() == 0) {
                //win
                if (lastPlayed == Piece.Color.WHITE) {
                    gameStatus = Game.GameStatus.FINISHED_WIN_WHITE;
                } else {
                    gameStatus = Game.GameStatus.FINISHED_WIN_BLACK;

                }
            }
        } else {
            if (currentToPlayLegalMoves.getNumberOfMoves() == 0) {

                //draw stalemate
                gameStatus = Game.GameStatus.FINISHED_DRAW;
            }
            if (insufficientMaterial()) {
                gameStatus = Game.GameStatus.FINISHED_DRAW;
            }

        }

        return gameStatus;
    }

    public boolean checkGameFinished(){
        boolean finished = false;
        Game.GameStatus gameStatus = checkStatus();
        if(gameStatus == Game.GameStatus.FINISHED_DRAW || gameStatus == Game.GameStatus.FINISHED_WIN_WHITE
        || gameStatus == Game.GameStatus.FINISHED_WIN_BLACK){
            finished = true;
        }
        return finished;
    }

    private boolean insufficientMaterial() {
        ArrayList<Integer> whitePieces = getWhitePositions();
        ArrayList<Integer> blackPieces = getBlackPositions();
        int whitePiecesNumber = whitePieces.size();
        int blackPiecesNumber = blackPieces.size();

        // tow kings remaining
        if (whitePiecesNumber + blackPiecesNumber == 2) {
            return true;
        }

        if (whitePiecesNumber + blackPiecesNumber == 3) {

            Piece.Type remainingPieceType = Piece.Type.PAWN;
            for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {
                if (getPieceAt(i) != null && getPieceAt(i).getType() != Piece.Type.KING) {
                    remainingPieceType = getPieceType(i);
                }
            }

            // tow kings and a bishop or knight
            if(remainingPieceType == Piece.Type.BISHOP || remainingPieceType == Piece.Type.KNIGHT){
                return true;
            }

        }

        if (whitePiecesNumber + blackPiecesNumber == 4) {

            ArrayList<Piece> remainingPieces = new ArrayList<>();
            for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {
                if (getPieceAt(i) != null && getPieceAt(i).getType() != Piece.Type.KING) {
                    remainingPieces.add(getPieceAt(i));
                }
            }
            Piece firstPiece = remainingPieces.get(0);
            Piece secondPiece = remainingPieces.get(1);

            // tow king and tow bishops of the same square color
            if(firstPiece.getType() == Piece.Type.BISHOP && secondPiece.getType() == Piece.Type.BISHOP){
                if(firstPiece.getColor() != secondPiece.getColor()){
                    return ChessBoard.GetSquareColor(firstPiece.getPosition()) ==
                            ChessBoard.GetSquareColor(secondPiece.getPosition());
                }
            }



        }



        return false;
    }


    public void setPieceAt(int position, Piece piece) {
        if(piece == null){
            pieces[position] = null;
            return;
        }
        pieces[position] = new Piece(piece);
    }

    public boolean isSquareEmpty(int position) {
        return getPieceAt(position) == null;
    }

    public boolean isPieceBlackAt(int position) {
        return pieces[position].getColor() == Piece.Color.BLACK;
    }

    public boolean isPieceWhiteAt(int position) {
        return pieces[position].getColor() == Piece.Color.WHITE;
    }

    public Piece.Color getPieceColor(int position){
        return pieces[position].getColor();
    }
    public Piece.Type getPieceType(int position){
        return pieces[position].getType();
    }

    public boolean isPieceAt(int square, Piece.Type type, Piece.Color color){
        if(square == OUT_OF_BOARD) return false;
        if(isSquareEmpty(square)) return false;
        return getPieceColor(square) == color && getPieceType(square) == type;
    }

    public static int GetPosition(int file, int rank) {
        if (file < FILE_A || file > FILE_H) return OUT_OF_BOARD;
        if (rank < RANK_1 || rank > RANK_8) return OUT_OF_BOARD;

        return (rank * 8) + file;
    }

    public static int GetFile(int position) {
        return position % 8;
    }

    public static int GetRank(int position) {
        return position / 8;
    }

    public static int offset(int square, int file, int rank){
        int newFile = GetFile(square) + file;
        int newRank = GetRank(square) + rank;
        if(newFile < ChessBoard.FILE_A || newFile > ChessBoard.FILE_H) return ChessBoard.OUT_OF_BOARD;
        if(newRank < ChessBoard.RANK_1 || newRank > ChessBoard.RANK_8) return ChessBoard.OUT_OF_BOARD;

        return (newRank * 8 )  + newFile;
    }
    public static int offsetFile(int square,int file){
        return offset(square,file,0);
    }
    public static int offsetRank(int square,int rank){
        return offset(square,0,rank);
    }
    public static Piece.Color GetSquareColor(int square){
        if((square % 2) == 0){
            return Piece.Color.BLACK;
        } else {
            return Piece.Color.WHITE;
        }
    }

    //------------------------


    //this function is for debugging
    public void print() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" 0,");
        for (int row = 7; row >= 0; row--) {
            for (int column = 0; column < 8; column++) {
                if (getPieceAt(column, row) == null) {
                    stringBuilder.append(" 0,");
                    continue;
                }
                stringBuilder.append(String.format("%2d,", getPieceAt(column, row).getPosition()));
            }
            stringBuilder.append('\n');


        }
        Log.d(Game.DEBUG, stringBuilder.toString());
    }


}
