package com.ssttkkl.mirai.lessonscheduleplugin.command

import com.ssttkkl.mirai.lessonscheduleplugin.data.WeekRange
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.description.CommandArgumentParser
import net.mamoe.mirai.console.command.description.CommandArgumentParserException

object WeekRangeParser : CommandArgumentParser<WeekRange> {
    override fun parse(raw: String, sender: CommandSender): WeekRange {
        val rawMaybeSingle = raw.toIntOrNull()
        return if (rawMaybeSingle != null) {
            WeekRange(rawMaybeSingle, rawMaybeSingle)
        } else {
            val rawMaybeRange = raw.split("-")
            if (rawMaybeRange.size != 2)
                throw CommandArgumentParserException()
            val weeksMaybeIntRange = rawMaybeRange.mapNotNull { it.toIntOrNull() }
            if (weeksMaybeIntRange.size != 2)
                throw CommandArgumentParserException()
            WeekRange(weeksMaybeIntRange[0], weeksMaybeIntRange[1])
        }
    }
}