package com.songoda.epicquests.story.quest.requirement;

import com.songoda.core.data.SavesData;
import com.songoda.core.gui.Gui;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.dialog.AttachedSpeech;
import com.songoda.epicquests.dialog.Speech;
import com.songoda.epicquests.story.quest.Objective;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractRequirement implements AttachedSpeech, SavesData {

    protected int id = -1;

    private final transient EpicQuests plugin;
    protected transient Objective objective;
    protected int reject = -1;

    public AbstractRequirement(Objective objective) {
        this.plugin = JavaPlugin.getPlugin(EpicQuests.class);

        this.objective = objective;
    }

    public Objective getObjective() {
        return this.objective;
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    public void reject(Player player) {
        Speech speech = this.plugin.getDialogManager().getSpeech(this.reject);
        if (speech == null) {
            this.plugin.getLocale().getMessage("general.requirements.not_met").sendMessage(player);
        } else {
            speech.sendMessages(player, speech.getDialog().getCitizen());
        }
    }

    public int getAttachedSpeech() {
        return this.reject;
    }

    public void setAttachedSpeech(int rejection) {
        this.reject = rejection;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReject(int reject) {
        this.reject = reject;
    }

    public abstract RequirementType getType();

    public abstract boolean isMet(Player player);

    public abstract void execute(Player player);

    public abstract void setup(Player player, Gui back, Runnable callback, Runnable onDelete);

    public abstract String getDescription();
}
