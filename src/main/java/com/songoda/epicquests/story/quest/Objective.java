package com.songoda.epicquests.story.quest;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.data.SavesData;
import com.songoda.epicquests.dialog.AttachedSpeech;
import com.songoda.epicquests.story.quest.requirement.AbstractRequirement;
import com.songoda.third_party.org.jooq.DSLContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Objective implements AttachedSpeech, SavesData {
    private Quest quest;
    private int startPosition = 0;
    private int endPosition = 0;

    private int id = -1;

    private String title = "An Objective";
    private final List<AbstractRequirement> requirements = new LinkedList<>();

    private boolean visible = true;

    private int attachedSpeech;

    public Objective(Quest quest) {
        this.quest = quest;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Quest getQuest() {
        return this.quest;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public List<AbstractRequirement> getRequirements() {
        return Collections.unmodifiableList(this.requirements);
    }

    public void addRequirement(AbstractRequirement requirement) {
        this.requirements.add(requirement);
    }

    public void removeRequirement(AbstractRequirement requirement) {
        this.requirements.remove(requirement);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getStartPosition() {
        return this.startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getEndPosition() {
        return this.endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    @Override
    public int getAttachedSpeech() {
        return this.attachedSpeech;
    }

    @Override
    public void setAttachedSpeech(int rejection) {
        this.attachedSpeech = rejection;
    }

    @Override
    public void saveImpl(DSLContext ctx, String... columns) {
        SQLInsert.create(ctx).insertInto("objective")
                .withField("id", id, id == -1)
                .withField("title", title)
                .withField("quest_id", quest.getId())
                .withField("visible", visible)
                .withField("attached_speech", attachedSpeech)
                .withField("start_position", startPosition)
                .withField("end_position", endPosition)
                .onDuplicateKeyUpdate(columns)
                .execute();

        if (id == -1)
            this.id = lastInsertedId("objective", ctx);
    }

    @Override
    public void deleteImpl(DSLContext dslContext) {
        SQLDelete.create(dslContext).delete("objective", "id", id);
        for (AbstractRequirement requirement : requirements)
            requirement.delete();
    }

    public boolean isObjectiveBefore(int position) {
        return this.startPosition < position;
    }
}
