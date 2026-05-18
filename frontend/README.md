# 4Four Fight Academy - Sistema de Adesao

Plataforma SaaS para gestao de adesoes de ginasia, incluindo seleccao de planos, registo de utilizadores e processamento de pagamentos.

## Tecnologias

- React 19
- Vite
- TanStack Router (roteamento baseado em ficheiros)
- TanStack Query (gestao de dados API)
- Tailwind CSS
- shadcn/ui
- TypeScript

## Como executar localmente

1. Clonar o repositorio
2. Instalar dependencias: `npm install`
3. Iniciar servidor de desenvolvimento: `npm run dev`
4. Aceder a aplicacao em `http://localhost:5173`

## Estrutura do Projeto

```
src/
├── routes/             # Rotas baseadas em ficheiros (TanStack Router)
│   ├── checkout/      # Fluxo de checkout
│   ├── membership/    # Processamento de pagamento e confirmacao
│   └── ...            # Outras paginas (planos, login, etc.)
├── components/
│   ├── site/          # Componentes especificos do negocio
│   └── ui/            # Componentes shadcn/ui
├── contexts/           # Contextos React (apenas dados verdadeiramente globais)
│   └── auth-context.tsx  # Estado de autenticacao (user, login, logout)
├── queries/            # TanStack Query hooks e query keys
│   ├── query-keys.ts  # Fabrica de chaves de query
│   ├── auth.ts        # Queries/mutations de autenticacao
│   ├── plans.ts       # Queries/mutations de planos
│   ├── memberships.ts # Queries/mutations de membresias e checkout
│   ├── classes.ts     # Queries/mutations de aulas e inscricoes
│   ├── student.ts     # Queries/mutations de perfil do aluno
│   ├── attendance.ts  # Queries/mutations de presencas
│   ├── belts.ts       # Queries/mutations de faixas
│   └── contacts.ts    # Mutations de contacto e aula experimental
├── providers/          # Providers de contexto
│   └── query-provider.tsx  # TanStack Query provider
├── types/              # Tipos TypeScript centralizados
│   ├── api.ts         # Interfaces de todas as entidades da API
│   ├── schedule.ts    # Tipos especificos do horario
│   └── index.ts       # Re-exportacao de todos os tipos
├── lib/                # Utilitarios e cliente API
├── hooks/              # Hooks UI (mobile, scroll reveal)
└── styles.css          # Estilos globais
```

## Gestao de Estado

O projeto segue uma arquitetura clara de separacao de responsabilidades:

### Estado Global (Context)
- **AuthContext** (`contexts/auth-context.tsx`): apenas dados de autenticacao — user logado, login, logout, verificacao de roles
- Acesso via hook `useAuth()`

### Dados da API (TanStack Query)
- Todos os dados vindos do backend sao geridos por TanStack Query
- Queries para leitura: `usePlans()`, `useMyMembership()`, `useClasses()`, etc.
- Mutations para escrita: `useLoginMutation()`, `useEnrollClass()`, `useCreateContact()`, etc.
- Chaves centralizadas em `queries/query-keys.ts`
- Cache automatico, invalidacao e refetch

### Estado Local (useState)
- Formularios (inputs, validacao)
- Modais e dialogs (aberto/fechado)
- Filtros e selecoes de UI
- Estados de loading/error locais
- Mensagens temporarias (toast, feedback)

### Regras
- Nunca duplicar dados da API em useState
- Formularios usam estado local, nunca Context
- Context apenas para dados verdadeiramente globais (auth, tema)
- Tipos centralizados em `types/`, nao duplicados em componentes

## Modo de Teste (Demo)

O fluxo de pagamento funciona sem backend. Se a API estiver indisponivel, e gerado um ID de teste (`demo-{timestamp}`), ativando o modo demo:

- Ignora validacoes reais
- Preenche automaticamente dados de teste: 4242 4242 4242 4242, 12/30, 123, Test User
- Simula pagamento com atraso de 1 segundo
- Redireciona automaticamente para a pagina de sucesso
