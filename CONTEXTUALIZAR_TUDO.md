# CONTEXTUALIZAR TUDO: Four Fight Gym System

Documento completo para contextualização de IA sobre o projeto, abrangendo frontend, backend, banco de dados, segurança, infraestrutura e alterações recentes.

---

## 1. VISÃO GERAL
Sistema completo de gerenciamento para a **4Four Fight Academy** (academia de artes marciais em Gondomar, Portugal). Atende três perfis:
- **Público**: Site institucional, planos, agendamento de aulas experimentais
- **Alunos**: Área do aluno, assinaturas, histórico de presença
- **Administração**: Dashboard, gestão de usuários, relatórios, auditoria

**Stack principal**: Spring Boot 3.3 (Java 21) + React 19 (Vite/TypeScript) + PostgreSQL 16 + Redis 7

---

## 2. STACK TECNOLÓGICA

### Frontend
| Tecnologia | Versão | Propósito |
|------------|---------|-----------|
| React | 19.2.0 | UI Library |
| TypeScript | 5.8.3 | Tipagem estática |
| Vite | 7.3.1 | Build tool / Dev server |
| TanStack Router | 1.168.0 | File-based routing |
| TanStack Query | 5.83.0 | Data fetching/cache |
| Tailwind CSS | 4.2.1 | Estilização |
| shadcn/Radix UI | - | Componentes acessíveis |
| Zod | 3.24.2 | Validação de schemas |
| Framer Motion | 12.38.0 | Animações |
| Recharts | 2.15.4 | Gráficos |

### Backend
| Tecnologia | Versão | Propósito |
|------------|---------|-----------|
| Java | 21 | Linguagem base |
| Spring Boot | 3.3.0 | Framework principal |
| Spring Security 6 | - | Auth + JWT |
| Spring Data JPA | - | ORM (Hibernate 6) |
| PostgreSQL | 16 | Banco produção |
| H2 | 2.2.224 | Banco dev/padrão (in-memory) |
| Flyway | 10.15.2 | Migrações de banco |
| Redis | 7 | Cache + Rate limiting |
| JWT (jjwt) | 0.12.5 | Tokens RS256 |
| MapStruct | 1.5.5 | Mapeamento DTO ↔ Entidade |
| Lombok | 1.18.32 | Redução de boilerplate |
| Bucket4j | 8.7.0 | Rate limiting |
| Swagger/OpenAPI | 2.5.0 | Documentação de API |

### Infraestrutura
- Docker + Docker Compose (orquestração)
- Nginx (reverse proxy + SSL)
- Fail2Ban (proteção SSH/Nginx)
- UFW (firewall)
- Let's Encrypt (certificados TLS)

---

## 3. ESTRUTURA DE DIRETÓRIOS
```
fourfight-gym-system/
├── backend/                      # Spring Boot Backend
│   ├── src/main/java/com/gym/
│   │   ├── controller/           # Endpoints REST (auth, plan, class, etc.)
│   │   │   ├── admin/          # Controllers administrativos
│   │   │   ├── auth/           # Controllers de autenticação
│   │   │   └── ...            # Outros controllers por domínio
│   │   ├── security/            # JWT, SecurityConfig, RateLimitFilter
│   │   ├── service/             # Lógica de negócio
│   │   ├── entity/              # Entidades JPA (User, Membership, etc.)
│   │   ├── dto/                 # Data Transfer Objects (request/response)
│   │   ├── repository/          # Spring Data repositories
│   │   ├── config/              # CORS, Swagger, Cache, Dados
│   │   └── exception/          # Exceções customizadas
│   ├── src/main/resources/
│   │   ├── application.yml      # Config padrão (H2)
│   │   ├── application-dev.yml  # Config dev (PostgreSQL)
│   │   └── db/migration/        # Migrações Flyway (V1 a V10)
│   ├── docker-compose.yml       # Stack produção (app + nginx + postgres + redis)
│   ├── nginx.conf               # Config reverse proxy (SSL, rate limit, CSP)
│   ├── pom.xml                  # Dependências Maven
│   └── fail2ban/               # Configuração Fail2Ban
├── frontend/                    # React Frontend
│   ├── src/
│   │   ├── routes/              # Rotas file-based (TanStack)
│   │   ├── components/          # Componentes (site/ ui/)
│   │   ├── lib/                 # API client, utilitários
│   │   └── hooks/               # Custom hooks
│   ├── package.json             # Dependências npm
│   └── vite.config.ts          # Config Vite
├── scripts/                     # Scripts shell
│   ├── setup-firewall.sh        # Configura UFW
│   ├── setup-fail2ban.sh        # Instala/configura Fail2Ban
│   ├── start-local.sh           # Inicia backend + frontend
│   └── stop-local.sh           # Para serviços
├── docs/                        # Documentação
│   ├── PROJECT_CONTEXT.md       # Contexto completo original
│   └── DEV_SETUP.md            # Guia desenvolvimento
├── media/gymluta/               # Mídia da academia (fotos/vídeos)
└── archive/                     # Versões antigas do projeto
```

---

## 4. FUNCIONALIDADES PRINCIPAIS

### Site Público
- Home (Hero, Programas, Sobre, Contato)
- Programas: Jiu-Jitsu, Boxe/Kickboxing, Força & Condicionamento, Capoeira, MMA
- Planos de assinatura (mensalidade)
- Agendamento de aulas experimentais
- Formulário de contato

### Autenticação e Usuários
- Registro/Login com JWT (access token 15min, refresh token 7 dias)
- MFA/TOTP (Google Authenticator)
- Roles: ADMIN, MANAGER, TRAINER, CLIENT
- Recuperação de senha (demo mode)

### Gestão de Membros
- Planos com features, preços, duração
- Assinaturas (ativa, expirada, cancelada)
- Checkout com múltiplos métodos de pagamento
- Auto-renovação de membrias

### Aulas e Agendamento
- Cadastro de aulas por modalidade/horário (Jiu-Jitsu, Boxe/Kickboxing, Força, Capoeira, MMA)
- Matrícula de alunos em aulas
- Controle de presença
- Solicitações de novos horários

### Administração
- Dashboard com métricas
- Relatórios financeiros
- Gestão de usuários (alteração de roles)
- Auditoria de mudanças (audit logs)

---

## 5. IMPLEMENTAÇÃO DE SEGURANÇA (CRÍTICO)

### Rating Atual: HIGH ✅ (Após auditoria de segurança)

#### 5.1 Autenticação JWT
- **Algoritmo**: RS256 (RSA 2048)
- **Access Token**: 15 minutos (armazenado em memória no frontend)
- **Refresh Token**: 7 dias (armazenado em HttpOnly cookie)
- **Chaves**: Carregadas via variáveis de ambiente (removidas do repositório)
- **Validação**: Expiração checada em cada request

#### 5.2 Proteção IDOR (Insecure Direct Object Reference)
- **Correção aplicada em**: `MembershipController.java`
- Todos os endpoints com ID de usuário verificam se o recurso pertence ao usuário autenticado
- Exemplo de implementação:
```java
if (!membership.userId().equals(principal.id()) && !"ADMIN".equals(principal.role())) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
}
```

#### 5.3 Rate Limiting Global
- **Implementação**: `RateLimitFilter.java` (Bucket4j)
- **Cobertura**: Todos os endpoints `/api/**` (exceto health check e Swagger)
- **Limites**:
  - Login: 5 requisições/minuto
  - Refresh: 10 requisições/minuto
  - Admin: 30 requisições/minuto
  - Geral: 60 requisições/minuto
- **Nginx**: Rate limiting adicional configurado

#### 5.4 Armazenamento de Tokens (Frontend)
- **Access Token**: Em memória (proteção XSS)
- **Refresh Token**: HttpOnly cookie (Secure, SameSite=Strict, Path=/api/auth/refresh)
- **Implementação**: `api.ts` (frontend) + `AuthController.java` (backend)

#### 5.5 HTTPS/TLS (Nginx)
- Redirecionamento HTTP → HTTPS configurado
- Headers de segurança:
  - Strict-Transport-Security (HSTS)
  - X-Frame-Options: DENY
  - X-Content-Type-Options: nosniff
  - Content-Security-Policy (CSP)
  - Referrer-Policy
- Certificados Let's Encrypt (configurados via Certbot)

#### 5.6 Firewall (UFW)
- **Script**: `scripts/setup-firewall.sh`
- **Portas abertas**: 22 (SSH), 80 (HTTP), 443 (HTTPS)
- **Portas bloqueadas**: 8080 (backend), 5432 (PostgreSQL), 6379 (Redis)

#### 5.7 Fail2Ban
- **Script**: `scripts/setup-fail2ban.sh`
- Proteção contra brute force em SSH e Nginx
- Monitora logs de erro do Nginx para violações de rate limit

#### 5.8 Banco de Dados
- **Usuário dedicado**: `gymapp` (não usa superuser postgres)
- **Privilégios mínimos**: SELECT, INSERT, UPDATE, DELETE apenas
- **Rede isolada**: PostgreSQL não exposto externamente (docker network interna)
- **Credenciais**: Carregadas via variáveis de ambiente

#### 5.9 Validação de Entrada
- Spring Validation em todos os DTOs
- Regras de senha: mín 8 caracteres, 1 maiúscula, 1 número
- Uso de ORM (JPA) previne SQL injection
- Headers de segurança no Nginx bloqueiam padrões de ataque comuns

---

## 6. CONFIGURAÇÃO DE AMBIENTE

### Variáveis de Ambiente (backend/.env)
```bash
# Database
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_segura
DB_APP_PASSWORD=senha_usuario_gymapp

# Redis
REDIS_PASSWORD=sua_senha_redis
REDIS_APP_PASSWORD=senha_app_redis

# JWT (gerar com: openssl genrsa -out private_key.pem 2048)
JWT_PRIVATE_KEY=-----BEGIN RSA PRIVATE KEY-----...
JWT_PUBLIC_KEY=-----BEGIN RSA PUBLIC KEY-----...
JWT_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

# CORS
CORS_ALLOWED_ORIGINS=https://seudominio.com

# Porta externa
APP_PORT=443
```

---

## 7. INSTRUÇÕES DE SETUP

### Desenvolvimento Local
```bash
# Instalar dependências frontend
cd frontend && npm install

# Iniciar backend (perfil default H2)
cd backend && mvn spring-boot:run

# Iniciar frontend
cd frontend && npm run dev

# Ou iniciar tudo de uma vez (raiz do projeto)
npm run dev:full
```

### Produção (Docker)
```bash
cd backend
docker-compose up -d --build
```

### Pós-Instalação (Produção)
```bash
# 1. Configurar firewall
sudo ./scripts/setup-firewall.sh

# 2. Instalar Fail2Ban
sudo ./scripts/setup-fail2ban.sh

# 3. Configurar SSL (após DNS apontar para o servidor)
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d seudominio.com
```

---

## 8. ALTERAÇÕES RECENTES

### Auditoria de Segurança (Upgrade: Medium → High)
1. **IDOR Fixes**: Adicionadas verificações de propriedade em todos os endpoints de usuário
2. **Rate Limiting Global**: Estendido para todos os endpoints `/api/**`
3. **JWT Key Exposure**: Removidas chaves privadas do repositório
4. **HTTPS Config**: Nginx configurado com SSL, redirect HTTP→HTTPS, CSP headers
5. **Firewall**: Script de configuração UFW criado
6. **DB Security**: Usuário dedicado `gymapp` com privilégios mínimos
7. **Token Storage**: Access token em memória, refresh token em HttpOnly cookie
8. **Fail2Ban**: Configuração e script de instalação criados

### Correção de Build (Maven Compilation)
**Root Cause**: 3 problemas independentes:
1. **pom.xml**: Propriedade `lombok.version` não definida (referenciada mas nunca setada)
2. **RateLimitFilter.java**: Método duplicado `createBucket(String)` (versão switch + if-else)
3. **AuthController.java**: Uso incorreto de acessores de record Java (`getAccessToken()` em vez de `accessToken()`)

**Arquivos Alterados**:
- `backend/pom.xml`: Adicionada propriedade `<lombok.version>1.18.32</lombok.version>`
- `backend/src/main/java/com/gym/security/RateLimitFilter.java`: Removido método duplicado
- `backend/src/main/java/com/gym/controller/AuthController.java`: Corrigidos acessores de record

**Resultado**:
```
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 15.298 s
```

### Adição de Modalidades e Sistemas de Graduação
**Objetivo**: Adicionar Capoeira e MMA com sistemas de faixas/níveis para todas as modalidades.

**Backend (Seed Data)**:
- `backend/src/main/java/com/gym/config/DataInitializer.java`: 
  - Adicionados métodos `initMartialArts()` e `initGraduations()`
  - 5 modalidades: Jiu-Jitsu, Boxe/Kickboxing, Força & Condicionamento, Capoeira, MMA
  - Sistemas de graduação completos para cada modalidade
- `backend/src/main/java/com/gym/repository/MartialArtRepository.java`:
  - Adicionado método `findByName()` para buscar modalidades

**Frontend (Páginas e Rotas)**:
- `frontend/src/routes/programs.tsx`: Adicionados cards para Capoeira e MMA
- `frontend/src/routes/programas.capoeira.tsx`: **Novo arquivo** - página completa da Capoeira
- `frontend/src/routes/programas.mma.tsx`: **Novo arquivo** - página completa do MMA
- `frontend/src/routes/programas.jiu-jitsu.tsx`: Adicionada seção de graduação (9 faixas)
- `frontend/src/routes/programas.boxe-kickboxing.tsx`: Adicionada seção de níveis de progressão (4 níveis)
- `frontend/src/routes/programas.forca-condicionamento.tsx`: Adicionada seção de níveis de progressão (4 níveis)

**Sistemas de Graduação/Progressão**:
- Jiu-Jitsu: 9 faixas (Branca, Cinzenta, Amarela, Laranja, Verde, Azul, Roxa, Castanha, Preta)
- Boxe/Kickboxing: 4 níveis (Iniciante, Intermédio, Avançado, Competição)
- Força & Condicionamento: 4 níveis (Base, Intermédio, Avançado, Performance)
- Capoeira: 15 cordões (Crua → Crua-Amarela → ... → Vermelha)
- MMA: 5 níveis (Fundamentos, Intermédio, Avançado, Sparring, Competição)

**Resultado**:
```
Backend: mvn clean install -DskipTests ✅
Frontend: npm run build ✅ (built in ~4s)
```

**Rotas Disponíveis**:
- `/programs` - Lista todos os 5 programas
- `/programas/jiu-jitsu` - Detalhes Jiu-Jitsu + graduação
- `/programas/boxe-kickboxing` - Detalhes Boxe/Kickboxing + progressão
- `/programas/forca-condicionamento` - Detalhes Força + progressão
- `/programas/capoeira` - Detalhes Capoeira + cordões
- `/programas/mma` - Detalhes MMA + níveis

---

## 9. TESTES
```bash
# Rodar testes backend
cd backend && mvn test

# Testes existentes:
- SecurityRegressionTest: 9 testes (validação de segurança)
```

---

## 10. NOTAS IMPORTANTES PARA IA
1. **JWT Keys**: Não estão mais no repositório. Usar variáveis de ambiente.
2. **Perfis Spring**: 
   - `default`: H2 in-memory (desenvolvimento rápido)
   - `dev`: PostgreSQL (desenvolvimento)
   - `prod`: PostgreSQL + Redis (produção)
3. **Demo Mode**: Frontend funciona sem backend (simula pagamentos com cartão teste 4242...)
4. **Entidades Principais**:
   - `User` → `Membership` → `Plan`
   - `Class` → `Enrollment` → `Attendance`
   - `MartialArt` → `Graduation` (belt/rank system for all modalities)
   - `Trainer`, `Payment`, `Contact`, `Notification`

5. **Modalidades e Graduações**:
   - Jiu-Jitsu: 9 faixas (Branca → Preta)
   - Boxe/Kickboxing: 4 níveis (Iniciante → Competição)
   - Força & Condicionamento: 4 níveis (Base → Performance)
   - Capoeira: 15 cordões (Crua → Vermelha)
   - MMA: 5 níveis (Fundamentos → Competição)
5. **Endpoints Principais**:
   - `/api/auth/*`: Login, registro, refresh, MFA
   - `/api/memberships/*`: Gestão de assinaturas
   - `/api/classes/*`: Aulas e matrículas
   - `/api/admin/*`: Administração (apenas ADMIN)
6. **Build**: Lombok 1.18.32 compatível com Java 21, annotation processing configurado no Maven compiler plugin.

---

## 11. COMANDOS ÚTEIS
```bash
# Build backend
cd backend && mvn clean install

# Rodar testes
cd backend && mvn test

# Verificar firewall
sudo ufw status verbose

# Verificar Fail2Ban
sudo fail2ban-client status

# Logs do backend
tail -f backend/logs/gym-management.log

# URLs locais:
# Frontend: http://localhost:5173
# Backend API: http://localhost:8080/api
# Swagger: http://localhost:8080/swagger-ui/index.html
# Health Check: http://localhost:8080/actuator/health
```
