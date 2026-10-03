package com.hadzha3.goldbrain.di

import android.content.Context
import com.hadzha3.goldbrain.background.IndexRecoveryCoordinator
import com.hadzha3.goldbrain.data.analysis.MlKitImageAnalyzer
import com.hadzha3.goldbrain.data.gallery.GalleryIndexer
import com.hadzha3.goldbrain.data.gallery.MediaStoreGalleryMediaSource
import com.hadzha3.goldbrain.data.local.AppDatabase
import com.hadzha3.goldbrain.data.media.AndroidMediaAccessChecker
import com.hadzha3.goldbrain.data.media.PersistedMediaPermissionManager
import com.hadzha3.goldbrain.data.preferences.IndexingPreferences
import com.hadzha3.goldbrain.data.repository.IndexFailureRepository
import com.hadzha3.goldbrain.data.repository.IndexStatusRepository
import com.hadzha3.goldbrain.data.repository.MemoryRepository
import com.hadzha3.goldbrain.data.storage.LocalStorageManager
import com.hadzha3.goldbrain.domain.search.MemorySearchEngine

class AppContainer(
    context: Context
) {
    private val appContext =
        context.applicationContext

    private val database by lazy {
        AppDatabase.get(appContext)
    }

    private val imageAnalyzer by lazy {
        MlKitImageAnalyzer(appContext)
    }

    private val searchEngine by lazy {
        MemorySearchEngine()
    }

    private val mediaAccessChecker by lazy {
        AndroidMediaAccessChecker(
            appContext
        )
    }

    val indexingPreferences: IndexingPreferences by lazy {
        IndexingPreferences(
            appContext
        )
    }

    val localStorageManager: LocalStorageManager by lazy {
        LocalStorageManager(
            appContext
        )
    }

    val persistedMediaPermissionManager:
        PersistedMediaPermissionManager by lazy {
            PersistedMediaPermissionManager(
                appContext
            )
        }

    val memoryRepository: MemoryRepository by lazy {
        MemoryRepository(
            dao = database.memoryDao(),
            imageAnalyzer = imageAnalyzer,
            searchEngine = searchEngine,
            mediaAccessChecker =
                mediaAccessChecker
        )
    }

    val indexStatusRepository: IndexStatusRepository by lazy {
        IndexStatusRepository(
            dao = database.indexStateDao()
        )
    }

    val indexFailureRepository: IndexFailureRepository by lazy {
        IndexFailureRepository(
            dao = database.indexFailureDao()
        )
    }

    val indexRecoveryCoordinator:
        IndexRecoveryCoordinator by lazy {
            IndexRecoveryCoordinator(
                context = appContext,
                statusRepository =
                    indexStatusRepository,
                preferences =
                    indexingPreferences
            )
        }

    private val galleryMediaSource by lazy {
        MediaStoreGalleryMediaSource(
            appContext
        )
    }

    val galleryIndexer: GalleryIndexer by lazy {
        GalleryIndexer(
            repository =
                memoryRepository,
            mediaSource =
                galleryMediaSource,
            failureRepository =
                indexFailureRepository
        )
    }
}
