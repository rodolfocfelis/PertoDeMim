<h1 align="center">
  📍 PertoDeMim
</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Em%20Desenvolvimento-green?style=for-the-badge" alt="Status">
  <img src="https://img.shields.io/badge/Licen%C3%A7a-MIT-blue?style=for-the-badge" alt="License">
</p>

---

## 📖 Sobre o Projeto

O **PertoDeMim** é um marketplace de serviços focado em hiperlocalização. O objetivo é simples: conectar usuários a profissionais independentes (eletricistas, encanadores, mecânicos) baseando-se na proximidade em quilômetros. O frontend principal está em `frontend-pertodemim` (Angular); `frontend` preserva as páginas HTML legadas.

Diferente de sistemas de busca comuns, o motor por trás do PertoDeMim utiliza a geolocalização exata do usuário e do prestador de serviço para calcular distâncias em linha reta usando a Fórmula de Haversine, entregando resultados precisos e integração com o WhatsApp com um único clique.

## 💻 Tecnologias

As seguintes ferramentas foram usadas na construção do projeto:

**Backend:**
- ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) **Java 21**
- ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) **Spring Boot** (Security, Web, Data JPA)
- ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens) **Auth0 Java-JWT**
- ![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white) **PostgreSQL**

**Frontend & APIs:**
- **Angular 21, TypeScript e RxJS**, com rotas por perfil e carregamento sob demanda.
- ![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=for-the-badge&logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=for-the-badge&logo=css3&logoColor=white) ![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E)
- 🗺️ **API Nominatim (OSM):** Geocodificação de endereços.
- 📮 **API ViaCEP:** Autocomplete e validação de código postal.

## 🏗️ Arquitetura

O sistema adota uma arquitetura de cliente-servidor com controle rigoroso de acesso (RBAC).

1. **Camada de Segurança:** Acesso gerenciado por tokens JWT.
2. **Perfis de Acesso:** - `ADMIN`: Acesso ao painel de controle e gerenciamento de usuários.
   - `PROFESSIONAL`: Gestão do próprio perfil, serviços e solicitações recebidas. Categorias são gerenciadas por ADMIN.
   - `USER`: Busca por proximidade, favoritos, solicitações e avaliações após atendimento concluído.
3. **Fluxo de Dados Geográficos:** O endereço fornecido no cadastro é enviado ao *Nominatim*, que retorna `Latitude` e `Longitude`. O Spring Boot armazena esses dados e processa o cálculo de distância em tempo real durante a requisição de busca.

## 🚀 Como Executar

### Backend (Java 21 e PostgreSQL)

```bash
git clone https://github.com/rodolfocfelis/PertoDeMim.git
cd PertoDeMim
```

Crie um banco PostgreSQL e configure no ambiente `DB_URL` (JDBC), `DB_USER`, `DB_PASSWORD` e `JWT_SECRET` (segredo forte). A aplicação não inclui credenciais reais. Para criar o administrador inicial, defina `BOOTSTRAP_ADMIN_EMAIL` e `BOOTSTRAP_ADMIN_PASSWORD` antes de iniciar pela primeira vez. Cadastro público permite apenas USER e PROFESSIONAL.

Execute `./mvnw spring-boot:run` (no Windows, `mvnw.cmd spring-boot:run`). A API inicia na porta 8080.

O ambiente de desenvolvimento usa `spring.jpa.hibernate.ddl-auto=update`. O script `src/main/resources/db/manual/20261102_marketplace.sql` documenta a expansão do marketplace e não é executado automaticamente. Instalações com validação de schema precisam aplicar as alterações antes de iniciar; esse script pressupõe as tabelas básicas existentes.

### Frontend Angular

Use uma versão do Node compatível com Angular 21 (a validação local usou Node 24).

```bash
cd frontend-pertodemim
npm ci
npm start
```

Acesse `http://127.0.0.1:4200`. O proxy local encaminha `/api` ao Spring Boot. Consulte [a documentação do frontend](frontend-pertodemim/README.md) para rotas e configuração do proxy em produção. A busca usa GPS com fallback para o endereço cadastrado.

### Validação

Na raiz: `./mvnw -Dtest=ProfessionalServiceTest,AccountDetailsServiceTest,RequestWorkflowTest test` (PowerShell: coloque o argumento `-Dtest=...` entre aspas).

Na pasta Angular: `npm run build` e `npm test -- --watch=false`. A última execução passou com 27 testes Angular e 10 testes Java selecionados. O teste de contexto Spring completo depende de PostgreSQL e das variáveis de ambiente.

Dados, contas, serviços e avaliações de demonstração criados no PostgreSQL local não fazem parte do código versionado. O histórico detalhado está em [CHANGELOG_REFATORACAO.md](CHANGELOG_REFATORACAO.md).

## 🤝 Como Contribuir

Ficou interessado em ajudar a evoluir o **PertoDeMim**? Toda contribuição (seja código, correção de bugs ou melhoria na documentação) é muito bem-vinda! Siga o passo a passo abaixo:

**1. Faça um Fork do projeto**
No canto superior direito desta página no GitHub, clique no botão **Fork**. Isso criará uma cópia exata deste repositório dentro da sua própria conta do GitHub, onde você terá total liberdade para mexer.

**2. Clone o seu Fork para a sua máquina**
No seu terminal, baixe a cópia que você acabou de criar (lembre-se de substituir `SEU_USUARIO` pelo seu nome de usuário no GitHub):
```bash
git clone [https://github.com/SEU_USUARIO/pertodemim-api.git](https://github.com/SEU_USUARIO/pertodemim-api.git)
```

**3. Crie uma Branch para a sua alteração**
Para manter tudo organizado, nunca trabalhe diretamente na branch `main`. Crie uma nova ramificação (branch) com um nome que indique o que você vai fazer:
```bash
cd pertodemim-api
git checkout -b feature/nome-da-sua-funcionalidade
```
*(Dica: use prefixos como `feature/` para novas funções, `fix/` para correções de erros ou `docs/` para documentação).*

**4. Faça as alterações e o Commit**
Escreva seu código! Quando terminar, adicione os arquivos modificados e crie um "ponto de salvamento" (commit) com uma mensagem clara do que foi feito:
```bash
git add .
git commit -m "feat: adiciona botão de voltar na tela de perfil"
```

**5. Envie (Push) as alterações para o seu GitHub**
Agora, mande o código da sua máquina de volta para o seu repositório (Fork) no GitHub:
```bash
git push origin feature/nome-da-sua-funcionalidade
```

**6. Abra um Pull Request (PR)**
Volte para a página deste repositório original no GitHub. Você verá um aviso amarelo com um botão verde escrito **Compare & pull request**. Clique nele, escreva um breve resumo do que você desenvolveu e envie.

Assim que enviado, revisaremos o seu código para integrá-lo ao projeto oficial! 🎉


## 👨‍💻 Autor

Desenvolvido com 💡 por **Rodolfo**.
