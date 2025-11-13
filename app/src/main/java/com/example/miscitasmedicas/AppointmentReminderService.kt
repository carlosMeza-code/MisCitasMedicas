package com.example.miscitasmedicas

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.LazyThreadSafetyMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Servicio en primer plano que revisa periódicamente las citas guardadas para
 * mantener al usuario informado aun cuando la app está en segundo plano.
 */
class AppointmentReminderService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val storage by lazy(LazyThreadSafetyMode.NONE) {
        AppointmentStorage(applicationContext)
    }
    private val notificationManager by lazy(LazyThreadSafetyMode.NONE) {
        NotificationManagerCompat.from(this)
    }
    private var lastStatusMessage: String? = null

    override fun onCreate() {
        super.onCreate()
        running = true
        createNotificationChannel()
        val notification = buildStatusNotification(
            getString(R.string.reminder_service_monitoring)
        )
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        observeAppointments()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        scope.cancel()
        stopForeground(true)
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
        lastStatusMessage = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeAppointments() {
        scope.launch {
            while (isActive) {
                updateReminderNotification()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private suspend fun updateReminderNotification() {
        val upcoming = findNextUpcomingAppointment()
        val message = upcoming?.let { upcomingAppointment ->
            val relative = DateUtils.getRelativeTimeSpanString(
                upcomingAppointment.scheduledTime,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
            getString(
                R.string.reminder_notification_next_appointment,
                relative,
                upcomingAppointment.appointment.specialty,
                upcomingAppointment.appointment.date,
                upcomingAppointment.appointment.time
            )
        } ?: getString(R.string.reminder_notification_no_appointments)

        if (message != lastStatusMessage) {
            lastStatusMessage = message
            val notification = buildStatusNotification(message)
            notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    private fun buildStatusNotification(message: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, AppointmentsActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.reminder_notification_title))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_schedule)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.reminder_channel_description)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private suspend fun findNextUpcomingAppointment(): UpcomingAppointment? {
        val appointments = storage.getAppointments()
        if (appointments.isEmpty()) return null

        val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        val now = System.currentTimeMillis()

        return appointments
            .asSequence()
            .mapNotNull { appointment ->
                val scheduled = runCatching {
                    formatter.parse("${appointment.date} ${appointment.time}")?.time
                }.getOrNull() ?: return@mapNotNull null

                if (scheduled < now) return@mapNotNull null
                UpcomingAppointment(appointment, scheduled)
            }
            .minByOrNull(UpcomingAppointment::scheduledTime)
    }

    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    private data class UpcomingAppointment(
        val appointment: Appointment,
        val scheduledTime: Long
    )

    companion object {
        private const val CHANNEL_ID = "appointment_reminders"
        private const val DATE_PATTERN = "dd 'de' MMMM yyyy HH:mm"
        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private val CHECK_INTERVAL_MS = TimeUnit.MINUTES.toMillis(15)

        @Volatile
        private var running: Boolean = false

        fun isRunning(): Boolean = running
    }
}
