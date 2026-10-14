package com.questboard.service.event;

import com.questboard.integration.slack.SlackNotifier;
import com.questboard.service.BossFightService;
import com.questboard.service.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlackBridgeListener {

    private final SlackNotifier slack;

    @Async
    @EventListener
    public void onBossDefeated(BossFightService.BossDefeatedEvent ev) {
        slack.notifyBossDefeated(ev.bossName());
    }

    @Async
    @EventListener
    public void onXpGranted(XpService.XpGrantedEvent ev) {
        if (ev.levelsGained() > 0) {
            slack.notifyLevelUp(ev.username(), ev.newLevel());
        }
    }
}