package com.example.teamup.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthToken(authToken: AuthToken)

    @Query("SELECT * FROM auth_tokens LIMIT 1")
    suspend fun getAuthToken(): AuthToken?
}