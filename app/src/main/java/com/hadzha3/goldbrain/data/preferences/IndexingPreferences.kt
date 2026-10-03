package com.hadzha3.goldbrain.data.preferences

import android.content.Context

class IndexingPreferences(
    context: Context
) {
    private val preferences =
        context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )

    var autoIndexEnabled: Boolean
        get() =
            preferences.getBoolean(
                KEY_AUTO_INDEX,
                true
            )
        set(value) {
            preferences.edit()
                .putBoolean(
                    KEY_AUTO_INDEX,
                    value
                )
                .apply()
        }

    private companion object {
        const val FILE_NAME =
            "goldbrain_indexing_preferences"

        const val KEY_AUTO_INDEX =
            "auto_index_enabled"
    }
}
