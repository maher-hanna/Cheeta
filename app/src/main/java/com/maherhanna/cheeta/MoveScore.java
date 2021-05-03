package com.maherhanna.cheeta;


public class MoveScore implements Comparable<MoveScore> {
    private int score;
    public int moveIndex;

    public MoveScore(int score, int moveIndex) {
        this.score = score;
        this.moveIndex = moveIndex;
    }

    @Override
    public int compareTo(MoveScore other) {
        return other.score - this.score;
    }
}

