package com.songoda.epicquests.dialog;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.data.SavesData;
import com.songoda.epicquests.story.player.StoryPlayer;
import com.songoda.third_party.org.jooq.DSLContext;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Dialog implements SavesData {
    private int id = -1;
    private int citizenId;
    private final List<Speech> messages = new LinkedList<>();

    public Dialog(int citizenId) {
        this.citizenId = citizenId;
    }

    public void sendMessages(Player player, StoryPlayer contender) {
        Speech speech = null;
        List<Speech> speeches = new ArrayList<>(this.messages);
        Collections.reverse(speeches);
        for (Speech s : speeches) {
            if (s.isDefaultDialog()
                    || !s.getQuestPrerequisites().isEmpty()
                    && contender.getCompletedQuests().containsAll(s.getQuestPrerequisites())) {
                speech = s;
                break;
            }
        }
        if (speech == null) {
            System.out.println("No speech found for " + player.getName());
            return;
        }
        System.out.println("Sending speech to " + player.getName());
        speech.sendMessages(player, getCitizen());
    }

    public int getCitizenId() {
        return this.citizenId;
    }

    public NPC getCitizen() {
        return CitizensAPI.getNPCRegistry().getById(this.citizenId);
    }

    public void setCitizenId(int citizenId) {
        this.citizenId = citizenId;
    }
    public List<Speech> getMessages() {
        return Collections.unmodifiableList(this.messages);
    }

    public void addMessage(Speech messages) {
        this.messages.add(messages);
    }

    public void removeSpeech(Speech messages) {
        this.messages.remove(messages);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public void saveImpl(DSLContext ctx, String... columns) {
        SQLInsert.create(ctx)
                .insertInto("dialog")
                .withField("id", id, id == -1)
                .withField("citizen_id", citizenId)
                .onDuplicateKeyUpdate(columns)
                .execute();

        if (this.id == -1)
            this.id = lastInsertedId("dialog", ctx);
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("dialog", "id", id);
        for (Speech speech : messages)
            speech.delete();
    }
}
