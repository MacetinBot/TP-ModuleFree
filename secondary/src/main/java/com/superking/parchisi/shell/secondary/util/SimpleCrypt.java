package com.superking.parchisi.shell.secondary.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SimpleCrypt {
  
  private static final String TAG = "SimpleCrypt";
  private static final String PREF_NAME = "SimpleCryptPrefs";
  private static final String PREF_KEY_EXTRACTION_DONE = "extraction_done_";
  
  public static boolean decryptAndExtract(Context context, String assetFileName) {
    // Verificar si ya se hizo la extracción para este archivo
    if (isExtractionDone(context, assetFileName)) {
      Log.d(TAG, "Archivo ya fue extraído previamente: " + assetFileName);
      return true;
    }
    
    try (InputStream assetStream = context.getAssets().open(assetFileName);
         DataInputStream dis = new DataInputStream(assetStream)) {
      
      SecretKey key = KeyGenerator.generateKey(context);
      
      // Leemos IV (primeros 16 bytes)
      byte[] iv = new byte[16];
      dis.readFully(iv);
      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
      
      try (CipherInputStream cis = new CipherInputStream(dis, cipher)) {
        unzipStreamNoOverwrite(cis, context.getFilesDir());
      }
      
      setExtractionDone(context, assetFileName);
      Log.d(TAG, "Archivo desencriptado y extraído correctamente en files/");
      return true;
      
    } catch (Exception e) {
      Log.e(TAG, "Error al desencriptar o extraer el archivo: " + assetFileName, e);
      return false;
    }
  }
  
  private static void unzipStreamNoOverwrite(InputStream zipStream, File targetDir) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(zipStream)) {
      ZipEntry entry;
      byte[] buffer = new byte[4096];
      
      while ((entry = zis.getNextEntry()) != null) {
        File outFile = new File(targetDir, entry.getName());
        
        if (entry.isDirectory()) {
          if (!outFile.exists() && !outFile.mkdirs()) {
            Log.w(TAG, "No se pudieron crear los directorios: " + outFile.getAbsolutePath());
          }
        } else {
          File parent = outFile.getParentFile();
          if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Log.w(TAG, "No se pudieron crear los directorios padre: " + parent.getAbsolutePath());
          }
          
          if (outFile.exists()) {
            Log.d(TAG, "Archivo existente, se omite: " + outFile.getAbsolutePath());
            zis.closeEntry();
            continue;
          }
          
          try (FileOutputStream fos = new FileOutputStream(outFile)) {
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          }
        }
        
        zis.closeEntry();
      }
    }
  }
  
  private static boolean isExtractionDone(Context context, String assetFileName) {
    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    return prefs.getBoolean(getPrefKey(assetFileName), false);
  }
  
  private static void setExtractionDone(Context context, String assetFileName) {
    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    prefs.edit().putBoolean(getPrefKey(assetFileName), true).apply();
  }
  
  private static String getPrefKey(String assetFileName) {
    return PREF_KEY_EXTRACTION_DONE + assetFileName;
  }
}