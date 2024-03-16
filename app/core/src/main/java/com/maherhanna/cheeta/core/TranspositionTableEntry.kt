package com.maherhanna.cheeta.core

data class TranspositionTableEntry(
    val hashKey: Long,
    val depth: Int,
    val flag: TranspositionTableFlag,
    val score: Int
) {
}