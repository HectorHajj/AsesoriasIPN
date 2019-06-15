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

        startActivity(intent);
    }

    public void IrAsesor(View view) {
        Intent intent = new Intent(getApplicationContext(), AsesorMain.class);

        startActivity(intent);
    }
}
