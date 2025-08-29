package com.superking.parchisi.shell.secondary.util;

import android.content.Context;
import com.superking.parchisi.shell.secondary.Utils;

public class PackageNameHash {
  public static String getHash(Context context) {
    return Utils.sha256(context.getPackageName());
  }
}