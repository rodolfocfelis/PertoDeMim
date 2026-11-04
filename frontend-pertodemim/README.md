# FrontendPertodemim

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.1.4.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.


## Interface migrada para Angular — 2026-11-02

O frontend principal agora é esta aplicação Angular standalone. O layout segue uma identidade única com azul, coral, formulários reativos, tabelas responsivas, feedback inline e diálogos de confirmação.

Execute `npm install` e `npm start` nesta pasta. A interface abre em `http://127.0.0.1:4200/login`. Inicie o Spring Boot da pasta superior na porta 8080 com `DB_URL`, `DB_USER`, `DB_PASSWORD` e `JWT_SECRET` definidos. A criação inicial de administrador usa `BOOTSTRAP_ADMIN_EMAIL` e `BOOTSTRAP_ADMIN_PASSWORD` opcionais. Nenhuma credencial foi embutida no frontend.

`proxy.conf.json` encaminha as chamadas locais à API. A aplicação usa caminhos relativos `/api`. Em produção, o servidor deve encaminhar `/api/auth/*`, `/api/categories` e `/api/professionals/*` às rotas equivalentes sem o primeiro `/api` do Spring. `/api/admin/*` e `/api/me` mantêm o prefixo.

### Rotas e integração

- `/admin/users`, `/admin/categories`, `/admin/search`, `/admin/profile` exigem ADMIN.
- `/app/user/search` e `/app/user/profile` exigem USER.
- `/app/professional/services`, `/app/professional/search` e `/app/professional/profile` exigem PROFESSIONAL.
- As páginas são carregadas por lazy loading. O interceptor envia JWT apenas à API e trata 401 e 403.
- A busca aceita raio maior que zero até 500 km com seleção automática de localização. O GPS é priorizado por até 10 segundos. Se estiver indisponível ou tiver precisão pior que 2 km, usa-se o endereço cadastrado e geocodificado. Sem ambos, a interface orienta completar o perfil.
- Dados da conta são editados em `/api/me`. A atualização do e-mail renova o JWT e mantém a vinculação dos serviços no backend.
- O profissional lista apenas seus serviços em `/professionals/mine`. Desativar usa DELETE com soft delete no backend.
- O endereço do serviço é geocodificado uma única vez no Spring. A consulta opcional de CEP continua via ViaCEP.

A configuração ativa é uma SPA. O SSR/prerender geral do scaffold foi desativado para não pré-renderizar páginas privadas dependentes de sessão do navegador.

### Validação

`npm run build` gera a versão de produção. `npm test -- --watch=false` executa os testes Angular. Na raiz do backend, `mvn -Dtest=ProfessionalServiceTest test` valida isolamento por proprietário, soft delete e coordenadas inválidas. O registro completo de alterações está em `../CHANGELOG_REFATORACAO.md`.

O cadastro e perfil agora incluem contato e endereço completo com consulta opcional de CEP. Novas contas exigem senha de 8 a 72 caracteres com confirmação na interface. Administradores podem editar cadastros em `/admin/users/:id`.


### Marketplace e atendimentos

Clientes podem favoritar serviços, solicitar orçamento e acompanhar pedidos em `/app/user/requests`. Profissionais respondem em `/app/professional/requests`. O fluxo é REQUESTED → ACCEPTED → AWAITING_CONFIRMATION → COMPLETED. Cliente confirma a conclusão e pode avaliar uma vez. Pedidos pendentes/aceitos podem ser cancelados pelo cliente; pedidos pendentes podem ser recusados pelo profissional. Notas públicas refletem somente avaliações visíveis.

O administrador usa `/admin/dashboard` para indicadores e moderação de avaliações. A rota `/api/market/**` deve ser encaminhada sem reescrita de prefixo à API também no servidor de produção. Campos de preço inicial e disponibilidade ficam em Meus serviços. Solicitar orçamento registra intenção de atendimento, com preço e data finais combinados entre as partes.
