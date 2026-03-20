# Contribuindo para o PertoDeMim

Primeiramente, muito obrigado por dedicar seu tempo para contribuir com o projeto! 🎉

O **PertoDeMim** é um marketplace focado em aproximar a comunidade local, e a nossa visão para o código não é diferente. Queremos construir um ambiente colaborativo onde desenvolvedores possam trocar experiências, aprender e criar uma ferramenta útil.

Este documento serve como um guia básico para garantir que o processo de contribuição seja produtivo, organizado e agradável para todos.

---

## 🐛 Reportando Bugs

Encontrou algum erro no cálculo de distância, na interface ou no banco de dados?
1. Verifique na aba de **Issues** se alguém já reportou esse mesmo problema.
2. Se for algo novo, abra uma nova Issue e adicione a label `bug`.
3. Na descrição, seja o mais detalhista possível:
   - O que você esperava que acontecesse.
   - O que realmente aconteceu.
   - O passo a passo exato para reproduzirmos o erro.

## 💡 Sugerindo Melhorias ou Funcionalidades

Tem uma ideia bacana para melhorar o projeto?
1. Dê uma olhada nas **Issues** para ver se a ideia já está sendo discutida.
2. Se não estiver, abra uma nova Issue com a label `enhancement` (melhoria).
3. Explique sua ideia detalhadamente e como ela ajudaria os usuários ou facilitaria a manutenção do código.

---

## 🛠️ Padrões de Código

Como estamos lidando com um ecossistema envolvendo Java, Spring Boot e integrações de API, é importante mantermos uma consistência. Tente seguir estas diretrizes:
- **Clean Code:** Mantenha classes e métodos focados em uma única responsabilidade.
- **Nomenclatura:** Seja claro nos nomes de variáveis, métodos e classes. Prefira nomes descritivos em vez de abreviações confusas.
- **Organização:** Respeite a arquitetura já estabelecida no projeto (separação de Controllers, Services, Repositories, etc.).

---

## 📝 Padrão de Commits

Para mantermos o histórico do Git legível e fácil de rastrear, adotamos o padrão do **Conventional Commits**. Use sempre um prefixo antes da mensagem do commit:

- `feat:` (Para desenvolver uma nova funcionalidade)
  *Ex: feat: adiciona integração com API do ViaCEP*
- `fix:` (Para correção de bugs)
  *Ex: fix: resolve erro de autenticação do JWT*
- `docs:` (Para alterações na documentação, como o README)
- `refactor:` (Para refatoração de código que não corrige um bug nem adiciona uma feature)
- `style:` (Para formatação de código, falta de ponto e vírgula, etc.)

---

## 🚀 Processo de Pull Request (PR)

Antes de enviar o seu código para a branch `main`, siga este checklist:
1. Certifique-se de que sua branch foi criada a partir da versão mais recente da `main`.
2. Verifique se o seu código não está quebrando nenhuma funcionalidade existente.
3. Se a sua contribuição exigir novas configurações (ex: variáveis de ambiente no `application.properties`), atualize o `README.md` com as instruções.
4. Abra o PR descrevendo claramente o que foi feito e referenciando a Issue relacionada (ex: "Resolve #12").
5. Aguarde o Code Review! Faremos o possível para revisar e aprovar o mais rápido possível.

Qualquer dúvida, sinta-se à vontade para perguntar nas Issues. Vamos construir isso juntos! 🚀