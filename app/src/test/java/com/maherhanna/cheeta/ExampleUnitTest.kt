package com.maherhanna.cheeta

import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.ChessEngine
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Piece
import com.maherhanna.cheeta.core.PlayerLegalMoves
import com.maherhanna.cheeta.core.Uci
import com.maherhanna.cheeta.core.util.Log
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.NoSuchElementException
import java.util.Random


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    val chessBoard = ChessBoard(ChessBoard.positionInUse)
    val moveGenerator = MoveGenerator()

    @Before
    fun setup() {
        chessBoard.toPlayColor = Piece.WHITE
        moveGenerator.updateWhiteLegalMoves(chessBoard)
        moveGenerator.updateBlackLegalMoves(chessBoard)
    }

    @Test
    fun testUciProtocol() {
        val numberOfGames = 4
        var currentGameNumber = 1
        var isCurrentGameFinished: Boolean
        var movesList: String
        var firstEngineWins = 0
        var firstEngineDraws = 0
        var firstEngineLoses = 0
        val uci = Uci()
        try {
            while (currentGameNumber <= numberOfGames) {
                isCurrentGameFinished = false
                movesList = ""
                val random = Random(System.currentTimeMillis())
                val randomPlayerIndex = random.nextInt(2)
                var currentPlayerIndex = randomPlayerIndex
                if(currentPlayerIndex == 0){
                    uci.parseInput("position startpos")
                    val move = uci.parseInput("go infinite")
                    val splits = move.trim().split("\\s+".toRegex())
                    if(splits.size > 1){
                        movesList += splits[1] + " "
                    }
                } else {
                    uci.parseInput("position startpos")
                    val move = uci.parseInput("go infinite")
                    val splits = move.trim().split("\\s+".toRegex())
                    if(splits.size > 1){
                        movesList += splits[1] + " "
                    }
                }
                while (!isCurrentGameFinished){
                    // flip current player index from 0 to 1 and vice versa
                    currentPlayerIndex = currentPlayerIndex xor 1

                    if(currentPlayerIndex == 0){
                        uci.parseInput("position startpos moves $movesList")
                        val move = uci.parseInput("go infinite")
                        val splits = move.trim().split("\\s+".toRegex())
                        if(splits.size > 1){
                            movesList += splits[1] + " "
                        }
                    } else{
                        uci.parseInput("position startpos moves $movesList")
                        val move = uci.parseInput("go infinite")
                        val splits = move.trim().split("\\s+".toRegex())
                        if(splits.size > 1){
                            movesList += splits[1] + " "
                        }
                    }
                    println(movesList)
                    val gameStatusResponse = uci.parseInput("check_status")
                    // game status : 0 not finished, 1 draw, 2 white winds, 3 black wins
                    val statusCode = gameStatusResponse.toInt()
                    isCurrentGameFinished = statusCode != 0
                    if(isCurrentGameFinished){
                        when(statusCode){
                            1 -> firstEngineDraws += 1
                            2 -> {
                                if(randomPlayerIndex == 0){
                                    firstEngineWins += 1
                                }else {
                                    firstEngineLoses += 1
                                }
                            }
                            3 -> {
                                if(randomPlayerIndex == 0){
                                    firstEngineLoses += 1
                                }else {
                                    firstEngineWins += 1
                                }
                            }
                        }
                    }
                }
                currentGameNumber += 1
            }
            println("First engine results compared to second engine are:")
            println("wins: $firstEngineWins draws: $firstEngineDraws loses: $firstEngineLoses")
        } catch (e: IllegalStateException) {
            println("System.in was closed; exiting")
        } catch (e: NoSuchElementException) {
            println("System.in was closed; exiting")

        }
    }

    @Test
    fun moveGeneratorMoves() {

        val perftResults = listOf<ULong>(
            1UL,
            20UL,
            400UL,
            8902UL,
            197281UL,
            4865609UL,
            119060324UL,
            3195901860UL,
            84998978956UL,
            2439530234167UL
        )

        for (i in 1 until 7) {
            val perftResult = Perft(i, chessBoard, moveGenerator)
            if(perftResults[i] != perftResult){
                Log.d(ChessEngine.DEBUG_TAG,"depth: ${i}")
            }
            assertEquals(perftResults[i], perftResult)

        }
    }

    @Test
    fun moveGeneratorCaptures() {
        val perftCaptureResults = listOf<ULong>(
            0UL,
            0UL,
            0UL,
            34UL,
            1576UL,
            82719UL,
            2812008UL,
            108329926UL,
            3523740106UL
        )
        for (i in 1 until 7) {
            val perftResult = PerftCaptures(i, chessBoard, moveGenerator, false)
            assertEquals(perftCaptureResults[i], perftResult)

        }
    }

    @Test
    fun moveGeneratorChecks() {
        val perftChecksResults = listOf<ULong>(
            0UL,
            0UL,
            0UL,
            12UL,
            469UL,
            27351UL,
            809099UL,
            33103848UL,
            968981593UL
        )
        for (i in 1 until 7) {
            val perftResult = PerftChecks(i, chessBoard, moveGenerator, false)
            assertEquals(perftChecksResults[i], perftResult)

        }
    }

    fun Perft(depth: Int, chessBoard: ChessBoard, moveGenerator: MoveGenerator): ULong {
        var nodes = 0UL;

        if (depth == 0)
            return 1UL;

        val move_list = moveGenerator.generateLegalMovesFor(chessBoard, chessBoard.toPlayColor);
        val n_moves = move_list.size()
        for (i in 0 until n_moves) {
            chessBoard.makeMove(move_list[i]);
            nodes += Perft(depth - 1, chessBoard, moveGenerator);
            chessBoard.unMakeMove();
        }
        return nodes;
    }

    fun PerftCaptures(
        depth: Int,
        chessBoard: ChessBoard,
        moveGenerator: MoveGenerator,
        isCapture: Boolean
    ): ULong {
        var nodes = 0UL;

        if (depth == 0) {
            return if (isCapture) 1UL else 0UL

        }

        val move_list = moveGenerator.generateLegalMovesFor(chessBoard, chessBoard.toPlayColor);
        val n_moves = move_list.size()
        for (i in 0 until n_moves) {
            chessBoard.makeMove(move_list[i]);
            nodes += PerftCaptures(depth - 1, chessBoard, moveGenerator, move_list[i].isTake);
            chessBoard.unMakeMove();
        }
        return nodes;
    }

    fun PerftChecks(
        depth: Int,
        chessBoard: ChessBoard,
        moveGenerator: MoveGenerator,
        isCheck: Boolean
    ): ULong {
        var nodes = 0UL;

        if (depth == 0) {
            return if (isCheck) 1UL else 0UL
        }
        val toPlayColor = chessBoard.toPlayColor
        val move_list = moveGenerator.generateLegalMovesFor(chessBoard, toPlayColor);
        val n_moves = move_list.size()
        for (i in 0 until n_moves) {
            chessBoard.makeMove(move_list[i]);
            nodes += PerftChecks(
                depth - 1,
                chessBoard,
                moveGenerator,
                moveGenerator.isKingAttacked(chessBoard,chessBoard.toPlayColor)
            )
            chessBoard.unMakeMove()
        }
        return nodes;
    }
}