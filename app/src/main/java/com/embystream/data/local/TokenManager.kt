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
    
    fun init(context: Context) {
        dataStore = context.applicationContext.dataStore
    }
    
    suspend fun save(server: String, token: String, userId: String) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(KEY_SERVER)] = server
            prefs[stringPreferencesKey(KEY_TOKEN)] = token
            prefs[stringPreferencesKey(KEY_USER_ID)] = userId
        }
    }
    
    suspend fun getServer(): String? = 
        dataStore.data.map { it[stringPreferencesKey(KEY_SERVER)] }.firstOrNull()
    
    suspend fun getToken(): String? = 
        dataStore.data.map { it[stringPreferencesKey(KEY_TOKEN)] }.firstOrNull()
    
    suspend fun getUserId(): String? = 
        dataStore.data.map { it[stringPreferencesKey(KEY_USER_ID)] }.firstOrNull()
    
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
    
    suspend fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()
}
