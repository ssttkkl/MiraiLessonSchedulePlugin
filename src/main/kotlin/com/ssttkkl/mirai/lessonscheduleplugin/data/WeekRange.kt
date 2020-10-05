package com.ssttkkl.mirai.lessonscheduleplugin.data

class WeekRange(start: Int, end: Int) : ClosedRange<Int> by IntRange(start, end) {
    override fun toString(): String {
        return if (start == endInclusive)
            "$start"
        else
            "$start-$endInclusive"
    }
}