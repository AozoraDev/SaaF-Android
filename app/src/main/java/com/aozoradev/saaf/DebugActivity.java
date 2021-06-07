package com.aozoradev.saaf;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.Toast;
import android.content.Context;

public class DebugActivity extends AppCompatActivity {

    String[] exceptionType = {
        "StringIndexOutOfBoundsException",
        "IndexOutOfBoundsException",
        "ArithmeticException",
        "NumberFormatException",
        "ActivityNotFoundException"

    };

    String[] errMessage= {
        "Invalid string operation\n",
        "Invalid list operation\n",
        "Invalid arithmetical operation\n",
        "Invalid toNumber block operation\n",
        "Invalid intent operation"
    };

    private TextView errorText;
    private Toolbar toolbar;
    private String errMsg = "";
    private String madeErrMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        initialize(savedInstanceState);

        Intent intent = getIntent();
        if(intent != null){
            errMsg = intent.getStringExtra("error");

            String[] spilt = errMsg.split("\n");
            //errMsg = spilt[0];
            try {
                for (int j = 0; j < exceptionType.length; j++) {
                    if (spilt[0].contains(exceptionType[j])) {
                        madeErrMsg = errMessage[j];

                        int addIndex = spilt[0].indexOf(exceptionType[j]) + exceptionType[j].length();

                        madeErrMsg += spilt[0].substring(addIndex, spilt[0].length());
                        break;

                    }
                }

                if(madeErrMsg.isEmpty()) madeErrMsg = errMsg;
            }catch(Exception e){}

        }
        errorText.setText(madeErrMsg);
    }

    private void initialize (Bundle savedInstanceState) {
        errorText = findViewById(R.id.errorText);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("An error occurred");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, getResources().getString(R.string.close_app)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 1, 1, getResources().getString(R.string.copy_error)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                finishAffinity();
                break;
            case 1:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied!", madeErrMsg);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(DebugActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

