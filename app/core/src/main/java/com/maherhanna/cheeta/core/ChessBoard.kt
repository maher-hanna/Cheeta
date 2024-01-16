package com.maherhanna.cheeta.core

import com.maherhanna.cheeta.core.MoveGenerator.Companion.getInitialRookKingSide
import com.maherhanna.cheeta.core.MoveGenerator.Companion.getInitialRookQueenSide
import com.maherhanna.cheeta.core.Piece.Companion.GetOppositeColor
import java.util.Scanner

class ChessBoard {

    //----------------------------------------------------------------------------------
    //data
    //-----------------------------------------------------------------------------------
    var moves: GameHistory = GameHistory()
    var states: ArrayList<State> = ArrayList()
    var whitePawns: Long = 0
    var whiteRooks: Long = 0
    var whiteBishops: Long = 0
    var whiteKnights: Long = 0
    var whiteQueens: Long = 0
    var whiteKing: Long = 0
    var blackPawns: Long = 0
    var blackRooks: Long = 0
    var blackBishops: Long = 0
    var blackKnights: Long = 0
    var blackQueens: Long = 0
    var blackKing: Long = 0
    var allWhitePieces: Long = 0
    var allBlackPieces: Long = 0
    var emptySquares: Long = 0
    var allPieces: Long = 0

    //state
    @JvmField
    var toPlayColor = Piece.WHITE
    var fiftyMovesDrawCount = 0
    var fullMovesCount = 1

    //---------------------------------------------------------------------------------
    constructor(copy: ChessBoard?)  {
        if (copy != null) {
            moves = GameHistory(copy.moves)
            states = ArrayList(copy.states)
            whitePawns = copy.whitePawns
            whiteRooks = copy.whiteRooks
            whiteBishops = copy.whiteBishops
            whiteKnights = copy.whiteKnights
            whiteQueens = copy.whiteQueens
            whiteKing = copy.whiteKing
            blackPawns = copy.blackPawns
            blackRooks = copy.blackRooks
            blackBishops = copy.blackBishops
            blackKnights = copy.blackKnights
            blackQueens = copy.blackQueens
            blackKing = copy.blackKing
            allWhitePieces = copy.allWhitePieces
            allBlackPieces = copy.allBlackPieces
            emptySquares = copy.emptySquares
            allPieces = copy.allPieces
            toPlayColor = copy.toPlayColor
            fiftyMovesDrawCount = copy.fiftyMovesDrawCount
            fullMovesCount = copy.fullMovesCount
        }

    }
    constructor(fenString: String){
        setupFromFen(fenString)

    }


    fun setupFromFen(fenString: String) {
        var currentFile = FILE_A
        var currentRank = RANK_8
        var currentChar = 0
        var blackCastlingRights = 0
        var whiteCastlingRights = 0
        var enPassantTarget = NO_SQUARE


        //parse piece placement
        //------------------------------------------------------------------------------
        while (currentRank >= RANK_1) {
            when (fenString[currentChar]) {
                'r' -> {
                    addBlackRook(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'p' -> {
                    addBlackPawn(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'b' -> {
                    addBlackBishop(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'n' -> {
                    addBlackKnight(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'q' -> {
                    addBlackQueen(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'k' -> {
                    addBlackKing(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'R' -> {
                    addWhiteRook(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'P' -> {
                    addWhitePawn(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'B' -> {
                    addWhiteBishop(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'N' -> {
                    addWhiteKnight(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'Q' -> {
                    addWhiteQueen(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                'K' -> {
                    addWhiteKing(GetPosition(currentFile, currentRank))
                    currentFile++
                }

                '1' -> currentFile += 1
                '2' -> currentFile += 2
                '3' -> currentFile += 3
                '4' -> currentFile += 4
                '5' -> currentFile += 5
                '6' -> currentFile += 6
                '7' -> currentFile += 7
                '8' -> currentFile += 8
            }
            currentChar++
            if (currentFile > FILE_H) {
                currentFile = FILE_A
                currentRank--
            }
        }
        //***************************************************************************

        //skip space
        currentChar++


        //parse active color
        toPlayColor = if (fenString[currentChar] == 'w') Piece.WHITE else Piece.BLACK
        currentChar++


        //skip space
        currentChar++

        //parse castling rights
        //-----------------------------------------------------------------------------------
        while (fenString[currentChar] != ' ') {
            when (fenString[currentChar]) {
                '-' -> {
                    whiteCastlingRights = NO_CASTLING
                    blackCastlingRights = NO_CASTLING
                    currentChar++
                }

                'K' -> {
                    whiteCastlingRights = whiteCastlingRights or CASTLING_KING_SIDE
                    currentChar++
                }

                'Q' -> {
                    whiteCastlingRights = whiteCastlingRights or CASTLING_QUEEN_SIDE
                    currentChar++
                }

                'k' -> {
                    blackCastlingRights = blackCastlingRights or CASTLING_KING_SIDE
                    currentChar++
                }

                'q' -> {
                    blackCastlingRights = blackCastlingRights or CASTLING_QUEEN_SIDE
                    currentChar++
                }
            }
        }
        //--------------------------------------------------------------------------------

        //skip space
        currentChar++


        //parse En passant target square
        if (fenString[currentChar] == '-') enPassantTarget = NO_SQUARE
        enPassantTarget = Square(fenString.substring(currentChar, currentChar + 2))
        currentChar++

        //skip space
        currentChar++
        val scanner = Scanner(fenString.substring(currentChar))

        //parse number of half moves since last pawn or capture occured
        fiftyMovesDrawCount = scanner.nextInt()

        //parse full move count since start of game
        fullMovesCount = scanner.nextInt()
        val startState = State(allPieces, enPassantTarget, blackCastlingRights, whiteCastlingRights)
        states.add(startState)

    }

    private fun addBlackPawn(square: Int) {
        blackPawns = blackPawns or (1L shl square)
        allBlackPieces = allBlackPieces or blackPawns
        allPieces = allPieces or blackPawns
        emptySquares = allPieces.inv()
    }

    private fun addBlackRook(square: Int) {
        blackRooks = blackRooks or (1L shl square)
        allBlackPieces = allBlackPieces or blackRooks
        allPieces = allPieces or blackRooks
        emptySquares = allPieces.inv()
    }

    private fun addBlackBishop(square: Int) {
        blackBishops = blackBishops or (1L shl square)
        allBlackPieces = allBlackPieces or blackBishops
        allPieces = allPieces or blackBishops
        emptySquares = allPieces.inv()
    }

    private fun addBlackKnight(square: Int) {
        blackKnights = blackKnights or (1L shl square)
        allBlackPieces = allBlackPieces or blackKnights
        allPieces = allPieces or blackKnights
        emptySquares = allPieces.inv()
    }

    private fun addBlackQueen(square: Int) {
        blackQueens = blackQueens or (1L shl square)
        allBlackPieces = allBlackPieces or blackQueens
        allPieces = allPieces or blackQueens
        emptySquares = allPieces.inv()
    }

    private fun addBlackKing(square: Int) {
        blackKing = blackKing or (1L shl square)
        allBlackPieces = allBlackPieces or blackKing
        allPieces = allPieces or blackKing
        emptySquares = allPieces.inv()
    }

    private fun addWhitePawn(square: Int) {
        whitePawns = whitePawns or (1L shl square)
        allWhitePieces = allWhitePieces or whitePawns
        allPieces = allPieces or whitePawns
        emptySquares = allPieces.inv()
    }

    private fun addWhiteRook(square: Int) {
        whiteRooks = whiteRooks or (1L shl square)
        allWhitePieces = allWhitePieces or whiteRooks
        allPieces = allPieces or whiteRooks
        emptySquares = allPieces.inv()
    }

    private fun addWhiteBishop(square: Int) {
        whiteBishops = whiteBishops or (1L shl square)
        allWhitePieces = allWhitePieces or whiteBishops
        allPieces = allPieces or whiteBishops
        emptySquares = allPieces.inv()
    }

    private fun addWhiteKnight(square: Int) {
        whiteKnights = whiteKnights or (1L shl square)
        allWhitePieces = allWhitePieces or whiteKnights
        allPieces = allPieces or whiteKnights
        emptySquares = allPieces.inv()
    }

    private fun addWhiteQueen(square: Int) {
        whiteQueens = whiteQueens or (1L shl square)
        allWhitePieces = allWhitePieces or whiteQueens
        allPieces = allPieces or whiteQueens
        emptySquares = allPieces.inv()
    }

    private fun addWhiteKing(square: Int) {
        whiteKing = whiteKing or (1L shl square)
        allWhitePieces = allWhitePieces or whiteKing
        allPieces = allPieces or whiteKing
        emptySquares = allPieces.inv()
    }

    fun removePiece(square: Int) {
        allPieces = BitMath.popBit(allPieces, square)
        allWhitePieces = allPieces and allWhitePieces
        allBlackPieces = allPieces and allBlackPieces
        whitePawns = allPieces and whitePawns
        whiteRooks = allPieces and whiteRooks
        whiteKnights = allPieces and whiteKnights
        whiteBishops = allPieces and whiteBishops
        whiteQueens = allPieces and whiteQueens
        whiteKing = allPieces and whiteKing
        blackPawns = allPieces and blackPawns
        blackRooks = allPieces and blackRooks
        blackKnights = allPieces and blackKnights
        blackBishops = allPieces and blackBishops
        blackQueens = allPieces and blackQueens
        blackKing = allPieces and blackKing
        emptySquares = allPieces.inv()
    }


    val blackPositions: ArrayList<Int>
        get() {
            val blackPositions = ArrayList<Int>()
            for (i in MIN_POSITION..MAX_POSITION) {
                if (isSquareEmpty(i)) continue
                if (isPieceBlackAt(i)) blackPositions.add(i)
            }
            return blackPositions
        }
    val whitePositions: ArrayList<Int>
        get() {
            val whitePositions = ArrayList<Int>()
            for (i in MIN_POSITION..MAX_POSITION) {
                if (isSquareEmpty(i)) continue
                if (isPieceWhiteAt(i)) whitePositions.add(i)
            }
            return whitePositions
        }


    fun move(move: Move) {
        val fromSquare = move.from
        val toSquare = move.to
        val moveColor = move.color
        val movedPieceType = move.pieceType
        setPieceAt(toSquare, move.pieceType, moveColor)
        removePiece(fromSquare)
        if (moveColor == Piece.WHITE) fullMovesCount++

        //set enPassant target
        var enPassantTargetAfterMove = NO_SQUARE
        if (move.isPawnDoubleMove) {
            enPassantTargetAfterMove = if (moveColor == Piece.WHITE) {
                move.to - 8
            } else {
                move.to + 8
            }
        }
        var blackCastlingRightsAfterMove = blackCastlingRights
        var whiteCastlingRightsAfterMove = whiteCastlingRights
        if (move.isCastling) {
            val rookPosition: Int
            val rookCastlingTarget: Int
            if (moveColor == Piece.WHITE) {
                whiteCastlingRightsAfterMove = NO_CASTLING
            } else {
                blackCastlingRightsAfterMove = NO_CASTLING
            }
            if (move.castlingType === Move.CastlingType.CASTLING_KING_SIDE) {
                rookPosition = getInitialRookKingSide(
                    moveColor
                )
                rookCastlingTarget = move.from + 1
            } else {
                rookPosition = getInitialRookQueenSide(
                    moveColor
                )
                rookCastlingTarget = move.from - 1
            }
            setPieceAt(rookCastlingTarget, Piece.ROOK, moveColor)
            removePiece(rookPosition)
        } else {
            if (movedPieceType == Piece.ROOK) {
                if (moveColor == Piece.WHITE) {
                    whiteCastlingRightsAfterMove = if (fromSquare == getInitialRookKingSide(Piece.WHITE)) {
                        //king side
                        BitMath.unSetBit(whiteCastlingRightsAfterMove, 0)
                    } else {
                        //queen side
                        BitMath.unSetBit(whiteCastlingRightsAfterMove, 1)
                    }
                } else {
                    blackCastlingRightsAfterMove = if (fromSquare == getInitialRookKingSide(Piece.BLACK)) {
                        //king side
                        BitMath.unSetBit(blackCastlingRightsAfterMove, 0)
                    } else {
                        //queen side
                        BitMath.unSetBit(blackCastlingRightsAfterMove, 1)
                    }
                }
            }
            if (movedPieceType == Piece.KING) {
                if (moveColor == Piece.WHITE) {
                    whiteCastlingRightsAfterMove = NO_CASTLING
                } else {
                    blackCastlingRightsAfterMove = NO_CASTLING
                }
            }
        }
        if (move.isPromote) {
            setPieceAt(toSquare, move.promotionPieceType, moveColor)
        }
        if (move.isEnPasant) {
            if (moveColor == Piece.WHITE) {
                removePiece(toSquare - 8)
            } else {
                removePiece(toSquare + 8)
            }
        }
        move.previousFiftyMoves = fiftyMovesDrawCount
        //increase fifty moves if no capture or pawn push
        fiftyMovesDrawCount++
        if (move.isTake || movedPieceType == Piece.PAWN) {
            fiftyMovesDrawCount = 0
        }
        val state = State(allPieces, enPassantTargetAfterMove, blackCastlingRightsAfterMove, whiteCastlingRightsAfterMove)
        states.add(state)
        moves.add(move)
        toPlayColor = if (toPlayColor == Piece.WHITE) Piece.BLACK else Piece.WHITE
    }

    fun unMove() {
        val lastMove = moves.lastMove ?: return
        val fromSquare = lastMove.from
        val toSquare = lastMove.to
        val moveColor = lastMove.color
        val movedPieceType = lastMove.pieceType
        setPieceAt(fromSquare, movedPieceType, moveColor)
        removePiece(toSquare)
        if (lastMove.isTake && !lastMove.isEnPasant) {
            setPieceAt(toSquare, lastMove.takenPieceType, GetOppositeColor(moveColor))
        }
        if (lastMove.isPromote) {
            setPieceAt(fromSquare, Piece.PAWN, moveColor)
        }
        if (moveColor == Piece.WHITE) {
            fullMovesCount--
        }
        if (lastMove.isCastling) {
            val rookPosition: Int
            val currentRookPosition: Int
            if (lastMove.castlingType === Move.CastlingType.CASTLING_KING_SIDE) {
                rookPosition = getInitialRookKingSide(
                    moveColor
                )
                currentRookPosition = lastMove.from + 1
            } else {
                rookPosition = getInitialRookQueenSide(
                    moveColor
                )
                currentRookPosition = lastMove.from - 1
            }
            setPieceAt(rookPosition, Piece.ROOK, moveColor)
            removePiece(currentRookPosition)
        }
        if (lastMove.isEnPasant) {
            if (moveColor == Piece.WHITE) {
                setPieceAt(toSquare - 8, Piece.PAWN, Piece.BLACK)
            } else {
                setPieceAt(toSquare + 8, Piece.PAWN, Piece.WHITE)
            }
        }
        toPlayColor = if (toPlayColor == Piece.WHITE) Piece.BLACK else Piece.WHITE


        //restore previous fifty moves count
        fiftyMovesDrawCount = lastMove.previousFiftyMoves
        states.removeAt(states.size - 1)
        moves.removeLastMove()
    }

    fun unMove(numberOfSteps: Int) {
        for (i in 0 until numberOfSteps) {
            unMove()
        }
    }

    //get and set a square info
    fun setPieceAt(position: Int, pieceType: Int, pieceColor: Int) {
        removePiece(position)
        if (pieceColor == Piece.WHITE) {
            when (pieceType) {
                Piece.PAWN -> addWhitePawn(position)
                Piece.ROOK -> addWhiteRook(position)
                Piece.KNIGHT -> addWhiteKnight(position)
                Piece.BISHOP -> addWhiteBishop(position)
                Piece.QUEEN -> addWhiteQueen(position)
                Piece.KING -> addWhiteKing(position)
            }
        } else {
            when (pieceType) {
                Piece.PAWN -> addBlackPawn(position)
                Piece.ROOK -> addBlackRook(position)
                Piece.KNIGHT -> addBlackKnight(position)
                Piece.BISHOP -> addBlackBishop(position)
                Piece.QUEEN -> addBlackQueen(position)
                Piece.KING -> addBlackKing(position)
            }
        }
    }


    val allPiecesCount: Int
        get() = BitMath.countSetBits(allPieces)
    val whitePiecesCount: Int
        get() = BitMath.countSetBits(allWhitePieces)
    val blackPiecesCount: Int
        get() = BitMath.countSetBits(allBlackPieces)
    val whiteBishopsCount: Int
        get() = BitMath.countSetBits(whiteBishops)
    val blackBishopsCount: Int
        get() = BitMath.countSetBits(blackBishops)

    fun insufficientMaterial(): Boolean {
        val allPiecesCount = allPiecesCount


        // tow kings remaining
        if (allPiecesCount == 2) {
            return true
        }
        if (allPiecesCount == 3) {

            // tow kings and a bishop or knight
            if (whiteBishops or blackBishops or whiteKnights or blackKnights != 0L) {
                return true
            }
        }
        if (allPiecesCount == 4) {
            if (whiteBishopsCount == 1 && blackBishopsCount == 1) {
                val whiteBishopPosition = BitMath.getPositionsOf(whiteBishops)[0]
                val blackBishopPosition = BitMath.getPositionsOf(blackBishops)[0]
                // tow king and tow bishops of the same square color
                return GetSquareColor(whiteBishopPosition) ==
                        GetSquareColor(blackBishopPosition)
            }
        }
        return false
    }

    fun removeKing(kingToRemoveColor: Int) {
        if (kingToRemoveColor == Piece.WHITE) {
            val whiteKingMask = whiteKing.inv()
            allWhitePieces = allWhitePieces and whiteKingMask
            allPieces = allPieces and whiteKingMask
            emptySquares = allPieces.inv()
            whiteKing = 0
        } else {
            val blackKingMask = blackKing.inv()
            allBlackPieces = allBlackPieces and blackKingMask
            allPieces = allPieces and blackKingMask
            emptySquares = allPieces.inv()
            blackKing = 0
        }
    }

    fun isSquareEmpty(position: Int): Boolean {
        return BitMath.isBitSet(emptySquares, position.toLong())
    }

    fun isPieceBlackAt(position: Int): Boolean {
        return BitMath.isBitSet(allBlackPieces, position.toLong())
    }

    fun isPieceWhiteAt(position: Int): Boolean {
        return BitMath.isBitSet(allWhitePieces, position.toLong())
    }

    fun pieceColor(position: Int): Int {
        return (allBlackPieces and (1L shl position) ushr position).toInt()
    }

    fun pieceType(position: Int): Int {
        val positionBit = 1L shl position
        val isPawn = whitePawns or blackPawns and positionBit ushr position
        val isRook = whiteRooks or blackRooks and positionBit ushr position
        val isKnight = whiteKnights or blackKnights and positionBit ushr position
        val isBishop = whiteBishops or blackBishops and positionBit ushr position
        val isQueen = whiteQueens or blackQueens and positionBit ushr position
        val isKing = whiteKing or blackKing and positionBit ushr position
        return (isPawn * Piece.PAWN + isRook * Piece.ROOK + isKnight * Piece.KNIGHT + isBishop * Piece.BISHOP + isQueen * Piece.QUEEN + isKing * Piece.KING).toInt()
    }

    //------------------------
    fun print():String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(" \n")
        val blackPiecesSymbols = charArrayOf('p', 'n', 'b','r',  'q', 'k')
        val whitePiecesSymbols = charArrayOf('P',  'N', 'B','R', 'Q', 'K')
        var currentSymbol = '0'
        for (rank in 7 downTo 0) {
            for (file in 0..7) {
                currentSymbol = '0'
                val pieceType = pieceType(Square(file, rank))
                val pieceColor = pieceColor(Square(file, rank))
                val charIndex = when(pieceType){
                    Piece.PAWN -> 0
                    Piece.KNIGHT -> 1
                    Piece.BISHOP -> 2
                    Piece.ROOK -> 3
                    Piece.QUEEN -> 4
                    Piece.KING -> 5
                    else -> -1
                }
                if(charIndex != -1) {
                    if (pieceColor == Piece.WHITE) currentSymbol = whitePiecesSymbols[charIndex]
                    if (pieceColor == Piece.BLACK) currentSymbol = blackPiecesSymbols[charIndex]
                }
                stringBuilder.append(currentSymbol)
                stringBuilder.append(' ')
            }
            stringBuilder.append('\n')
        }
        return stringBuilder.toString()
    }

    val whiteCastlingRights: Int
        get() = states[states.size - 1].whiteCastlingRights
    val blackCastlingRights: Int
        get() = states[states.size - 1].blackCastlingRights
    val enPassantTarget: Int
        get() = states[states.size - 1].enPassantTarget

    companion object {
        //public constants
        const val startPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 "
        const val trickyPosition =
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1 "
        const val killerPosition =
            "rnbqkb1r/pp1p1pPp/8/2p1pP2/1P1P4/3P3P/P1P1P3/RNBQKBNR w KQkq e6 0 1"
        const val cmkPosition =
            "r2q1rk1/ppp2ppp/2n1bn2/2b1p3/3pP3/3P1NPP/PPP1NPB1/R1BQ1RK1 b - - 0 9 "
        const val KiwipetePosition =
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"
        const val positionInUse = startPosition

        //-------------------------------------------------------------------------------
        const val MIN_POSITION = 0
        const val MAX_POSITION = 63
        const val OUT = -1
        const val NO_SQUARE = -1
        const val EMPTY = -1
        const val RANK_1 = 0
        const val RANK_2 = 1
        const val RANK_3 = 2
        const val RANK_4 = 3
        const val RANK_5 = 4
        const val RANK_6 = 5
        const val RANK_7 = 6
        const val RANK_8 = 7
        const val FILE_A = 0
        const val FILE_B = 1
        const val FILE_C = 2
        const val FILE_D = 3
        const val FILE_E = 4
        const val FILE_F = 5
        const val FILE_G = 6
        const val FILE_H = 7
        const val NO_CASTLING = 0
        const val CASTLING_KING_SIDE = 1
        const val CASTLING_QUEEN_SIDE = 2
        const val CASTLING_BOTH_SIDES = 3

        @JvmStatic
        fun GetPosition(file: Int, rank: Int): Int {
            if (file < FILE_A || file > FILE_H) return OUT
            return if (rank < RANK_1 || rank > RANK_8) OUT else rank * 8 + file
        }

        fun Square(file: Int, rank: Int): Int {
            return rank * 8 + file
        }

        fun Square(square: String): Int {
            val fileChar = square[0]
            val rankChar = square[1]
            var file = NO_SQUARE
            var rank = NO_SQUARE
            when (fileChar) {
                'a' -> file = FILE_A
                'b' -> file = FILE_B
                'c' -> file = FILE_C
                'd' -> file = FILE_D
                'e' -> file = FILE_E
                'f' -> file = FILE_F
                'g' -> file = FILE_G
                'h' -> file = FILE_H
            }
            when (rankChar) {
                '1' -> rank = RANK_1
                '2' -> rank = RANK_2
                '3' -> rank = RANK_3
                '4' -> rank = RANK_4
                '5' -> rank = RANK_5
                '6' -> rank = RANK_6
                '7' -> rank = RANK_7
                '8' -> rank = RANK_8
            }
            return GetPosition(file, rank)
        }

        @JvmStatic
        fun GetFile(position: Int): Int {
            return position % 8
        }

        @JvmStatic
        fun GetRank(position: Int): Int {
            return position / 8
        }

        fun offset(square: Int, file: Int, rank: Int): Int {
            val newFile = GetFile(square) + file
            val newRank = GetRank(square) + rank
            if (newFile < FILE_A || newFile > FILE_H) return OUT
            return if (newRank < RANK_1 || newRank > RANK_8) OUT else newRank * 8 + newFile
        }

        fun offsetFile(square: Int, file: Int): Int {
            return offset(square, file, 0)
        }

        fun offsetRank(square: Int, rank: Int): Int {
            return offset(square, 0, rank)
        }

        fun GetSquareColor(square: Int): Int {
            return if (square % 2 == 0) {
                Piece.BLACK
            } else {
                Piece.WHITE
            }
        }

        fun printBitboard(bitboard: Long) {
            val stringBuilder = StringBuilder()
            stringBuilder.append(" \n")
            var currentSymbol = '0'
            for (rank in 7 downTo 0) {
                for (file in 0..7) {
                    currentSymbol = '0'
                    if (BitMath.getBit(bitboard, Square(file, rank)) == 1) currentSymbol = '1'
                    stringBuilder.append(currentSymbol)
                    stringBuilder.append(' ')
                }
                stringBuilder.append('\n')
            }
        }
    }
}
