<h1 align="center">
  📍 PertoDeMim
</h1>

<p align="center">
  <a href="#-sobre-o-projeto">Sobre</a> •
  <a href="#-tecnologias">Tecnologias</a> •
  <a href="#-arquitetura">Arquitetura</a> •
  <a href="#-como-executar">Como Executar</a> •
  <a href="#-roadmap">Roadmap</a> •
  <a href="#-autor">Autor</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Em%20Desenvolvimento-green?style=for-the-badge" alt="Status">
  <img src="https://img.shields.io/badge/Licen%C3%A7a-MIT-blue?style=for-the-badge" alt="License">
</p>

---

## 📖 Sobre o Projeto

O **PertoDeMim** é um marketplace de serviços focado em hiperlocalização. O objetivo é simples: conectar usuários a profissionais independentes (eletricistas, encanadores, mecânicos) baseando-se na proximidade real em quilômetros. 

Diferente de sistemas de busca comuns, o motor por trás do PertoDeMim utiliza a geolocalização exata do usuário e do prestador de serviço para calcular rotas e distâncias usando a Fórmula de Haversine, entregando resultados precisos e integração com o WhatsApp com um único clique.

## 💻 Tecnologias

As seguintes ferramentas foram usadas na construção do projeto:

**Backend:**
- ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) **Java 17+**
- ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) **Spring Boot** (Security, Web, Data JPA)
- ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens) **Auth0 Java-JWT**
- ![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white) **PostgreSQL**

**Frontend & APIs:**
- ![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=for-the-badge&logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=for-the-badge&logo=css3&logoColor=white) ![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E)
- 🗺️ **API Nominatim (OSM):** Geocodificação de endereços.
- 📮 **API ViaCEP:** Autocomplete e validação de código postal.

## 🏗️ Arquitetura

O sistema adota uma arquitetura de cliente-servidor com controle rigoroso de acesso (RBAC).

1. **Camada de Segurança:** Acesso gerenciado por tokens JWT.
2. **Perfis de Acesso:** - `ADMIN`: Acesso ao painel de controle e gerenciamento de usuários.
   - `PROFESSIONAL`: Permissão para criar perfis, categorias de serviços e registrar geolocalização.
   - `USER`: Acesso à busca por raio de distância.
3. **Fluxo de Dados Geográficos:** O endereço fornecido no cadastro é enviado ao *Nominatim*, que retorna `Latitude` e `Longitude`. O Spring Boot armazena esses dados e processa o cálculo de distância em tempo real durante a requisição de busca.

## 🚀 Como Executar

### 1. Clonando o repositório
```bash
git clone [https://github.com/SEU_USUARIO/pertodemim-api.git](https://github.com/SEU_USUARIO/pertodemim-api.git)
```

### 2. Configurando as Variáveis de Ambiente
Crie um arquivo `application-dev.properties` em `src/main/resources` e adicione as chaves sensíveis:
```properties
spring.datasource.password=sua_senha_do_postgres
api.security.token.secret=sua_chave_secreta_aqui
```

### 3. Rodando o Backend
Abra a pasta do projeto na sua IDE e execute a classe principal `BackendApplication.java`. O servidor iniciará na porta `8080`.

### 4. Rodando o Frontend
Abra o arquivo `registro.html` no seu navegador de preferência para criar o usuário `ADMIN` inicial e navegue pelo sistema.

## 🛣️ Roadmap

- [x] Cadastro e Busca de Profissionais
- [x] Integração com ViaCEP e Nominatim
- [x] Cálculo de distância (Haversine)
- [x] Autenticação JWT e Perfis de Acesso
- [ ] Implementação de Mapa visual (Leaflet.js) para exibição de pins
- [ ] Edição de perfil para o profissional
- [ ] Deploy na nuvem (Ex: Render)

---

## 👨‍💻 Autor

Desenvolvido com 💡 por **Rodolfo**.
