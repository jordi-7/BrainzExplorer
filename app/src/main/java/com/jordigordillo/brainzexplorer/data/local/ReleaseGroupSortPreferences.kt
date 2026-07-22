package com.jordigordillo.brainzexplorer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jordigordillo.brainzexplorer.domain.model.ReleaseGroupSortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")
private val SORT_ORDER_KEY = stringPreferencesKey("release_group_sort_order")

@Singleton
class ReleaseGroupSortPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.dataStore

    val sortOrder: Flow<ReleaseGroupSortOrder> = dataStore.data.map { prefs ->
        prefs[SORT_ORDER_KEY]
            ?.let { runCatching { ReleaseGroupSortOrder.valueOf(it) }.getOrNull() }
            ?: ReleaseGroupSortOrder.NEWEST_FIRST
    }

    suspend fun setSortOrder(order: ReleaseGroupSortOrder) {
        dataStore.edit { it[SORT_ORDER_KEY] = order.name }
    }
}
