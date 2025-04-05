package com.songoda.epicquests.story.quest;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.data.SavesData;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import com.songoda.third_party.org.jooq.DSLContext;

import java.util.*;

public class ActiveQuest implements SavesData {
    private final UUID playerId;
    private final int activeQuest;

    private int currentPosition = 0;

    private final Map<Integer, Integer> actions = new HashMap<>();

    private boolean focused;

    public ActiveQuest(UUID playerId, Quest activeQuest) {
        this.playerId = playerId;
        this.activeQuest = activeQuest.getId();
    }

    public int getActiveQuest() {
        return this.activeQuest;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void removeAction(ActiveAction action) {
        this.actions.remove(action.getId());
    }

    public boolean completeAction(ActiveAction action, int amount) {
        int currentAmount = actions.computeIfAbsent(action.getId(), k -> 0);
        if (currentAmount >= action.getAmount())
            return false;

        int newAmount = this.actions.get(action.getId()) + amount;
        this.actions.put(action.getId(), newAmount);
        return newAmount >= action.getAmount();
    }

    public boolean isFocused() {
        return this.focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setCurrentPosition(int positionId) {
        currentPosition = positionId;
    }

    public void setActions(Map<Integer, Integer> actions) {
        this.actions.clear();
        this.actions.putAll(actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.activeQuest);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Quest) {
            return ((Quest) obj).getId() == this.activeQuest;
        } else if (obj instanceof ActiveQuest) {
            return ((ActiveQuest) obj).activeQuest == this.activeQuest;
        }
        return false;
    }

    @Override
    public void saveImpl(DSLContext ctx, String... columns) {
        SQLInsert.create(ctx)
                .insertInto("active_quest")
                .withField("player_id", this.playerId)
                .withField("active_quest", this.activeQuest)
                .withField("current_position", this.currentPosition)
                .withField("focused", this.focused)
                .withField("actions", this.actions.isEmpty() ? null : this.actions.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).reduce((a, b) -> a + "," + b).orElse(""))
                .onDuplicateKeyUpdate()
                .execute();
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("active_quest", "player_id", this.playerId,
                "active_quest", this.activeQuest);
    }

    public int getActions() {
        return this.actions.size();
    }

    public int getAmount(ActiveAction activeAction) {
        return this.actions.getOrDefault(activeAction.getId(), 0);
    }

    public int getTotalAmount() {
        return this.actions.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void clearActions() {
        this.actions.clear();
    }
}
