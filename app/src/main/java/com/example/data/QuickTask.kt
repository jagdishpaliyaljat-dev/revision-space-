package com.example.data
import com.example.ui.*


import com.google.firebase.firestore.PropertyName

data class QuickTask(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var subject: String = "",
    @get:PropertyName("isOneTime")
    @set:PropertyName("isOneTime")
    var isOneTime: Boolean = true,
    @get:PropertyName("isCarryForward")
    @set:PropertyName("isCarryForward")
    var isCarryForward: Boolean = false,
    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    var dateCreatedStr: String = "",
    var completedDateStr: String = ""
)
