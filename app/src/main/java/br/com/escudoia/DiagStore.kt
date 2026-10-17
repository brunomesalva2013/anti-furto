package br.com.escudoia

import android.content.Context

/**
 * Diagnóstico mínimo, armazenado SOMENTE no aparelho.
 * NÃO coleta texto, remetente, destinatário ou número de telefone.
 * Os registros são feitos apenas para notificações de fontes autorizadas.
 */
object DiagStore {
    private const val NOME = "escudo_diagnostico_local"
    fun listenerConectado(context: Context) {
        context.getSharedPreferences(NOME, Context.MODE_PRIVATE)
            .edit().putLong("conectado", System.currentTimeMillis()).apply()
    }

    fun smsRecebido(context: Context, pacote: String, resultado: String) {
        val p = context.getSharedPreferences(NOME, Context.MODE_PRIVATE)
        p.edit()
            .putLong("ultimo", System.currentTimeMillis())
            .putString("app", pacote)
            .putString("resultado", resultado)
            .putInt("quantidade", p.getInt("quantidade", 0) + 1)
            .apply()
    }

    fun ultimaConexao(context: Context): Long =
        context.getSharedPreferences(NOME, Context.MODE_PRIVATE).getLong("conectado", 0L)

    fun ultimoSms(context: Context): Long =
        context.getSharedPreferences(NOME, Context.MODE_PRIVATE).getLong("ultimo", 0L)

    fun aplicativo(context: Context): String =
        context.getSharedPreferences(NOME, Context.MODE_PRIVATE).getString("app", "").orEmpty()

    fun resultado(context: Context): String =
        context.getSharedPreferences(NOME, Context.MODE_PRIVATE).getString("resultado", "").orEmpty()

    fun total(context: Context): Int =
        context.getSharedPreferences(NOME, Context.MODE_PRIVATE).getInt("quantidade", 0)

    fun limpar(context: Context) {
        context.getSharedPreferences(NOME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
