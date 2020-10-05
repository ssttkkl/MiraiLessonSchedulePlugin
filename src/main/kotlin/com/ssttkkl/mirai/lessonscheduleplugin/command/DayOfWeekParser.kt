package com.ssttkkl.mirai.lessonscheduleplugin.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.description.CommandArgumentParser
import net.mamoe.mirai.console.command.description.CommandArgumentParserException
import java.time.DayOfWeek

internal object DayOfWeekParser : CommandArgumentParser<DayOfWeek> {
    override fun parse(raw: String, sender: CommandSender): DayOfWeek {
        val rawMayBeInt = raw.toIntOrNull()
        if (rawMayBeInt != null) {
            if (rawMayBeInt in 1..7) {
                return DayOfWeek.of(rawMayBeInt)
            } else {
                throw CommandArgumentParserException("value $raw should be between 1 and 7")
            }
        } else {
            return when {
                "Monday".equals(raw, true) ||
                    "Mon.".equals(raw, true) -> DayOfWeek.MONDAY
                "Tuesday".equals(raw, true) ||
                    "Tue.".equals(raw, true) -> DayOfWeek.TUESDAY
                "Wednesday".equals(raw, true) ||
                    "Wed.".equals(raw, true) -> DayOfWeek.WEDNESDAY
                "Thursday".equals(raw, true) ||
                    "Thu.".equals(raw, true) -> DayOfWeek.THURSDAY
                "Friday".equals(raw, true) ||
                    "Fri.".equals(raw, true) -> DayOfWeek.FRIDAY
                "Saturday".equals(raw, true) ||
                    "Sat.".equals(raw, true) -> DayOfWeek.SATURDAY
                "Sunday".equals(raw, true) ||
                    "Sun.".equals(raw, true) -> DayOfWeek.SUNDAY
                else -> throw CommandArgumentParserException("illegal value $raw")
            }
        }
    }
}