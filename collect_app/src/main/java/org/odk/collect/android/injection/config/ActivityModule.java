package org.odk.collect.android.injection.config;

import org.odk.collect.android.activities.FirstLaunchActivity;
import org.odk.collect.android.authentication.LoginActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract FirstLaunchActivity contributesFirstLaunchActivity();
    
    @ContributesAndroidInjector
    abstract LoginActivity contributesLoginActivity();
} 