package com.example.juegofinal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar tbMenu;
    private Button bPlay, bScore, bExit;
    private MediaPlayer mp;
    private static String nom;
    private TextView tvGameName;

    private static String numEnemics, tipusNinja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mp = MediaPlayer.create(MainActivity.this, R.raw.theme);
        init();
        buttons();
        setSupportActionBar(tbMenu);
        getSettings();
        animacioTitol();
    }
    @Override
    protected void onResume(){
        super.onResume();
        getSettings();
    }

    private void animacioTitol(){
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.title);
        tvGameName.startAnimation(animation);
    }

    private void buttons(){
        bPlay.setOnClickListener(v -> alertaNom());
        bScore.setOnClickListener(v -> mostrarPuntuacions());
        bExit.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            alertaInfo();
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    private void alertaInfo(){
        AlertDialog.Builder alertaBuilder = new AlertDialog.Builder(this);
        alertaBuilder.setCancelable(true);
        alertaBuilder.setMessage(R.string.info_text);
        alertaBuilder.setNegativeButton(R.string.close_info, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertaDialog = alertaBuilder.create();
        alertaDialog.show();
    }

    private void alertaNom(){
        AlertDialog.Builder alertaBuilder = new AlertDialog.Builder(this);
        alertaBuilder.setCancelable(true);
        final EditText etNom = new EditText(MainActivity.this);
        etNom.setHint(R.string.hint_nom);
        alertaBuilder.setView(etNom);
        alertaBuilder.setPositiveButton("Confirmar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (numEnemics.matches("\\d+")){
                            Intent i = new Intent(MainActivity.this, JocActivity.class);
                            nom = etNom.getText().toString().trim();
                            if (nom.isEmpty()){
                                etNom.setError(R.string.nomBuit +"");
                            } else {
                                i.putExtra("name", nom);
                                startActivity(i);
                            }
                        }else {
                            Toast.makeText(MainActivity.this, R.string.formatEnemics, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        AlertDialog alertaDialog = alertaBuilder.create();
        alertaDialog.show();
    }

    private void mostrarPuntuacions(){
        SharedPreferences sharedPref = getSharedPreferences("cat.institutmarianao.juegoFinal", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        AlertDialog.Builder alertaBuilder = new AlertDialog.Builder(this);
        alertaBuilder.setCancelable(true);
        final ListView lvPuntuacions = new ListView(MainActivity.this);
        //String[] puntuaciones = {"Antonio", "Manolo", "Jose", "Antonio", "Manolo", "Jose", "Antonio", "Manolo", "Jose", "Antonio", "Manolo", "Jose"};
        ArrayAdapter<String> puntuacionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getSortedPlayersData());
        lvPuntuacions.setAdapter(puntuacionsAdapter);
        alertaBuilder.setView(lvPuntuacions);
        alertaBuilder.setTitle("Puntuacions");
        AlertDialog alertaDialog = alertaBuilder.create();
        alertaDialog.show();
    }

    private String[] getSortedPlayersData() {
        SharedPreferences sharedPreferences = getSharedPreferences("cat.institutmarianao.JocNinja", Context.MODE_PRIVATE);
        int playerCount = sharedPreferences.getInt("player_count", 0);
        List<String> playerData = new ArrayList<>();

        List<String> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            String playerDataKey = "player_" + i;
            String playerDataString = sharedPreferences.getString(playerDataKey, null);
            if (playerDataString != null) {
                players.add(playerDataString);
            }
        }

        // Ordenar la lista de jugadores por puntuación en orden descendente
        Collections.sort(players, new Comparator<String>() {
            @Override
            public int compare(String p1, String p2) {
                int score1 = Integer.parseInt(p1.split(":")[1].trim());
                int score2 = Integer.parseInt(p2.split(":")[1].trim());
                return Integer.compare(score2, score1);
            }
        });

        // Limitar la lista de salida a los 5 primeros jugadores
        int limit = Math.min(players.size(), 5);
        for (int i = 0; i < limit; i++) {
            String player = players.get(i);
            String[] parts = player.split(":");
            String name = parts[0];
            String score = parts[1].trim();
            playerData.add(score + " - " + name);
        }

        return playerData.toArray(new String[0]);
    }

    private void getSettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean musica = sharedPreferences.getBoolean("cbpMusica", false);
        tipusNinja = sharedPreferences.getString("lpNinja", "1");
        numEnemics = sharedPreferences.getString("etpEnemics", "default value");

        if (musica){
            playMusic(1);
        }
        else{
            playMusic(0);
        }

    }

    private void playMusic(int action){
        if (action == 0){
            if (mp.isPlaying()) {
                mp.stop();
                try {
                    mp.prepare();
                } catch (IOException e) {
                    new RuntimeException();
                }
            }
        } else if (action == 1){
            if (mp.isPlaying()) {
                mp.stop();
                try {
                    mp.prepare();
                } catch (IOException e) {
                    new RuntimeException();
                }
            }
            mp.start();
        }
    }

    public static String getnumEnemics() {
        return numEnemics;
    }

    public static String getTipusNinja() {
        return tipusNinja;
    }

    public void dialegFinal(){
        AlertDialog.Builder alertaBuilder = new AlertDialog.Builder(this);
        final EditText etNom = new EditText(this);
        etNom.setHint(R.string.hint_nom);
        alertaBuilder.setView(etNom);
        alertaBuilder.setPositiveButton("Confirmar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(MainActivity.this, JocActivity.class);
                        String nom = etNom.getText().toString().trim();
                        if (nom.isEmpty()){
                            etNom.setError("El nom no pot estar buit");
                        } else {
                            i.putExtra("name", nom);
                            startActivity(i);
                        }
                    }
                });

        AlertDialog alertaDialog = alertaBuilder.create();
        alertaDialog.show();
    }

    private void init(){
        tbMenu = findViewById(R.id.tbMenu);
        bPlay = findViewById(R.id.bPlay);
        bScore = findViewById(R.id.bScores);
        bExit = findViewById(R.id.bExit);
        tvGameName = findViewById(R.id.tvGameName);
    }

}