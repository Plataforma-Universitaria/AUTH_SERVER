# AuthServer - Servidor de Autenticação para Bot Assistente

Este projeto implementa um servidor de autenticação que integra um bot do Assistente com uma plataforma de autenticação institucional, seguindo o fluxo:

1. Usuário acessa o bot Assistente
2. Bot envia um link para autenticação com o id do chat
3. Usuário acessa o link que abre a interface de login
4. Usuário seleciona instituição, persona e insere credenciais
5. AuthServer envia as credenciais para a plataforma principal
6. Plataforma autentica com a instituição e retorna um UUID
7. AuthServer gera um JWT com o UUID no claim `sub` e a instituição no claim `institution_id`, armazenando também o token em memória pelo id do chat
8. Quando `bot.callback.url` e o id do chat estão disponíveis, o AuthServer redireciona para `${bot.callback.url}?jwt={token}&assistenteId={id}`; caso contrário, retorna a tela `callback`
9. `GET /token?assistenteId=...` exige `x-api-key` administrativo; `POST /logout` usa a mesma proteção e nunca devolve o JWT na resposta.

## Tecnologias Utilizadas

- Java 21
- Spring Boot 3.5.0
- Maven
- Spring WebFlux (para integração com APIs externas)
- Thymeleaf (para templates HTML)
- JJWT (para geração de tokens JWT)

## Configuração

O `application.properties` atual fixa a porta `9090` e lê as variáveis `ROOT_URL_AUTH`, `ROOT_URL_LOGOUT`, `ROOT_URL_SALUTATION`, `ROOT_URL_INSTITUTIONS`, `PRIVATE_KEY`, `EXP_TIME`, `ISSUER` e `CALLBACK`. `AUTH_MANAGEMENT_KEY` é lida diretamente do ambiente. O arquivo `.env.example` documenta esses valores. A chave administrativa deve ter pelo menos 32 caracteres; sem ela, `/token` e `/logout` rejeitam todas as chamadas.

`PRIVATE_KEY` assina os JWTs e deve permanecer somente no Auth Server. Configure `ISSUER` com o emissor que será validado pelos consumidores do token. A chave pública correspondente deve ser configurada como `PUBLIC_KEY` na PIPA e `GUARA_JWT_PUBLIC_KEY` no Guará; no Guará, `GUARA_JWT_ISSUER` deve repetir exatamente o valor de `ISSUER`.

Na tela de login, o botão `Entrar` muda para `Entrando...` e fica desabilitado após o envio válido do formulário, evitando submissões duplicadas enquanto a autenticação é processada. Ao retornar à página pelo histórico do navegador, o botão é reabilitado.

## Segurança

- O JWT contém o UUID externo do usuário no claim `sub` e o identificador/nome curto da instituição no claim `institution_id`
- A chave de assinatura do JWT deve ser mantida segura
- Todas as comunicações devem ser realizadas via HTTPS
- O token JWT deve ser armazenado de forma segura no backend do bot, nunca no cliente
- `GET /token` e `POST /logout` exigem `x-api-key: <AUTH_MANAGEMENT_KEY>`; o logout informa apenas o resultado, sem retornar o token.
