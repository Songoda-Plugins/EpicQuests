package com.songoda.epicquests.story.quest.reward.rewards;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.gui.Gui;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.gui.GuiCommands;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.epicquests.story.quest.reward.AbstractReward;
import com.songoda.epicquests.story.quest.reward.RewardType;
import com.songoda.third_party.org.jooq.DSLContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class CommandReward extends AbstractReward {

    private final EpicQuests plugin;
    private final List<String> commands = new ArrayList<>();

    public CommandReward(Quest quest) {
        super(quest);
        this.plugin = JavaPlugin.getPlugin(EpicQuests.class);
    }

    @Override
    public RewardType getType() {
        return RewardType.COMMAND;
    }

    @Override
    public void setup(Player player, Gui back,Runnable callback, Runnable onDelete) {
        this.plugin.getGuiManager().showGUI(player, new GuiCommands(this.plugin, player, this, back, callback, onDelete));
    }

    @Override
    public void give(Player player) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            for (String command : this.commands) {
                Bukkit.getServer().dispatchCommand(
                        Bukkit.getConsoleSender(),
                        command
                                .replace("@p", player.getName())
                                .replace("%player%", player.getName())
                );
            }
        });
    }

    public List<String> getCommands() {
        return Collections.unmodifiableList(this.commands);
    }

    public void addCommand(String command) {
        this.commands.add(command);
    }

    public void removeCommand(String command) {
        this.commands.remove(command);
    }

    @Override
    public void saveImpl(DSLContext ctx, String... columns) {
        String encodedCommands = Base64.getEncoder().encodeToString(String.join("\n", this.commands).getBytes());

        SQLInsert.create(ctx).insertInto("command_reward")
                .withField("id", id, id == -1)
                .withField("commands", encodedCommands)
                .withField("quest", this.getQuest().getId())
                .onDuplicateKeyUpdate(columns)
                .execute();

        if (id == -1)
            this.id = lastInsertedId("command_reward", ctx);
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("command_reward", "id", id);
    }
}
