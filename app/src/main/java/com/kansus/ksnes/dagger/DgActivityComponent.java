package com.kansus.ksnes.dagger;

import android.app.Activity;

import com.kansus.ksnes.dagger.scopes.PerActivity;

import dagger.Component;

@PerActivity
@Component(modules = DgActivityModule.class)
public interface DgActivityComponent {

    //Exposed to sub-graphs.
    Activity activity();
}