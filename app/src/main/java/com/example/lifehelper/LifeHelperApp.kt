package com.example.lifehelper

import android.app.Application
import com.example.lifehelper.data.SampleDataSeeder
import com.example.lifehelper.notification.NotificationHelper
import com.example.lifehelper.work.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LifeHelperApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannels(this)
        // 安排每日目标检查提醒
        ReminderScheduler.scheduleDailyGoalCheck(this)
        // 首次启动填充示例数据
        appScope.launch {
            SampleDataSeeder.seedIfNeeded(this@LifeHelperApp, container)
        }
    }
}
