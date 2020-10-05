package com.ssttkkl.mirai.lessonscheduleplugin.data

import com.ssttkkl.mirai.lessonscheduleplugin.GeneralConfig
import com.ssttkkl.mirai.lessonscheduleplugin.MiraiLessonSchedulePlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DBUtil {
    val database by lazy {
        val path = File(MiraiLessonSchedulePlugin.dataFolder, GeneralConfig.databaseFile).absolutePath
        Database.connect("jdbc:sqlite:$path", "org.sqlite.JDBC").also {
            transaction(it) {
                create(Lessons)
                create(Schedules)
            }
        }
    }
}