# Registro de Refatoração - PertoDeMim

## [Análise inicial] - 2026-09-30
- **Descrição do que foi feito:** Mapeamento da estrutura existente. O backend utiliza Java 21, Spring Boot 4 e PostgreSQL. O frontend contém páginas estáticas em HTML/CSS/JavaScript; não há workspace Angular, configuração de rotas, interceptor ou guards. O modelo atual representa profissionais como registros próprios, sem uma entidade separada para serviços ofertados.
- **Arquivos Backend Afetados:** Nenhum nesta etapa.
- **Arquivos Frontend Afetados:** Nenhum nesta etapa.
- **Impacto na Segurança/Banco:** Identificados pontos a corrigir: herança de authorities entre roles, escrita de categorias por profissionais, credenciais e segredo JWT versionados e exclusão física de profissionais. Nenhuma migração foi aplicada nesta etapa.

## [Plano de implementação] - 2026-09-30
- **Descrição do que foi feito:** Criada a branch isolada `codex/refatoracao-rbac-admin-soft-delete` para receber as alterações. A implementação será alinhada à arquitetura que existe hoje, preservando JWT e busca geográfica Haversine. A criação de um frontend Angular completo não pode ser considerada concluída sem converter o frontend estático atual.
- **Arquivos Backend Afetados:** Nenhum nesta etapa.
- **Arquivos Frontend Afetados:** Nenhum nesta etapa.
- **Impacto na Segurança/Banco:** As mudanças de modelo e de autorização serão registradas nas entradas seguintes deste documento.


## [Backend — autorização RBAC e configuração] - 2026-09-30
- **Descrição do que foi feito:** Spring Security agora exige a role específica em rotas administrativas e profissionais. As authorities de cada usuário correspondem somente à role cadastrada; ADMIN e PROFESSIONAL não recebem implicitamente permissões de USER. O cadastro de categorias por rotas legadas ficou restrito a ADMIN. Credenciais de PostgreSQL e segredo JWT deixaram de usar valores fixos versionados. O bootstrap de administrador só acontece quando `BOOTSTRAP_ADMIN_EMAIL` e `BOOTSTRAP_ADMIN_PASSWORD` forem informados; a senha é codificada pelo PasswordEncoder.
- **Arquivos Backend Afetados:** `src/main/java/com/services/backend/config/SecurityConfig.java`; `src/main/java/com/services/backend/config/DataInitializer.java`; `src/main/java/com/services/backend/entities/User.java`; `src/main/resources/application.properties`.
- **Arquivos Frontend Afetados:** Nenhum nesta etapa.
- **Impacto na Segurança/Banco:** Rotas `/api/admin/**`, `/admin/**` e `/users/**` exigem ADMIN; leitura da rota exata `/professionals/me` e suas subrotas exige PROFESSIONAL; escrita de profissionais exige PROFESSIONAL ou ADMIN. Adicionado o campo `active` em `tb_users`; usuários desativados passam a falhar autenticação via `UserDetails.isEnabled()`. Hibernate está configurado com ddl-auto=update, mas a migração de produção deve ser formalizada antes de implantação.

## [Backend — painel e categorias] - 2026-09-30
- **Descrição do que foi feito:** Criado `AdminController` sob `/api/admin/**`, com busca de usuários por nome/e-mail, role e status em páginas limitadas (máximo de 100 itens), ativação/desativação de usuários e operações paginadas de criação, edição e exclusão de categorias. A busca usa consulta no repositório.
- **Arquivos Backend Afetados:** `src/main/java/com/services/backend/controllers/AdminController.java` (novo); `src/main/java/com/services/backend/repositories/UserRepository.java`.
- **Arquivos Frontend Afetados:** Nenhum. O repositório não contém Angular nem telas administrativas Angular.
- **Impacto na Segurança/Banco:** Endpoints administrativos são cobertos por `/api/admin/**` e exigem ROLE_ADMIN. Categorias são removidas pelo JPA; categorias vinculadas a profissionais existentes podem ter a exclusão recusada por integridade referencial.

## [Backend — perfil e exclusão lógica] - 2026-09-30
- **Descrição do que foi feito:** Inclusão do estado `active` no perfil profissional, rotas de edição/desativação limitadas ao e-mail autenticado, carregamento do próprio perfil por consulta dedicada e exclusão lógica no endpoint DELETE. A lista, busca Haversine e consulta por categoria ignoram registros inativos. A busca valida coordenadas e limita o raio a 500 km.
- **Arquivos Backend Afetados:** `src/main/java/com/services/backend/entities/Professional.java`; `src/main/java/com/services/backend/repositories/ProfessionalRepository.java`; `src/main/java/com/services/backend/services/ProfessionalService.java`; `src/main/java/com/services/backend/controllers/ProfessionalController.java`; `src/main/java/com/services/backend/controllers/AdminProfessionalController.java`.
- **Arquivos Frontend Afetados:** Nenhum nesta etapa.
- **Impacto na Segurança/Banco:** Inclusão do campo `active` em `professional` com padrão true para compatibilidade com registros existentes; DELETE agora grava `active=false` e mantém a linha no banco. O histórico relacionado não é fisicamente removido. A fórmula Haversine foi preservada e limitada numericamente para evitar erro de domínio de `acos`.
