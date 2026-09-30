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
