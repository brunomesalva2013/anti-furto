package br.com.escudoia

import org.junit.Assert.assertEquals
import org.junit.Test

class RiskEngineTest {
  @Test fun numeroNovoComPix() {
    assertEquals(Risk.ALTO, RiskEngine.mensagem("Mãe, troquei meu número. Faz um Pix agora? É urgente").risco)
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
