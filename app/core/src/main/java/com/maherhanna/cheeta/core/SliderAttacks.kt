package com.maherhanna.cheeta.core

/**
 * Sliding-piece attacks with magic bitboards: the full attack set of a rook, bishop or
 * queen on a square for any occupancy is one multiply, one shift and one table lookup.
 *
 * For each square, the "relevant" occupancy (the squares on its rays, excluding the board
 * edges) is multiplied by a magic number; the top bits of the product index a table of
 * precomputed attack sets. The magic numbers were found offline with a deterministic
 * random search, so only the tables are built at startup (a few milliseconds).
 */
object SliderAttacks {
    private val ROOK_DIRECTIONS = arrayOf(intArrayOf(0, 1), intArrayOf(0, -1), intArrayOf(1, 0), intArrayOf(-1, 0))
    private val BISHOP_DIRECTIONS = arrayOf(intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1))

    private val ROOK_MAGICS = longArrayOf(
            0x2880001080204000UL.toLong(), 0x0040100020004000UL.toLong(), 0x0100081040200100UL.toLong(), 0x0100042008100100UL.toLong(),
            0x030008001211000CUL.toLong(), 0x2200010408100200UL.toLong(), 0x0400241098111012UL.toLong(), 0x01000081000B422AUL.toLong(),
            0x104C800540009020UL.toLong(), 0x4444808040002000UL.toLong(), 0x0400801000802000UL.toLong(), 0x0040801000080080UL.toLong(),
            0x0403000801001004UL.toLong(), 0x0000800200040080UL.toLong(), 0x200C000221040810UL.toLong(), 0x0030800100004080UL.toLong(),
            0x8480848001644004UL.toLong(), 0x0D21020040220081UL.toLong(), 0x8000848020021000UL.toLong(), 0x8001848008001000UL.toLong(),
            0x0008010008050011UL.toLong(), 0x2010808002000400UL.toLong(), 0x9100040001100802UL.toLong(), 0x4819020000804401UL.toLong(),
            0x048006484001A000UL.toLong(), 0x1840048500204500UL.toLong(), 0x0005011100200340UL.toLong(), 0x0850220200401008UL.toLong(),
            0x0210080080040080UL.toLong(), 0x4800040080020080UL.toLong(), 0x00A3010400080210UL.toLong(), 0x4000105200140881UL.toLong(),
            0x1080002004400042UL.toLong(), 0x081C401000402000UL.toLong(), 0x0000200101004010UL.toLong(), 0x0002089003002100UL.toLong(),
            0x0029000801000411UL.toLong(), 0x8002002004040010UL.toLong(), 0x0000020001010004UL.toLong(), 0x0000008506000444UL.toLong(),
            0x0200800040048020UL.toLong(), 0x0040220100860044UL.toLong(), 0x8080412001050014UL.toLong(), 0x0100100100210008UL.toLong(),
            0x4000080005010010UL.toLong(), 0x0001104004080120UL.toLong(), 0x9502501168040042UL.toLong(), 0x0001188404460021UL.toLong(),
            0x0113800061084100UL.toLong(), 0x6800400020008480UL.toLong(), 0x401A100081200280UL.toLong(), 0x000A900488008080UL.toLong(),
            0x0200040008008080UL.toLong(), 0x00C0800200040080UL.toLong(), 0x4084822110080400UL.toLong(), 0x0020005400812200UL.toLong(),
            0x2062150040800061UL.toLong(), 0x8004410016812202UL.toLong(), 0x0008200008104101UL.toLong(), 0x4101002019C41001UL.toLong(),
            0x0003001004280017UL.toLong(), 0x1905004882040001UL.toLong(), 0x000A000800840142UL.toLong(), 0x2300004C24008102UL.toLong()
    )

    private val BISHOP_MAGICS = longArrayOf(
            0xA008900410404A00UL.toLong(), 0x0004910214010006UL.toLong(), 0x009104008A004000UL.toLong(), 0x0C84410420000000UL.toLong(),
            0x0004042001260C41UL.toLong(), 0x2018444220018014UL.toLong(), 0x0102081124900044UL.toLong(), 0x0118140888141000UL.toLong(),
            0x441240A208810301UL.toLong(), 0x4000041044004084UL.toLong(), 0x4040043832084000UL.toLong(), 0x0080080483008000UL.toLong(),
            0x040101104040C400UL.toLong(), 0x0010020822090000UL.toLong(), 0x00000D0090108900UL.toLong(), 0x0000245208040200UL.toLong(),
            0x0040000850045080UL.toLong(), 0x00A0290202840100UL.toLong(), 0x00064A8804840180UL.toLong(), 0x10AC01A801485000UL.toLong(),
            0x0001052820080080UL.toLong(), 0x0003001203008220UL.toLong(), 0x4010805110D01000UL.toLong(), 0x1004440202108400UL.toLong(),
            0x440440011010C148UL.toLong(), 0x4201080010028801UL.toLong(), 0xA848080004202020UL.toLong(), 0x2104040000401080UL.toLong(),
            0x0101001003024001UL.toLong(), 0x9020420010411020UL.toLong(), 0x200900601C020820UL.toLong(), 0x0201002831040140UL.toLong(),
            0x0310101000090200UL.toLong(), 0x0812300222900201UL.toLong(), 0x0800109011020400UL.toLong(), 0x0341400820C20200UL.toLong(),
            0x0C08004240040100UL.toLong(), 0x0010020082880880UL.toLong(), 0x0AA4029680022800UL.toLong(), 0x204420A200002100UL.toLong(),
            0x084C100252100840UL.toLong(), 0x240108B024145000UL.toLong(), 0x0142501804002800UL.toLong(), 0x0084084010400206UL.toLong(),
            0x080008100900B020UL.toLong(), 0xC040008804850040UL.toLong(), 0x0005142802080840UL.toLong(), 0x504102008100020AUL.toLong(),
            0x4811082210061000UL.toLong(), 0x0002004114108200UL.toLong(), 0x4009008401210000UL.toLong(), 0x4000000241108161UL.toLong(),
            0x4260001102020001UL.toLong(), 0x8002881044082022UL.toLong(), 0x00E00A0226042000UL.toLong(), 0x2120420202042080UL.toLong(),
            0x0900472208024020UL.toLong(), 0x0A00008051101024UL.toLong(), 0x0020444600A40400UL.toLong(), 0x0492272111084820UL.toLong(),
            0x840800004005010DUL.toLong(), 0x0084100420248100UL.toLong(), 0x20002288100A9085UL.toLong(), 0x0008A0080C504083UL.toLong()
    )

    private val rookMasks = LongArray(64)
    private val rookShifts = IntArray(64)
    private val rookTables = arrayOfNulls<LongArray>(64)
    private val bishopMasks = LongArray(64)
    private val bishopShifts = IntArray(64)
    private val bishopTables = arrayOfNulls<LongArray>(64)

    init {
        for (square in 0 until 64) {
            initSquare(square, ROOK_DIRECTIONS, ROOK_MAGICS[square], rookMasks, rookShifts, rookTables)
            initSquare(square, BISHOP_DIRECTIONS, BISHOP_MAGICS[square], bishopMasks, bishopShifts, bishopTables)
        }
    }

    fun rook(square: Int, occupied: Long): Long {
        val index = ((occupied and rookMasks[square]) * ROOK_MAGICS[square]) ushr rookShifts[square]
        return rookTables[square]!![index.toInt()]
    }

    fun bishop(square: Int, occupied: Long): Long {
        val index = ((occupied and bishopMasks[square]) * BISHOP_MAGICS[square]) ushr bishopShifts[square]
        return bishopTables[square]!![index.toInt()]
    }

    fun queen(square: Int, occupied: Long): Long = rook(square, occupied) or bishop(square, occupied)

    /** Slow reference: walks each ray until it hits an occupied square (inclusive). */
    fun slowAttacks(square: Int, occupied: Long, directions: Array<IntArray>): Long {
        var attacks = 0L
        for (direction in directions) {
            var file = square % 8 + direction[0]
            var rank = square / 8 + direction[1]
            while (file in 0..7 && rank in 0..7) {
                val bit = 1L shl (rank * 8 + file)
                attacks = attacks or bit
                if (occupied and bit != 0L) break
                file += direction[0]
                rank += direction[1]
            }
        }
        return attacks
    }

    fun slowRook(square: Int, occupied: Long) = slowAttacks(square, occupied, ROOK_DIRECTIONS)
    fun slowBishop(square: Int, occupied: Long) = slowAttacks(square, occupied, BISHOP_DIRECTIONS)

    private fun initSquare(
        square: Int,
        directions: Array<IntArray>,
        magic: Long,
        masks: LongArray,
        shifts: IntArray,
        tables: Array<LongArray?>
    ) {
        // relevant occupancy: ray squares except the last one before the edge
        var mask = 0L
        for (direction in directions) {
            var file = square % 8 + direction[0]
            var rank = square / 8 + direction[1]
            while (file + direction[0] in 0..7 && rank + direction[1] in 0..7) {
                mask = mask or (1L shl (rank * 8 + file))
                file += direction[0]
                rank += direction[1]
            }
        }
        val bits = java.lang.Long.bitCount(mask)
        masks[square] = mask
        shifts[square] = 64 - bits
        val table = LongArray(1 shl bits)
        // enumerate every subset of the mask (carry-rippler trick)
        var subset = 0L
        do {
            val index = ((subset * magic) ushr (64 - bits)).toInt()
            table[index] = slowAttacks(square, subset, directions)
            subset = (subset - mask) and mask
        } while (subset != 0L)
        tables[square] = table
    }
}
