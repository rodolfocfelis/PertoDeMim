<h1 align="center">
  📍 PertoDeMim
</h1>

<p align="center">
  <a href="#-sobre-o-projeto">Sobre</a> •
  <a href="#-tecnologias">Tecnologias</a> •
  <a href="#-arquitetura">Arquitetura</a> •
  <a href="#-como-executar">Como Executar</a> •
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
