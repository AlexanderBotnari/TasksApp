package com.example.tasksapp;

import androidx.room.*;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    public long addTask(Task task);

    @Update
    public void editTask(Task task);

    @Delete
    public void deleteTask(Task task);

    @Query("select * from tasks")
    public List<Task> getAllTask();

    @Query("select * from tasks where task_id==:taskId")
    public Task getTaskById(long taskId);
}
