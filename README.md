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

### 2) Com banco externo (SQL Server ou Postgres)
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

### 3) Docker Compose (stack completa)
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
- `SERVER_PORT` — porta do backend (default 8080, no perfil local 8081).
- `APP_JWT_SECRET` / `APP_JWT_EXPIRATION_SECONDS` — chave e expiracao do JWT.
- `VITE_API_URL` — base da API consumida pelo frontend (default `http://localhost:8080/api/v1`).

## Usuarios seed (criados no startup)
- admin / admin123 (ADMIN)
- operador / operador123 (OPERADOR)
- logistica / log123 (LOGISTICA)
- ong / ong123 (ONG)

## Endpoints principais (prefixo `/api/v1`)
- Autenticacao: `/auth/login`, `/auth/refresh`, `/auth/email-token`, `/auth/register`
- Producao: `/produtos`, `/unidades-produtivas`, `/lotes`
- Estoque: `/estoques`, `/estoques/movimentacoes`, `/estoques/reservas`
- Doacoes & logistica: `/beneficiarios`, `/campanhas`, `/pedidos-doacao`, `/entregas`, `/rotas`, `/motoristas`, `/veiculos`
- Energia: `/energia/biodigestor/entradas`, `/energia/biogas`, `/energia/geracao`, `/energia/consumo`, `/energia/fermentacao`, `/energia/destilacao`, `/energia/emissao-co2`, `/energia/recirculacao-co2`, `/energia/simulacao`
- Dashboards: `/dashboard/**`
- Observabilidade: `/actuator/health`

## Frontend (sem Docker)
```bash
cd frontend
npm install
npm run dev -- --host    # http://localhost:5173
```

## Testes
```bash
cd backend
mvn test
```
