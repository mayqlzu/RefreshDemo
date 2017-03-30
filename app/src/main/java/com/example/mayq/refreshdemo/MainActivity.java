package com.example.mayq.refreshdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RefreshLayout refreshLayout;
    private ListView lv;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.lv);
        btn = (Button) findViewById(R.id.btn);
        // Defined Array values to show in ListView
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            data.add(new String("item " + i));
        }

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, data.toArray(new String[0]));

        /*
        ImageView header = new ImageView(this);
        header.setBackgroundResource(R.drawable.ic_launcher);
        lv.addHeaderView(header);
        */

        // Assign adapter to ListView
        lv.setAdapter(adapter);


        refreshLayout = (RefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("mayq", "onRefresh()");
                doRefresh();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout.performRefresh();
            }
        });

    }

    private void doRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.finishRefresh();
            }
        }, 2000);

    }
}
