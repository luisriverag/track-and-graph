/*
 * This file is part of Track & Graph
 *
 * Track & Graph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Track & Graph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Track & Graph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.samco.trackandgraph.base.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import com.samco.trackandgraph.base.R
import com.samco.trackandgraph.base.navigation.PendingIntentProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var pendingIntentProvider: PendingIntentProvider

    companion object {
        private const val REMINDERS_CHANNEL_ID = "reminder_notifications_channel"
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.reminders_notifications_channel_name)
            val descriptionText =
                context.getString(R.string.reminders_notifications_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(REMINDERS_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        createNotificationChannel(context)

        val message = (intent?.extras?.get("Message") as String?) ?: return

        val pendingIntent = pendingIntentProvider.getMainActivityPendingIntent()

        val notification = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setContentTitle(message)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentText("")
            .setContentIntent(pendingIntent)
            .setDefaults(DEFAULT_ALL)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(System.currentTimeMillis().toInt() % 10, notification)
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(250, 250, 250, 250), -1))
        } else vibrator.vibrate(longArrayOf(250, 250, 250, 250), -1)
    }
}

@AndroidEntryPoint
class RecreateAlarms : BroadcastReceiver() {

    @Inject
    lateinit var dataInteractor: DataInteractor

    override fun onReceive(context: Context?, intent: Intent?) {
        val validActions = listOf(
            "action.REMINDERS_CHANGED",
            "android.intent.action.BOOT_COMPLETED",
            "android.intent.action.QUICKBOOT_POWERON",
            "android.intent.action.MY_PACKAGE_REPLACED"
        )
        if (!validActions.contains(intent?.action)) return
        if (context == null) return
        runBlocking { dataInteractor.syncAlarms() }
    }
}
