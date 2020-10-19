# MiraiLessonSchedulePlugin

自用课表插件

## 指令

> 课表管理
> 
>   `(/)lessonschedule add <lesson> <weekRange> <dayOfWeek> <lessonOfDay> <location>`   添加课程安排，若课程不存在则自动添加
>
>   `(/)lessonschedule lesson`    列出所有课程
>
>   `(/)lessonschedule rmlesson <lesson>`   删除课程，该课程的安排也会一同删除
>
>   `(/)lessonschedule rmschedule <lesson> <weeks> <dayOfWeek> <lessonOfDay>`   删除课程安排
>
>   `(/)lessonschedule schedule <lessonName>`   列出课程的所有安排
>
>   `(/)lessonschedule thisweek`    查看本周所有课程
>
>   `(/)lessonschedule today`    查看今日所有课程

## 配置文件

```yaml
databaseFile: database.db
dateFormat: yyyy-MM-dd
timeFormat: 'HH:mm'
mondayOfFirstWeek: 2020-08-31
lessonTimetable: 
  - '08:00'
  - '10:05'
  - '14:00'
  - '16:05'
notifies: 
  114514: 
    notifyFriendList: 
      - 1919810
    notifyGroupList: []
scheduleNotifyBefore: 600000
dayScheduleNotifyEnabled: true
dayScheduleNotifyTime: '07:00'
```