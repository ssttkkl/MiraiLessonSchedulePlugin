package com.ssttkkl.mirai.lessonscheduleplugin.command

import com.ssttkkl.mirai.lessonscheduleplugin.GeneralConfig
import com.ssttkkl.mirai.lessonscheduleplugin.MiraiLessonSchedulePlugin
import com.ssttkkl.mirai.lessonscheduleplugin.data.*
import com.ssttkkl.mirai.lessonscheduleplugin.data.DBUtil.database
import com.ssttkkl.mirai.lessonscheduleplugin.msgmaker.LessonsMessageMaker
import com.ssttkkl.mirai.lessonscheduleplugin.msgmaker.PeriodSchedulesMessageMaker
import com.ssttkkl.mirai.lessonscheduleplugin.msgmaker.LessonSchedulesMessageMaker
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.description.CommandArgumentContextBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.isContentEmpty
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.DayOfWeek
import java.time.LocalDate

private const val DESCRIPTION = "课表管理"
private const val DESCRIPTION_ADD = "添加课程安排，若课程不存在则自动添加"
private const val DESCRIPTION_REMOVE_LESSON = "删除课程，该课程的安排也会一同删除"
private const val DESCRIPTION_REMOVE_SCHEDULE = "删除课程安排"
private const val DESCRIPTION_SCHEDULES = "列出课程的所有安排"
private const val DESCRIPTION_LESSONS = "列出所有课程"
private const val DESCRIPTION_TODAY = "查看今日所有课程"
private const val DESCRIPTION_THIS_WEEK = "查看本周所有课程"

@Suppress("Unused")
object LessonScheduleCommand : CompositeCommand(
    MiraiLessonSchedulePlugin,
    "lessonschedule",
    "kb", "课表",
    prefixOptional = true,
    description = DESCRIPTION,
    overrideContext = CommandArgumentContextBuilder()
        .add(WeekRangeParser)
        .add(DayOfWeekParser)
        .build()
) {

    @SubCommand
    @Description(DESCRIPTION_ADD)
    suspend fun CommandSender.add(
        lesson: String,
        weekRange: WeekRange,
        dayOfWeek: DayOfWeek,
        lessonOfDay: Int,
        location: String
    ): Boolean {
        // get lesson
        val lessonDao = transaction(database) {
            val result = Lesson.find { Lessons.name eq lesson }.notForUpdate()
            if (result.empty())
                Lesson.new { name = lesson }
            else
                result.single()
        }

        val msg = transaction(database) {
            // search for conflict schedule
            val schs = Schedule.find {
                (Schedules.dayOfWeek eq dayOfWeek.value) and
                    (Schedules.lessonOfDay eq lessonOfDay) and
                    not((Schedules.toWeek less weekRange.start) or (Schedules.fromWeek greater weekRange.endInclusive))
            }
            if (!schs.empty()) {
                "存在冲突课程：${schs.single().lesson.name}"
            } else {
                Schedule.new {
                    this.location = location
                    this.weekRange = weekRange
                    this.dayOfWeek = dayOfWeek
                    this.lessonOfDay = lessonOfDay
                    this.lesson = lessonDao
                }
                "OK"
            }
        }
        sendMessage(msg)
        return true
    }

    @SubCommand("rmlesson", "rmles")
    @Description(DESCRIPTION_REMOVE_LESSON)
    suspend fun CommandSender.removeLesson(lesson: String) {
        val msg = transaction(database) {
            val result = Lesson.find { Lessons.name eq lesson }.notForUpdate()
            if (result.empty())
                "未找到课程"
            else {
                val les = result.single()
                les.schedules.forUpdate().forEach { it.delete() }
                les.delete()
                "OK"
            }
        }
        sendMessage(msg)
    }

    @SubCommand("rmschedule", "rmsch")
    @Description(DESCRIPTION_REMOVE_SCHEDULE)
    suspend fun CommandSender.removeSchedule(
        lesson: String,
        weeks: WeekRange,
        dayOfWeek: DayOfWeek,
        lessonOfDay: Int,
    ): Boolean {
        val msg = transaction(database) {
            val query = Schedules.join(Lessons, JoinType.INNER) { Schedules.lessonId eq Lessons.id }
                .select {
                    (Lessons.name eq lesson) and
                        (Schedules.fromWeek eq weeks.start) and
                        (Schedules.toWeek eq weeks.endInclusive) and
                        (Schedules.dayOfWeek eq dayOfWeek.value) and
                        (Schedules.lessonOfDay eq lessonOfDay)
                }.notForUpdate()
            val schs = Schedule.wrapRows(query)
            if (schs.empty())
                "未找到时间安排"
            else {
                schs.single().delete()
                "OK"
            }
        }
        sendMessage(msg)
        return true
    }

    @SubCommand("schedule", "sch")
    @Description(DESCRIPTION_SCHEDULES)
    suspend fun CommandSender.schedule(lessonName: String) {
        val msg = transaction(database) {
            val lesson = Lesson.find { Lessons.name eq lessonName }.notForUpdate()
            if (lesson.empty())
                PlainText("未找到课程")
            else {
                val schs = lesson.single().schedules.orderBy(
                    Schedules.fromWeek to SortOrder.ASC,
                    Schedules.toWeek to SortOrder.ASC,
                    Schedules.dayOfWeek to SortOrder.ASC,
                    Schedules.lessonOfDay to SortOrder.ASC
                )
                LessonSchedulesMessageMaker.make(schs)
            }
        }

        if (msg.isContentEmpty())
            sendMessage("该课程暂无安排")
        else
            sendMessage(msg)
    }

    @SubCommand("lesson", "les")
    @Description(DESCRIPTION_LESSONS)
    suspend fun CommandSender.lesson() {
        val msg = transaction(database) {
            LessonsMessageMaker.make(Lesson.all())
        }
        
        if (msg.isContentEmpty())
            sendMessage("课表为空")
        else
            sendMessage(msg)
    }

    @SubCommand
    @Description(DESCRIPTION_TODAY)
    suspend fun CommandSender.today() {
        val today = LocalDate.now()
        val weekOfToday = GeneralConfig.weekOf(today)

        val msg = transaction(database) {
            val schs = Schedule.find {
                (Schedules.fromWeek lessEq weekOfToday) and
                    (Schedules.toWeek greaterEq weekOfToday) and
                    (Schedules.dayOfWeek eq today.dayOfWeek.value)
            }.notForUpdate()
            PeriodSchedulesMessageMaker.make(schs)
        }

        if (msg.isContentEmpty())
            sendMessage("今日无课")
        else
            sendMessage(msg)
    }

    @SubCommand("thisweek")
    @Description(DESCRIPTION_THIS_WEEK)
    suspend fun CommandSender.thisWeek() {
        val today = LocalDate.now()
        val weekOfToday = GeneralConfig.weekOf(today)

        val msg = transaction(database) {
            val schs = Schedule.find {
                (Schedules.fromWeek lessEq weekOfToday) and
                    (Schedules.toWeek greaterEq weekOfToday)
            }.notForUpdate()
            PeriodSchedulesMessageMaker.make(schs)
        }

        if (msg.isContentEmpty())
            sendMessage("本周无课")
        else
            sendMessage(msg)
    }
}