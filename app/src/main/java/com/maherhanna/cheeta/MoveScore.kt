package com.maherhanna.cheeta

class MoveScore(private val score: Int, @JvmField var moveIndex: Int) : Comparable<MoveScore> {
    override fun compareTo(other: MoveScore): Int {
        return other.score - score
    }
}
