package com.questboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Gerencia a curva de XP e o cálculo de nível.
 *
 * XP necessário para sair do nível N para N+1:
 *     xpToNext(N) = base * N^expoente   (arredondado)
 *
 * O XP "acumulado" (u.xp) representa progresso DENTRO do nível atual.
 * O XP "total"    (u.totalXp) é a soma de tudo já ganho.
 */
@Service
public class LevelService {

    @Value("${app.xp.level-base:100}")
    private long base;

    @Value("${app.xp.level-exponent:1.5}")
    private double exponent;

    @Value("${app.xp.max-level:100}")
    private int maxLevel;

    /** XP necessário para completar o nível atual e ir para o próximo. */
    public long xpToNextLevel(int currentLevel) {
        if (currentLevel >= maxLevel) return Long.MAX_VALUE;
        return Math.round(base * Math.pow(currentLevel, exponent));
    }

    /**
     * Aplica `xpGained` ao usuário.
     * @return novo nível, XP restante dentro do novo nível e quantos níveis subiu.
     */
    public LevelUpResult applyXp(int currentLevel, long currentXp, long xpGained) {
        int lvl = currentLevel;
        long xp = currentXp + xpGained;
        int levelsGained = 0;

        while (lvl < maxLevel) {
            long needed = xpToNextLevel(lvl);
            if (xp < needed) break;
            xp -= needed;
            lvl++;
            levelsGained++;
        }
        // Se bateu no teto, congela xp
        if (lvl >= maxLevel) {
            xp = Math.min(xp, xpToNextLevel(maxLevel));
        }
        return new LevelUpResult(lvl, xp, levelsGained);
    }

    public int getMaxLevel() { return maxLevel; }

    public record LevelUpResult(int newLevel, long remainingXp, int levelsGained) {
        public boolean leveledUp() { return levelsGained > 0; }
    }
}