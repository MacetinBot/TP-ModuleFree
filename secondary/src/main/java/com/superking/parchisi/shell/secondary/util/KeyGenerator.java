package com.superking.parchisi.shell.secondary.util;

import android.content.Context;
import com.superking.parchisi.shell.secondary.Utils;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyGenerator {
  
  public static SecretKey generateKey(Context context) {
    String combined = AppNameHash.getHash(context) +
            PackageNameHash.getHash(context) +
            AppSignatureHash.getHash(context);
    
    byte[] keyBytes = Utils.sha256Bytes(combined);
    return new SecretKeySpec(keyBytes, "AES");
  }
}