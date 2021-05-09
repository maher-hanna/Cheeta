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
    public Move currentMove = null;
    public float xTouchStart = 0;
    public float yTouchStart = 0;
    //for dragging a piece
    public int dragFrom = ChessBoard.NO_SQUARE;
    int dragFromHighlight = dragFrom;
    public int selectedSquare = ChessBoard.NO_SQUARE;
    public int selectedSquareHighlight = ChessBoard.NO_SQUARE;
    public int touchSquare = ChessBoard.NO_SQUARE;
    public float x = 0;
    public float y = 0;
    //---------------------



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

    private boolean waitingForHuman = false;


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





    public int flip(int position) {
        return ChessBoard.MAX_POSITION - position;
    }




    public void drawAllPieces() {
        clearBoard();
        int from;
        int to;
        boolean fliped = isChessBoardFlipped();
        int legalTargetsSquare = ChessBoard.NO_SQUARE;
        dragFromHighlight = dragFrom;
        selectedSquareHighlight = selectedSquare;
        if(fliped){
            dragFromHighlight = flip(dragFromHighlight);
            selectedSquareHighlight = flip(selectedSquareHighlight);
        }

        if (currentMove != null) {

                from = currentMove.getFrom();
                to = currentMove.getTo();
                if (fliped) {
                    from = flip(from);
                    to = flip(to);
                }
                drawHighlight(from);
                drawHighlight(to);

        }
        if (selectedSquare != ChessBoard.NO_SQUARE) {
            legalTargetsSquare = selectedSquare;

            drawHighlight(selectedSquareHighlight);

        }

        if (dragFrom != ChessBoard.NO_SQUARE) {
            legalTargetsSquare = dragFrom;
            drawHighlight(dragFromHighlight);
            drawHighlight(touchSquare);

        }




        int index = 0;
        for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {

            // flip the board if the black is at the bottom of screen

            if (fliped) {
                index = ChessBoard.MAX_POSITION - i;
            } else {
                index = i;
            }

            if (!chessBoard.isSquareEmpty(i)) {
                if (i == dragFrom) continue;

                drawPiece(chessBoard.pieceType(i), chessBoard.pieceColor(i), index, 0f, 0f);
            }
        }

        if (dragFrom != ChessBoard.NO_SQUARE) {

            drawPiece(chessBoard.pieceType(dragFrom), chessBoard.pieceColor(dragFrom),
                    dragFromHighlight, x - xTouchStart, y - yTouchStart);

        }
        if (legalTargetsSquare != ChessBoard.OUT ) {
            ArrayList<Integer> squareLegalMoves = getLegalMoves(legalTargetsSquare);

            for (int square : squareLegalMoves) {
                drawLegalSquare(square);
            }

        }




        show();
    }



    public void show() {
        chessboardView.invalidate();
    }


    public boolean canMove(int from, int to) {
        if (!isWaitingHumanToPlay()) {
            return false;
        }
        return chessBoard.canMove(from, to);
    }


    private void drawPiece(int pieceType, int pieceColor, int position, float xOffset, float yOffset) {

        RectF pieceRect = getPieceDrawingRect(pieceType, pieceColor, position);
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
        switch (pieceType) {

            case Piece.PAWN:
                if (pieceColor == Piece.WHITE)
                    chessboardView.drawPiece(whitePawnBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackPawnBitmap, pieceRect);
                break;
            case Piece.ROOK:
                if (pieceColor == Piece.WHITE)
                    chessboardView.drawPiece(whiteRookBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackRookBitmap, pieceRect);
                break;
            case Piece.KNIGHT:
                if (pieceColor == Piece.WHITE)
                    chessboardView.drawPiece(whiteKnightBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackKnightBitmap, pieceRect);
                break;
            case Piece.BISHOP:
                if (pieceColor == Piece.WHITE)
                    chessboardView.drawPiece(whiteBishopBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackBishopBitmap, pieceRect);
                break;
            case Piece.QUEEN:
                if (pieceColor == Piece.WHITE)
                    chessboardView.drawPiece(whiteQueenBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackQueenBitmap, pieceRect);
                break;
            case Piece.KING:
                if (pieceColor == Piece.WHITE)
                    chessboardView.drawPiece(whiteKingBitmap, pieceRect);
                else
                    chessboardView.drawPiece(blackKingBitmap, pieceRect);
                break;
        }
    }


    private RectF getPieceDrawingRect(int pieceType, int pieceColor, int position) {
        RectF result = new RectF();
        float x = ChessBoard.GetFile(position) * squareSize;
        float y = ChessBoard.GetRank(position) * squareSize;

        y = chessBoardViewRect.width() - y - squareSize;


        switch (pieceType) {

            case Piece.PAWN:
                result = new RectF(pawnDrawingRect);
                result.offset(x, y);
                break;
            case Piece.ROOK:
                result = new RectF(rookDrawingRect);
                result.offset(x, y);
                break;
            case Piece.KNIGHT:
                result = new RectF(knightDrawingRect);
                result.offset(x, y);
                break;
            case Piece.BISHOP:
                result = new RectF(bishopDrawingRect);
                result.offset(x, y);
                break;
            case Piece.QUEEN:
                result = new RectF(queenDrawingRect);
                result.offset(x, y);
                break;
            case Piece.KING:
                result = new RectF(kingDrawingRect);
                result.offset(x, y);
                break;
        }
        return result;

    }


    public void finishGame(Game.GameStatus gameStatus) {
        int message_id;
        if (gameStatus == Game.GameStatus.FINISHED_DRAW) {
            message_id = R.string.message_draw;
        } else {

            if (game.humanPlayerColor == Piece.WHITE) {
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
        chessboardView.finishGame(message_id);
    }

    public void waitHumanToPlay() {
        waitingForHuman = true;
    }

    public void humanPlayed(Move humanMove) {
        game.humanPlayed(humanMove);
        waitingForHuman = false;

    }

    public boolean isWaitingHumanToPlay() {
        return waitingForHuman;
    }


    public boolean isGameFinished() {
        return game.isGameFinished();
    }

    public ArrayList<Integer> getLegalMoves(int square) {
        return chessBoard.getLegalTargetsFor(square);
    }

    public boolean canSelect(int position) {
        return !chessBoard.isSquareEmpty(position) &&
                chessBoard.pieceColor(position) == game.humanPlayerColor;

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
        return game.humanPlayerColor == Piece.BLACK;
    }

    public int getBottomScreenPlayerColor() {
        return game.humanPlayerColor;
    }


}
