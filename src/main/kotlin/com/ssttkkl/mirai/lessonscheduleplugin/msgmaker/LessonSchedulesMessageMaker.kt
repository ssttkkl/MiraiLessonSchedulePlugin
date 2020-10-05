package com.ssttkkl.mirai.lessonscheduleplugin.msgmaker

import com.ssttkkl.mirai.lessonscheduleplugin.data.DBUtil.database
import com.ssttkkl.mirai.lessonscheduleplugin.data.Schedule
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

object LessonSchedulesMessageMaker : MessageMaker<SizedIterable<Schedule>> {
    private data class GroupKey(
        val dayOfWeek: DayOfWeek,
        val lessonOfDay: Int,
        val location: String
    )

    override fun makePlain(data: SizedIterable<Schedule>): String = transaction(database) {
        data.groupBy { it.lesson }.toList()
            .joinToString("\n") { (lesson, schs) ->
                lesson.name + '\n' + schs.groupBy { GroupKey(it.dayOfWeek, it.lessonOfDay, it.location) }.toList()
                    .sortedBy { it.first.lessonOfDay }.sortedBy { it.first.dayOfWeek }
                    .joinToString("\n") {
                        val (dayOfWeek, lessonOfDay, location) = it.first

                        "第[${it.second.joinToString(", ") { it.weekRange.toString() }}]周\t" +
                            "${dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}\t" +
                            "第${lessonOfDay}节\t" +
                            "地点：${location}"
                    }
            }
    }
}