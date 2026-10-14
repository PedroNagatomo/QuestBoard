package com.questboard.domain.enums;

public enum Difficulty {
    TRIVIAL(5, 2),
    EASY(10, 5),
    MEDIUM(25, 12),
    HARD(50, 25),
    EPIC(100, 50),
    LEGENDARY(250, 120);

    private final int baseXp;
    private final int baseCoins;

    Difficulty(int baseXp, int baseCoins) {
        this.baseXp = baseXp;
        this.baseCoins = baseCoins;
    }
    public int getBaseXp() { return baseXp; }
    public int getBaseCoins() { return baseCoins; }
}