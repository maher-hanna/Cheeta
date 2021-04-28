package com.maherhanna.cheeta;

import java.util.Objects;

public class State {
    public long allPieces;
    public int enPassantTarget;
    public int blackCastlingRights;
    public int whiteCastlingRights;

    public State(long allPieces,  int enPassantTarget,int blackCastlingRights,
                 int whiteCastlingRights){

        this.allPieces = allPieces;
        this.enPassantTarget = enPassantTarget;
        this.blackCastlingRights = blackCastlingRights;
        this.whiteCastlingRights = whiteCastlingRights;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return allPieces == state.allPieces &&
                enPassantTarget == state.enPassantTarget &&
                blackCastlingRights == state.blackCastlingRights &&
                whiteCastlingRights == state.whiteCastlingRights;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allPieces,enPassantTarget, blackCastlingRights, whiteCastlingRights);
    }
}
