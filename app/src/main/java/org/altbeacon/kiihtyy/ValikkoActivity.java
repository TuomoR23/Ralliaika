package org.altbeacon.ralliaika;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ValikkoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valikko);
    }

    public void onClickUusiSessio (View v) {
        Intent data = new Intent();
        data.putExtra("ACTION","uusi_sessio");
        setResult(RESULT_OK,data);
        finish();
    }
}