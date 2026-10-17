package br.com.escudoia

import org.junit.Assert.assertEquals
import org.junit.Test

class RiskEngineTest {
  @Test fun numeroNovoComPix() {
    assertEquals(Risk.ALTO, RiskEngine.mensagem("Mãe, troquei meu número. Faz um Pix agora? É urgente").risco)
  }
  @Test fun simulacaoPixExatamenteComoNoAplicativo() {
    val mensagem = "Oi, mãe! Troquei de número. Preciso de um Pix de R$ 850 agora, é urgente."
    val analise = RiskEngine.mensagem(mensagem)
    assertEquals(Risk.ALTO, analise.risco)
  }
  @Test fun simulacaoMensagemComAvisoDeTeste() {
    val mensagem = "Oi, mãe! Troquei de número e estou usando esse WhatsApp agora. Preciso fazer um pagamento urgente de R$ 850. Consegue fazer um Pix para mim agora? SIMULAÇÃO PARA TESTAR A ESCUDO IA — NÃO ENVIE DINHEIRO."
    assertEquals(Risk.ALTO, RiskEngine.mensagem(mensagem).risco)
  }
  @Test fun codigoSecreto() {
    assertEquals(Risk.ALTO, RiskEngine.mensagem("Me envie o código do banco").risco)
  }
  @Test fun informacaoSimples() {
    assertEquals(Risk.SEM_SINAIS, RiskEngine.mensagem("Bom dia, tudo bem?").risco)
  }
  @Test fun shortener() {
    assertEquals(Risk.ATENCAO, RiskEngine.link("https://bit.ly/abc").risco)
  }
}
