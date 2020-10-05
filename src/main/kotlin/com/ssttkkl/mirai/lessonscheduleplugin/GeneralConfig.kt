package com.ssttkkl.mirai.lessonscheduleplugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object GeneralConfig : AutoSavePluginConfig("general") {
    var databaseFile by value("database.db")

    var dateFormat by value("yyyy-MM-dd")
    val dateFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(dateFormat)

    var timeFormat by value("HH:mm")
    val timeFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(timeFormat)

    var mondayOfFirstWeek by value("1970-01-01")
    val mondayOfFirstWeekAsDate: LocalDate
        get() = LocalDate.parse(mondayOfFirstWeek, dateFormatter)

    fun weekOf(date: LocalDate): Int {
        val mondayOfDate = date.minusDays((date.dayOfWeek.value - 1).toLong())
        return (mondayOfDate.toEpochDay() - mondayOfFirstWeekAsDate.toEpochDay()).toInt() / 7 + 1
    }

    var lessonTimetable by value(listOf("08:00", "10:05", "14:00", "16:05"))
    val lessonTimetableAsTime: List<LocalTime>
        get() = lessonTimetable.map { LocalTime.parse(it, timeFormatter) }.sorted()

    var notifies by value<Map<Long, BotNotify>>(emptyMap())
    var scheduleNotifyBefore by value(10 * 60 * 1000)

    var dayScheduleNotifyEnabled by value(true)

    var dayScheduleNotifyTime by value("07:00")
    val dayScheduleNotifyTimeAsTime
        get() = LocalTime.parse(dayScheduleNotifyTime, timeFormatter)
}