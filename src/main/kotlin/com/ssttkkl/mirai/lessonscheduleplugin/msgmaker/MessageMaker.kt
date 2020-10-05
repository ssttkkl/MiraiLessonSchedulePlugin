package com.ssttkkl.mirai.lessonscheduleplugin.msgmaker

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

interface MessageMaker<T : Any> {
    fun makePlain(data: T): String
    fun make(data: T): Message = PlainText(makePlain(data))
}