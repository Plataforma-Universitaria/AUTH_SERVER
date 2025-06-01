# AuthServer - Servidor de Autenticação para Bot Assistente

Este projeto implementa um servidor de autenticação que integra um bot do Assistente com uma plataforma de autenticação institucional, seguindo o fluxo:

1. Usuário acessa o bot Assistente
2. Bot envia um link para autenticação
3. Usuário acessa o link que abre a interface de login
4. Usuário seleciona instituição, persona e insere credenciais
5. AuthServer envia as credenciais para a plataforma principal
6. Plataforma autentica com a instituição e retorna um UUID
7. AuthServer gera um JWT contendo o UUID
8. AuthServer redireciona de volta para o bot com o JWT

## Tecnologias Utilizadas

- Java 21
- Spring Boot 3.2.5
- Maven
- Spring WebFlux (para integração com APIs externas)
- Thymeleaf (para templates HTML)
- JJWT (para geração de tokens JWT)

## Estrutura do Projeto

## Segurança

- O JWT contém apenas o UUID do usuário, sem informações sensíveis
- A chave de assinatura do JWT deve ser mantida segura
- Todas as comunicações devem ser realizadas via HTTPS
- O token JWT deve ser armazenado de forma segura no backend do bot, nunca no cliente