# ESCUDO IA — Android nativo (v1.5 alfa)

Protótipo experimental de proteção assistida contra golpes, com telas acessíveis, análise local e permissões opcionais.

## Novidade: proteção de SMS

O usuário pode ativar **separadamente** a análise de notificações do WhatsApp e de SMS. Depois da autorização no aplicativo e do acesso às notificações no Android, a ESCUDO IA examina apenas as **prévias visíveis** de apps selecionados:

- WhatsApp e WhatsApp Business opcional
- Google Mensagens, Samsung Mensagens e alguns aplicativos nativos de SMS de Motorola, Xiaomi e Sony.

O detector usa **regras locais** para reconhecer combinações de suspeita: encomendas retidas com link e cobrança, falsa regularização bancária com link, pedidos de senha/código e outros padrões de golpes.

**Não** solicita permissões de leitura da caixa de SMS ou contatos. Não lê a caixa de SMS, não abre links, não confirma destinatários, não identifica números de telefone e não envia prévias para a nuvem. Algumas notificações podem não fornecer o texto para análise, o que impede a detecção. A ausência de alerta não comprova segurança.

O acesso Android a notificações é sensível, pois o sistema dá visibilidade técnica a notificações de outros apps. A ESCUDO IA descarta notificações fora dos aplicativos selecionados. Essa versão não foi auditada independentemente.

## Teste seguro da proteção

No aplicativo, toque em **TESTAR ALERTA DE SMS (SIMULAÇÃO)**. A ESCUDO IA classifica uma mensagem fictícia e publica uma notificação claramente rotulada como teste. Isso **não** testa a integração real com SMS. Para isso, ative a proteção SMS, autorize o acesso às notificações e receba uma mensagem de teste por um segundo telefone com a prévia visível no aplicativo de mensagens compatível.

## Compilação e testes

Em **Actions** → **Compilar ESCUDO IA Android**, abra a execução concluída com sucesso e baixe o artefato ESCUDO-IA-Android-APK-ALFA. Extraia o ZIP para obter app-debug.apk.

Também há o fluxo **Testar ESCUDO IA no emulador**, que instala e inicia o APK em um Android virtual.

## O que ainda falta

- Base externa de reputação de links e números.
- Notificações automáticas aos familiares de confiança com consentimento bilateral e segurança.
- Validação de notificações reais dos principais fabricantes e teste com idosos.
- Avaliação de falsos positivos, robustez e revisão independente de privacidade e segurança.

Não utilize esta versão como único mecanismo de proteção contra golpes.
