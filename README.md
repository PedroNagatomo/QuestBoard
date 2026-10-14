# QuestBoard 🎮

Gamificação para equipes de trabalho. Transforme tarefas em **quests** com XP, níveis, badges, guildas, boss fights e recompensas reais.

<img width="1915" height="906" alt="image" src="https://github.com/user-attachments/assets/e69408c9-1628-4bb7-b539-80b68d4b41a1" />

## ✨ Funcionalidades

- 🗡️ **Quests** — tarefas com dificuldade, XP, prazo
- 🧙 **Perfis de jogador** — nível, classe (Dev/Designer/PM/QA/DevOps)
- ⚡ **XP, níveis, badges, achievements**
- 🏰 **Guildas** — times com progresso compartilhado
- 🐉 **Boss fights** — projetos grandes com HP
- 🎁 **Marketplace de loot** — cafés, home office, folgas
- 🔥 **Streaks** — dias consecutivos de produtividade
- 🏆 **Rankings** — semana / mês / all-time em tempo real
- 🔌 **Integrações** — GitHub, Jira, Trello, Slack
- 🛡️ **Anti-cheat** — rate limit, cap diário, tempo mínimo

## 🛠️ Stack

| Camada   | Tecnologia                                             |
|----------|--------------------------------------------------------|
| Backend  | Spring Boot 3.5, Java 17, PostgreSQL 16, Redis 7       |
| Frontend | React 18, TypeScript, Vite, TailwindCSS, Framer Motion |
| Auth     | JWT (access + refresh), BCrypt                         |
| Realtime | STOMP sobre WebSocket + Redis Pub/Sub                  |
| Infra    | Docker, Docker Compose, GitHub Actions                 |


## 🚀 Rodando com Docker

Pré-requisito: **Docker Desktop** instalado e rodando.

```bash
cp .env.example .env
# Edite .env e troque JWT_SECRET por algo longo (32+ chars)

docker compose up --build -d

# Aguarde ~1min e verifique
docker compose ps
```
