package br.com.escudoia

import java.text.Normalizer
import java.util.Locale

/**
 * Analisa somente texto disponível em prévias de notificações, sem acessar a caixa de SMS.
 * Não consulta reputação de links ou remetentes e não confirma que há fraude.
 */
object SmsProtection {
    val aplicativosCompativeis: Set<String> = setOf(
        "com.google.android.apps.messaging", // Google Mensagens
        "com.samsung.android.messaging",     // Samsung Mensagens
        "com.android.mms",                   // Mensagens em alguns aparelhos Xiaomi e Android
        "com.motorola.messaging",            // Motorola Mensagens
        "com.sonyericsson.conversations"      // Sony Mensagens
    )

    private fun normal(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)

    /**
     * Nunca abra nem faça download de URLs para examiná-las.
     * A avaliação combina elementos da mensagem, não identifica remetente legítimo.
     */
    fun analisarPrevia(texto: String): Analise {
        if (texto.isBlank()) return Analise(
            Risk.INSUFICIENTE, emptyList(), "O SMS não mostrou texto suficiente na notificação."
        )
        val base = RiskEngine.mensagem(texto)
        val s = normal(texto)
        val temLink = Regex("https?://|www\\.|\\b(?:bit\\.ly|tinyurl\\.com|cutt\\.ly|is\\.gd)\\b", RegexOption.IGNORE_CASE)
            .containsMatchIn(s)
        val mencionaEntrega = Regex("\\b(encomenda|entrega|correios|transportadora|pacote|pedido)\\b")
            .containsMatchIn(s)
        val problemaEntrega = Regex("retid|pendente|taxa|libera|devolu|cobranca|pague|pagamento|regulariz")
            .containsMatchIn(s)
        val assuntoFinanceiro = Regex("\\b(banco|conta|cartao|pix|cpf|cadastro|beneficio)\\b")
            .containsMatchIn(s)
        val ameacaFinanceira = Regex("bloque|suspend|cancel|irregular|regulariz|revalid|atualiz|confirm|inativ")
            .containsMatchIn(s)
        val pedeSegredo = Regex("(envie|informe|digite|passe|compartilhe).{0,50}(codigo|token|senha|pin)")
            .containsMatchIn(s)

        val motivos = base.motivos.toMutableList()
        val golpeEntrega = temLink && mencionaEntrega && problemaEntrega
        val golpeFinanceiro = temLink && assuntoFinanceiro && ameacaFinanceira

        if (golpeEntrega) motivos.add("SMS com link e cobrança ou problema de entrega: padrão comum de fraude.")
        if (golpeFinanceiro) motivos.add("SMS com link e pedido de regularização financeira: confirme por canal oficial.")
        if (pedeSegredo && motivos.none { it.contains("senha ou código") })
            motivos.add("SMS pede senha ou código de segurança.")

        val risco = when {
            base.risco == Risk.ALTO || golpeEntrega || golpeFinanceiro || pedeSegredo -> Risk.ALTO
            base.risco == Risk.ATENCAO -> Risk.ATENCAO
            temLink && (mencionaEntrega || assuntoFinanceiro) -> Risk.ATENCAO
            else -> Risk.SEM_SINAIS
        }
        return Analise(
            risco,
            motivos.distinct(),
            when (risco) {
                Risk.ALTO -> "Não abra o link nem pague a cobrança. Confirme pelo aplicativo ou telefone oficial da empresa."
                Risk.ATENCAO -> "Tenha cuidado. Consulte a empresa pelo canal oficial antes de clicar ou pagar."
                else -> "Não encontramos indícios fortes na prévia. Isso NÃO significa que o SMS seja seguro."
            }
        )
    }
}
