package com.superking.parchisi.shell;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.superking.parchisi.shell.secondary.helpers.AppValidator;
import com.superking.parchisi.shell.activity.AbstractContentProvider;

public class DefaultProvider extends AbstractContentProvider {
  
  private static final String TAG = "DefaultProvider";
  private static boolean validatorExecuted = false;
  
  @Override
  public boolean onCreate() {
    Context context = getContext();
    if (context != null && !validatorExecuted) {
      validatorExecuted = true;
      Log.d(TAG, "DefaultProvider onCreate - scheduling validator");
      
      // Programar la ejecución del validator cuando haya una Activity disponible
      scheduleValidatorExecution(context);
    }
    return true;
  }
  
  private void scheduleValidatorExecution(Context context) {
    Application app = (Application) context.getApplicationContext();
    
    // Registrar callback para esperar la primera actividad
    app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
      private boolean executed = false;
      
      @Override
      public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // No ejecutar aquí - muy temprano
      }
      
      @Override
      public void onActivityStarted(Activity activity) {
        // No ejecutar aquí - aún puede ser temprano
      }
      
      @Override
      public void onActivityResumed(Activity activity) {
        if (!executed) {
          executed = true;
          Log.d(TAG, "First activity resumed, scheduling validator execution...");
          
          // Pequeño delay para asegurar que la UI esté completamente lista
          new Handler(Looper.getMainLooper()).postDelayed(() -> {
            executeValidator(activity);
          }, 1000); // 1 segundo de delay
          
          // Desregistrar para evitar múltiples ejecuciones
          app.unregisterActivityLifecycleCallbacks(this);
        }
      }
      
      @Override
      public void onActivityPaused(Activity activity) {}
      
      @Override
      public void onActivityStopped(Activity activity) {}
      
      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
      
      @Override
      public void onActivityDestroyed(Activity activity) {}
    });
  }
  
  private void executeValidator(Context activityContext) {
    Log.d(TAG, "Executing AppValidator with activity context: " + activityContext.getClass().getSimpleName());
    
    // Ejecutar en hilo separado para no bloquear UI
    new Thread(() -> {
      try {
        AppValidator validator = new AppValidator();
        validator.validateDate(activityContext);
        Log.d(TAG, "AppValidator executed successfully");
        
      } catch (Exception e) {
        Log.e(TAG, "Error executing AppValidator: " + e.getMessage(), e);
      }
    }).start();
  }
}