package com.ssttkkl.mirai.lessonscheduleplugin.msgmaker

import com.ssttkkl.mirai.lessonscheduleplugin.data.DBUtil.database
import com.ssttkkl.mirai.lessonscheduleplugin.data.Lesson
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction

object LessonsMessageMaker : MessageMaker<SizedIterable<Lesson>> {
    override fun makePlain(data: SizedIterable<Lesson>): String = transaction(database) {
        data.joinToString("\n") { it.name }
    }
}