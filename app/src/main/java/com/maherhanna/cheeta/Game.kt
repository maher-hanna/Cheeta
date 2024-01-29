package com.maherhanna.cheeta

import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.ChessEngine
import com.maherhanna.cheeta.core.GameStatus
import com.maherhanna.cheeta.core.Move
import com.maherhanna.cheeta.core.Piece.Companion.GetOppositeColor

class Game(private val drawing: Drawing, humanPlayerColor: Int) {
    private val chessBoard: ChessBoard
    var chessEngine: ChessEngine = ChessEngine()

    @JvmField
    var paused: Boolean = false
    private var currentPlayer = 0

    @JvmField
    var humanPlayerColor: Int
    var isGameFinished = false
        private set

    init {
        this.humanPlayerColor = humanPlayerColor
        chessBoard = ChessBoard(ChessBoard.positionInUse)
        chessEngine.moveGenerator.updateWhiteLegalMoves(chessBoard)
        chessEngine.moveGenerator.updateBlackLegalMoves(chessBoard)
        drawing.chessBoard = chessBoard
    }

    fun start() {
        if (humanPlayerColor == chessBoard.toPlayColor) {
            currentPlayer = humanPlayerColor
            drawing.waitHumanToPlay()
        } else {
            playComputer()
        }
    }

    fun resume() {
        if (chessBoard.toPlayColor == humanPlayerColor) {
            drawing.waitHumanToPlay()
            return
        }
    }

    fun humanPlayed(humanMoveArg: Move?) {
        var humanMove = humanMoveArg
        humanMove =
            chessEngine.moveGenerator.generateLegalMovesFor(humanPlayerColor).getMove(humanMove!!)
        if (humanMove != null) {
            chessBoard.makeMove(humanMove)
        }
        currentPlayer = GetOppositeColor(humanPlayerColor)
        chessEngine.moveGenerator.updateLegalMovesFor(chessBoard, humanPlayerColor)
        val opponentColor = currentPlayer
        chessEngine.moveGenerator.updateLegalMovesFor(chessBoard, opponentColor)
        if (chessEngine.moveGenerator.isKingChecked(opponentColor)) {
            drawing.kingInCheck =
                chessEngine.moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        val gameStatus = chessEngine.checkStatus(chessBoard)
        if (gameStatus == GameStatus.NOT_FINISHED) {
            playComputer()
        } else {
            setGameFinished()
            drawing.finishGame(gameStatus)
            return
        }
    }

    fun computerPlayed(computerMove: Move) {
        drawing.currentMove = computerMove
        //computerAi.cancel(true)
        chessBoard.makeMove(computerMove)
        val color = computerMove.color
        chessEngine.moveGenerator.updateLegalMovesFor(chessBoard, color)
        val opponentColor = GetOppositeColor(color)
        chessEngine.moveGenerator.updateLegalMovesFor(chessBoard, opponentColor)
        if (chessEngine.moveGenerator.isKingChecked(opponentColor)) {
            drawing.kingInCheck =
                chessEngine.moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        currentPlayer = GetOppositeColor(color)
        val gameStatus = chessEngine.checkStatus(chessBoard)
        if (gameStatus == GameStatus.NOT_FINISHED) {
            drawing.waitHumanToPlay()
        } else {
            //game finished
            setGameFinished()
            drawing.finishGame(gameStatus)
            return
        }
    }

    fun playComputer() {
        if (paused) return
        //computerAi = ChessEngine()
        val engineThread = object : Thread() {
            override fun run() {
                val move = chessEngine.getMove(ChessBoard(chessBoard))
                if (move != null) {
                    computerPlayed(move)
                }
            }
        }
        engineThread.start()
    }

    fun setGameFinished() {
        isGameFinished = true
    }


//    private inner class ComputerAi : ChessEngine() {
//        override fun onPostExecute(move: Move) {
//            computerPlayed(move)
//        }
//    }


}