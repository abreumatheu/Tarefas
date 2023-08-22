package com.comunidadedevspace.taskbeats

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.Serializable

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private var taskList = arrayListOf(
        Task(0,"Academia", "Sempre malhar de segunda a sexta"),
        Task(1,"Trabalho", "Sempre chegar no horario"),
        Task(2,"Lazer", "Nunca se atrasar"),
        Task(3,"Familia", "Nunca se atrasar"),
        Task(4,"Familia", "Nunca se atrasar"),
        Task(5,"Familia", "Nunca se atrasar"),
        Task(6,"Familia", "Nunca se atrasar"),
    )

    private lateinit var ctnContent: LinearLayout

    private val adapter: TaskListAdapter by lazy{
        TaskListAdapter(::onListItemClicked)
    }

    private val dataBase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java, "tarefas-database"
        ).build()
    }

    private val dao by lazy {
        dataBase.taskDao()
    }

    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK){
            val data = result.data
            val taskAction = data?.getSerializableExtra(TASK_ACTION_RESULT) as TaskAction
            val task: Task = taskAction.task

            if(taskAction.actionType == ActionType.DELETE.name){
                val newList = arrayListOf<Task>()
                    .apply{
                        addAll(taskList)
                    }

                newList.remove(task)

                showMessage(ctnContent, "Tarefa ${task.title} Deletado")

                if(newList.size == 0){
                    ctnContent.visibility = View.VISIBLE
                }
                adapter.submitList(newList)
                taskList = newList

            }else if (taskAction.actionType == ActionType.CREATE.name){
                insertIntoDataBase(task)
            }else if(taskAction.actionType == ActionType.UPDATE.name){

                val tempEmptyList = arrayListOf<Task>()
                taskList.forEach {
                    if (it.id == task.id){
                        val newItem = Task(it.id, task.title, task.description)
                        tempEmptyList.add(newItem)
                    } else {
                        tempEmptyList.add(it)
                    }
                }

                showMessage(ctnContent, "Tarefa ${task.title} Alterada")
                adapter.submitList(tempEmptyList)
                taskList = tempEmptyList
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)






        listFromDataBase()
        ctnContent = findViewById(R.id.ctn_content)



        val rvTasks :RecyclerView = findViewById(R.id.rv_task_list)
        rvTasks.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener{
            openTaskListDetail(null)
        }

    }

    private fun insertIntoDataBase(task: Task){
        CoroutineScope(context = IO).launch {
            dao.insert(task)
        }
    }

    private fun listFromDataBase(){
        CoroutineScope(context = IO).launch {

            val myDataBaseList: List<Task> = dao.getAll()
            adapter.submitList(myDataBaseList)
        }
    }
    private fun showMessage(view: View, message:String ){
        Snackbar.make(view,message, Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .show()
    }

    private fun onListItemClicked(task: Task){
        openTaskListDetail(task)
    }

    private fun openTaskListDetail(task: Task?){
        val intent = TaskDetailActivity.start(this, task)
        startForResult.launch(intent)
    }
}

enum class  ActionType {
    DELETE,
    UPDATE,
    CREATE
}

data class TaskAction(
    val task: Task,
    val actionType: String
    ) : Serializable

const val TASK_ACTION_RESULT = "TASK_ACTION_RESULT"