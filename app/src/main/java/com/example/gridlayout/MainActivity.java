package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int ROWS = GameEngine.ROWS;
    private static final int COLS = GameEngine.COLS;

    // UI
    private GridLayout grid;
    private TextView tvMinesLeft, tvTimer, tvModeIcon, tvMode;

    // Board + state
    private final ArrayList<TextView> cells = new ArrayList<>();
    private GameEngine engine;
    private boolean digMode = true;          // true = dig, false = flag
    private int flagsPlaced = 0;             // can exceed MINES (counter may go negative)

    // Simple elapsed timer (Commit 4 can change to countdown if desired)
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startMs = 0L;
    private boolean ticking = false;

    // End-of-game extra tap gate (Commit 4 will navigate to ResultsActivity)
    private boolean awaitingResultTap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grid        = findViewById(R.id.gridBoard);
        tvMinesLeft = findViewById(R.id.tvMinesLeft);
        tvTimer     = findViewById(R.id.tvTimer);
        tvModeIcon  = findViewById(R.id.tvModeIcon);
        tvMode      = findViewById(R.id.tvMode);

        tvModeIcon.setOnClickListener(v -> toggleMode());
        tvMode.setOnClickListener(v -> toggleMode());

        startNewGame();
    }

    private void startNewGame() {
        engine = new GameEngine();
        flagsPlaced = 0;
        awaitingResultTap = false;

        // UI initial state
        updateMinesLeft();
        tvModeIcon.setText(getString(R.string.pick));  // ⛏
        tvMode.setText(getString(R.string.mode_dig));
        tvTimer.setText("0");

        buildGrid();
        renderAll();

        // Start elapsed timer
        startMs = System.currentTimeMillis();
        ticking = true;
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!ticking) return;
            long elapsed = (System.currentTimeMillis() - startMs) / 1000L;
            tvTimer.setText(String.valueOf(elapsed));
            handler.postDelayed(this, 1000);
        }
    };

    private void buildGrid() {
        grid.removeAllViews();
        grid.setRowCount(ROWS);
        grid.setColumnCount(COLS);
        cells.clear();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                final int rr = r, cc = c;

                TextView tv = new TextView(this);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(18);
                tv.setTextColor(Color.DKGRAY);
                tv.setBackgroundColor(Color.LTGRAY);

                // Primary click
                tv.setOnClickListener(v -> onCellClick(rr, cc));
                // Quick-flag via long press (optional convenience)
                tv.setOnLongClickListener(v -> { toggleFlag(rr, cc); return true; });

                // Make cells expand evenly
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                        GridLayout.spec(r, 1f), GridLayout.spec(c, 1f));
                lp.width = 0;
                lp.height = 0;
                int m = dp(2);
                lp.setMargins(m, m, m, m);

                grid.addView(tv, lp);
                cells.add(tv);
            }
        }
    }

    private void toggleMode() {
        digMode = !digMode;
        if (digMode) {
            tvModeIcon.setText(getString(R.string.pick));  // ⛏
            tvMode.setText(getString(R.string.mode_dig));
        } else {
            tvModeIcon.setText(getString(R.string.flag));  // 🚩
            tvMode.setText(getString(R.string.mode_flag));
        }
    }

    private void onCellClick(int r, int c) {
        if (awaitingResultTap) {
            // Commit 4 will navigate to a results screen here.
            Toast.makeText(this, "Results screen coming next.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (engine.gameOver) return;

        if (digMode) {
            // Dig: block if flagged
            if (engine.board[r][c].isFlagged) {
                Toast.makeText(this, "Cannot dig a flagged cell.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean changed = engine.reveal(r, c);
            if (changed) renderAll();

            if (engine.gameOver) {
                ticking = false;
                handler.removeCallbacks(tick);
                engine.revealAll();
                renderAll();
                awaitingResultTap = true; // require one extra tap before results
                Toast.makeText(this,
                        (engine.won ? "You won!" : "You hit a mine!") + " Tap once more to continue.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Flag mode uses bottom toggle
            toggleFlag(r, c);
        }
    }

    private void toggleFlag(int r, int c) {
        if (engine.gameOver || awaitingResultTap) return;

        Cell cell = engine.board[r][c];
        if (cell.isRevealed) {
            Toast.makeText(this, "Cannot flag a revealed cell.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean changed = engine.toggleFlag(r, c);
        if (changed) {
            flagsPlaced += cell.isFlagged ? 1 : -1;
            updateMinesLeft();
            renderCell(r, c);
        }
    }

    private void updateMinesLeft() {
        // Counter can be negative per requirement
        int remaining = GameEngine.MINES - flagsPlaced;
        tvMinesLeft.setText(getString(R.string.mines_left_label) + ": " + remaining);
    }

    private void renderAll() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                renderCell(r, c);
            }
        }
    }

    private void renderCell(int r, int c) {
        TextView tv = cells.get(r * COLS + c);
        Cell cell = engine.board[r][c];

        if (!cell.isRevealed) {
            tv.setEnabled(true);
            tv.setAlpha(1f);
            tv.setBackgroundColor(Color.LTGRAY);
            tv.setText(cell.isFlagged ? getString(R.string.flag) : ""); // 🚩 when flagged
            return;
        }

        // Revealed
        tv.setEnabled(false);
        tv.setAlpha(0.95f);
        if (cell.isMine) {
            tv.setText(getString(R.string.mine)); // 💣
            tv.setBackgroundColor(Color.parseColor("#FFCDD2")); // light red
        } else {
            tv.setText(cell.adj == 0 ? "" : String.valueOf(cell.adj));
            tv.setBackgroundColor(Color.WHITE);
        }
    }

    private int dp(int d) {
        return Math.round(getResources().getDisplayMetrics().density * d);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ticking = false;
        handler.removeCallbacks(tick);
    }
}
