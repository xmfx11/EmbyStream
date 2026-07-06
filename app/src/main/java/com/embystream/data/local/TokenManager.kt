package com.embystream.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "emby_prefs")

object TokenManager {
    private const val KEY_SERVER = "server"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"

    private lateinit var dataStore: DataStore<Preferences>

    // 内存缓存，避免在 OkHttp 拦截器中使用 runBlocking
    @Volatile
    private var cachedToken: String? = null
    @Volatile
    private var cachedServer: String? = null
    @Volatile
    private var cachedUserId: String? = null

    fun init(context: Context) {
        dataStore = context.applicationContext.dataStore
    }

    suspend fun save(server: String, token: String, userId: String) {
        // 更新内存缓存
        cachedServer = server
        cachedToken = token
        cachedUserId = userId

        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(KEY_SERVER)] = server
            prefs[stringPreferencesKey(KEY_TOKEN)] = token
            prefs[stringPreferencesKey(KEY_USER_ID)] = userId
        }
    }

    suspend fun getServer(): String? {
        cachedServer?.let { return it }
        val server = dataStore.data.map { it[stringPreferencesKey(KEY_SERVER)] }.firstOrNull()
        cachedServer = server
        return server
    }

    suspend fun getToken(): String? {
        cachedToken?.let { return it }
        val token = dataStore.data.map { it[stringPreferencesKey(KEY_TOKEN)] }.firstOrNull()
        cachedToken = token
        return token
    }

    suspend fun getUserId(): String? {
        cachedUserId?.let { return it }
        val userId = dataStore.data.map { it[stringPreferencesKey(KEY_USER_ID)] }.firstOrNull()
        cachedUserId = userId
        return userId
    }

    fun getCachedToken(): String? = cachedToken

    suspend fun clear() {
        cachedServer = null
        cachedToken = null
        cachedUserId = null
        dataStore.edit { it.clear() }
    }

    suspend fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()
}
