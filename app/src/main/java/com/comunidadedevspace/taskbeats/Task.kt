package com.comunidadedevspace.taskbeats

import androidx.room.PrimaryKey
import java.io.Serializable

data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String
    ): Serializable
