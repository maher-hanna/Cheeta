package com.maherhanna.cheeta

import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.GameStatus
import com.maherhanna.cheeta.core.Move
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Piece
import com.maherhanna.cheeta.core.Piece.Companion.GetOppositeColor
import com.maherhanna.cheeta.core.PlayerLegalMoves

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
        return chessEngine.checkStatus(chessBoard,chessEngine.moveGenerator.getLegalMovesFor(GetOppositeColor(lastPlayed)))
    }

    fun humanPlayed(humanMove: Move?) {
        var humanMove = humanMove
        humanMove = chessEngine.moveGenerator.getLegalMovesFor(humanPlayerColor).getMove(humanMove!!)
        chessBoard.move(humanMove)
        currentPlayer = GetOppositeColor(humanPlayerColor)
        chessEngine.moveGenerator.updateLegalMovesFor(chessBoard,humanPlayerColor, false)
        val opponentColor = currentPlayer
        val isOpponentKingInCheck = chessEngine.moveGenerator.isKingInCheck(chessBoard,opponentColor)
        if (isOpponentKingInCheck) {
            drawing.kingInCheck = chessEngine.moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        chessEngine.moveGenerator.updateLegalMovesFor(chessBoard,opponentColor, isOpponentKingInCheck)
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
        //computerAi.cancel(true)
        chessBoard.move(computerMove)
        val color = computerMove.color
        chessEngine.moveGenerator.updateLegalMovesFor(chessBoard,color, false)
        val opponentColor = GetOppositeColor(color)
        val isOpponentKingInCheck = chessEngine.moveGenerator.isKingInCheck(chessBoard,opponentColor)
        if (isOpponentKingInCheck) {
            drawing.kingInCheck = chessEngine.moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        chessEngine.moveGenerator.updateLegalMovesFor(chessBoard,opponentColor, isOpponentKingInCheck)
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
        //computerAi = ChessEngine()
        val engineThread = object : Thread() {
            override fun run() {
                val move = chessEngine.getMove(ChessBoard(chessBoard))
                computerPlayed(move)
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