package com.example.tasksapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tasksapp.databinding.TaskItemListBinding;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<Task> tasks;
    private MainActivity mainActivity;

    public TaskAdapter(ArrayList<Task> tasks, MainActivity mainActivity) {
        this.tasks = tasks;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @NotNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        TaskItemListBinding taskItemListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.task_item_list, parent, false);
        return new TaskViewHolder(taskItemListBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull TaskViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Task task = tasks.get(position);
        holder.taskItemListBinding.setTask(task);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.addAndEditTask(true, task, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder{

        private TaskItemListBinding taskItemListBinding;

        public TaskViewHolder(@NonNull @NotNull TaskItemListBinding taskItemListBinding) {
            super(taskItemListBinding.getRoot());
            this.taskItemListBinding = taskItemListBinding;
        }
    }
}
