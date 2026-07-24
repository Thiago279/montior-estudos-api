# Roadmap

## ✅ v0.1.0 — MVP Core & CRUDs
- CRUD de Matérias
- CRUD de Sessões de Estudo (suporte a sessões em andamento)
- Estatísticas Diárias e Semanais
- Tratamento global de exceções inicial
- Testes unitários iniciais (Services e Exceptions)

---

## ✅ v0.2.0 — OpenAPI & API Documentation
- Configuração completa do SpringDoc / Swagger UI
- Documentação interativa dos endpoints e schemas de DTOs
- Mapeamento detalhado dos códigos de resposta HTTP

---

## ✅ v0.3.0 — Database Migrations & Validation
- Integração do Flyway para controle de versão do banco
- Scripts de migration `V1` e `V2` para criação de tabelas
- Validação estrita do Hibernate schema (`ddl-auto=validate`)
- Validação de conflito de horários e regra de sessão única em andamento
- Endpoint para finalização (PATCH/PUT) de sessão

---

## 🔜 v0.4.0 — Estatísticas Avançadas & Refatoração DTOs

### Estatísticas
- [ ] Endpoint de estatísticas por período customizado:
  `GET /estatisticas/periodo?inicio=YYYY-MM-DD&fim=YYYY-MM-DD`
  *(Substitui a necessidade de endpoints fixos por mês ou ano)*

### Qualidade de Código & DTOs
- [ ] Padronização completa dos DTOs de Request e Response
- [ ] Refatoração e refinamento do `GlobalExceptionHandler`
- [ ] Auditoria e revisão das respostas HTTP dos Controllers

---

## 🔜 v0.5.0 — Testes & Qualidade (Coverage)

### Testes de Software
- [ ] Testes unitários completos da camada de Service com Mockito
- [ ] Testes de integração/Slice das Controllers (`@WebMvcTest` ou `MockMvc`)
- [ ] Cobertura completa dos cenários de exceção e regras de negócio
- [ ] Configuração de relatório de cobertura de testes (JaCoCo)

---

## 🔜 v0.6.0 — Multi-Perfis & Ambientes

### Configuração de Ambientes
- [ ] `application-dev.properties` (desenvolvimento local com Docker)
- [ ] `application-test.properties` (banco H2 em memória para testes velozes)
- [ ] `application-prod.properties` (configurações preparadas para produção)

---

## 🔜 v0.7.0 — CI/CD & Automação

### GitHub Actions
- [ ] Workflow de Integracao Contínua (CI) com GitHub Actions
- [ ] Build automatizado do Maven no `push` / `pull_request`
- [ ] Execução automática do suite de testes
- [ ] Adição do Badge de status do Build no `README.md`

---

## 🔜 v0.8.0 — Frontend Angular

### Dashboard & UI
- [ ] Interface gráfica em Angular para consumir a API
- [ ] Gerenciamento visual de Matérias e Cronômetro de Sessões
- [ ] Gráficos e dashboards para visualização de estatísticas por período