import android.Manifest
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.miscitasmedicas.R

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Verificar si tienes citas pendientes
        val hasPendingAppointments = checkPendingAppointments()

        if (hasPendingAppointments) {
            // Mostrar notificación
            showNotification()
        }

        return Result.success()
    }

    private fun checkPendingAppointments(): Boolean {
        // Aquí va la lógica para verificar si hay citas pendientes
        // Puedes acceder a tu base de datos o SharedPreferences para determinar si hay citas
        return true // Simulación de que hay citas pendientes
    }

    private fun showNotification() {
        // Verificar si tenemos permiso para enviar notificaciones
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {

            val notification = NotificationCompat.Builder(applicationContext, "com.example.miscitasmedicas.channel")
                .setContentTitle("Recordatorio de citas pendientes")
                .setContentText("Tienes citas pendientes en la aplicación")
                .setSmallIcon(R.drawable.login_logo)  // Usa el ícono que prefieras
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify(1, notification)
        }
    }
}
