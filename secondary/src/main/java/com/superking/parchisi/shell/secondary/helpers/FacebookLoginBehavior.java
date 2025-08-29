package com.superking.parchisi.shell.secondary.helpers;

import android.content.Intent;
import android.net.Uri;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class FacebookLoginBehavior {
  private static final String LOGIN_BEHAVIOR_CLASS = "com.facebook.login.LoginBehavior";
  private static final String CUSTOM_TAB_LOGIN_METHOD_HANDLER = "com.facebook.login.CustomTabLoginMethodHandler";
  private static Field[] fields;
  private static Class<?> loginBehaviorClass;

  static {
    try {
      loginBehaviorClass = Class.forName(LOGIN_BEHAVIOR_CLASS);
      fields = new Field[]{
              getField("allowsGetTokenAuth"),
              getField("allowsKatanaAuth"),
              getField("allowsWebViewAuth"),
              getField("allowsDeviceAuth"),
              getField("allowsCustomTabAuth"),
              getField("allowsFacebookLiteAuth")
      };
    } catch (Throwable ignored) {
      fields = null;
    }
  }

  private static Field getField(String name) throws NoSuchFieldException {
    Field f = loginBehaviorClass.getDeclaredField(name);
    f.setAccessible(true);
    return f;
  }

  public boolean init() {
    if (fields == null) return false;

    try {
      Object[] enums = loginBehaviorClass.getEnumConstants();
      if (enums == null || enums.length == 0) return false;

      // Modificar comportamiento de login
      boolean modified = Arrays.stream(enums)
              .anyMatch(this::modifyLoginBehavior);

      // Interceptar y modificar Custom Tabs
      if (modified) {
        modifyCustomTabUserAgent();
      }

      return modified;
    } catch (Throwable ignored) {
      return false;
    }
  }

  private boolean modifyLoginBehavior(Object o) {
    try {
      fields[0].set(o, false); // allowsGetTokenAuth
      fields[1].set(o, false); // allowsKatanaAuth
      fields[2].set(o, false); // allowsWebViewAuth
      fields[3].set(o, false); // allowsDeviceAuth
      fields[4].set(o, true);  // allowsCustomTabAuth
      fields[5].set(o, false); // allowsFacebookLiteAuth
      return true;
    } catch (IllegalAccessException e) {
      return false;
    }
  }

  private void modifyCustomTabUserAgent() {
    try {
      // Obtener clase CustomTabLoginMethodHandler
      Class<?> customTabHandlerClass = Class.forName(CUSTOM_TAB_LOGIN_METHOD_HANDLER);

      // Obtener el método que crea el Intent para Custom Tabs
      Method getLoginIntentMethod = Arrays.stream(customTabHandlerClass.getDeclaredMethods())
              .filter(m -> m.getReturnType().equals(Intent.class))
              .findFirst()
              .orElse(null);

      if (getLoginIntentMethod != null) {
        getLoginIntentMethod.setAccessible(true);

        // Crear un proxy para interceptar el Intent
        Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                customTabHandlerClass.getClassLoader(),
                new Class[]{customTabHandlerClass},
                (obj, method, args) -> {
                  if (method.getName().equals(getLoginIntentMethod.getName())) {
                    Intent intent = (Intent) method.invoke(obj, args);
                    Uri uri = intent.getData();
                    if (uri != null) {
                      // Agregar parámetro &m2w para forzar vista de escritorio
                      Uri modifiedUri = Uri.parse(uri.toString() + "&m2w");
                      intent.setData(modifiedUri);
                      // Opcional: Modificar User-Agent (requiere Custom Tabs extras)
                      intent.putExtra("android.support.customtabs.extra.USER_AGENT", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                      // Agregar zoom inicial (150%)
                      intent.putExtra("android.support.customtabs.extra.INITIAL_SCALE", 150);
                    }
                    return intent;
                  }
                  return method.invoke(obj, args);
                });

        // Reemplazar la instancia de CustomTabLoginMethodHandler en el SDK
        Field loginMethodHandlerField = loginBehaviorClass.getDeclaredField("loginMethodHandler");
        loginMethodHandlerField.setAccessible(true);
        loginMethodHandlerField.set(null, proxy);
      }
    } catch (Throwable ignored) {
      // Manejar errores silenciosamente
    }
  }
}