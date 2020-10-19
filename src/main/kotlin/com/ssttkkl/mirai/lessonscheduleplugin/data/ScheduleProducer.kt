package com.ssttkkl.mirai.lessonscheduleplugin.data

import com.ssttkkl.mirai.lessonscheduleplugin.GeneralConfig
import com.ssttkkl.mirai.lessonscheduleplugin.MiraiLessonSchedulePlugin
import com.ssttkkl.mirai.lessonscheduleplugin.data.DBUtil.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalTime
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
fun CoroutineScope.newScheduleProducer(capacity: Int = 16) = produce(capacity = capacity) {
    val now = LocalTime.now()
    var nextLesson = GeneralConfig.lessonTimetableAsTime.indexOfFirst { it > now }

    while (true) {
        var now = OffsetDateTime.now()
        var today = now.toLocalDate()
        val nextTime = if (nextLesson == -1) {
            GeneralConfig.lessonTimetableAsTime[0].atDate(today.plusDays(1))
        } else {
            GeneralConfig.lessonTimetableAsTime[nextLesson].atDate(today)
        }.atOffset(now.offset)

        delay(1000 * (nextTime.toEpochSecond() - now.toEpochSecond()) - GeneralConfig.scheduleNotifyBefore)

        MiraiLessonSchedulePlugin.logger.info("正在确认第${++nextLesson}节的课程安排……")
        now = OffsetDateTime.now()
        today = now.toLocalDate()
        val weekOfToday = GeneralConfig.weekOf(today)

        transaction(database) {
            Schedule.find {
                (Schedules.fromWeek lessEq weekOfToday) and
                    (Schedules.toWeek greaterEq weekOfToday) and
                    (Schedules.dayOfWeek eq today.dayOfWeek.value) and
                    (Schedules.lessonOfDay eq nextLesson)
            }.map { it.lesson.name to it }
        }.forEach {
            MiraiLessonSchedulePlugin.logger.info("课程通知：${it.first}")
            send(it.second)
        }
    }
}