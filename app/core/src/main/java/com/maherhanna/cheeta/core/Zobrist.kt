package com.maherhanna.cheeta.core

/**
 * Zobrist hashing keys, shared by all boards so hashes are comparable everywhere.
 * The board keeps its hash up to date incrementally (see ChessBoard); [hash] computes it
 * from scratch and is used for setup and for checking the incremental value in tests.
 */
object Zobrist {
    // SplitMix64 with a fixed seed: deterministic, good quality and available on all
    // Android versions (java.util.SplittableRandom needs API 24)
    private var seed = 0x5EEDC4EE7A5EEDL

    private fun next(): Long {
        seed += -0x61c8864680b583ebL
        var z = seed
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
        return z xor (z ushr 31)
    }

    // index: (color * 6 + pieceType - 1) * 64 + square
    private val pieces = LongArray(2 * 6 * 64) { next() }
    // index: whiteCastlingRights or (blackCastlingRights shl 2)
    private val castling = LongArray(16) { next() }
    private val enPassantFile = LongArray(8) { next() }
    private val blackToMove = next()

    fun piece(color: Int, pieceType: Int, square: Int): Long =
        pieces[(color * 6 + pieceType - 1) * 64 + square]

    /** Hash of the pieces only, computed from the piece bitboards. */
    fun piecesHash(board: ChessBoard): Long {
        var hash = 0L
        val bitboards = longArrayOf(
            board.whitePawns, board.whiteKnights, board.whiteBishops,
            board.whiteRooks, board.whiteQueens, board.whiteKing,
            board.blackPawns, board.blackKnights, board.blackBishops,
            board.blackRooks, board.blackQueens, board.blackKing
        )
        val types = intArrayOf(
            Piece.PAWN, Piece.KNIGHT, Piece.BISHOP, Piece.ROOK, Piece.QUEEN, Piece.KING
        )
        for (i in bitboards.indices) {
            val color = if (i < 6) Piece.WHITE else Piece.BLACK
            var remaining = bitboards[i]
            while (remaining != 0L) {
                val square = java.lang.Long.numberOfTrailingZeros(remaining)
                remaining = remaining and (remaining - 1)
                hash = hash xor piece(color, types[i % 6], square)
            }
        }
        return hash
    }

    /** Everything except the pieces: side to move, castling rights and en passant. */
    fun stateHash(
        board: ChessBoard,
        sideToMove: Int,
        whiteCastlingRights: Int,
        blackCastlingRights: Int,
        enPassantTarget: Int
    ): Long {
        var hash = castling[whiteCastlingRights or (blackCastlingRights shl 2)]
        if (sideToMove == Piece.BLACK) hash = hash xor blackToMove
        if (canCaptureEnPassant(board, sideToMove, enPassantTarget)) {
            hash = hash xor enPassantFile[ChessBoard.GetFile(enPassantTarget)]
        }
        return hash
    }

    /** Full hash of the board's current position, computed from scratch. */
    fun hash(board: ChessBoard): Long = piecesHash(board) xor stateHash(
        board,
        board.toPlayColor,
        board.whiteCastlingRights,
        board.blackCastlingRights,
        board.enPassantTarget
    )

    // the en passant square only changes the position if a pawn can actually capture
    // there; otherwise identical positions would get different hashes
    private fun canCaptureEnPassant(board: ChessBoard, sideToMove: Int, target: Int): Boolean {
        if (target == ChessBoard.NO_SQUARE) return false
        val file = ChessBoard.GetFile(target)
        // capturing pawns stand next to the pawn that just made the double push
        val pawnRank = if (sideToMove == Piece.WHITE) ChessBoard.RANK_5 else ChessBoard.RANK_4
        val pawns = if (sideToMove == Piece.WHITE) board.whitePawns else board.blackPawns
        if (file > ChessBoard.FILE_A && pawns and (1L shl (pawnRank * 8 + file - 1)) != 0L) return true
        if (file < ChessBoard.FILE_H && pawns and (1L shl (pawnRank * 8 + file + 1)) != 0L) return true
        return false
    }
}
