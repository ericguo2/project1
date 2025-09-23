package com.example.gridlayout;

import java.util.*;
public class GameEngine {
    public static final int ROWS = 10, COLS = 10, MINES = 5;

    public final Cell[][] board = new Cell[ROWS][COLS];
    public boolean gameOver = false, won = false;
    private int revealedSafe = 0;

    public GameEngine() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                board[r][c] = new Cell(r, c);
        placeMines();
        computeAdjacency();
    }

    private void placeMines() {
        Random rnd = new Random();
        Set<Integer> used = new HashSet<>();
        while(used.size() < MINES){
            int idx = rnd.nextInt(ROWS * COLS);
            if (used.add(idx)) board[idx / COLS][idx % COLS].isMine = true;
        }
    }

    private void computeAdjacency(){
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                if (board[r][c].isMine) continue;
                int count = 0;
                for (int dr = -1; dr <= 1; dr++)
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc = c + dc;
                        if (inBounds(nr, nc) && board[nr][nc].isMine) count++;
                    }
                board[r][c].adj = count;
            }
    }

    private boolean inBounds(int r, int c) { return 0 <= r && r < ROWS && 0 <= c && c < COLS; }

    public boolean reveal(int r, int c) {
        if (gameOver) return false;
        Cell cell = board[r][c];
        if (cell.isFlagged || cell.isRevealed) return false;

        cell.isRevealed = true;
        if (cell.isMine) { gameOver = true; won = false; return true; }

        revealedSafe++;
        if (cell.adj == 0) floodFillFromZero(r, c);

        if (revealedSafe == ROWS * COLS - MINES) { gameOver = true; won = true; }
        return true;
    }

    private void floodFillFromZero(int r, int c) {
        ArrayDeque<int[]> q = new ArrayDeque<>();
        boolean[][] seen = new boolean[ROWS][COLS];

        q.add(new int[]{r, c});
        seen[r][c] = true;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int cr = cur[0], cc = cur[1];

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = cr + dr, nc = cc + dc;
                    if (!inBounds(nr, nc) || seen[nr][nc]) continue;

                    Cell n = board[nr][nc];
                    seen[nr][nc] = true;

                    if (n.isMine || n.isFlagged || n.isRevealed) continue;

                    n.isRevealed = true;
                    revealedSafe++;
                    if (n.adj == 0) q.add(new int[]{nr, nc});
                }
            }
        }
    }

    public boolean toggleFlag(int r, int c) {
        if (gameOver) return false;
        Cell cell = board[r][c];
        if (cell.isRevealed) return false;
        cell.isFlagged = !cell.isFlagged;
        return true;
    }

    public void revealAll() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                board[r][c].isRevealed = true;
    }
}
