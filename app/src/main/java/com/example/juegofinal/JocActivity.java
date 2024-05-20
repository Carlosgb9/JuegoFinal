package com.example.juegofinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JocActivity extends AppCompatActivity {

    private String nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joc);
        Bundle bundle = getIntent().getExtras();
        nombre = bundle.getString("name");
    }

    private void setPoints(){
        SharedPreferences sharedPref = getSharedPreferences("cat.institutmarianao.JocNinja", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int scorePlayer = sharedPref.getInt(nombre, 0);
        if (scorePlayer == 0){
            editor.putInt(nombre,0);
            editor.commit();
        }
        int puntuacio = 0;
        if (true){
            if (puntuacio > scorePlayer){
                editor.putInt(nombre, puntuacio);
            }
            editor.commit();
        }
    }

    private void updatePlayerScore(String playerName, int newScore) {
        SharedPreferences sharedPreferences = getSharedPreferences("cat.institutmarianao.JocNinja", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean playerExists = false;
        int playerCount = sharedPreferences.getInt("player_count", 0);

        for (int i = 1; i <= playerCount; i++) {
            String playerDataKey = "player_" + i;
            String playerDataString = sharedPreferences.getString(playerDataKey, null);
            if (playerDataString != null) {
                String[] parts = playerDataString.split(":");
                String name = parts[0];
                if (name.equals(playerName)) {
                    editor.putString(playerDataKey, playerName + ":" + newScore);
                    playerExists = true;
                    break;
                }
            }
        }

        if (!playerExists) {
            editor.putString("player_" + (playerCount + 1), playerName + ":" + newScore);
            editor.putInt("player_count", playerCount + 1);
        }

        editor.apply();
    }
}