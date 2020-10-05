package com.ssttkkl.mirai.lessonscheduleplugin.msgmaker

import com.ssttkkl.mirai.lessonscheduleplugin.GeneralConfig
import com.ssttkkl.mirai.lessonscheduleplugin.data.DBUtil.database
import com.ssttkkl.mirai.lessonscheduleplugin.data.Schedule
import org.jetbrains.exposed.sql.transactions.transaction

object ScheduleNotifyMessageMaker : MessageMaker<Schedule> {
    override fun makePlain(data: Schedule): String = transaction(database) {
        "（${data.lessonOfDay}）${data.lesson.name} " +
            "将于 ${GeneralConfig.scheduleNotifyBefore / 60000.0} 分钟后开始上课"
    }
}