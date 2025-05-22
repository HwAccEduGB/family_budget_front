package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tableLayout = findViewById(R.id.tableLayout);
        String result = getIntent().getStringExtra("result");

        String[] lines = result.split("\n\n");
        for (String line : lines) {
            String[] fields = line.split("\n");
            if (fields.length >= 6) {
                addTableRow(fields);
            }
        }
    }

    private void addTableRow(String[] fields) {
        TableRow row = new TableRow(this);
        for (String field : fields) {
            TextView tv = new TextView(this);
            tv.setText(field);
            tv.setPadding(4, 4, 4, 4);
            tv.setBackgroundColor(Color.parseColor("#FFFFFF"));
            row.addView(tv);
        }
        tableLayout.addView(row);
    }
}
