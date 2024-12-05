package com.example.expense;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password LIMIT 1")
    User getUserByUsernameAndPassword(String username, String password);

    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);
}