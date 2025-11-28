import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.app.Service
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.miscitasmedicas.R

class AppService : Service() {

    private val channelId = "com.example.miscitasmedicas.channel"
    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel() // Crear el canalAC de notificación
        startForegroundService() // Iniciar el servicio en primer plano
        startReminderWorker() // Iniciar el WorkManager
    }

    // Inicia el WorkManager para las tareas periódicas
    private fun startReminderWorker() {
        val reminderWorkRequest = PeriodicWorkRequest.Builder(ReminderWorker::class.java, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueue(reminderWorkRequest)
    }

    // Crear el canal de notificación (requiere Android Oreo o superior)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // Inicia el servicio en primer plano con una notificación
    private fun startForegroundService() {
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mis Citas Médicas")
            .setContentText("La aplicación sigue en uso")
            .setSmallIcon(R.drawable.login_logo) // Cambia con tu ícono
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(notificationId, notification) // Inicia el servicio en primer plano
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true) // Detener el servicio cuando se cierre
    }

    override fun onBind(intent: Intent?) = null // Implementación requerida, pero no usamos binding.

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, AppService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AppService::class.java)
            context.stopService(intent)
        }
    }
}
