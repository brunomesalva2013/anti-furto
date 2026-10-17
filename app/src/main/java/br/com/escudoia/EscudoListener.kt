package br.com.escudoia

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class EscudoListener : NotificationListenerService() {
    private var ultimoAviso = 0L
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName !in setOf("com.whatsapp", "com.whatsapp.w4b")) return
        val pref = getSharedPreferences("escudo", MODE_PRIVATE)
        if (!pref.getBoolean("protege", false)) return
        if (sbn.packageName == "com.whatsapp.w4b" && !pref.getBoolean("business", false)) return
        val extras = sbn.notification.extras ?: return
        val texto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (texto.isBlank() || RiskEngine.mensagem(texto).risco != Risk.ALTO) return
        val agora = System.currentTimeMillis()
        if (agora - ultimoAviso < 60_000) return
        ultimoAviso = agora
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "escudo_alertas"
        manager.createNotificationChannel(NotificationChannel(channelId,"Alertas de segurança",NotificationManager.IMPORTANCE_HIGH))
        val abrir = Intent(this, MainActivity::class.java).putExtra("aviso_escudo", true)
        val pending = PendingIntent.getActivity(this, 10, abrir, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alerta = Notification.Builder(this, channelId)
            .setSmallIcon(R.drawable.escudo_icon)
            .setContentTitle("ESCUDO IA — cuidado!")
            .setContentText("Uma mensagem pode apresentar sinais de golpe. Abra para orientações.")
            .setStyle(Notification.BigTextStyle().bigText("Uma prévia autorizada do WhatsApp apresentou sinais de possível golpe. Não envie dinheiro ou códigos antes de confirmar com um contato conhecido."))
            .setContentIntent(pending).setAutoCancel(true)
            .setCategory(Notification.CATEGORY_ALARM).build()
        manager.notify(774, alerta)
    }
}
