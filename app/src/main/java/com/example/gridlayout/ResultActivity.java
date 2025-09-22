package com.example.gridlayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView tvMsg = findViewById(R.id.tvResultMsg);
        TextView tvSec = findViewById(R.id.tvSecondsUsed);
        Button btnAgain = findViewById(R.id.btnPlayAgain);

        boolean won = getIntent().getBooleanExtra("won", false);
        int seconds = getIntent().getIntExtra("seconds", 0);

        tvMsg.setText(won ? getString(R.string.result_win) : getString(R.string.result_lose));
        tvSec.setText("Time: " + seconds + "s");

        btnAgain.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
