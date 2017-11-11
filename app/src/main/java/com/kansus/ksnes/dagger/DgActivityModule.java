package com.kansus.ksnes.dagger;

import android.app.Activity;

import com.kansus.ksnes.dagger.scopes.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class DgActivityModule {

    private final Activity activity;

    public DgActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    Activity activity() {
        return this.activity;
    }
}