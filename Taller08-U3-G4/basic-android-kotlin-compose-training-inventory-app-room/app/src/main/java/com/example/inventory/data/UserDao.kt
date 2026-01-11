package com.example.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Query(
        "SELECT * FROM users " +
                "WHERE username = :username AND password = :password " +
                "LIMIT 1"
    )
    suspend fun login(
        username: String,
        password: String
    ): User?
}
