package com.feelbloom.app

import android.app.Application
import com.feelbloom.app.data.DataStoreEmotionRepository
import com.feelbloom.app.data.EmotionRepository
import com.feelbloom.app.data.feelBloomDataStore

class FeelBloomApp : Application() {

    /** Una única instancia del repositorio para toda la app. */
    val repository: EmotionRepository by lazy { DataStoreEmotionRepository(feelBloomDataStore) }
}
