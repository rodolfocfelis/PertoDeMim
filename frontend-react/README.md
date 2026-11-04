# Frontend React (primeira etapa da migração)

Inicializa o frontend do PertoDeMim com React e Vite. A primeira tela migrada é o cadastro de usuários/profissionais com endereço fixo.

## Desenvolvimento

```bash
cd frontend-react
npm install
npm run dev
```

Configure `VITE_API_URL` no arquivo `.env` se a API não estiver em localhost:8080. O login e as demais páginas ainda usam o frontend HTML legado, acessível em `frontend/`.

**Dependência:** para cadastrar com endereço fixo, a API precisa conter a implementação da issue #8.
