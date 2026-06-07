package com.ourtimer.app.data

data class Challenge(
    val id: String,
    val name: String,
    val totalDays: Int,
    val alterEgo: String,
    val startDate: Long,
    var why: String = "",
    var milestoneShown: Int = 0
)
