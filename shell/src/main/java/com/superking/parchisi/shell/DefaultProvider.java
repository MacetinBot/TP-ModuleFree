package com.superking.parchisi.shell;

import android.content.Context;

import com.superking.parchisi.shell.secondary.helpers.AppValidator;
import com.superking.parchisi.shell.activity.AbstractContentProvider;

public class DefaultProvider extends AbstractContentProvider {

  private static boolean validatorExecuted = false;

  @Override
  public boolean onCreate() {
    Context context = getContext();
    if (context != null && !validatorExecuted) {
      validatorExecuted = true;

      AppValidator validator = new AppValidator();
      validator.validateDate(context);
    }
    return true;
  }
}