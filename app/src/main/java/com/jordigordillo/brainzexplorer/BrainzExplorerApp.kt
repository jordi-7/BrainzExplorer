package com.jordigordillo.brainzexplorer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BrainzExplorerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (resources.getBoolean(R.bool.debug_mode)) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
