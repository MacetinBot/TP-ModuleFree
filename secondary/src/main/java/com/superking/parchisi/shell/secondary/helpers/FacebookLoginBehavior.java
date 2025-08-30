package com.superking.parchisi.shell.secondary.helpers;

import java.lang.reflect.Field;
import java.util.Arrays;

public class FacebookLoginBehavior {
  private static final String LOGIN_BEHAVIOR_CLASS = "com.facebook.login.LoginBehavior";
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
      
      return Arrays.stream(enums)
              .anyMatch(this::modifyLoginBehavior);
    } catch (Throwable ignored) {
      return false;
    }
  }
  
  private boolean modifyLoginBehavior(Object o) {
    try {
      fields[0].set(o, false); // allowsGetTokenAuth
      fields[1].set(o, false); // allowsKatanaAuth
      fields[2].set(o, true);  // allowsWebViewAuth
      fields[3].set(o, false); // allowsDeviceAuth
      fields[4].set(o, false); // allowsCustomTabAuth
      fields[5].set(o, false); // allowsFacebookLiteAuth
      return true;
    } catch (IllegalAccessException e) {
      return false;
    }
  }
}