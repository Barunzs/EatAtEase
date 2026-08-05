package com.shop.eatatease

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Application class — initialised once when the process starts.
 *
 * Enables Firestore **offline persistence** (disk cache) so the app can:
 *  - Read cached data instantly while the network loads fresh data
 *  - Continue serving the last known data when the device is offline
 *  - Automatically sync pending writes when connectivity is restored
 */
class EatAtEaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        enableFirestoreOfflinePersistence()
    }

    private fun enableFirestoreOfflinePersistence() {
        try {
            val cacheSettings = PersistentCacheSettings.newBuilder()
                // Unlimited disk cache — set a byte limit here if you want a cap
                // e.g. .setSizeBytes(100L * 1024 * 1024) for 100 MB
                .build()

            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(cacheSettings)
                .build()

            FirebaseFirestore.getInstance().firestoreSettings = settings

            Log.i("EatAtEaseApp", "Firestore offline persistence enabled (PersistentCache)")
        } catch (e: Exception) {
            // Settings must be applied before any Firestore use;
            // log the error but don't crash the app.
            Log.e("EatAtEaseApp", "Failed to configure Firestore offline persistence", e)
        }
    }
}
