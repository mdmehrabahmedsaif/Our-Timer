package com.ourtimer.app.data

data class Challenge(
    val id: String,
    val name: String,
    val days: Int,
    val alterEgo: String,
    val startTime: Long,
    var why: String = "",
    var lastWhyPromptDate: String = "",
    val shownMilestones: MutableList<Int> = mutableListOf()
)
