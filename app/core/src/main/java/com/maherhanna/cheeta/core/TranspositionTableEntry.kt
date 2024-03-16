package com.maherhanna.cheeta.core

data class TranspositionTableEntry(
    var hashKey: ULong = 0UL,
    var depth: Int = 0,
    var flag: TranspositionTableFlag = TranspositionTableFlag.EXACT,
    var score: Int = 0
) {
}