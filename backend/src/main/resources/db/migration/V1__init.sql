-- =========================================
-- QuestBoard — Schema inicial
-- =========================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ---------- USERS ----------
CREATE TABLE users (
                       id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username        VARCHAR(50)  NOT NULL UNIQUE,
                       email           VARCHAR(255) NOT NULL UNIQUE,
                       password_hash   VARCHAR(255) NOT NULL,
                       display_name    VARCHAR(100),
                       avatar_url      VARCHAR(500),
                       player_class    VARCHAR(30)  NOT NULL DEFAULT 'DEV',  -- DEV, DESIGNER, PM, QA, DEVOPS, MARKETING, OTHER
                       level           INT          NOT NULL DEFAULT 1,
                       xp              BIGINT       NOT NULL DEFAULT 0,
                       total_xp        BIGINT       NOT NULL DEFAULT 0,
                       coins           BIGINT       NOT NULL DEFAULT 0,
                       current_streak  INT          NOT NULL DEFAULT 0,
                       longest_streak  INT          NOT NULL DEFAULT 0,
                       last_active_date DATE,
                       guild_id        UUID,
                       enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       version         BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_users_guild ON users(guild_id);
CREATE INDEX idx_users_level ON users(level DESC);

-- ---------- GUILDS ----------
CREATE TABLE guilds (
                        id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        name         VARCHAR(100) NOT NULL UNIQUE,
                        description  TEXT,
                        emblem_url   VARCHAR(500),
                        total_xp     BIGINT       NOT NULL DEFAULT 0,
                        level        INT          NOT NULL DEFAULT 1,
                        owner_id     UUID         NOT NULL,
                        created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                        updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_guild
        FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE SET NULL;

-- ---------- QUESTS ----------
CREATE TABLE quests (
                        id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        title           VARCHAR(255) NOT NULL,
                        description     TEXT,
                        difficulty      VARCHAR(20)  NOT NULL DEFAULT 'EASY',  -- TRIVIAL, EASY, MEDIUM, HARD, EPIC, LEGENDARY
                        status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',  -- OPEN, IN_PROGRESS, IN_REVIEW, COMPLETED, FAILED, CANCELLED
                        xp_reward       INT          NOT NULL DEFAULT 10,
                        coin_reward     INT          NOT NULL DEFAULT 5,
                        due_date        TIMESTAMPTZ,
                        completed_at    TIMESTAMPTZ,
                        assignee_id     UUID,
                        created_by_id   UUID         NOT NULL,
                        guild_id        UUID,
                        parent_boss_id  UUID,
                        external_source VARCHAR(30),   -- JIRA, TRELLO, GITHUB, MANUAL
                        external_id     VARCHAR(255),
                        external_url    VARCHAR(500),
                        tags            TEXT[],
                        created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                        updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                        version         BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_quests_assignee ON quests(assignee_id);
CREATE INDEX idx_quests_status ON quests(status);
CREATE INDEX idx_quests_guild ON quests(guild_id);
CREATE INDEX idx_quests_boss ON quests(parent_boss_id);
CREATE UNIQUE INDEX uq_quests_external ON quests(external_source, external_id)
    WHERE external_source IS NOT NULL AND external_id IS NOT NULL;

-- ---------- BADGES ----------
CREATE TABLE badges (
                        id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        code         VARCHAR(50)  NOT NULL UNIQUE,
                        name         VARCHAR(100) NOT NULL,
                        description  TEXT,
                        icon_url     VARCHAR(500),
                        rarity       VARCHAR(20)  NOT NULL DEFAULT 'COMMON', -- COMMON, RARE, EPIC, LEGENDARY
                        xp_bonus     INT          NOT NULL DEFAULT 0
);

CREATE TABLE user_badges (
                             id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             user_id     UUID NOT NULL,
                             badge_id    UUID NOT NULL,
                             awarded_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             UNIQUE (user_id, badge_id),
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE
);

-- ---------- ACHIEVEMENTS ----------
CREATE TABLE achievements (
                              id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              code         VARCHAR(50)  NOT NULL UNIQUE,
                              name         VARCHAR(100) NOT NULL,
                              description  TEXT,
                              target_value BIGINT       NOT NULL DEFAULT 1,
                              metric       VARCHAR(50)  NOT NULL,  -- QUESTS_COMPLETED, XP_EARNED, STREAK_DAYS, BOSSES_DEFEATED
                              reward_xp    INT          NOT NULL DEFAULT 0,
                              reward_coins INT          NOT NULL DEFAULT 0
);

CREATE TABLE user_achievements (
                                   id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   user_id         UUID NOT NULL,
                                   achievement_id  UUID NOT NULL,
                                   progress        BIGINT NOT NULL DEFAULT 0,
                                   completed       BOOLEAN NOT NULL DEFAULT FALSE,
                                   completed_at    TIMESTAMPTZ,
                                   UNIQUE (user_id, achievement_id),
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                   FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE
);

-- ---------- BOSS FIGHTS ----------
CREATE TABLE boss_fights (
                             id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             name          VARCHAR(150) NOT NULL,
                             description   TEXT,
                             image_url     VARCHAR(500),
                             total_hp      BIGINT       NOT NULL,
                             current_hp    BIGINT       NOT NULL,
                             status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DEFEATED, EXPIRED
                             starts_at     TIMESTAMPTZ,
                             ends_at       TIMESTAMPTZ,
                             guild_id      UUID,
                             created_by_id UUID         NOT NULL,
                             created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                             updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_boss_status ON boss_fights(status);

-- ---------- LOOT / MARKETPLACE ----------
CREATE TABLE loot_items (
                            id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            name         VARCHAR(150) NOT NULL,
                            description  TEXT,
                            image_url    VARCHAR(500),
                            cost_coins   INT          NOT NULL,
                            stock        INT,                -- NULL = ilimitado
                            rarity       VARCHAR(20)  NOT NULL DEFAULT 'COMMON',
                            active       BOOLEAN      NOT NULL DEFAULT TRUE,
                            created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE loot_redemptions (
                                  id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  user_id      UUID NOT NULL,
                                  loot_id      UUID NOT NULL,
                                  cost_paid    INT  NOT NULL,
                                  status       VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, DELIVERED, REJECTED
                                  redeemed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  FOREIGN KEY (loot_id) REFERENCES loot_items(id)
);

-- ---------- XP LOG (auditoria anti-cheat) ----------
CREATE TABLE xp_events (
                           id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           user_id      UUID NOT NULL,
                           source_type  VARCHAR(30) NOT NULL,  -- QUEST, BADGE, ACHIEVEMENT, BOSS, STREAK_BONUS, MANUAL
                           source_id    UUID,
                           xp_delta     INT NOT NULL,
                           coin_delta   INT NOT NULL DEFAULT 0,
                           reason       VARCHAR(255),
                           flagged      BOOLEAN NOT NULL DEFAULT FALSE,
                           created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_xp_events_user_date ON xp_events(user_id, created_at DESC);

-- ---------- INTEGRATIONS ----------
CREATE TABLE integrations (
                              id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              provider      VARCHAR(30) NOT NULL,   -- GITHUB, JIRA, TRELLO, SLACK
                              guild_id      UUID,
                              config_json   JSONB NOT NULL DEFAULT '{}'::jsonb,
                              active        BOOLEAN NOT NULL DEFAULT TRUE,
                              created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              UNIQUE (provider, guild_id)
);

-- ---------- SEED ----------
INSERT INTO badges (code, name, description, rarity, xp_bonus) VALUES
                                                                   ('FIRST_BLOOD',   'First Blood',      'Conclua sua primeira quest',                'COMMON',    5),
                                                                   ('STREAK_7',      'Semana Perfeita',  'Mantenha 7 dias seguidos de streak',        'RARE',      50),
                                                                   ('STREAK_30',     'Mês Lendário',     'Mantenha 30 dias seguidos de streak',       'LEGENDARY', 500),
                                                                   ('QUEST_10',      'Veterano',         'Conclua 10 quests',                         'COMMON',    20),
                                                                   ('QUEST_100',     'Mestre das Quests','Conclua 100 quests',                        'EPIC',      300),
                                                                   ('BOSS_SLAYER',   'Matador de Boss',  'Derrote seu primeiro boss',                 'RARE',      100);

INSERT INTO achievements (code, name, description, target_value, metric, reward_xp, reward_coins) VALUES
                                                                                                      ('QUESTS_50',   'Meio Caminho',     'Complete 50 quests',      50,  'QUESTS_COMPLETED', 500, 100),
                                                                                                      ('XP_10K',      'Colecionador XP',  'Acumule 10.000 XP totais',10000,'XP_EARNED',        1000, 250),
                                                                                                      ('BOSS_5',      'Caçador de Bosses','Derrote 5 bosses',         5,  'BOSSES_DEFEATED',  1500, 500);

INSERT INTO loot_items (name, description, image_url, cost_coins, rarity) VALUES
                                                                              ('Café Premium',  'Um café especial por conta da empresa', NULL, 50,   'COMMON'),
                                                                              ('Meio dia off',  'Saia mais cedo na sexta',               NULL, 300,  'RARE'),
                                                                              ('Home Office',   'Um dia de home office extra',           NULL, 500,  'RARE'),
                                                                              ('Dia de Folga',  'Uma folga remunerada',                  NULL, 2000, 'LEGENDARY');