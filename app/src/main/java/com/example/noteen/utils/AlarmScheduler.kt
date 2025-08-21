package com.example.noteen.utils

import com.example.noteen.viewmodel.TaskGroup

interface AlarmScheduler {
    fun schedule(task: TaskGroup)
    fun cancel(task: TaskGroup)
}
