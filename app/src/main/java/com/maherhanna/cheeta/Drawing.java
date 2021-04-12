package com.maherhanna.cheeta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.ArrayList;

public class Drawing {
    public Game game;
    private final GameActivity activity;
    private final ChessboardView chessboardView;
    public ChessBoard chessBoard;


    //pieces drawables
    private final Bitmap blackPawnBitmap;
    private final Bitmap whitePawnBitmap;
    private final Bitmap blackBishopBitmap;
    private final Bitmap whiteBishopBitmap;
    private final Bitmap blackKnightBitmap;
    private final Bitmap whiteKnightBitmap;
    private final Bitmap blackRookBitmap;
    private final Bitmap whiteRookBitmap;
    private final Bitmap blackQueenBitmap;
    private final Bitmap whiteQueenBitmap;
    private final Bitmap blackKingBitmap;
    private final Bitmap whiteKingBitmap;
    //pieces drawing dimensions
    private RectF pawnDrawingRect;
    private RectF bishopDrawingRect;
    private RectF knightDrawingRect;
    private RectF rookDrawingRect;
    private RectF queenDrawingRect;
    private RectF kingDrawingRect;
    //chess board dimensions
    private RectF chessBoardViewRect;
    public float squareSize;
    private static final float SCALE_PIECES_DOWN = 0.8f;
    //----------------

    boolean waitingForHuman = false;


    public Drawing(GameActivity activity) {
        this.activity = activity;
        this.chessboardView = this.activity.findViewById(R.id.chessboardView);
        this.chessboardView.drawing = this;

        blackPawnBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_pawn);
        whitePawnBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_pawn);
        blackBishopBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_bishop_transparent);
        whiteBishopBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_bishop);
        blackKnightBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_knight_transparent);
        whiteKnightBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_knight);
        blackRookBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_rook_transparent);
        whiteRookBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_rook);
        blackQueenBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_queen_transparent);
        whiteQueenBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_queen);
        blackKingBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_king_transparent);
        whiteKingBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_king);

        updateDrawingRects(new RectF(0, 0, chessboardView.getWidth()
                , chessboardView.getHeight()));


    }


    public void updateDrawingRects(RectF newChessBoardRect) {
        //called when the size of chessboard view change
        chessBoardViewRect = newChessBoardRect;

        squareSize = chessBoardViewRect.height() / 8.0f;


        //----------
        pawnDrawingRect = calculateRect(blackPawnBitmap.getWidth(), blackPawnBitmap.getHeight());
        bishopDrawingRect = calculateRect(blackBishopBitmap.getWidth(), blackBishopBitmap.getHeight());
        knightDrawingRect = calculateRect(blackKnightBitmap.getWidth(), blackKnightBitmap.getHeight());
        rookDrawingRect = calculateRect(blackRookBitmap.getWidth(), blackRookBitmap.getHeight());
        queenDrawingRect = calculateRect(blackQueenBitmap.getWidth(), blackQueenBitmap.getHeight());
        kingDrawingRect = calculateRect(blackKingBitmap.getWidth(), blackKingBitmap.getHeight());
        //------------


    }

    private RectF calculateRect(float bitmapWidth, float bitmapHeight) {
        RectF result;
        float squareScaleDownFactor = Math.max(bitmapWidth, bitmapHeight)
                / squareSize;
        float width = bitmapWidth / squareScaleDownFactor;
        float height = bitmapHeight / squareScaleDownFactor;
        width *= SCALE_PIECES_DOWN;
        height *= SCALE_PIECES_DOWN;
        float left = (squareSize - width) / 2;
        float top = (squareSize - height) / 2;
        result = new RectF(0, 0, width, height);
        result.offset(left, top);
        return result;

    }


    public void clearBoard() {
        chessboardView.clearBoard();
    }


    private void drawHighlight(int square) {
        RectF highlightRect = getSquareRect(square);
        chessboardView.drawHighlight(highlightRect);
    }

    private RectF getSquareRect(int square) {
        float squareLeft = ChessBoard.GetFile(square) * squareSize;
        float squareTop;
        squareTop = (8 - ChessBoard.GetRank(square) - 1) * squareSize;
        float squareRight = squareLeft + squareSize;
        float squareBottom = squareTop + squareSize;
        return new RectF(squareLeft, squareTop, squareRight, squareBottom);


    }


    public void drawAllPieces() {
        int flip = 0;
        if (isChessBoardFlipped()) {
            flip = 1;
        }
        int index = 0;

        for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {
            Piece piece = chessBoard.getPieceAt(i);
            if (piece != null) {
                // flip the board if the black is at the bottom of screen
                index = i - (ChessBoard.MAX_POSITION * flip);
                index = Math.abs(index);
                drawPiece(piece, index, 0, 0);
            }
        }

    }

    public void dragPiece(int source, int target, float xOffset, float yOffset) {
        clearBoard();
        int flip = 0;
        if (isChessBoardFlipped()) {
            flip = 1;

        }
        if (flip == 1) {

            drawHighlight(flip(source));

        } else {
            drawHighlight(source);
        }
        drawHighlight(target);

        int index = 0;
        for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {

            Piece piece = chessBoard.getPieceAt(i);
            // flip the board if the black is at the bottom of screen

            index = i - (ChessBoard.MAX_POSITION * flip);
            index = Math.abs(index);
            if (piece != null) {
                if (i == source) continue;

                drawPiece(piece, index, 0f, 0f);
            }
        }
        if (flip == 1) {

            drawPiece(chessBoard.getPieceAt(source), flip(source), xOffset, yOffset);


        } else {
            drawPiece(chessBoard.getPieceAt(source), source, xOffset, yOffset);
        }
        show();
    }

    public int flip(int position) {
        return ChessBoard.MAX_POSITION - position;
    }


    public void drawAllPieces(Move move) {
        clearBoard();
        int from = move.getFrom();
        int to = move.getTo();
        if (isChessBoardFlipped()) {
            from = flip(from);
            to = flip(to);
        }
        drawHighlight(from);
        drawHighlight(to);
        drawAllPieces();
        show();
    }

    public void drawAllPieces(int highlight) {
        clearBoard();
        if (isChessBoardFlipped()) {
            highlight = flip(highlight);
        }
        drawHighlight(highlight);
        drawAllPieces();
        show();
    }


    public void show() {
        chessboardView.invalidate();
    }


    public boolean canMove(int from, int to) {
        return chessBoard.canMove(from, to);
    }


    private void drawPiece(Piece piece, int position, float xOffset, float yOffset) {

        RectF pieceRect = getPieceDrawingRect(piece, position);
        pieceRect.offset(xOffset, yOffset);

        //limit the piece drawing rect inside the board
        if (pieceRect.left < 0) {
            pieceRect.offset(-pieceRect.left, 0);
        }

        if (pieceRect.top < 0) {
            pieceRect.offset(0, -pieceRect.top);
        }
        if (pieceRect.right > chessBoardViewRect.width()) {
            pieceRect.offset(-(pieceRect.right - chessBoardViewRect.width()), 0);
        }
        if (pieceRect.bottom > chessBoardViewRect.height()) {
            pieceRect.offset(0, -(pieceRect.bottom - chessBoardViewRect.height()));
        }
        //---------------------
        switch (piece.getType()) {

            case PAWN:
                if (piece.getColor() == Piece.Color.WHITE)
                    chessboardView.drawPiece(whitePawnBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackPawnBitmap, pieceRect);
                break;
            case ROOK:
                if (piece.getColor() == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteRookBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackRookBitmap, pieceRect);
                break;
            case KNIGHT:
                if (piece.getColor() == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteKnightBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackKnightBitmap, pieceRect);
                break;
            case BISHOP:
                if (piece.getColor() == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteBishopBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackBishopBitmap, pieceRect);
                break;
            case QUEEN:
                if (piece.getColor() == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteQueenBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackQueenBitmap, pieceRect);
                break;
            case KING:
                if (piece.getColor() == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteKingBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackKingBitmap, pieceRect);
                break;
        }
    }


    private RectF getPieceDrawingRect(Piece piece, int position) {
        RectF result = new RectF();
        float x = ChessBoard.GetFile(position) * squareSize;
        float y = ChessBoard.GetRank(position) * squareSize;

        y = chessBoardViewRect.width() - y - squareSize;


        switch (piece.getType()) {

            case PAWN:
                result = new RectF(pawnDrawingRect);
                result.offset(x, y);
                break;
            case ROOK:
                result = new RectF(rookDrawingRect);
                result.offset(x, y);
                break;
            case KNIGHT:
                result = new RectF(knightDrawingRect);
                result.offset(x, y);
                break;
            case BISHOP:
                result = new RectF(bishopDrawingRect);
                result.offset(x, y);
                break;
            case QUEEN:
                result = new RectF(queenDrawingRect);
                result.offset(x, y);
                break;
            case KING:
                result = new RectF(kingDrawingRect);
                result.offset(x, y);
                break;
        }
        return result;

    }


    public void finishGame(Game.GameStatus gameStatus, int gameType) {
        int message_id;
        if (gameStatus == Game.GameStatus.FINISHED_DRAW) {
            message_id = R.string.message_draw;
        } else {
            if (gameType == Game.COMPUTER_COMPUTER) {
                if (gameStatus == Game.GameStatus.FINISHED_WIN_WHITE)
                    message_id = R.string.message_white_won;
                else message_id = R.string.message_black_won;
            } else {
                if (game.bottomScreenPlayerColor == Piece.Color.WHITE) {
                    if (gameStatus == Game.GameStatus.FINISHED_WIN_WHITE) {
                        message_id = R.string.message_you_won;
                    } else {
                        message_id = R.string.message_computer_won;
                    }
                } else {
                    if (gameStatus == Game.GameStatus.FINISHED_WIN_BLACK) {
                        message_id = R.string.message_you_won;
                    } else {
                        message_id = R.string.message_computer_won;
                    }
                }
            }


        }
        chessboardView.finishGame(message_id);
    }

    public void waitHumanToPlay() {
        waitingForHuman = true;
    }

    public boolean isWaitingForHumanToPlay() {
        return waitingForHuman;
    }

    public void humanPlayed(Move humanMove) {
        game.humanPlayed(humanMove);

    }

    public int getGameType() {
        return game.gameType;
    }

    public boolean isGameFinished() {
        return game.isGameFinished();
    }

    public Piece getPieceAt(int position) {
        return chessBoard.getPieceAt(position);
    }

    public ArrayList<Integer> getLegalMoves(int square) {
        return chessBoard.getLegalTargetsFor(square);
    }

    public boolean canSelect(int position) {
        Piece targetPiece = chessBoard.getPieceAt(position);

        return targetPiece != null &&
                chessBoard.getPieceColor(position) == game.bottomScreenPlayerColor;

    }

    public void drawLegalSquare(int square) {
        RectF squareRect;
        if (isChessBoardFlipped()) {
            squareRect = getSquareRect(flip(square));
        } else {
            squareRect = getSquareRect(square);
        }

        chessboardView.drawLegalSquare(squareRect, !chessBoard.isSquareEmpty(square));
    }

    public boolean isChessBoardFlipped() {
        return game.bottomScreenPlayerColor == Piece.Color.BLACK;
    }

    public Piece.Color getBottomScreenPlayerColor() {
        return game.bottomScreenPlayerColor;
    }
    public boolean canHumanPlay(){
        return game.currentPlayer == game.bottomScreenPlayerColor;
    }
}
