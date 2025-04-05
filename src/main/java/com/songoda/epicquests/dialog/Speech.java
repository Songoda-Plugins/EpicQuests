package com.songoda.epicquests.dialog;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.data.SavesData;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.third_party.org.jooq.DSLContext;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Speech implements SavesData {
    private int id = -1;

    private transient Dialog dialog;

    private final List<Integer> questPrerequisites = new LinkedList<>();
    private boolean defaultDialog = false;

    private final List<String> messages = new LinkedList<>();

    public Speech(Dialog dialog) {
        this.dialog = dialog;
    }

    public void sendMessages(Player player, NPC npc) {
        EpicQuests plugin = EpicQuests.getInstance();
        int timeout = 0;
        for (String message : getMessages()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String msg = message
                        .replace("@p", player.getName())
                        .replace("%player%", player.getName());
                if (player.isOnline()) {
                    plugin.getLocale().newMessage("[NPC] " + npc.getName() + ": " + msg).sendMessage(player);
                }
            }, timeout);
            timeout += 20;
        }
    }

    public Dialog getDialog() {
        return this.dialog;
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(this.messages);
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public void removeMessage(String message) {
        this.messages.remove(message);
    }

    public void moveMessageToEnd(String message) {
        this.messages.remove(message);
        this.messages.add(message);
    }

    public boolean isDefaultDialog() {
        return this.defaultDialog;
    }

    public void setDefaultDialog(boolean defaultDialog) {
        this.defaultDialog = defaultDialog;
    }

    public void addQuestPrerequisite(Quest quest) {
        addQuestPrerequisite(quest.getId());
    }

    public void addQuestPrerequisite(int quest) {
        this.questPrerequisites.add(quest);
    }

    public void removeQuestPrerequisite(Quest quest) {
        removeQuestPrerequisite(quest.getId());
    }

    public void removeQuestPrerequisite(int quest) {
        this.questPrerequisites.remove(quest);
    }

    public void clearQuestPrerequisites() {
        this.questPrerequisites.clear();
    }

    public List<Integer> getQuestPrerequisites() {
        return Collections.unmodifiableList(this.questPrerequisites);
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
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
                .insertInto("speech")
                .withField("id", id, id == -1)
                .withField("dialog_id", dialog.getId())
                .withField("default_dialog", defaultDialog)
                .withField("messages", messages.isEmpty() ? null : messages.stream().reduce((a, b) -> a + "\n" + b).orElse(""))
                .withField("quest_prerequisites", questPrerequisites.isEmpty() ? null : questPrerequisites.stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElse(""))
                .onDuplicateKeyUpdate(columns)
                .execute();

        if (this.id == -1)
            this.id = lastInsertedId("speech", ctx);
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("speech", "id", id);
    }
}
