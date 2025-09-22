package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int ROWS = 10;
    private static final int COLS = 10;

    private final ArrayList<TextView> cell_tvs = new ArrayList<>();

    private GridLayout grid;
    private TextView tvMinesLeft;
    private TextView tvTimer;
    private TextView tvModeIcon;
    private TextView tvMode;
    private boolean digMode = true;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grid        = findViewById(R.id.gridBoard);
        tvMinesLeft = findViewById(R.id.tvMinesLeft);
        tvTimer     = findViewById(R.id.tvTimer);
        tvModeIcon  = findViewById(R.id.tvModeIcon);
        tvMode      = findViewById(R.id.tvMode);

        tvMinesLeft.setText("Mines: 5");
        tvTimer.setText("0");
        tvModeIcon.setText(getString(R.string.pick));
        tvMode.setText(getString(R.string.mode_dig));
        View.OnClickListener modeToggle = v -> toggleMode();
        tvModeIcon.setOnClickListener(modeToggle);
        tvMode.setOnClickListener(modeToggle);

        buildGrid();
    }

    private void buildGrid() {
        grid.removeAllViews();
        grid.setRowCount(ROWS);
        grid.setColumnCount(COLS);
        cell_tvs.clear();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                TextView tv = new TextView(this);
                tv.setText("");
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextSize(18);
                tv.setTextColor(Color.DKGRAY);
                tv.setBackgroundColor(Color.LTGRAY);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(r, 1f),
                                GridLayout.spec(c, 1f));
                lp.width = 0;
                lp.height = 0;
                int m = dpToPixel(2);
                lp.setMargins(m, m, m, m);

                grid.addView(tv, lp);
                cell_tvs.add(tv);
            }
        }
    }

    private void toggleMode() {
        digMode = !digMode;
        if (digMode) {
            tvModeIcon.setText(getString(R.string.pick));
            tvMode.setText(getString(R.string.mode_dig));
        } else {
            tvModeIcon.setText(getString(R.string.flag));
            tvMode.setText(getString(R.string.mode_flag));
        }
    }
    private int findIndexOfCellTextView(TextView tv) {
        for (int n = 0; n < cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv) return n;
        }
        return -1;
    }

    public void onClickTV(View view) {
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        if (n < 0) return;
        int i = n / COLS;
        int j = n % COLS;

        tv.setText(String.valueOf(i) + String.valueOf(j));


        if (tv.getCurrentTextColor() == Color.DKGRAY) {
            tv.setTextColor(Color.GREEN);
            tv.setBackgroundColor(Color.parseColor("#CCFF90"));
        } else {
            tv.setTextColor(Color.DKGRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }
    }
}
