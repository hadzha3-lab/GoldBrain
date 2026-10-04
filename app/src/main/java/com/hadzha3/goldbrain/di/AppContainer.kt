package com.hadzha3.goldbrain.di

import android.content.Context
import com.hadzha3.goldbrain.background.AndroidIndexingLoadProvider
import com.hadzha3.goldbrain.background.IndexRecoveryCoordinator
import com.hadzha3.goldbrain.data.analysis.MlKitImageAnalyzer
import com.hadzha3.goldbrain.data.gallery.GalleryIndexer
import com.hadzha3.goldbrain.data.gallery.MediaStoreGalleryMediaSource
import com.hadzha3.goldbrain.data.local.AppDatabase
import com.hadzha3.goldbrain.data.media.AndroidMediaAccessChecker
import com.hadzha3.goldbrain.data.media.AndroidPhotoSourceDetector
import com.hadzha3.goldbrain.data.media.MediaStoreChangeTracker
import com.hadzha3.goldbrain.data.media.PersistedMediaPermissionManager
import com.hadzha3.goldbrain.data.preferences.IndexingPreferences
import com.hadzha3.goldbrain.data.repository.IndexFailureRepository
import com.hadzha3.goldbrain.data.repository.IndexLookupRepository
import com.hadzha3.goldbrain.data.repository.IndexStatusRepository
import com.hadzha3.goldbrain.data.repository.MemoryIndexMaintenanceRepository
import com.hadzha3.goldbrain.data.repository.MemoryRepository
import com.hadzha3.goldbrain.data.storage.LocalStorageManager
import com.hadzha3.goldbrain.domain.search.MemorySearchEngine
import com.hadzha3.goldbrain.domain.source.PhotoSourceClassifier

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

    private val photoSourceClassifier by lazy {
        PhotoSourceClassifier(
            appContext.packageName
        )
    }

    private val photoSourceDetector by lazy {
        AndroidPhotoSourceDetector(
            context = appContext,
            classifier =
                photoSourceClassifier
        )
    }

    private val mediaAccessChecker by lazy {
        AndroidMediaAccessChecker(
            appContext
        )
    }

    val indexingLoadProvider:
        AndroidIndexingLoadProvider by lazy {
            AndroidIndexingLoadProvider(
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
            context = appContext,
            database = database
        )
    }

    val mediaStoreChangeTracker:
        MediaStoreChangeTracker by lazy {
            MediaStoreChangeTracker(
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
                mediaAccessChecker,
            sourceDetector =
                photoSourceDetector
        )
    }

    val memoryIndexMaintenanceRepository:
        MemoryIndexMaintenanceRepository by lazy {
            MemoryIndexMaintenanceRepository(
                database
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

    val indexLookupRepository: IndexLookupRepository by lazy {
        IndexLookupRepository(
            dao = database.indexLookupDao()
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
            context = appContext,
            sourceClassifier =
                photoSourceClassifier
        )
    }

    val galleryIndexer: GalleryIndexer by lazy {
        GalleryIndexer(
            repository =
                memoryRepository,
            mediaSource =
                galleryMediaSource,
            failureRepository =
                indexFailureRepository,
            maintenanceRepository =
                memoryIndexMaintenanceRepository,
            lookupRepository =
                indexLookupRepository
        )
    }
}
