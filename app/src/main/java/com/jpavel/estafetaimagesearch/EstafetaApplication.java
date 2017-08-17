package com.jpavel.estafetaimagesearch;

import android.app.Application;

import io.realm.Realm;

public class EstafetaApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Realm.init(this);
  }
}