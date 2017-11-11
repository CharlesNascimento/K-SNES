package com.kansus.ksnes.dagger;

import android.app.Activity;

import com.kansus.ksnes.activity.CheatsActivity;
import com.kansus.ksnes.activity.EmulatorActivity;
import com.kansus.ksnes.dagger.scopes.EmulatorScope;
import com.kansus.ksnes.dagger.scopes.PerActivity;

import dagger.Component;

/**
 * Created by charl on 26/08/2017.
 */
@PerActivity
@Component(dependencies = DgApplicationComponent.class,
        modules = {DgActivityModule.class, DgEmulatorModule.class})
public interface DgEmulatorComponent extends DgActivityComponent {

    void inject(EmulatorActivity activity);

    void inject(CheatsActivity activity);
}
