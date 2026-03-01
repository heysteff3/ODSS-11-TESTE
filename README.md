# SustentaFome

Sistema web completo para rastreabilidade da produção, estoque, logística de doações e monitoramento energético de um ecossistema circular (estufas, aquicultura, cogumelos, biodigestor, termelétrica e recirculação de CO2).

## Estrutura
- `backend/` Spring Boot 3 (API REST, JWT, JPA, Flyway, Actuator, OpenAPI)
- `frontend/` React + Vite (dashboard, CRUDs básicos)
- `docker-compose.yml` Postgres + backend + frontend

## Como rodar rapidamente (Docker)
```bash
cd "C:\Users\HeySteff\IdeaProjects\ODS 11 ONU"
docker-compose up --build
```
- API: http://localhost:8080/api/v1
- Swagger: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:5173

## Rodar local (sem docker)
Backend:
```bash
cd backend
# defina variáveis conforme abaixo
mvn spring-boot:run
```
Frontend (Node 18+ requerido):
```bash
cd frontend
npm install
npm run dev -- --host
```

## Variáveis de ambiente relevantes
- `POSTGRES_HOST` (default `localhost`)
- `POSTGRES_PORT` (default `5432`)
- `POSTGRES_DB` (default `sustentafome`)
- `POSTGRES_USER` (default `postgres`)
- `POSTGRES_PASSWORD` (default `postgres`)
- `JWT_SECRET` (default definido em `application.yml`)
- `APP_PORT` (use `server.port` se quiser alterar)

## Usuários seed
Criados na inicialização (CommandLineRunner):
- admin / admin123 (ADMIN)
- operador / operador123 (OPERADOR)
- logistica / log123 (LOGISTICA)
- ong / ong123 (ONG)

## Endpoints principais (prefixo `/api/v1`)
- Auth: `/auth/login`, `/auth/refresh`
- Produção: `/produtos`, `/unidades-produtivas`, `/lotes`
- Estoque: `/estoques`, `/estoques/movimentacoes`, `/estoques/reservas`
- Doações: `/beneficiarios`, `/campanhas`, `/pedidos-doacao`, `/entregas`, `/rotas`
- Energia: `/energia/biodigestor/entradas`, `/energia/biogas`, `/energia/geracao`, `/energia/consumo`, `/energia/simulacao`
- Dashboards: `/dashboard/*`
- Observabilidade: `/actuator/health`, logs estruturados JSON

## Testes
```bash
cd backend
mvn test
```

## Notas
- Flyway está habilitado (baseline em `V1__baseline.sql`) e JPA gera/atualiza as tabelas automaticamente.
- Seeds iniciais de produtos, unidades e beneficiários são carregados no `DataSeeder`.
- Frontend usa axios com interceptador JWT; configure `VITE_API_URL` se o backend não estiver em `localhost:8080/api/v1`.
