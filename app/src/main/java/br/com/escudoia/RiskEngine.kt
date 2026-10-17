package br.com.escudoia

import java.net.URI
import java.text.Normalizer
import java.util.Locale

enum class Risk { ALTO, ATENCAO, SEM_SINAIS, INSUFICIENTE }
data class Analise(val risco: Risk, val motivos: List<String>, val orientacao: String)

object RiskEngine {
    private fun normal(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase(Locale.ROOT)

    fun mensagem(texto: String): Analise {
        if (texto.isBlank()) return Analise(Risk.INSUFICIENTE, emptyList(), "Informe uma mensagem para analisar.")
        val s = normal(texto)
        var pontos = 0
        val motivos = mutableListOf<String>()
        val pedeSegredo = Regex("\\b(senha|codigo|token|otp|pin)\\b").containsMatchIn(s) &&
            Regex("informe|envie|mande|passe|compartilhe|digite|preciso do").containsMatchIn(s)
        val mudaNumero = Regex("troquei|mudei|numero novo|celular novo|outro numero").containsMatchIn(s)
        val pedePix = Regex("\\b(pix|transferencia|deposito)\\b").containsMatchIn(s)
        val pressa = Regex("\\b(urgente|agora|imediatamente|rapido|hoje)\\b").containsMatchIn(s)
        val link = Regex("https?://|www\\.|clique|acesse").containsMatchIn(s)
        val banco = Regex("\\b(banco|cartao|bloquead\\w*|gerente)\\b").containsMatchIn(s)
        val trabalho = Regex("\\b(emprego|vaga|trabalho|contratad\\w*)\\b").containsMatchIn(s)
        val taxa = Regex("\\b(taxa|cadastro|liberar)\\b").containsMatchIn(s)
        if (pedeSegredo) { pontos += 5; motivos.add("A mensagem pede senha ou código de segurança.") }
        if (mudaNumero) { pontos += 2; motivos.add("A pessoa menciona que trocou de número.") }
        if (pedePix) { pontos += 1; motivos.add("Há pedido ou menção a transferência de dinheiro.") }
        if (pressa) { pontos += 1; motivos.add("Há pressão para agir com urgência.") }
        if (link) { pontos += 1; motivos.add("Há link ou pedido para abrir um site.") }
        if (mudaNumero && pedePix) { pontos += 4; motivos.add("Troca de número acompanhada de pedido de Pix é um padrão de fraude.") }
        if (banco && (pedeSegredo || link)) { pontos += 2; motivos.add("Mensagem bancária com link ou solicitação sensível.") }
        if (trabalho && taxa) { pontos += 3; motivos.add("Vaga de emprego combinada com cobrança de taxa.") }
        val risco = when { pontos >= 4 -> Risk.ALTO; pontos >= 2 -> Risk.ATENCAO; else -> Risk.SEM_SINAIS }
        val orientacao = when(risco) {
            Risk.ALTO -> "Não envie dinheiro nem códigos. Ligue para a pessoa ou empresa por um número conhecido."
            Risk.ATENCAO -> "Verifique a identidade da pessoa por outro canal antes de responder ou pagar."
            else -> "Não foram encontrados sinais fortes com essas regras. Isso NÃO garante segurança."
        }
        return Analise(risco, motivos, orientacao)
    }

    fun link(texto: String): Analise {
        val raw = texto.trim().split(Regex("\\s+")).firstOrNull().orEmpty().trimEnd('.',',',')')
        if (raw.isEmpty()) return Analise(Risk.INSUFICIENTE, emptyList(), "Informe o link suspeito.")
        val parsed = runCatching { URI(if (raw.contains("://")) raw else "https://" + raw) }.getOrNull()
        val host = parsed?.host?.lowercase(Locale.ROOT)
        if (host.isNullOrBlank()) return Analise(Risk.INSUFICIENTE, listOf("O endereço não pôde ser interpretado."), "Não abra o endereço antes de confirmar a origem.")
        val motivos = mutableListOf<String>()
        var pontos = 0
        if (parsed.scheme != "https") { pontos++; motivos.add("O endereço não utiliza HTTPS, ou usa outro protocolo.") }
        if (parsed.userInfo != null) { pontos += 3; motivos.add("O endereço contém dados antes de @, que podem ocultar o destino.") }
        if (host in setOf("bit.ly","tinyurl.com","t.co","cutt.ly","is.gd")) { pontos += 2; motivos.add("É um link encurtado, que oculta o destino final.") }
        if (host.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}"))) { pontos += 2; motivos.add("O endereço usa números no lugar do nome do site.") }
        if (host.startsWith("xn--") || host.contains(".xn--")) { pontos++; motivos.add("O domínio contém caracteres codificados e precisa ser confirmado.") }
        val risco = when { pontos >= 3 -> Risk.ALTO; pontos > 0 -> Risk.ATENCAO; else -> Risk.SEM_SINAIS }
        return Analise(risco, motivos, "Não abra links desconhecidos. Procure o site oficial digitando o endereço você mesmo. Esta análise não consulta a reputação da página.")
    }
}
