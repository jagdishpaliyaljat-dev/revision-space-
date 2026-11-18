package com.example.ui.utils
import com.example.ui.*

import com.example.ui.screens.auth.*
import com.example.ui.screens.home.*
import com.example.ui.screens.revision.*
import com.example.ui.screens.subject.*
import com.example.ui.screens.profile.*
import com.example.ui.screens.rewards.*
import com.example.ui.screens.settings.*
import com.example.ui.screens.admin.*
import com.example.ui.components.*
import com.example.ui.utils.*

fun formatEstimatedTime(mins: Int): String {
    if (mins == 0) return "0 min"
    if (mins < 60) return "$mins min"
    val h = mins / 60
    val m = mins % 60
    if (m == 0) return "${h}h"
    return "${h}h ${m}m"
}
