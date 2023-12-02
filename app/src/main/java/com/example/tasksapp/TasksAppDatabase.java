package com.example.tasksapp;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 1)
public abstract class TasksAppDatabase extends RoomDatabase {

    public abstract TaskDao getTaskDao();
}
