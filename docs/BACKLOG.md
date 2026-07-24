# Backlog (Future Features & Ideas)

Recursos e melhorias mapeadas para evolução futura do projeto, divididas por categoria de domínio.

---

##  Regras de Negócio & Analytics (Backend)

- [ ] **Metas de Estudo (Study Goals):**
  - Definição de metas de horas semanais/mensais por matéria.
  - Endpoint de progresso (`GET /metas/progresso`) comparando horas planejadas vs. realizadas.
- [ ] **Ranking & Métricas por Matéria:**
  - Endpoint que retorna o TOP 5 matérias mais estudadas no mês/ano.
  - Cálculo de média diária e consistência (dias seguidos de estudo / *streaks*).
- [ ] **Exportação de Relatórios:**
  - Exportação de estatísticas de estudo nos formatos **CSV** e **PDF**.
- [ ] **Filtros Avançados de Busca:**
  - Busca de sessões por palavra-chave na descrição, intervalo de datas ou por cor da matéria.



##  Funcionalidades de Interface (UI/UX - Angular)

- [ ] **Cronômetro / Timer em Tempo Real:**
  - Modo *Pomodoro* ou cronômetro progressivo direto na interface, disparando os eventos de `POST` (início) e `PATCH` (fim) da sessão.
- [ ] **Dashboard Gráfico:**
  - Gráficos de barras/rosca (usando Chart.js ou Ngx-Charts) para distribuição de tempo por matéria e progresso semanal.
- [ ] **Suporte a Temas (Dark/Light Mode):**
  - Alternância de tema escuro/claro na aplicação web.

---

##  DevOps & Ferramentas de Processo

- [ ] **Containerização Completa:**
  - Arquivo `Dockerfile` para a aplicação Spring Boot, permitindo subir App + Banco via `docker-compose up`.
- [ ] **Gestão do Projeto no GitHub:**
  - Organização das tarefas utilizando **GitHub Projects** (Kanban de Sprints com colunas *To Do*, *In Progress*, *Done*).

  ---

## Autenticação & Multi-Usuário (Security)

- [ ] **Autenticação & Autorização:**
  - Integração com **Spring Security** e **JWT (JSON Web Tokens)**.
  - Cadastro e Login de usuários (`/auth/register`, `/auth/login`).
  - Isolamento de dados por usuário (cada estudante visualiza apenas suas próprias matérias e estatísticas).

---