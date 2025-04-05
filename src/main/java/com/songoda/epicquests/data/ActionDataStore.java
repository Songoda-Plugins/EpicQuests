package com.songoda.epicquests.data;

import com.songoda.core.data.SavesData;
import com.songoda.core.input.ChatPrompt;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.gui.GuiObjective;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class ActionDataStore implements SavesData {

    protected int id = -1;

    protected final Objective objective;
    protected int amount = 0;
    private UUID setter;

    public ActionDataStore(Objective objective) {
        this.objective = objective;
    }

    public Objective getObjective() {
        return this.objective;
    }

    public void startSetup(Entity setter) {
        startSetup(setter.getUniqueId());
    }

    public void startSetup(UUID setter) {
        this.setter = setter;
    }

    public boolean isBeingSetup(Entity entity) {
        return isBeingSetup(entity.getUniqueId());
    }

    public boolean isBeingSetup(UUID setter) {
        return this.setter != null && this.setter.equals(setter);
    }

    public void finishSetup(EpicQuests plugin, Player player, ActiveAction activeAction) {
        this.setter = null;

        if (activeAction.getAction().isSingleAmount()) {
            activeAction.setAmount(1);
            plugin.getGuiManager().showGUI(player, new GuiObjective(plugin, player, getObjective()));
            save();
        } else {
            ChatPrompt.showPrompt(plugin, player,
                    "Enter a required amount.",
                    response -> {
                        activeAction.setAmount(Integer.parseInt(response.getMessage()));
                        plugin.getGuiManager().showGUI(player, new GuiObjective(plugin, player, getObjective()));
                        save();
                    });
        }
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
