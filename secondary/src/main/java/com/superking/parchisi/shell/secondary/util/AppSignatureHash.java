package com.superking.parchisi.shell.secondary.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import com.superking.parchisi.shell.secondary.Utils;

public class AppSignatureHash {
  
  public static String getHash(Context context) {
    try {
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
              context.getPackageName(), PackageManager.GET_SIGNATURES);
      
      Signature[] signatures = packageInfo.signatures;
      if (signatures == null || signatures.length == 0) {
        throw new RuntimeException("No se encontró firma de la app");
      }
      
      String hash = Utils.sha256(signatures[0].toByteArray());
      Log.i("AppSignatureHash", "HASH: " + hash);
      return Utils.sha256(signatures[0].toByteArray());
    } catch (Exception e) {
      // Convertimos cualquier excepción en RuntimeException
      throw new RuntimeException("Error obteniendo la firma de la app", e);
    }
  }
}