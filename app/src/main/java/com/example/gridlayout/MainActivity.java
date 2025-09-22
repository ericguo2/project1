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

    private GridLayout grid;
    private TextView tvMinesLeft, tvTimer, tvModeIcon, tvMode;

    private final ArrayList<TextView> cells = new ArrayList<>();
    private GameEngine engine;
    private boolean digMode = true;

    // Simple stopwatch (Commit 4 will refine)
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startMs = 0L;
    private boolean ticking = false;

    private boolean awaitingResultTap = false; // set when game ends; next tap → results (Commit 4)

    @Override protected void onCreate(Bundle savedInstanceState) {
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
        awaitingResultTap = false;

        tvMinesLeft.setText(getString(R.string.mines_left_label) + ": " + GameEngine.MINES);
        tvModeIcon.setText(getString(R.string.pick));
        tvMode.setText(getString(R.string.mode_dig));

        buildGrid();
        renderAll();

        // start basic elapsed timer (Commit 4 can switch to countdown)
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

        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) {
            final int rr = r, cc = c;
            TextView tv = new TextView(this);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setTextColor(Color.DKGRAY);
            tv.setBackgroundColor(Color.LTGRAY);
            tv.setOnClickListener(v -> onCellClick(rr, cc));

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(r, 1f), GridLayout.spec(c, 1f));
            lp.width = 0; lp.height = 0;
            int m = dp(2); lp.setMargins(m, m, m, m);
            grid.addView(tv, lp);
            cells.add(tv);
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
            // Commit 4: navigate to results here
            Toast.makeText(this, "Results screen coming in next commit.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (engine.gameOver) return; // safety

        if (digMode) {
            if (engine.board[r][c].isFlagged) {
                Toast.makeText(this, "Cannot dig a flagged cell.", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean changed = engine.reveal(r, c);
            if (changed) renderAll();
            if (engine.gameOver) {
                ticking = false; handler.removeCallbacks(tick);
                engine.revealAll();
                renderAll();
                awaitingResultTap = true; // extra tap rule (Commit 4 will navigate)
                Toast.makeText(this, (engine.won ? "You won!" : "You hit a mine!") +
                        " Tap once more to see results.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Commit 3 will implement flagging here
            Toast.makeText(this, "Flagging arrives in next commit.", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderAll() {
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) renderCell(r, c);
    }

    private void renderCell(int r, int c) {
        TextView tv = cells.get(r * COLS + c);
        Cell cell = engine.board[r][c];

        if (!cell.isRevealed) {
            tv.setEnabled(true);
            tv.setAlpha(1f);
            tv.setText(cell.isFlagged ? getString(R.string.flag) : ""); // 🚩 later (Commit 3)
            tv.setBackgroundColor(Color.LTGRAY);
            return;
        }

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

    private int dp(int d) { return Math.round(getResources().getDisplayMetrics().density * d); }

    @Override protected void onPause() {
        super.onPause();
        ticking = false;
        handler.removeCallbacks(tick);
    }
}
