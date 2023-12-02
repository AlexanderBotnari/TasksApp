package com.example.tasksapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.tasksapp.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter taskAdapter;
    private ArrayList<Task> tasksArrayList = new ArrayList<>();
    private TasksAppDatabase tasksAppDatabase;

    private ActivityMainBinding activityMainBinding;
    private ActivityMainButtonsHandler activityMainButtonsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        tasksAppDatabase = Room.databaseBuilder(this, TasksAppDatabase.class, "TasksDb").build();
        activityMainButtonsHandler = new ActivityMainButtonsHandler(this);
        activityMainBinding.setHandler(activityMainButtonsHandler);

        taskAdapter = new TaskAdapter(tasksArrayList, MainActivity.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(taskAdapter);

        new GetAllTasksAsyncTask().execute();

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
                Snackbar snackbar = Snackbar.make(activityMainBinding.mainConstraintLayout, "Item Deleted!", Snackbar.LENGTH_LONG);
                snackbar.show();
                deleteTask(tasksArrayList.get(viewHolder.getAdapterPosition()), viewHolder.getAdapterPosition());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(activityMainBinding.recyclerView);

    }

    public class ActivityMainButtonsHandler{
        private Context context;
        public ActivityMainButtonsHandler(Context context) {
            this.context = context;
        }

        public void onAddButtonClicked(View view){
            addAndEditTask(false, null, -1);
        }
    }

    public void addAndEditTask(boolean isUpdate, Task task, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View view = layoutInflater.inflate(R.layout.layout_add_task, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        TextView newTaskTitle = view.findViewById(R.id.newTaskTitle);
        EditText taskTitleEditText = view.findViewById(R.id.taskTitleEditText);
        EditText taskTextEditText = view.findViewById(R.id.taskTextEditText);

        newTaskTitle.setText(!isUpdate ? "Add Task" : "Edit Task");

        if(isUpdate && task != null){
            taskTitleEditText.setText(task.getTaskTitle());
            taskTextEditText.setText(task.getTaskText());
        }

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(isUpdate ? "Update" : "Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(taskTitleEditText.getText().toString())){
                    Toast.makeText(MainActivity.this, "Please input task title", Toast.LENGTH_LONG).show();
                    return;
                }else if (TextUtils.isEmpty(taskTextEditText.getText().toString())){
                    Toast.makeText(MainActivity.this, "Please input task text", Toast.LENGTH_LONG).show();
                    return;
                }else {
                    alertDialog.dismiss();
                }

                if (isUpdate && task != null){
                    updateTask(
                            taskTitleEditText.getText().toString(),
                            taskTextEditText.getText().toString(),
                            position
                    );
                }else {
                    createTask(
                            taskTitleEditText.getText().toString(),
                            taskTextEditText.getText().toString()
                    );
                }
            }
        });
    }

    private void createTask(String taskTitle, String taskText){
        new CreateTaskAsyncTask().execute(new Task(0, taskTitle, taskText));
    }

    private void updateTask(String taskTitle, String taskText, int position){
        Task task = tasksArrayList.get(position);

        task.setTaskTitle(taskTitle);
        task.setTaskText(taskText);
        new UpdateTaskAsyncTask().execute(task);

        tasksArrayList.set(position, task);
    }

    private void deleteTask(Task task, int position){
        new DeleteTaskAsyncTask().execute(task);
        tasksArrayList.remove(position);
    }

    private class CreateTaskAsyncTask extends AsyncTask<Task, Void, Void>{

        @Override
        protected Void doInBackground(Task... tasks) {
            long id = tasksAppDatabase.getTaskDao().addTask(tasks[0]);
            Task task = tasksAppDatabase.getTaskDao().getTaskById(id);
            if(task != null){
                tasksArrayList.add(task);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            taskAdapter.notifyDataSetChanged();
            super.onPostExecute(unused);
        }
    }

    private class GetAllTasksAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            tasksArrayList.addAll(tasksAppDatabase.getTaskDao().getAllTask());
            return null;
        }
    }

    private class UpdateTaskAsyncTask extends AsyncTask<Task, Void, Void>{

        @Override
        protected Void doInBackground(Task... tasks) {
            tasksAppDatabase.getTaskDao().editTask(tasks[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            taskAdapter.notifyDataSetChanged();
            super.onPostExecute(unused);
        }
    }

    private class DeleteTaskAsyncTask extends AsyncTask<Task, Void, Void>{

        @Override
        protected Void doInBackground(Task... tasks) {
            tasksAppDatabase.getTaskDao().deleteTask(tasks[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            taskAdapter.notifyDataSetChanged();
            super.onPostExecute(unused);
        }
    }

}