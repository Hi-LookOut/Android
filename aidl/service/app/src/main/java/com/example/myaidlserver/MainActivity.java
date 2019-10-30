package com.example.myaidlserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent=new Intent(this, AidlService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        stopService(intent);
        super.onDestroy();
    }
}
