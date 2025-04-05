package com.songoda.epicquests.story;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.data.SavesData;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.third_party.org.jooq.DSLContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Story implements SavesData {
    private int id = -1;
    private String name = "Unnamed Story";
    private final List<Quest> quests = new LinkedList<>();

    private boolean active = false;

    public void setId(int id) {
        this.id = id;
    }

    public Quest addQuest(Quest quest) {
        this.quests.add(quest);
        return quest;
    }

    public void removeQuest(Quest quest) {
        this.quests.remove(quest);
    }

    public List<Quest> getQuests() {
        return Collections.unmodifiableList(this.quests);
    }

    public List<Quest> getEnabledQuests() {
        return this.quests.stream().filter(Quest::isActive).collect(Collectors.toList());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public void saveImpl(DSLContext ctx, String... columns) {
        SQLInsert.create(ctx).insertInto("story")
                .withField("id", id, id == -1)
                .withField("name", name)
                .withField("active", active)
                .onDuplicateKeyUpdate(columns)
                .execute();

        if (id == -1)
            this.id = lastInsertedId("story", ctx);
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("story", "id", id);
        for (Quest quest : quests)
            quest.delete();
    }
}
