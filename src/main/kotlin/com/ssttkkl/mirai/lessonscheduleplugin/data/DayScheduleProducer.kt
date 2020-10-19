package com.ssttkkl.mirai.lessonscheduleplugin.data

import com.ssttkkl.mirai.lessonscheduleplugin.GeneralConfig
import com.ssttkkl.mirai.lessonscheduleplugin.MiraiLessonSchedulePlugin
import com.ssttkkl.mirai.lessonscheduleplugin.data.DBUtil.database
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
fun CoroutineScope.newDayScheduleProducer() = produce {
    while (true) {
        try {
            var now = OffsetDateTime.now()
            var today = now.toLocalDate()
            val nextNotifyTime = GeneralConfig.dayScheduleNotifyTimeAsTime.atDate(
                if (now.toLocalTime() > GeneralConfig.dayScheduleNotifyTimeAsTime)
                    today.plusDays(1)
                else
                    today
            ).atOffset(now.offset)

            delay(1000 * (nextNotifyTime.toEpochSecond() - now.toEpochSecond()))

            now = OffsetDateTime.now()
            today = now.toLocalDate()
            val schs = transaction(database) {
                val weekOfToday = GeneralConfig.weekOf(today)
                Schedule.find {
                    (Schedules.fromWeek lessEq weekOfToday) and
                        (Schedules.toWeek greaterEq weekOfToday) and
                        (Schedules.dayOfWeek eq today.dayOfWeek.value)
                }.notForUpdate()
            }
            send(schs)
        } catch (exc: CancellationException) {
            throw exc
        } catch (exc: Exception) {
            MiraiLessonSchedulePlugin.logger.error(exc)
        }
    }
}