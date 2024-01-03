package com.maherhanna.cheeta

import com.maherhanna.cheeta.Piece.Companion.GetOppositeColor

class Game(private val drawing: Drawing, humanPlayerColor: Int) {
    private val chessBoard: ChessBoard
    private var computerAi: ComputerAi
    @JvmField
    var paused: Boolean
    var currentPlayer = 0
    @JvmField
    var humanPlayerColor: Int
    var isGameFinished = false
        private set

    init {
        computerAi = ComputerAi()
        paused = false
        this.humanPlayerColor = humanPlayerColor
        chessBoard = ChessBoard(positionInUse)
        drawing.chessBoard = chessBoard
    }

    fun start() {
        if (humanPlayerColor == chessBoard.toPlayColor) {
            currentPlayer = humanPlayerColor
            drawing.waitHumanToPlay()
        } else {
            playComputer(GetOppositeColor(humanPlayerColor))
        }
    }

    fun resume() {
        if (chessBoard.toPlayColor == humanPlayerColor) {
            drawing.waitHumanToPlay()
            return
        }
    }

    fun checkGameFinished(lastPlayed: Int): GameStatus {
        return chessBoard.checkStatus(chessBoard.getLegalMovesFor(GetOppositeColor(lastPlayed)))
    }

    fun humanPlayed(humanMove: Move?) {
        var humanMove = humanMove
        humanMove = chessBoard.getLegalMovesFor(humanPlayerColor).getMove(humanMove!!)
        chessBoard.move(humanMove)
        currentPlayer = GetOppositeColor(humanPlayerColor)
        chessBoard.updateLegalMovesFor(humanPlayerColor, false)
        val opponentColor = currentPlayer
        val isOpponentKingInCheck = chessBoard.isKingInCheck(opponentColor)
        if (isOpponentKingInCheck) {
            drawing.kingInCheck = moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        chessBoard.updateLegalMovesFor(opponentColor, isOpponentKingInCheck)
        val gameStatus = checkGameFinished(humanPlayerColor)
        if (gameStatus == GameStatus.NOT_FINISHED) {
            playComputer(opponentColor)
        } else {
            setGameFinished()
            drawing.finishGame(gameStatus)
            return
        }
    }

    fun computerPlayed(computerMove: Move) {
        drawing.currentMove = computerMove
        computerAi.cancel(true)
        chessBoard.move(computerMove)
        val color = computerMove.color
        chessBoard.updateLegalMovesFor(color, false)
        val opponentColor = GetOppositeColor(color)
        val isOpponentKingInCheck = chessBoard.isKingInCheck(opponentColor)
        if (isOpponentKingInCheck) {
            drawing.kingInCheck = moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        chessBoard.updateLegalMovesFor(opponentColor, isOpponentKingInCheck)
        currentPlayer = GetOppositeColor(color)
        val gameStatus = checkGameFinished(color)
        if (gameStatus == GameStatus.NOT_FINISHED) {
            drawing.waitHumanToPlay()
        } else {
            //game finished
            setGameFinished()
            drawing.finishGame(gameStatus)
            return
        }
    }

    fun playComputer(color: Int) {
        if (paused) return
        computerAi = ComputerAi()
        computerAi.execute(chessBoard)
    }

    fun setGameFinished() {
        isGameFinished = true
    }

    enum class GameStatus {
        NOT_FINISHED,
        FINISHED_DRAW,
        FINISHED_WIN_WHITE,
        FINISHED_WIN_BLACK
    }

    private inner class ComputerAi : ChessEngine() {
        override fun onPostExecute(move: Move) {
            computerPlayed(move)
        }
    }

    companion object {
        const val DEBUG = "Cheeta_Debug"
        const val startPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 "
        private const val trickyPosition =
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1 "
        private const val killerPosition =
            "rnbqkb1r/pp1p1pPp/8/2p1pP2/1P1P4/3P3P/P1P1P3/RNBQKBNR w KQkq e6 0 1"
        private const val cmkPosition =
            "r2q1rk1/ppp2ppp/2n1bn2/2b1p3/3pP3/3P1NPP/PPP1NPB1/R1BQ1RK1 b - - 0 9 "
        private const val positionInUse = startPosition
        const val COMPUTER_MAX_SEARCH_TIME: Long = 4
        @JvmField
        var moveGenerator = MoveGenerator()
    }
}