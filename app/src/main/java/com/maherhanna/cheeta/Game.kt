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
    private var chessEngine: ChessEngine
    @JvmField
    var paused: Boolean
    var currentPlayer = 0
    @JvmField
    var humanPlayerColor: Int
    var isGameFinished = false
        private set

    init {
        chessEngine = ChessEngine()
        paused = false
        this.humanPlayerColor = humanPlayerColor
        chessBoard = ChessBoard(ChessBoard.positionInUse)
        moveGenerator.updateWhiteLegalMoves(chessBoard)
        moveGenerator.updateBlackLegalMoves(chessBoard)
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
        return checkStatus(chessBoard,moveGenerator.getLegalMovesFor(GetOppositeColor(lastPlayed)))
    }

    fun humanPlayed(humanMove: Move?) {
        var humanMove = humanMove
        humanMove = moveGenerator.getLegalMovesFor(humanPlayerColor).getMove(humanMove!!)
        chessBoard.move(humanMove)
        currentPlayer = GetOppositeColor(humanPlayerColor)
        moveGenerator.updateLegalMovesFor(chessBoard,humanPlayerColor, false)
        val opponentColor = currentPlayer
        val isOpponentKingInCheck = moveGenerator.isKingInCheck(chessBoard,opponentColor)
        if (isOpponentKingInCheck) {
            drawing.kingInCheck = moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        moveGenerator.updateLegalMovesFor(chessBoard,opponentColor, isOpponentKingInCheck)
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
        moveGenerator.updateLegalMovesFor(chessBoard,color, false)
        val opponentColor = GetOppositeColor(color)
        val isOpponentKingInCheck = moveGenerator.isKingInCheck(chessBoard,opponentColor)
        if (isOpponentKingInCheck) {
            drawing.kingInCheck = moveGenerator.getKingPosition(chessBoard, opponentColor)
        } else {
            drawing.kingInCheck = ChessBoard.NO_SQUARE
        }
        drawing.drawAllPieces()
        moveGenerator.updateLegalMovesFor(chessBoard,opponentColor, isOpponentKingInCheck)
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
                computerPlayed(chessEngine.getMove(chessBoard))
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

    companion object {
        const val DEBUG = "Cheeta_Debug"

        const val COMPUTER_MAX_SEARCH_TIME: Long = 4
        @JvmField
        var moveGenerator = MoveGenerator(PlayerLegalMoves(), PlayerLegalMoves())
        fun checkStatus(chessBoard: ChessBoard,toPlayPlayerLegalMoves: PlayerLegalMoves): GameStatus {
            val lastPlayed = chessBoard.moves.lastPlayed
            var gameStatus = GameStatus.NOT_FINISHED
            val currentToPlayColor = GetOppositeColor(lastPlayed)
            if (toPlayPlayerLegalMoves.size() == 0) {
                val isKingInCheck = moveGenerator.isKingAttacked(chessBoard, currentToPlayColor)
                gameStatus = if (isKingInCheck) {
                    //win
                    if (lastPlayed == Piece.WHITE) {
                        GameStatus.FINISHED_WIN_WHITE
                    } else {
                        GameStatus.FINISHED_WIN_BLACK
                    }
                } else {
                    //draw stalemate
                    GameStatus.FINISHED_DRAW
                }
            }
            if (chessBoard.insufficientMaterial()) {
                gameStatus = GameStatus.FINISHED_DRAW
                return gameStatus
            }
            if (chessBoard.fiftyMovesDrawCount == 50) {
                gameStatus = GameStatus.FINISHED_DRAW
                return gameStatus
            }

            //check for third repetition draw
            var repeatedPositionCount = 1
            val lastState = chessBoard.states[chessBoard.states.size - 1]
            for (i in 0 until chessBoard.states.size - 1) {
                if (lastState.equals(chessBoard.states[i])) {
                    repeatedPositionCount++
                    if (repeatedPositionCount == 3) {
                        gameStatus = GameStatus.FINISHED_DRAW
                        break
                    }
                }
            }
            return gameStatus
        }

    }
}