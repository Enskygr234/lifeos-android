package com.lifeos.app

import android.app.Application
import com.lifeos.app.data.AppDatabase
import com.lifeos.app.data.Repository
import com.lifeos.app.data.SettingsStore
import com.lifeos.app.work.ReminderScheduler

class LifeOSApp : Application() {
    lateinit var settingsStore: SettingsStore
        private set
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsStore = SettingsStore(this)
        repository = Repository(this, AppDatabase.build(this), settingsStore)
        ReminderScheduler.createChannels(this)
    }
}
