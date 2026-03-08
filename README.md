# SustentaFome

Plataforma web para rastrear producao, estoque, logistica de doacoes e cadeia energetica (biodigestor, biogas, geracao/consumo e recirculacao de CO2) em um ecossistema circular.

## Visao geral
- **Backend (`/backend`)**: Spring Boot 3.2, Spring Security + JWT, JPA, Flyway, Actuator e OpenAPI. Modulos: autenticacao/registro, producao, estoque, doacoes/logistica, energia e dashboards.
- **Frontend (`/frontend`)**: React 18 + Vite, Axios com interceptor JWT, React Router e graficos Recharts.
- **Orquestracao**: `docker-compose.yml` com Postgres 15, backend e frontend (Nginx). CORS ja libera `http://localhost:5173`.

## Estrutura do repositorio
- `backend/` — API REST com seeds de usuarios, produtos, unidades produtivas, armazem e entidades beneficiarias.
- `frontend/` — UI de dashboard (login, producao, estoque, doacoes, energia).
- `docker-compose.yml` — stack local contendo Postgres + servicos.
- `src/main/java/...Main.java` — stub gerado pelo IDE (nao usado).

## Como rodar
### 1) Dev rapido com H2 em memoria
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
- API: http://localhost:8081/api/v1
- Swagger: http://localhost:8081/swagger-ui.html
- Usa H2 em memoria (sem dependencia externa) e desativa Flyway.

### 2) Stage/Prod (SQL Server ou Postgres) com Flyway validando
Escolha o perfil via `SPRING_PROFILES_ACTIVE=stage` ou `prod`. Credenciais/URLs devem vir de variaveis (recomendado Key Vault + App Settings no Azure).
```bash
cd backend
set SPRING_PROFILES_ACTIVE=prod
set PROD_DB_URL=jdbc:sqlserver://<host>:1433;databaseName=<db>;encrypt=true
set PROD_DB_USERNAME=<user>
set PROD_DB_PASSWORD=<secret>
mvn spring-boot:run
```
- Flyway roda com `baselineOnMigrate=true` e `validateOnMigrate=true`. Se o banco tiver dados legados, rode `mvn flyway:baseline` antes da primeira migração ou suba a aplicação para aplicar a baseline V1 e a migração V2 (auditoria/precision).

### 3) Com banco externo manual (override rapido)
O `application.yml` padrao aponta para SQL Server. Para trocar o banco, sobrescreva as propriedades do Spring:
```bash
# exemplo Postgres local
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sustentafome
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=postgres
set SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
set SERVER_PORT=8080
cd backend
mvn spring-boot:run
```
Dialeto e URL devem refletir o banco escolhido (SQL Server, Postgres etc).

### 3.1) Backend com Azure SQL (perfil `prod`)
Exemplo com o servidor `dbonu.database.windows.net` e banco `DBonu`:
```bash
cd backend
set SPRING_PROFILES_ACTIVE=prod
set SERVER_PORT=8080
set PROD_DB_URL=jdbc:sqlserver://dbonu.database.windows.net:1433;databaseName=DBonu;encrypt=true;trustServerCertificate=false;loginTimeout=30
set PROD_DB_USERNAME=<login-do-servidor>@dbonu
set PROD_DB_PASSWORD=<senha-do-login>
set APP_JWT_SECRET=<segredo-forte>
set APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5176
set APP_SECURITY_REQUIRE_SSL=false   # evita redirecionamento 302 para https://localhost:8443 em dev
mvn spring-boot:run
```
- Se precisar garantir o admin com senha `admin123`, atualize direto no banco:
```sql
UPDATE dbo.users
SET password = '$2b$10$rKuGjRw.Hnm5ljlZrbOw/uSlPKPe.nmAo8.xsnvyVpAQoIOqTu5aa'
WHERE username = 'admin';
```

### 4) Docker Compose (stack completa)
```bash
cd "C:\Users\Injected\IdeaProjects\ODSS-11-TESTE"
# Recomenda-se ajustar o service backend para receber as variaveis abaixo:
# SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/sustentafome
# SPRING_DATASOURCE_USERNAME=postgres
# SPRING_DATASOURCE_PASSWORD=postgres
# SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
# APP_JWT_SECRET=troque-esta-chave
docker-compose up --build
```
- API: http://localhost:8080/api/v1
- Swagger: http://localhost:8080/swagger-ui.html
- Frontend (Nginx): http://localhost:5173

## Variaveis importantes
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT` — conexao do banco.
- `SPRING_PROFILES_ACTIVE` — use `local` para H2.
- `STAGE_DB_URL`, `STAGE_DB_USERNAME`, `STAGE_DB_PASSWORD` — usados no perfil `stage`.
- `PROD_DB_URL`, `PROD_DB_USERNAME`, `PROD_DB_PASSWORD` — usados no perfil `prod`.
- `SERVER_PORT` — porta do backend (default 8080, no perfil local 8081).
- `APP_JWT_SECRET` / `APP_JWT_EXPIRATION_SECONDS` — chave e expiracao do JWT.
- `APP_CORS_ALLOWED_ORIGINS`, `APP_CORS_ALLOWED_METHODS`, `APP_CORS_ALLOWED_HEADERS` — lista de CORS permitidos (default apenas http://localhost:5173).
- `APP_REQUIRE_SSL` / `APP_SECURITY_REQUIRE_SSL` — força HTTPS quando true (ativo por default em `prod`); em dev local use false para evitar redirecionamento para 8443.
- `APP_RATE_LIMIT_ENABLED`, `APP_RATE_LIMIT_RPM` — liga/desliga e define limite por IP+rota (default 120 rpm; prod default 80).
- `SPRING_FLYWAY_VALIDATE` — mantém validação das migrações (default true nos ambientes controlados).
- `VITE_API_URL` — base da API consumida pelo frontend (default `http://localhost:8080/api/v1`).

## Usuarios seed (criados no startup)
- admin / admin123 (ADMIN)
- operador / operador123 (OPERADOR)
- logistica / log123 (LOGISTICA)
- ong / ong123 (ONG)

## Endpoints principais (prefixo `/api/v1`)
- Autenticacao: `/auth/login`, `/auth/refresh`, `/auth/email-token`, `/auth/register`
- Producao: `/produtos`, `/unidades-produtivas`, `/lotes`
- Estoque: `/estoques`, `/estoques/movimentacoes`, `/estoques/reservas`, `/estoques/reservas/{id}/bloquear`, `/estoques/transferencias`, `/estoques/contagens`
- Doacoes & logistica: `/beneficiarios`, `/campanhas`, `/pedidos-doacao`, `/entregas`, `/rotas`, `/motoristas`, `/veiculos`
- Energia: `/energia/biodigestor/entradas`, `/energia/biogas`, `/energia/geracao`, `/energia/consumo`, `/energia/fermentacao`, `/energia/destilacao`, `/energia/emissao-co2`, `/energia/recirculacao-co2`, `/energia/simulacao`
- Dashboards: `/dashboard/**`
- Observabilidade: `/actuator/health`

## Auditoria e integridade de dados
- Entidades sensíveis (estoque, doações e energia) agora têm `created_at`, `updated_at`, `created_by` via JPA Auditing. Certifique-se de autenticar requests para popular `created_by`; em cenarios sem usuário, cairá como `system`.
- Migração `V2__audit_and_precision.sql` padroniza colunas de quantidade para DECIMAL(19,4) e adiciona os campos de auditoria.
- Baseline `V1__baseline.sql` permanece como marcador para bancos já existentes; `baselineOnMigrate=true` está ativo.

## Estoque & Produção (novidades)
- Produtos passaram a ter `perecivel` e `validadeDiasPadrao`; lotes guardam `codigoLote` e `dataValidade`, propagados para o item de estoque.
- Expedição segue FEFO (primeiro que vence) e, na ausência de validade, PEPS (ordem de criação) ao reservar ou dar saída sem lote informado.
- Reservas não reduzem o saldo físico: aumentam `reservado`; o bloqueio durante separação move para `bloqueado` via `/estoques/reservas/{id}/bloquear`, consumido automaticamente na saída.
- Transferências internas entre armazéns criam bloqueio na origem, geram movimentos de saída/entrada e finalizam a reserva como consumida.
- Inventário cíclico semanal: `/estoques/contagens` registra a contagem, calcula diferenças auditáveis e lança movimentação de ajuste (AJUSTE) quando necessário.

## Solução de problemas comuns
- **CORS / preflight 403:** inclua a origem do frontend em `APP_CORS_ALLOWED_ORIGINS` (ex.: `http://localhost:5173,http://localhost:5176`) e reinicie o backend.
- **Redirect para HTTPS (302 para https://localhost:8443):** deixe `APP_SECURITY_REQUIRE_SSL=false` em dev.
- **“Bad credentials” mesmo com usuário seed:** confirme o hash bcrypt no banco; para `admin123` use `$2b$10$rKuGjRw.Hnm5ljlZrbOw/uSlPKPe.nmAo8.xsnvyVpAQoIOqTu5aa`.
- **Backend não responde na porta esperada:** verifique `SERVER_PORT` e se o processo Java está escutando (`Get-NetTCPConnection -LocalPort 8080 -State Listen` no PowerShell).

## Frontend (sem Docker)
```bash
cd frontend
npm install
npm run dev -- --host    # http://localhost:5173
```
- Para apontar para o backend: defina `VITE_API_URL` antes de subir. Ex.: `set VITE_API_URL=http://localhost:8080/api/v1`.
- Porta alternativa usada em testes: `npm run dev -- --host --port 5176` (libere no backend via `APP_CORS_ALLOWED_ORIGINS`).

## Testes
```bash
cd backend
mvn test
```
