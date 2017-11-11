package com.kansus.ksnes.dagger;

import android.app.Activity;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = DgApplicationModule.class)
public interface DgApplicationComponent {

    void inject(Activity baseActivity);

    //Exposed to sub-graphs.
    Context context();
}