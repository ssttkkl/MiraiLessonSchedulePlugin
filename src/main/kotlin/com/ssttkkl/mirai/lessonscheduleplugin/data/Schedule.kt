package com.ssttkkl.mirai.lessonscheduleplugin.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.time.DayOfWeek

object Schedules : IntIdTable() {
    val location = varchar("location", 50)
    val fromWeek = integer("from_week")
    val toWeek = integer("to_week")
    val dayOfWeek = integer("day_of_week")
    val lessonOfDay = integer("lesson_of_day")
    val lessonId = reference("lesson_id", Lessons)
}

class Schedule(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Schedule>(Schedules)

    var location by Schedules.location
    private var fromWeek by Schedules.fromWeek
    private var toWeek by Schedules.toWeek
    private var dayOfWeekValue by Schedules.dayOfWeek
    var lessonOfDay by Schedules.lessonOfDay
    var lesson by Lesson referencedOn Schedules.lessonId

    var dayOfWeek: DayOfWeek
        get() = DayOfWeek.of(dayOfWeekValue)
        set(value) {
            dayOfWeekValue = value.value
        }

    var weekRange: WeekRange
        get() = WeekRange(fromWeek, toWeek)
        set(value) {
            fromWeek = value.start
            toWeek = value.endInclusive
        }
}