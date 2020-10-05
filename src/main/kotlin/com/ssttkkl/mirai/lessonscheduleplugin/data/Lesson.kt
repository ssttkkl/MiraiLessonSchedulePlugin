package com.ssttkkl.mirai.lessonscheduleplugin.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Lessons : IntIdTable() {
    val name = varchar("name", 50)
}

class Lesson(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Lesson>(Lessons)

    var name by Lessons.name
    val schedules by Schedule referrersOn Schedules.lessonId
}