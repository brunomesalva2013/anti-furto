package br.com.escudoia

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.WindowInsets
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

/**
 * Protótipo Android nativo: interface grande, simples e local.
 * Não lê conversas completas. Sem envio automático a familiares.
 */
class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private val prefs by lazy { getSharedPreferences("escudo", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private var pagina = "inicio"
    private var tts: TextToSpeech? = null
    private var voz = false
    private val verde = Color.rgb(0, 104, 109)
    private val escuro = Color.rgb(20, 46, 62)
    private val vermelho = Color.rgb(145, 27, 42)
    private val cinza = Color.rgb(61, 75, 85)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 35) {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            window.statusBarColor = escuro
            window.navigationBarColor = escuro
        }
        tts = TextToSpeech(this, this)
        if (!prefs.getBoolean("visto", false)) boasVindas() else inicio()
        abrirCompartilhamento(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        abrirCompartilhamento(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::root.isInitialized && pagina == "protecao") protecao()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val idioma = tts?.setLanguage(Locale("pt", "BR"))
            voz = idioma != TextToSpeech.LANG_MISSING_DATA && idioma != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun sp(v: Float) = if (prefs.getBoolean("letras", false)) v * 1.25f else v
    private fun fundo(cor: Int): GradientDrawable = GradientDrawable().apply {
        setColor(cor)
        cornerRadius = dp(18).toFloat()
    }

    private fun tela(nome: String, titulo: String) {
        pagina = nome
        val scroll = ScrollView(this).apply { isFillViewport = true }
        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener { v, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
        }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(246, 249, 249))
            setPadding(dp(20), dp(18), dp(20), dp(42))
        }
        scroll.addView(root)
        setContentView(scroll)
        texto("🛡  ESCUDO IA", 28f, verde, true)
        texto(titulo, 27f, escuro, true)
    }

    private fun texto(conteudo: String, tamanho: Float = 20f, cor: Int = cinza, forte: Boolean = false) {
        val t = TextView(this).apply {
            text = conteudo
            textSize = sp(tamanho)
            setTextColor(cor)
            if (forte) setTypeface(null, Typeface.BOLD)
            setLineSpacing(dp(3).toFloat(), 1.05f)
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(t)
    }

    private fun caixa(conteudo: String, cor: Int = Color.WHITE) {
        val t = TextView(this).apply {
            text = conteudo
            textSize = sp(19f)
            setTextColor(escuro)
            setLineSpacing(dp(4).toFloat(), 1.05f)
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = fundo(cor)
        }
        root.addView(t, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(12)
            bottomMargin = dp(12)
        })
    }

    private fun botao(titulo: String, cor: Int = verde, acao: () -> Unit) {
        val b = Button(this).apply {
            text = titulo
            isAllCaps = false
            textSize = sp(20f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(cor)
            minHeight = dp(72)
            minimumHeight = dp(72)
            setPadding(dp(14), dp(13), dp(14), dp(13))
            setOnClickListener { acao() }
        }
        root.addView(b, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
    }

    private fun campo(rotulo: String, multi: Boolean = true): EditText {
        texto(rotulo, 19f, escuro, true)
        val entrada = EditText(this).apply {
            hint = rotulo
            textSize = sp(20f)
            setTextColor(escuro)
            setHintTextColor(cinza)
            minHeight = dp(if (multi) 110 else 64)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            inputType = if (multi)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            else InputType.TYPE_CLASS_TEXT
            if (!multi) setSingleLine(true)
            background = fundo(Color.WHITE)
            contentDescription = rotulo
        }
        root.addView(entrada)
        return entrada
    }

    private fun voltar() = botao("VOLTAR AO INÍCIO", escuro) { inicio() }
    private fun aviso(m: String) { AlertDialog.Builder(this).setMessage(m).setPositiveButton("Entendi", null).show() }
    private fun falar(m: String) {
        if (voz) tts?.speak(m, TextToSpeech.QUEUE_FLUSH, null, "escudo_fala")
        else aviso("A leitura em voz alta não está disponível neste aparelho.")
    }

    private fun boasVindas() {
        tela("bemvindo", "Bem-vindo! Estamos aqui para ajudar.")
        caixa("A ESCUDO IA ajuda você a identificar mensagens suspeitas e pedir ajuda à família.")
        botao("COMEÇAR AGORA") {
            prefs.edit().putBoolean("visto", true).apply()
            inicio()
        }
        botao("OUVIR EXPLICAÇÃO", escuro) {
            falar("A Escudo IA ajuda a perceber possíveis golpes. Você escolhe as permissões e pode pedir ajuda à família.")
        }
        texto("Depois de começar, você poderá ativar a análise de prévias autorizadas do WhatsApp e cadastrar uma pessoa de confiança.", 18f)
        texto("Nenhuma análise garante segurança. O aplicativo não lê conversas completas nem envia mensagens automaticamente para familiares.", 17f)
    }

    private fun listenerAtivo(): Boolean {
        val lista = runCatching {
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners").orEmpty()
        }.getOrDefault("")
        val comp = ComponentName(this, EscudoListener::class.java)
        return lista.split(':').any { it == comp.flattenToString() || it == comp.flattenToShortString() }
    }

    private fun inicio() {
        tela("inicio", "Sua segurança em primeiro lugar")
        val whatsappAtivo = prefs.getBoolean("protege", false)
        val smsAtivo = prefs.getBoolean("sms_protege", false)
        val avisosAtivos = Build.VERSION.SDK_INT < 33 ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val pronto = (whatsappAtivo || smsAtivo) && listenerAtivo() && avisosAtivos
        caixa(
            if (pronto) "PROTEÇÃO ATIVADA\nWhatsApp: " + (if (whatsappAtivo) "ligado" else "desligado") +
                "\nSMS: " + (if (smsAtivo) "ligado" else "desligado")
            else "PROTEÇÃO DESLIGADA OU INCOMPLETA\nConfigure o acesso e autorize WhatsApp e SMS separadamente.",
            if (pronto) Color.rgb(225, 245, 236) else Color.rgb(255, 247, 223)
        )
        botao("ATIVAR / VER MINHA PROTEÇÃO") { protecao() }
        botao("MINHA PESSOA DE CONFIANÇA", escuro) { confianca() }
        botao("PRECISO DE AJUDA AGORA", vermelho) { emergencia() }
        texto("Mais ferramentas", 23f, escuro, true)
        botao("VERIFICAR MENSAGEM") { analisarMensagem() }
        botao("VERIFICAR LINK") { analisarLink() }
        botao("ANTES DE FAZER UM PIX") { pix() }
        botao("TESTAR ALERTA DE SMS (SIMULAÇÃO)", vermelho) { testarSmsSimulado() }
        botao("TESTAR ALERTA DE PIX (SIMULAÇÃO)", vermelho) { testarAlertaSimulado() }
        botao("VER EXEMPLO DE GOLPE", escuro) {
            resultado(RiskEngine.mensagem("Mãe, troquei de número. Faz um Pix agora? É urgente."), true)
        }
        botao("LETRAS MAIORES / AJUSTES", escuro) { ajustes() }
        texto("Versão experimental: só prévias exibidas em notificações podem ser analisadas. Não conseguimos detectar todos os golpes.", 17f)
    }

    private fun protecao() {
        tela("protecao", "Minha proteção automática")
        val autorizado = prefs.getBoolean("protege", false)
        val listener = listenerAtivo()
        val podeAvisar = Build.VERSION.SDK_INT < 33 ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        caixa("Análise das prévias: " + if (autorizado) "AUTORIZADA" else "DESLIGADA")
        caixa("Acesso no Android: " + if (listener) "PERMITIDO" else "NÃO PERMITIDO")
        caixa("Avisos da ESCUDO IA: " + if (podeAvisar) "PERMITIDOS" else "NÃO PERMITIDOS")
        texto("A permissão do Android pode mostrar notificações de outros aplicativos. A ESCUDO IA processa somente prévias do WhatsApp (e Business, se você ativar). Não armazena nem envia o texto recebido.", 18f)
        if (!autorizado) botao("1 · AUTORIZAR ANÁLISE") {
            AlertDialog.Builder(this).setTitle("Você autoriza?")
                .setMessage("A ESCUDO IA analisará localmente prévias de notificações do WhatsApp em busca de sinais de golpe. Você pode desligar quando quiser.")
                .setNegativeButton("Agora não", null)
                .setPositiveButton("Eu autorizo") { _, _ ->
                    prefs.edit().putBoolean("protege", true).apply()
                    protecao()
                }.show()
        }
        if (autorizado) {
            botao("2 · PERMITIR ACESSO NO ANDROID") {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            if (!podeAvisar && Build.VERSION.SDK_INT >= 33) botao("3 · PERMITIR AVISOS") {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 42)
            }
            botao(if (prefs.getBoolean("business", false)) "WHATSAPP BUSINESS: INCLUÍDO" else "WHATSAPP BUSINESS: NÃO INCLUÍDO", escuro) {
                prefs.edit().putBoolean("business", !prefs.getBoolean("business", false)).apply()
                protecao()
            }
            botao("DESLIGAR ANÁLISE", vermelho) {
                prefs.edit().putBoolean("protege", false).apply()
                protecao()
            }
        }
        texto("Esta versão só analisa prévias que realmente aparecem nas notificações; não lê conversas completas. Prévia escondida ou acesso negado impede a análise.", 17f)
        voltar()
    }

    private fun testarAlertaSimulado() {
        val textoFicticio = "Oi, mãe! Troquei de número. Preciso de um Pix de R$ 850 agora, é urgente."
        val analise = RiskEngine.mensagem(textoFicticio)
        if (analise.risco != Risk.ALTO) {
            aviso("O teste do detector não passou. Não considere a proteção validada.")
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 43)
            aviso("Autorize as notificações da ESCUDO IA no Android e toque novamente em TESTAR ALERTA.")
            return
        }
        val gerente = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val canal = "escudo_teste"
        gerente.createNotificationChannel(
            NotificationChannel(canal, "Simulações de segurança", NotificationManager.IMPORTANCE_HIGH)
        )
        val abrir = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 77, abrir, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, canal)
            .setSmallIcon(R.drawable.escudo_icon)
            .setContentTitle("TESTE ESCUDO IA — risco alto")
            .setContentText("SIMULAÇÃO: número novo + pedido urgente de Pix.")
            .setStyle(Notification.BigTextStyle().bigText(
                "SIMULAÇÃO, NÃO É UM WHATSAPP REAL. A mensagem fictícia combina mudança de número, Pix e urgência. Não transfira dinheiro."
            ))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        gerente.notify(775, n)
        AlertDialog.Builder(this)
            .setTitle("Teste de alerta enviado")
            .setMessage("O detector identificou risco alto em uma mensagem fictícia. Deslize a barra de notificações do celular para procurar o aviso 'TESTE ESCUDO IA'.\n\nIsso comprova apenas o teste interno, NÃO que o WhatsApp está sendo monitorado corretamente. Para testar o WhatsApp, receba uma mensagem de teste de outro aparelho.")
            .setPositiveButton("Entendi", null)
            .show()
    }

    private fun analisarMensagem(inicial: String = "") {
        tela("mensagem", "Verificar uma mensagem")
        texto("Cole uma mensagem suspeita ou compartilhe texto de outro aplicativo. Não envie senhas.")
        val entrada = campo("Mensagem recebida")
        entrada.setText(inicial)
        botao("ANALISAR AGORA") { resultado(RiskEngine.mensagem(entrada.text.toString())) }
        voltar()
    }

    private fun analisarLink(inicial: String = "") {
        tela("link", "Verificar um link")
        texto("O link NÃO será aberto. Esta análise examina apenas sinais no endereço; não consulta listas de sites perigosos.")
        val entrada = campo("Endereço recebido")
        entrada.setText(inicial)
        botao("VERIFICAR LINK") { resultado(RiskEngine.link(entrada.text.toString())) }
        voltar()
    }

    private fun pix() {
        tela("pix", "Antes de fazer um Pix")
        caixa("Confira o nome do recebedor e o valor dentro do aplicativo oficial do banco.\n\nSe alguém pedir Pix urgente ou afirmar ter trocado de número, ligue para essa pessoa por um contato conhecido.")
        texto("A ESCUDO IA ainda NÃO verifica titularidade de chaves, contas ou transações Pix. Não autorize pagamentos com base no aplicativo.", 18f)
        botao("PEDIR AJUDA A UM FAMILIAR") { compartilharAjuda() }
        voltar()
    }

    private fun resultado(a: Analise, exemplo: Boolean = false) {
        tela("resultado", if (exemplo) "Exemplo de alerta" else "Resultado preliminar")
        val titulo = when (a.risco) {
            Risk.ALTO -> "ATENÇÃO: RISCO ALTO"
            Risk.ATENCAO -> "TENHA CUIDADO"
            Risk.SEM_SINAIS -> "NENHUM SINAL FORTE DETECTADO"
            Risk.INSUFICIENTE -> "DADOS INSUFICIENTES"
        }
        caixa(titulo, if (a.risco == Risk.ALTO) Color.rgb(253, 225, 224) else Color.WHITE)
        texto("Motivos", 22f, escuro, true)
        if (a.motivos.isEmpty()) texto("Não há sinais suficientes para explicar o risco.")
        else a.motivos.forEach { texto("• " + it) }
        texto("O que fazer agora", 22f, escuro, true)
        caixa(a.orientacao)
        texto("Análise por regras, sem verificação de identidade ou fontes externas. Não confirma fraude nem garante segurança.", 17f)
        botao("OUVIR ORIENTAÇÃO") { falar(titulo + ". " + a.orientacao) }
        botao("PEDIR AJUDA À FAMÍLIA", escuro) { compartilharAjuda() }
        voltar()
    }

    private fun confianca() {
        tela("confianca", "Pessoa de confiança")
        val nome = prefs.getString("nome", "").orEmpty()
        val telefone = prefs.getString("telefone", "").orEmpty()
        caixa(if (telefone.isBlank())
            "Nenhuma pessoa cadastrada. Escolha alguém conhecido em quem confia."
        else
            "Pessoa escolhida: " + nome + "\nO contato só recebe pedidos de ajuda que você enviar. Não existem alertas automáticos entre celulares nesta versão.")
        val campoNome = campo("Nome da pessoa", false)
        val campoTelefone = campo("Telefone com DDD", false)
        campoTelefone.inputType = InputType.TYPE_CLASS_PHONE
        botao("SALVAR PESSOA DE CONFIANÇA") {
            val n = campoNome.text.toString().trim()
            val f = campoTelefone.text.toString().filter { it.isDigit() }
            if (n.isBlank() || f.length !in 10..13) aviso("Informe nome e telefone com DDD.")
            else {
                prefs.edit().putString("nome", n).putString("telefone", f).apply()
                confianca()
            }
        }
        if (telefone.isNotBlank()) {
            botao("LIGAR PARA " + nome) { ligar() }
            botao("ENVIAR PEDIDO DE AJUDA", escuro) { compartilharAjuda() }
            botao("REMOVER PESSOA", vermelho) {
                AlertDialog.Builder(this).setMessage("Deseja remover a pessoa de confiança?")
                    .setNegativeButton("Não", null)
                    .setPositiveButton("Remover") { _, _ ->
                        prefs.edit().remove("nome").remove("telefone").apply()
                        confianca()
                    }.show()
            }
        }
        texto("Cadastro local, sem ler toda a agenda. O familiar não tem acesso automático às suas conversas.", 17f)
        voltar()
    }

    private fun ligar() {
        val telefone = prefs.getString("telefone", "").orEmpty()
        if (telefone.isBlank()) { aviso("Cadastre uma pessoa de confiança primeiro."); return }
        runCatching { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(telefone)))) }
            .onFailure { aviso("Não foi possível abrir o telefone.") }
    }

    private fun compartilharAjuda() {
        val mensagem = "Preciso de ajuda para verificar uma situação suspeita. Por favor, me ligue pelo número que você já conhece. — ESCUDO IA"
        val numero = prefs.getString("telefone", "").orEmpty()
        if (numero.isNotBlank()) {
            val sms = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(numero)))
                .putExtra("sms_body", mensagem)
            if (runCatching { startActivity(sms) }.isSuccess) return
        }
        runCatching {
            val compartilhar = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, mensagem)
            }
            startActivity(Intent.createChooser(compartilhar, "Pedir ajuda"))
        }.onFailure { aviso("Não foi possível abrir mensagens.") }
    }

    private fun emergencia() {
        tela("emergencia", "Preciso de ajuda agora")
        caixa("Se você desconfia de um golpe, não envie mais dinheiro, senhas nem códigos. Peça ajuda por um número conhecido.")
        botao("LIGAR PARA PESSOA DE CONFIANÇA", vermelho) { ligar() }
        botao("ENVIAR PEDIDO DE AJUDA") { compartilharAjuda() }
        texto("O que aconteceu?", 23f, escuro, true)
        botao("ENVIEI UM PIX", escuro) {
            instrucoes("Pix enviado", "Abra seu banco pelo aplicativo oficial. Avise imediatamente o banco e pergunte sobre o MED. Guarde comprovantes e registre ocorrência. A devolução não é garantida.")
        }
        botao("ENTREGUEI MINHA SENHA", escuro) {
            instrucoes("Senha ou código", "Usando um aparelho confiável, entre em contato com o banco por canal oficial, troque senhas e encerre sessões desconhecidas.")
        }
        botao("INSTALEI APP SUSPEITO", escuro) {
            instrucoes("Aplicativo suspeito", "Se houver suspeita de acesso remoto, desconecte o aparelho da internet. Avise o banco por outro aparelho e procure ajuda técnica confiável.")
        }
        botao("COMPARTILHEI DOCUMENTOS", escuro) {
            instrucoes("Dados e documentos", "Pare de enviar informações, guarde as provas, registre ocorrência se necessário e acompanhe movimentações ou cadastros desconhecidos.")
        }
        voltar()
    }

    private fun instrucoes(titulo: String, orientacao: String) {
        tela("instrucoes", titulo)
        caixa(orientacao)
        botao("OUVIR INSTRUÇÕES") { falar(orientacao) }
        botao("PEDIR AJUDA", vermelho) { compartilharAjuda() }
        voltar()
    }

    private fun ajustes() {
        tela("ajustes", "Ajustes e privacidade")
        val letras = prefs.getBoolean("letras", false)
        botao(if (letras) "LETRAS GRANDES: ATIVADAS" else "AUMENTAR AS LETRAS") {
            prefs.edit().putBoolean("letras", !letras).apply()
            ajustes()
        }
        caixa("Proteção opcional: apenas prévias autorizadas do WhatsApp.\n\nMensagens analisadas não são armazenadas nem enviadas a servidores.\n\nPessoa de confiança fica somente neste aparelho.\n\nNão há alertas automáticos ao familiar nesta versão.\n\nA análise pode errar e não substitui conferência por canais oficiais.")
        botao("REVER ACESSO ÀS NOTIFICAÇÕES", escuro) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        botao("APAGAR DADOS LOCAIS", vermelho) {
            AlertDialog.Builder(this).setTitle("Apagar configurações?")
                .setMessage("Isso apaga o contato salvo e desliga a análise da ESCUDO IA. Para retirar a permissão de notificações, revogue-a também no Android.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Apagar") { _, _ ->
                    prefs.edit().clear().apply()
                    inicio()
                }.show()
        }
        voltar()
    }

    private fun abrirCompartilhamento(i: Intent?) {
        if (i == null) return
        if (i.getBooleanExtra("aviso_escudo", false)) {
            i.removeExtra("aviso_escudo")
            resultado(Analise(Risk.ALTO,
                listOf("Uma prévia de notificação autorizada apresentou sinais preocupantes."),
                "Antes de enviar dinheiro ou códigos, confirme com a pessoa pelo número que você já conhece."))
        } else if (i.action == Intent.ACTION_SEND && i.type == "text/plain") {
            val partilhado = i.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            if (partilhado.isNotBlank()) {
                if (partilhado.startsWith("https://") || partilhado.startsWith("http://") ||
                    partilhado.startsWith("www.")) analisarLink(partilhado)
                else analisarMensagem(partilhado)
            }
        }
    }

    @Deprecated("Navegação simplificada")
    override fun onBackPressed() {
        if (pagina == "inicio") super.onBackPressed() else inicio()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
