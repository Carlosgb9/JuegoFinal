package com.example.juegofinal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Vector;

public class VistaJoc extends View {

    private Context context;
    MediaPlayer mpLlancament, mpExplosio;
    private Drawable drawableObjectiu[] = new Drawable[8];
    private static int INC_VELOCITAT_GANIVET = 12;
    private static final float INC_ACCELERACIO = 0.5f;
    private static final int INC_GIR = 5;
    private static int PERIODE_PROCES = 50;
    private Grafics ninja;
    private int girNinja;
    private float acceleracioNinja;
    private ThreadJoc thread = new ThreadJoc();
    private long ultimProces = 0;
    private float mX=0, mY=0;
    private boolean llancament = false;
    private Grafics ganivet;
    private boolean ganivetActiu = false;
    private int tempsGanivet;
    private Vector<Grafics> objectius;
    private int numObjectius;
    private String tipusNinja;
    private static int enemicsEleminats;
    Drawable drawableNinja, drawableGanivet, drawableEnemic;
    private boolean continua;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public VistaJoc(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        enemicsEleminats = 0;
        continua = true;
        numObjectius = Integer.parseInt(MainActivity.getnumEnemics());
        tipusNinja = MainActivity.getTipusNinja();
        drawableGanivet = context.getResources().getDrawable(R.drawable.ganivet, null);
        ganivet = new Grafics(this, drawableGanivet);
        drawableNinja = setChosenNinja(tipusNinja, context);
        ninja = new Grafics(this, drawableNinja);
        drawableEnemic = context.getResources().getDrawable(R.drawable.ninja_enemic, null);
        objectius = new Vector<>();
        for (int i = 0; i < numObjectius; i++) {
            Grafics objectiu = new Grafics(this, drawableEnemic);
            objectiu.setIncY(Math.random() * 4 - 2);
            objectiu.setIncX(Math.random() * 4 - 2);
            objectiu.setAngle((int) (Math.random() * 360));
            objectiu.setRotacio((int) (Math.random() * 8 - 4));
            objectius.add(objectiu);
        }
        drawableObjectiu[0] = context.getResources().getDrawable(R.drawable.cap_ninja, null); //cap
        drawableObjectiu[1] = context.getResources().getDrawable(R.drawable.cos_ninja, null); //cos
        drawableObjectiu[2] = context.getResources().getDrawable(R.drawable.cua_ninja, null);
        mpLlancament = MediaPlayer.create(context, R.raw.llancament);
        mpExplosio = MediaPlayer.create(context, R.raw.explosio);
    }

    private Drawable setChosenNinja(String ninjaType, Context context) {
        if (ninjaType.equals("res/drawable/ninja01.png")) {
            return context.getResources().getDrawable(R.drawable.ninja01, null);
        } else if (ninjaType.equals("res/drawable/ninja02.png")) {
            return context.getResources().getDrawable(R.drawable.ninja02, null);
        } else if (ninjaType.equals("res/drawable/ninja03.png")) {
            return context.getResources().getDrawable(R.drawable.ninja03, null);
        } else return null;
    }

    @Override
    protected void onSizeChanged(int ancho, int alto, int ancho_anter, int alto_anter) {
        super.onSizeChanged(ancho, alto, ancho_anter, alto_anter);
        ninja.setPosX((ancho / 2) - (ninja.getAmplada() / 2));
        ninja.setPosY((alto / 2) - (ninja.getAltura() / 2));
        for (Grafics objectiu : objectius) {
            do {
                objectiu.setPosX(Math.random() * (ancho - objectiu.getAmplada()));
                objectiu.setPosY(Math.random() * (alto - objectiu.getAltura()));
            } while (objectiu.distancia(ninja) < (ancho + alto) / 5);
        }
        ultimProces = System.currentTimeMillis();
        thread.start();
    }

    @Override
    synchronized protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Grafics objetiu : objectius) {
            objetiu.dibuixaGrafic(canvas);
        }
        ninja.dibuixaGrafic(canvas);
        ganivet.dibuixaGrafic(canvas);
    }

    synchronized protected void actualitzaMoviment() {
        long instant_actual = System.currentTimeMillis();
        if (ultimProces + PERIODE_PROCES > instant_actual) {
            return;
        }
        double retard = (instant_actual - ultimProces) / (double) PERIODE_PROCES;
        ultimProces = instant_actual;
        ninja.setAngle((int) (ninja.getAngle() + girNinja * retard));
        double nIncX = ninja.getIncX() + acceleracioNinja * Math.cos(Math.toRadians(ninja.getAngle())) * retard;
        double nIncY = ninja.getIncY() + acceleracioNinja * Math.sin(Math.toRadians(ninja.getAngle())) * retard;
        if (Math.hypot(nIncX, nIncY) <= Grafics.MAX_VELOCITAT) {
            ninja.setIncX(nIncX);
            ninja.setIncY(nIncY);
        }
        ninja.incrementaPos(retard);
        for (Grafics objectiu : objectius) {
            objectiu.incrementaPos(retard);
            if (ninja.verificaColisio(objectiu)) {
                continua = false;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialegFinal();
                        updatePlayerScore(JocActivity.getNomJugador(), enemicsEleminats * 10);
                    }
                });
                return;
            }
        }
        if (ganivetActiu) {
            ganivet.incrementaPos(retard);
            tempsGanivet -= retard;
            if (tempsGanivet < 0) {
                ganivetActiu = false;
            } else {
                for (int i = 0; i < objectius.size(); i++) {
                    if (ganivet.verificaColisio(objectius.elementAt(i))) {
                        destrueixObjectiu(i);
                        enemicsEleminats++;
                        if (enemicsEleminats == numObjectius) {
                            continua = false;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    dialegFinal();
                                    updatePlayerScore(JocActivity.getNomJugador(), enemicsEleminats * 10);
                                }
                            });
                        }
                        break;
                    }
                }
            }
        }
    }

    class ThreadJoc extends Thread {
        @Override
        public void run() {
            while (continua) {
                actualitzaMoviment();
            }
        }
    }

    public void dialegFinal() {
        AlertDialog.Builder alertaBuilder = new AlertDialog.Builder(context);
        final TextView tvResult = new TextView(context);
        tvResult.setText(JocActivity.getNomJugador() + ": " + enemicsEleminats * 10 + " punts");
        tvResult.setTextSize(24);
        tvResult.setGravity(Gravity.CENTER);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tvResult.setLayoutParams(layoutParams);
        int padding = 50;
        tvResult.setPadding(padding, padding, padding, padding);

        alertaBuilder.setView(tvResult);
        alertaBuilder.setPositiveButton("Tornar al menu",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (context instanceof AppCompatActivity) {
                            ((AppCompatActivity) context).finish();
                        }
                    }
                });

        AlertDialog alertaDialog = alertaBuilder.create();
        alertaDialog.show();
    }

    private void updatePlayerScore(String playerName, int newScore) {
        SharedPreferences sharedPreferences = JocActivity.getPreferences();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (!continua) {
            dialegFinal();
        } else {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    llancament = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if (dy < 6 && dx > 6) {
                        girNinja = Math.round((x - mX) / 2);
                        llancament = false;
                    } else if (dx < 6 && dy > 6) {
                        acceleracioNinja = Math.round((mY - y) / 25);
                        llancament = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    girNinja = 0;
                    acceleracioNinja = 0;
                    if (llancament) {
                        DisparaGanivet();
                    }
                    break;
            }
            mX = x;
            mY = y;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int codiTecla, KeyEvent event) {
        super.onKeyDown(codiTecla, event);
        boolean procesada = true;
        switch (codiTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                acceleracioNinja = +INC_ACCELERACIO;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                acceleracioNinja = -INC_ACCELERACIO;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                girNinja = -INC_GIR;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                girNinja = +INC_GIR;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                DisparaGanivet();
                break;
            default:
                procesada = false;
                break;
        }
        return procesada;
    }

    @Override
    public boolean onKeyUp(int codigoTecla, KeyEvent evento) {
        super.onKeyUp(codigoTecla, evento);
        boolean procesada = true;
        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                acceleracioNinja = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                acceleracioNinja = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                girNinja = 0;
                break;
            default:
                procesada = false;
                break;
        }
        return procesada;
    }

    private void destrueixObjectiu(int i) {
        int numParts = 3;
        if (objectius.get(i).getDrawable() == drawableEnemic) {
            for (int n = 0; n < numParts; n++) {
                Grafics objectiu = new Grafics(this, drawableObjectiu[n]);
                objectiu.setPosX(objectius.get(i).getPosX());
                objectiu.setPosY(objectius.get(i).getPosY());
                objectiu.setIncX(Math.random() * 7 - 3);
                objectiu.setIncY(Math.random() * 7 - 3);
                objectiu.setAngle((int) (Math.random() * 360));
                objectiu.setRotacio((int) (Math.random() * 8 - 4));
                objectius.add(objectiu);
            }
        }
        objectius.remove(i);
        ganivetActiu = false;
        mpExplosio.start();
    }

    private void DisparaGanivet() {
        ganivet.setPosX(ninja.getPosX() + ninja.getAmplada() / 2 - ganivet.getAmplada() / 2);
        ganivet.setPosY(ninja.getPosY() + ninja.getAltura() / 2 - ganivet.getAltura() / 2);
        ganivet.setAngle(ninja.getAngle());
        ganivet.setIncX(Math.cos(Math.toRadians(ganivet.getAngle())) * INC_VELOCITAT_GANIVET);
        ganivet.setIncY(Math.sin(Math.toRadians(ganivet.getAngle())) * INC_VELOCITAT_GANIVET);
        tempsGanivet = (int) Math.min(this.getWidth() / Math.abs(ganivet.getIncX()), this.getHeight() / Math.abs(ganivet.getIncY())) - 2;
        ganivetActiu = true;
        mpLlancament.start();
    }
}
