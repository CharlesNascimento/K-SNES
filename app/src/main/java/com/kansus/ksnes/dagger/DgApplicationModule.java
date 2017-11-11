package com.kansus.ksnes.dagger;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DgApplicationModule {

    Application mApplication;

    public DgApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    public Context providesApplicationContext() {
        return mApplication;
    }
}