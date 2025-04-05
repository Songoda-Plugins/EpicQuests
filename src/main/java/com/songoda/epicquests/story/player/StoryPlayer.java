package com.songoda.epicquests.story.player;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.data.SavesData;
import com.songoda.epicquests.story.quest.ActiveQuest;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.third_party.org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StoryPlayer implements SavesData {
    private UUID uniqueId;
    private boolean inDialogCreation = false;
    private boolean silent;

    protected final List<ActiveQuest> activeQuests = new ArrayList<>();
    protected final Set<Integer> completedQuests = new HashSet<>();

    public StoryPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public ActiveQuest addActiveQuest(Quest quest) {
        ActiveQuest activeQuest = new ActiveQuest(uniqueId, quest);
        this.activeQuests.add(activeQuest);
        return activeQuest;
    }

    public ActiveQuest addActiveQuest(ActiveQuest quest) {
        this.activeQuests.add(quest);
        return quest;
    }

    public void removeActiveQuest(ActiveQuest quest) {
        this.activeQuests.remove(quest);
    }

    public int addCompletedQuest(int quest) {
        this.completedQuests.add(quest);
        return quest;
    }

    public List<ActiveQuest> getActiveQuests() {
        return Collections.unmodifiableList(this.activeQuests);
    }

    public Set<Integer> getCompletedQuests() {
        return Collections.unmodifiableSet(this.completedQuests);
    }

    public boolean hasCompletedQuest(UUID uniqueId) {
        return this.completedQuests.contains(uniqueId);
    }

    public ActiveQuest getActiveQuest(Quest quest) {
        for (ActiveQuest q : this.activeQuests)
            if (q.getActiveQuest() == quest.getId())
                return q;
        return null;
    }

    public ActiveQuest getNextQuest() {
        for (ActiveQuest quest : this.activeQuests) {
            return quest;
        }
        return null;
    }

    public void completeQuest(Quest quest) {
        ActiveQuest active = getActiveQuest(quest);
        this.completedQuests.add(active.getActiveQuest());
        this.activeQuests.remove(active);
    }

    protected boolean addCompletedQuests(Set<Integer> completedQuests) {
        return this.completedQuests.addAll(completedQuests);
    }

    private boolean isActiveQuest(Quest quest) {
        for (ActiveQuest activeQuest : this.activeQuests)
            if (activeQuest.getActiveQuest() == quest.getId())
                return true;

        return false;
    }

    public boolean isFocused(Quest quest) {
        ActiveQuest activeQuest = getActiveQuest(quest);
        return activeQuest != null && activeQuest.isFocused();
    }

    public void toggleAllFocusedOff() {
        for (ActiveQuest quest : this.activeQuests) {
            quest.setFocused(false);
        }
    }

    public void toggleFocus(Quest quest) {
        ActiveQuest activeQuest = getActiveQuest(quest);
        activeQuest.setFocused(!activeQuest.isFocused());
    }

    public void reset() {
        this.activeQuests.clear();
        this.completedQuests.clear();
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public boolean isSilent() {
        return this.silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setInDialogCreation(boolean inCreation) {
        this.inDialogCreation = inCreation;
    }

    public boolean isInDialogCreation() {
        return this.inDialogCreation;
    }

    @Override
    public String toString() {
        return "StoryContender{" +
                "uniqueId=" + this.uniqueId +
                ", activeQuests=" + this.activeQuests +
                ", completedQuests=" + this.completedQuests +
                '}';
    }

    @Override
    public void saveImpl(DSLContext ctx, String... columns) {
        SQLInsert.create(ctx)
                .insertInto("player")
                .withField("id", uniqueId.toString())
                .withField("completed_quests", completedQuests.stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElse(""))
                .onDuplicateKeyUpdate(columns)
                .execute();
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("player", "id", uniqueId.toString());
    }

}
