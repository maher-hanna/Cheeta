package com.maherhanna.cheeta

import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.GameStatus
import com.maherhanna.cheeta.core.Move
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Piece.Companion.GetOppositeColor

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
        chessBoard = ChessBoard(ChessBoard.positionInUse, moveGenerator)
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
        chessBoard.updateLegalMovesFor(moveGenerator,humanPlayerColor, false)
        val opponentColor = currentPlayer
        val isOpponentKingInCheck = chessBoard.isKingInCheck(opponentColor)
        if (isOpponentKingInCheck) {
            drawing.kingInCheck = moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        chessBoard.updateLegalMovesFor(moveGenerator,opponentColor, isOpponentKingInCheck)
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
        chessBoard.updateLegalMovesFor(moveGenerator,color, false)
        val opponentColor = GetOppositeColor(color)
        val isOpponentKingInCheck = chessBoard.isKingInCheck(opponentColor)
        if (isOpponentKingInCheck) {
            drawing.kingInCheck = moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        chessBoard.updateLegalMovesFor(moveGenerator,opponentColor, isOpponentKingInCheck)
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

    private inner class ComputerAi : ChessEngine() {
        override fun onPostExecute(move: Move) {
            computerPlayed(move)
        }
    }

    companion object {
        const val DEBUG = "Cheeta_Debug"

        const val COMPUTER_MAX_SEARCH_TIME: Long = 4
        @JvmField
        var moveGenerator = MoveGenerator()
    }
}