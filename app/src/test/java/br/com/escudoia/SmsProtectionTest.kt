package br.com.escudoia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsProtectionTest {
    @Test fun encomendaComLinkDeTaxaERiscoAlto() {
        val sms = "Sua encomenda está retida. Pague uma taxa de liberação em https://exemplo.invalid/entrega"
        assertEquals(Risk.ALTO, SmsProtection.analisarPrevia(sms).risco)
    }

    @Test fun contaBloqueadaComLinkERiscoAlto() {
        val sms = "Banco: sua conta foi bloqueada. Atualize seus dados em https://seguranca.invalid"
        assertEquals(Risk.ALTO, SmsProtection.analisarPrevia(sms).risco)
    }

    @Test fun pedidoDeCodigoERiscoAlto() {
        assertEquals(Risk.ALTO, SmsProtection.analisarPrevia("Informe seu código de segurança agora").risco)
    }

    @Test fun entregaComLinkPodeSerApenasAtencao() {
        val sms = "Seu pedido saiu para entrega. Veja https://rastreio.invalid"
        assertEquals(Risk.ATENCAO, SmsProtection.analisarPrevia(sms).risco)
    }

    @Test fun codigoDeAutenticacaoRecebidoNaoDeveSerAutomaticamenteGolpe() {
        assertEquals(Risk.SEM_SINAIS, SmsProtection.analisarPrevia("Seu código de acesso é 123456. Não compartilhe.").risco)
    }

    @Test fun smsComumNaoGeraAlerta() {
        assertEquals(Risk.SEM_SINAIS, SmsProtection.analisarPrevia("Oi, tudo bem? Chego às 18h.").risco)
    }

    @Test fun mensagemSemPreviaFicaIndeterminada() {
        assertEquals(Risk.INSUFICIENTE, SmsProtection.analisarPrevia(" ").risco)
    }

    @Test fun googleMensagensESamsungSaoCompativeis() {
        assertTrue(SmsProtection.aplicativosCompativeis.contains("com.google.android.apps.messaging"))
        assertTrue(SmsProtection.aplicativosCompativeis.contains("com.samsung.android.messaging"))
    }
}
