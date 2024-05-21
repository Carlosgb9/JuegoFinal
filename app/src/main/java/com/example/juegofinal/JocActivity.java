package com.example.juegofinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
public class JocActivity extends AppCompatActivity {

    private static String nombre;

    private static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("cat.institutmarianao.JocNinja", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_joc);
        Bundle bundle = getIntent().getExtras();
        nombre = bundle.getString("name");
    }
    public static String getNomJugador(){
        return nombre;
    }

    public static SharedPreferences getPreferences(){
        return sharedPreferences;
    }
}