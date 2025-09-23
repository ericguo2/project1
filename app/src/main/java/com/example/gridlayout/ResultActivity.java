package com.example.gridlayout;

import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;


public class ResultActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        int seconds = getIntent().getIntExtra("seconds", 0);
        setContentView(R.layout.activity_result);
        TextView tvSec = findViewById(R.id.tvSecondsUsed);
        Button btnAgain = findViewById(R.id.btnPlayAgain);
        TextView tvMsg = findViewById(R.id.tvResultMsg);
        boolean won = getIntent().getBooleanExtra("won", false);
        if(won){
            tvMsg.setText(getString(R.string.result_win));
        }else{
            tvMsg.setText(getString(R.string.result_lose));
        }
        tvSec.setText("Time: " + seconds + "s");

        btnAgain.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
