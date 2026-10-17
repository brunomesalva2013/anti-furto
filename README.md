# ESCUDO IA — Android nativo

Protótipo alfa de proteção familiar assistida para Android, projetado com fontes grandes e botões simples para idosos.

## O que funciona
- Análise manual de mensagens, links e orientações de Pix **por regras locais**.
- Serviço Android de notificações: mediante autorização explícita, analisa **apenas prévias disponibilizadas em notificações** do WhatsApp e WhatsApp Business e produz avisos de risco alto.
- Cadastro manual de contato de confiança e ações explícitas para ligar ou pedir ajuda por SMS.
- Orientações emergenciais; leitura em voz alta quando o aparelho tem voz em português.

## Limitações importantes
- Não detecta todos os golpes, não examina todas as conversas e não consulta bases remotas.
- Ainda NÃO envia avisos automaticamente aos familiares. Isso exigirá cadastro, confirmação de ambos e um serviço seguro de notificações.
- Nenhuma permissão para internet, SMS, leitura de agenda completa ou acessibilidade.
- Não coleta conteúdo das notificações no servidor; não salva as mensagens analisadas.
- O APK de debug é experimental. Teste em aparelhos reais antes de divulgar.

## Como obter o APK
Abra a aba **Actions**, escolha **Compilar ESCUDO IA Android**, selecione uma execução concluída com sucesso e baixe o artefato **ESCUDO-IA-Android-APK-ALFA**. Descompacte-o para encontrar o arquivo APK. O fluxo também inicia automaticamente quando há alteração na branch main.

## Nome
O nome exibido ("ESCUDO IA") pode ser trocado no futuro sem precisar mudar agora o nome do repositório anti-furto.
