package com.superking.parchisi.shell.secondary.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;
import com.superking.parchisi.shell.secondary.Utils;

public class AppNameHash {
  public static String getHash(Context context) {
    String appName = getAppNameInSpanish(context);
    return Utils.sha256(appName);
  }
  
  private static String getAppNameInSpanish(Context context) {
    Resources res = context.getResources();
    Configuration conf = new Configuration(res.getConfiguration());
    
    conf.setLocale(new Locale("es"));
    
    Resources spanishRes = new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    int labelRes = context.getApplicationInfo().labelRes;
    
    if (labelRes != 0) {
      return spanishRes.getString(labelRes);
    } else {
      CharSequence nonLocalized = context.getApplicationInfo().nonLocalizedLabel;
      return nonLocalized != null ? nonLocalized.toString() : "";
    }
  }
}