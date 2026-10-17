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

/**
 * O Android entrega notificações à ESCUDO IA somente após o usuário autorizar
 * acesso no sistema. A aplicação descarta as notificações não selecionadas,
 * analisa somente prévias disponíveis e não armazena o conteúdo.
 */
class EscudoListener : NotificationListenerService() {
    private val ultimosAlertas = mutableMapOf<String, Pair<Int, Long>>()
    private val whatsapp = setOf("com.whatsapp", "com.whatsapp.w4b")

    override fun onListenerConnected() {
        super.onListenerConnected()
        DiagStore.listenerConectado(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pacote = sbn.packageName
        val prefs = getSharedPreferences("escudo", MODE_PRIVATE)
        val origem = when {
            pacote in whatsapp && prefs.getBoolean("protege", false) &&
                (pacote != "com.whatsapp.w4b" || prefs.getBoolean("business", false)) -> "WhatsApp"
            pacote in SmsProtection.aplicativosCompativeis && prefs.getBoolean("sms_protege", false) -> "SMS"
            else -> return
        }

        val extras = sbn.notification.extras ?: run {
            if (origem == "SMS") DiagStore.smsRecebido(this, pacote, "PREVIA_INDISPONIVEL")
            return
        }
        val trechos = listOfNotNull(
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        ) + (extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.map { it.toString() } ?: emptyList())
        val previa = trechos.filter { it.isNotBlank() }.distinct().take(4).joinToString(" ").trim()
        if (previa.isBlank()) {
            if (origem == "SMS") DiagStore.smsRecebido(this, pacote, "PREVIA_INDISPONIVEL")
            return
        }

        val analise = if (origem == "SMS") SmsProtection.analisarPrevia(previa) else RiskEngine.mensagem(previa)
        if (analise.risco != Risk.ALTO) {
            if (origem == "SMS") DiagStore.smsRecebido(this, pacote, when (analise.risco) {
                Risk.ATENCAO -> "ATENCAO_SEM_ALERTA"
                Risk.SEM_SINAIS -> "SEM_SINAIS_SEM_ALERTA"
                else -> "DADOS_INSUFICIENTES"
            })
            return
        }

        // Evita disparos repetidos quando o app de mensagens atualiza uma notificação.
        val agora = System.currentTimeMillis()
        val chave = pacote
        val anterior = ultimosAlertas[chave]
        if (anterior != null && (anterior.first == previa.hashCode() || agora - anterior.second < 45_000L)) {
            if (origem == "SMS") DiagStore.smsRecebido(this, pacote, "ALTO_AVISO_REPETIDO")
            return
        }
        ultimosAlertas[chave] = previa.hashCode() to agora

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (origem == "SMS") DiagStore.smsRecebido(this, pacote, "ALTO_SEM_PERMISSAO_DE_AVISOS")
            return
        }

        val gerenciador = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val canal = "escudo_alertas"
        gerenciador.createNotificationChannel(
            NotificationChannel(canal, "Alertas de segurança", NotificationManager.IMPORTANCE_HIGH)
        )
        val id = if (origem == "SMS") 776 else 774
        val abrir = Intent(this, MainActivity::class.java)
            .putExtra("aviso_escudo", true)
            .putExtra("aviso_origem", origem)
        val pending = PendingIntent.getActivity(
            this, id, abrir, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val aviso = Notification.Builder(this, canal)
            .setSmallIcon(R.drawable.escudo_icon)
            .setContentTitle("ESCUDO IA — possível golpe em $origem")
            .setContentText("Encontramos sinais de risco alto. Confira antes de agir.")
            .setStyle(Notification.BigTextStyle().bigText(
                "Uma prévia autorizada de $origem parece suspeita. Não abra links, transfira dinheiro nem informe códigos antes de confirmar por um canal oficial."
            ))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_STATUS)
            .build()
        val enviado = runCatching { gerenciador.notify(id, aviso) }.isSuccess
        if (origem == "SMS") {
            DiagStore.smsRecebido(
                this, pacote,
                if (enviado) "ALTO_ALERTA_SOLICITADO" else "ALTO_ERRO_AO_AVISAR"
            )
        }
    }
}
