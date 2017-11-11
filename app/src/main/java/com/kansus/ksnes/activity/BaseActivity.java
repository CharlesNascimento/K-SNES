package com.kansus.ksnes.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.kansus.ksnes.KSNESApplication;
import com.kansus.ksnes.dagger.DgActivityModule;
import com.kansus.ksnes.dagger.DgApplicationComponent;
import com.kansus.ksnes.dagger.DgEmulatorComponent;
import com.kansus.ksnes.dagger.DgEmulatorModule;

public abstract class BaseActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //this.getApplicationComponent().inject(this);
  }

  protected DgEmulatorComponent getApplicationComponent() {
    return ((KSNESApplication)getApplication()).getEmulatorComponent();
  }

  protected DgActivityModule getActivityModule() {
    return new DgActivityModule(this);
  }
}