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
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
fun CoroutineScope.newScheduleProducer(capacity: Int = 16) = produce(capacity = capacity) {
    var nextLesson = LocalTime.now().let { now ->
        GeneralConfig.lessonTimetableAsTime.indexOfFirst { it > now }
    }

    while (true) {
        val now = OffsetDateTime.now()
        val today = now.toLocalDate()
        val nextTime = if (nextLesson == -1) {
            nextLesson = 0
            GeneralConfig.lessonTimetableAsTime[0].atDate(today.plusDays(1))
        } else {
            GeneralConfig.lessonTimetableAsTime[nextLesson].atDate(today)
        }.atOffset(now.offset)

        MiraiLessonSchedulePlugin.logger.info("下一次课程通知将于${nextTime}进行")
        delay(1000 * (nextTime.toEpochSecond() - now.toEpochSecond() - GeneralConfig.scheduleNotifyBefore))

        // 注意：这里可能已经过去了一天，所以要重新获取today
        val weekOfToday = GeneralConfig.weekOf(LocalDate.now())

        transaction(database) {
            MiraiLessonSchedulePlugin.logger.info("正在确认第${nextLesson + 1}节课的课程安排……")
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

        nextLesson++
        if (nextLesson == GeneralConfig.lessonTimetable.size)
            nextLesson = -1 // 下一节课是明天第一节
    }
}