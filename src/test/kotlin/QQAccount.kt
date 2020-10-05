package com.ssttkkl.mirai.lessonscheduleplugin

import kotlinx.serialization.Serializable

@Serializable
data class QQAccount(
    val id: Long,
    val password: String
)