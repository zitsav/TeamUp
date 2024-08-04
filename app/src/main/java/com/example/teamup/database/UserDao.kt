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

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): User?

    @Query("DELETE FROM auth_tokens")
    suspend fun deleteAuthToken()

    @Query("DELETE FROM users")
    suspend fun deleteUser()
}