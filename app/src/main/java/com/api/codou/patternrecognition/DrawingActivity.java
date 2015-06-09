package com.api.codou.patternrecognition;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

public class DrawingActivity extends Activity {
    EditText userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display);
        userId = (EditText) findViewById(R.id.userId);
    }



    public void onClickButton(View v){
        String valStr = userId.getText().toString();
        Intent intent = new Intent(DrawingActivity.this, SensorActivity.class);
        //Intent newIntent=new Intent(DrawingActivity.this,LoggedActivity.class);
        intent.putExtra("id", valStr);
       // newIntent.putExtra("id",valStr);
        startActivity(intent);
        DrawingActivity.this.finish();
    }

}