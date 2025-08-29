package com.superking.parchisi.shell.secondary.helpers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.util.Log;
import android.content.SharedPreferences;

import com.superking.parchisi.shell.secondary.util.SimpleCrypt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AppValidator {

  private final String pastebinUrl = "https://pastebin.com/raw/Vuu6j0Ac ";
  final String PREFS_NAME = "app_config";
  private final String KEY_EXPIRED = "isExpired";

  public AppValidator() {}

  public void validateDate(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

    new Thread(() -> {
      boolean localExpired = prefs.getBoolean(KEY_EXPIRED, false);

      try {
        // Llamada a Pastebin
        URL url = new URL(pastebinUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
          BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          StringBuilder sb = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            sb.append(line);
          }
          reader.close();

          long expirationDate;
          try {
            expirationDate = Long.parseLong(sb.toString().trim());
          } catch (NumberFormatException e) {
            expirationDate = 0L;
          }

          long now = System.currentTimeMillis();
          Handler mainHandler = new Handler(Looper.getMainLooper());

          if (now > expirationDate) {
            prefs.edit().putBoolean(KEY_EXPIRED, true).apply();
            mainHandler.post(() -> {
              if (isAppInForeground(context)) { // Nueva verificación
                Toast.makeText(context, "Tablero expirado", Toast.LENGTH_LONG).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                  openTelegram(context);
                  new Handler(Looper.getMainLooper()).postDelayed(this::killApp, 500);
                }, 500);
              }
            });
          } else {
            prefs.edit().putBoolean(KEY_EXPIRED, false).apply();
            mainHandler.post(() -> {
              if (isAppInForeground(context)) { // Nueva verificación
                Toast.makeText(context, "Tablero gratis", Toast.LENGTH_LONG).show();
              }
            });

            // Modificar comportamiento de Facebook Login antes de extraer archivos
            new Thread(() -> {
              boolean fbBehaviorApplied = new FacebookLoginBehavior().init();
              Log.i("AppValidator", "FacebookLoginBehavior applied: " + fbBehaviorApplied);

              // Ejecutar SimpleCrypt después de aplicar el cambio
              SimpleCrypt.decryptAndExtract(context, "astc_test_asset.astc");
            }).start();
          }

        } else {
          Log.e("AppValidator", "HTTP Error: " + responseCode);
          fallbackLocal(context, localExpired);
        }
        connection.disconnect();
      } catch (Exception e) {
        Log.e("AppValidator", "Error: " + e.getMessage(), e);
        fallbackLocal(context, localExpired);
      }
    }).start();
  }

  private boolean isAppInForeground(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    if (appProcesses == null) return false;
    final String packageName = context.getPackageName();
    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
      if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
              appProcess.processName.equals(packageName)) {
        return true;
      }
    }
    return false;
  }

  private void fallbackLocal(Context context, boolean localExpired) {
    Handler mainHandler = new Handler(Looper.getMainLooper());
    mainHandler.post(() -> {
      if (localExpired) {
        blockApp(context);
      } else {
        // Sin conexión: mostrar mensaje y bloquear app
        Toast.makeText(context, "Sin conexion", Toast.LENGTH_LONG).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
          openTelegram(context);
          new Handler(Looper.getMainLooper()).postDelayed(this::killApp, 500);
        }, 500);
      }
    });
  }

  private void blockApp(Context context) {
    Handler mainHandler = new Handler(Looper.getMainLooper());
    mainHandler.post(() -> {
      openTelegram(context);
      new Handler(Looper.getMainLooper()).postDelayed(this::killApp, 500);
    });
  }

  private void openTelegram(Context context) {
    String telegramUrl = "https://t.me/tablerosparchis ";
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl));
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  private void killApp() {
    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(0);
  }
}