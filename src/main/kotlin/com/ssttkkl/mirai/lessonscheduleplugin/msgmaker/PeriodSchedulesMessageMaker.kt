package com.ssttkkl.mirai.lessonscheduleplugin.msgmaker

import com.ssttkkl.mirai.lessonscheduleplugin.data.DBUtil.database
import com.ssttkkl.mirai.lessonscheduleplugin.data.Schedule
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.TextStyle
import java.util.*

object PeriodSchedulesMessageMaker : MessageMaker<SizedIterable<Schedule>> {
    override fun makePlain(data: SizedIterable<Schedule>): String = transaction(database) {
        data.groupBy { it.dayOfWeek }.toList().sortedBy { it.first.value }
            .joinToString("\n") { (dayOfWeek, schs) ->
                "------${dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}------\n" + schs.sortedBy { it.lessonOfDay }
                    .joinToString("\n") {
                        "(${it.lessonOfDay}) ${it.lesson.name}\t${it.location}"
                    }
            }
    }
}