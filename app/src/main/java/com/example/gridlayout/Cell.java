package com.example.gridlayout;

public class Cell{
    public final int r, c;
    public boolean isMine = false;
    public boolean isFlagged = false;
    public int adj = 0;
    public boolean isRevealed = false;

    public Cell(int r, int c){
        this.r = r;
        this.c = c;
    }
}
