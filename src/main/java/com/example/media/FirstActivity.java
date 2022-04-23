package com.example.media;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FirstActivity extends AppCompatActivity {
    private List<Connect> contacts = new ArrayList<Connect>();

    TextView n;
    TextView p;

    private String monthmenu;
    private String yearmenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);


        initConnects();
        ConnectAdapter adapter = new ConnectAdapter(FirstActivity.this, R.layout.contacts, contacts);
        ListView listView = (ListView) findViewById(R.id.contacts);
        listView.setAdapter((ListAdapter) adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(FirstActivity.this,FirstActivity1.class);
                startActivity(intent);
            }
        });
    }

    private void initConnects() {
        Connect ats = new Connect("黎黎", "13778257465");
        contacts.add(ats);
    }
}