package com.superking.parchisi.shell.secondary.util;

import android.content.Context;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger {
  private static final String LOG_FILE_NAME = "app_log.txt";
  private final Context context;
  
  public FileLogger(Context context) {
    this.context = context;
  }
  
  public void log(String tag, String message) {
    String logMessage = System.currentTimeMillis() + " - " + tag + ": " + message + "\n";
    writeLogToFile(logMessage);
  }
  
  private void writeLogToFile(String message) {
    File logFile = getLogFile();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
      writer.append(message);
    } catch (IOException e) {
      Log.e("FileLogger", "❌ Error al escribir en log: " + e);
      Log.e("FileLogger", Log.getStackTraceString(e));
    }
  }
  
  private File getLogFile() {
    File logDir;
    try {
      // 📂 Carpeta privada persistente distinta a "files"
      logDir = context.getDir("app_logs", Context.MODE_PRIVATE);
    } catch (Exception e) {
      Log.w("FileLogger", "⚠️ No se pudo crear carpeta app_logs, usando cache interna.", e);
      logDir = context.getCacheDir(); // fallback
    }
    
    return new File(logDir, LOG_FILE_NAME);
  }
}