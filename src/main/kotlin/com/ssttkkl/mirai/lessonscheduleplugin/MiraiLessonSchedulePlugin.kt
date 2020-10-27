package com.ssttkkl.mirai.lessonscheduleplugin

import com.ssttkkl.mirai.lessonscheduleplugin.command.LessonScheduleCommand
import com.ssttkkl.mirai.lessonscheduleplugin.data.Schedule
import com.ssttkkl.mirai.lessonscheduleplugin.data.newDayScheduleProducer
import com.ssttkkl.mirai.lessonscheduleplugin.data.newScheduleProducer
import com.ssttkkl.mirai.lessonscheduleplugin.msgmaker.PeriodSchedulesMessageMaker
import com.ssttkkl.mirai.lessonscheduleplugin.msgmaker.ScheduleNotifyMessageMaker
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.isContentEmpty
import org.jetbrains.exposed.sql.SizedIterable

const val ID = "com.ssttkkl.mirai.lesson-schedule-plugin"
const val VERSION = "0.1.0"
const val NAME = "MiraiLessonSchedulePlugin"
const val AUTHOR = "ssttkkl"

object MiraiLessonSchedulePlugin : KotlinPlugin(
    JvmPluginDescription(
        id = ID,
        version = VERSION
    ) {
        name(NAME)
        author(AUTHOR)
    }
) {
    lateinit var scheduleProducer: ReceiveChannel<Schedule>

    private val scheduleConsumer = launch(start = CoroutineStart.LAZY) {
        for (s in scheduleProducer) {
            try {
                val msg = ScheduleNotifyMessageMaker.make(s)

                GeneralConfig.notifies.forEach { (qqId, noti) ->
                    Bot.getInstanceOrNull(qqId)?.let { bot ->
                        noti.notify(bot, msg)
                    } ?: logger.error("bot instance of [$qqId] was not found")
                }
            } catch (exc: CancellationException) {
                throw exc
            } catch (exc: Exception) {
                logger.error(exc)
            }
        }
    }

    lateinit var dayScheduleProducer: ReceiveChannel<SizedIterable<Schedule>>

    private val dayScheduleConsumer = launch(start = CoroutineStart.LAZY) {
        for (d in dayScheduleProducer) {
            try {
                val msg = PeriodSchedulesMessageMaker.make(d).let {
                    if (it.isContentEmpty())
                        PlainText("今日无课")
                    else
                        it
                }

                GeneralConfig.notifies.forEach { (qqId, noti) ->
                    Bot.getInstanceOrNull(qqId)?.let { bot ->
                        noti.notify(bot, msg)
                    } ?: logger.error("bot instance of [$qqId] was not found")
                }
            } catch (exc: CancellationException) {
                throw exc
            } catch (exc: Exception) {
                logger.error(exc)
            }
        }
    }

    override fun onEnable() {
        GeneralConfig.reload()

        if (GeneralConfig.scheduleNotifyEnabled) {
            scheduleProducer = newScheduleProducer()
            scheduleConsumer.start()
        }

        if (GeneralConfig.dayScheduleNotifyEnabled) {
            dayScheduleProducer = newDayScheduleProducer()
            dayScheduleConsumer.start()
        }

        LessonScheduleCommand.register()
    }

    override fun onDisable() {
        if (GeneralConfig.scheduleNotifyEnabled) {
            scheduleProducer.cancel()
            runBlocking {
                scheduleConsumer.cancelAndJoin()
            }
        }

        if (GeneralConfig.dayScheduleNotifyEnabled) {
            dayScheduleProducer.cancel()
            runBlocking {
                dayScheduleConsumer.cancelAndJoin()
            }
        }

        LessonScheduleCommand.unregister()
    }
}