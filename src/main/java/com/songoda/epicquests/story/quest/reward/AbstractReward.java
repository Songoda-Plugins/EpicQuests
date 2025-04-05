package com.songoda.epicquests.story.quest.reward;

import com.songoda.core.data.SavesData;
import com.songoda.core.gui.Gui;
import com.songoda.epicquests.story.quest.Quest;
import org.bukkit.entity.Player;

public abstract class AbstractReward implements SavesData {

    protected int id = -1;

    private Quest quest;

    public AbstractReward(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return this.quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public abstract RewardType getType();

    public abstract void setup(Player player, Gui back, Runnable callback, Runnable onDelete);

    public abstract void give(Player player);

    public void setId(int id) {
        this.id = id;
    }
}
