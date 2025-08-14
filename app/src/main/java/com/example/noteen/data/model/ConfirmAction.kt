package com.example.noteen.data.model

data class ConfirmAction(
    val title: String,
    val message: String,
    val confirmButtonText: String = "Yes",
    val cancelButtonText: String = "No",
    val action: suspend () -> Unit
)
