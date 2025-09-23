package com.example.gridlayout;

import android.widget.Toast;

import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.os.Looper;
import android.view.Gravity;
import android.widget.TextView;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;



public class MainActivity extends AppCompatActivity {

    private static final int ROWS = GameEngine.ROWS;
    private static final int COLS = GameEngine.COLS;

    private static final int COLOR_HIDDEN = Color.parseColor("#79e336");
    private static final int COLOR_REVEALED = Color.parseColor("#EEEEEE");
    private static final int COLOR_MINE = Color.parseColor("#FFCDD2");

    private GridLayout grid;
    private TextView tvMinesLeft, tvTimer, tvModeIcon, tvMode;

    private final ArrayList<TextView> cells = new ArrayList<>();
    private GameEngine engine;
    private boolean digMode = true;
    private int flagsPlaced = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startMs = 0L;
    private boolean ticking = false;
    private boolean awaitingResultTap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();


        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grid = findViewById(R.id.gridBoard);
        tvMinesLeft = findViewById(R.id.tvMinesLeft);
        tvTimer = findViewById(R.id.tvTimer);
        tvModeIcon = findViewById(R.id.tvModeIcon);
        tvMode = findViewById(R.id.tvMode);
        tvModeIcon.setOnClickListener(v -> toggleMode());
        tvMode.setOnClickListener(v -> toggleMode());
        startNewGame();
    }

    private void startNewGame(){
        engine = new GameEngine();

        flagsPlaced = 0;
        awaitingResultTap = false;
        updateMinesLeft();

        tvModeIcon.setText(getString(R.string.pick));
        tvMode.setText(getString(R.string.mode_dig));
        tvTimer.setText(getString(R.string.clock) + " 0");

        buildGrid();
        renderAll();
        startMs = System.currentTimeMillis();
        ticking = true;
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    private final Runnable tick = new Runnable(){
        @Override public void run(){
            if(!ticking){
                return;
            }
            long elapsed = (System.currentTimeMillis() - startMs) / 1000L;
            tvTimer.setText(getString(R.string.clock) + " " + elapsed);


            handler.postDelayed(this, 1000);
        }
    };

    private void buildGrid() {
        grid.removeAllViews();
        grid.setRowCount(ROWS);
        grid.setColumnCount(COLS);
        cells.clear();
        for(int r = 0; r < ROWS; r++){
            for(int c = 0; c < COLS; c++){
                final int cc = c;
                final int rr = r;
                TextView tv = new TextView(this);
                tv.setGravity(Gravity.CENTER);

                tv.setTextColor(Color.DKGRAY);
                tv.setBackgroundColor(COLOR_HIDDEN);
                tv.setTextSize(18);
                tv.setOnClickListener(v -> onCellClick(rr, cc));

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
        if(digMode){
            tvModeIcon.setText(getString(R.string.pick));
            tvMode.setText(getString(R.string.mode_dig));
        }else{
            tvModeIcon.setText(getString(R.string.flag));
            tvMode.setText(getString(R.string.mode_flag));
        }
    }

    private void onCellClick(int r, int c) {
        if(awaitingResultTap){
            goToResults();
            return;
        }

        if(engine.gameOver) {
            return;
        }

        if(digMode){
            if(engine.board[r][c].isFlagged){
                Toast.makeText(this, "Cannot dig a flagged cell.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean changed = engine.reveal(r, c);
            if(changed){
                renderAll();
            }
            if(engine.gameOver){
                ticking = false;
                handler.removeCallbacks(tick);
                engine.revealAll();
                renderAll();
                awaitingResultTap = true;
                Toast.makeText(this,
                        (engine.won ? "You win" : "You lose") + " Tap again for results.",
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            toggleFlag(r, c);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ticking = false;
        handler.removeCallbacks(tick);
    }

    private void toggleFlag(int r, int c){
        if(engine.gameOver || awaitingResultTap){
            return;
        }

        Cell cell = engine.board[r][c];
        if(cell.isRevealed){
            Toast.makeText(this, "Cannot flag a revealed cell.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean changed = engine.toggleFlag(r, c);
        if(changed){
            if(cell.isFlagged){
                flagsPlaced++;
            }else{
                flagsPlaced--;
            }
            updateMinesLeft();
            renderCell(r, c);
        }
    }

    private void updateMinesLeft(){
        int remaining = GameEngine.MINES - flagsPlaced;
        tvMinesLeft.setText(getString(R.string.flag) + " " + remaining);
    }

    private void renderAll() {
        for(int r = 0; r < ROWS; r++){
            for(int c = 0; c < COLS; c++){
                renderCell(r, c);
            }
        }
    }

    private int dp(int d) {
        return Math.round(getResources().getDisplayMetrics().density * d);
    }
    private void renderCell(int r, int c) {
        TextView tv = cells.get(r * COLS + c);
        Cell cell = engine.board[r][c];
        tv.setEnabled(true);

        if(!cell.isRevealed){
            tv.setAlpha(1f);
            tv.setBackgroundColor(COLOR_HIDDEN);
            tv.setText(cell.isFlagged ? getString(R.string.flag) : "");

            return;
        }

        // Revealed
        tv.setAlpha(engine.gameOver ? 1f : 0.95f);
        if(cell.isMine){
            tv.setText(getString(R.string.mine));
            tv.setBackgroundColor(COLOR_MINE);
        }else{
            tv.setText(cell.adj == 0 ? "" : String.valueOf(cell.adj));
            tv.setBackgroundColor(COLOR_REVEALED);

        }
    }

    private void goToResults(){
        int seconds = (int)((System.currentTimeMillis() - startMs) / 1000L);
        startActivity(new android.content.Intent(this, ResultActivity.class)
                .putExtra("won", engine.won)
                .putExtra("seconds", seconds));
        finish();
    }




}
