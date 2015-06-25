package com.api.codou.patternrecognition;


import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DrawingActivity extends Activity {
    EditText userId;
    private  DataHandler dataSource;
    int num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display);
        dataSource= new DataHandler(this);
        userId = (EditText) findViewById(R.id.userId);
    }


/*
il s'agit ici de la premiere activite. en appuyant sur le bouton sign , l'appli doit se connecter à la bdd pour verifier s'il y a deja des enregistrement
et relever la valeur du compteur
Si le id existe deja dans la bdee, on va selectionner la valeur max du numero d'essai*/

    public void onClickButton(View v){
        String valStr = userId.getText().toString();
        dataSource.open();
        Cursor c = dataSource.selectMaxNum(userId.getText().toString());
        if(c.moveToFirst()) num= c.getInt(0);
        else num=0;
        Toast.makeText(getBaseContext(),"You tried "+ num+"time(s)",Toast.LENGTH_LONG).show();
        Bundle obj= new Bundle();
        obj.putInt("numero",num);
        Intent intent = new Intent(DrawingActivity.this, SensorActivity.class);
        //Intent newIntent=new Intent(DrawingActivity.this,LoggedActivity.class);
        intent.putExtra("id", valStr);
        intent.putExtras(obj);
       // newIntent.putExtra("id",valStr);
        startActivity(intent);
        DrawingActivity.this.finish();
    }



    @Override
    protected void onResume() {

        dataSource.open();

        super.onResume();
    }

    @Override
    protected void onPause() {
        dataSource.close();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        dataSource.close();
        super.onDestroy();
    }

}