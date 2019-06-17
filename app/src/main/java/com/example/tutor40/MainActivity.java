package com.example.tutor40;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void IrAlumno(View view) {
        Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);

        intent.putExtra("RolID","2");

        startActivity(intent);
    }

    public void IrAsesor(View view) {
        Intent intent = new Intent(getApplicationContext(), AsesorMain.class);

        intent.putExtra("RolID","1");

        startActivity(intent);
    }
}
