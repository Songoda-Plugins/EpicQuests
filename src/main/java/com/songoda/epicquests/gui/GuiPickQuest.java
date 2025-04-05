package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.dialog.Speech;
import com.songoda.epicquests.story.StoryManager;
import com.songoda.epicquests.story.quest.Quest;
import org.bukkit.entity.Player;

import java.util.List;

public class GuiPickQuest extends Gui {
    private EpicQuests plugin;
    private Player player;
    private Quest quest;
    private Speech speech;
    private StoryManager storyManager;

    public GuiPickQuest(EpicQuests plugin, Player player, Quest quest) {
        this.quest = quest;
        init(plugin, player);
    }

    public GuiPickQuest(EpicQuests plugin, Player player, Speech speech) {
        this.speech = speech;
        init(plugin, player);
    }

    public void init(EpicQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.storyManager = plugin.getStoryManager();
        setRows(6);
        setDefaultItem(null);

        setTitle("Pick a quest.");

        show();
    }

    public void show() {
        reset();

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> {
                    if (this.quest != null) {
                        this.guiManager.showGUI(this.player, new GuiQuestPrereqs(this.plugin, this.player, this.quest));
                    } else {
                        this.guiManager.showGUI(this.player, new GuiQuestPrereqs(this.plugin, this.player, this.speech));
                    }
                });

        List<Quest> quests = this.storyManager.getQuests();
        quests.remove(this.quest);
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, quest.getName()),
                    (event) -> {
                        if (this.quest == null) {
                            this.speech.addQuestPrerequisite(quest);
                            this.speech.save("quest_prerequisites");
                            this.guiManager.showGUI(this.player, new GuiQuestPrereqs(this.plugin, this.player, this.speech));
                        } else {
                            this.quest.addQuestPrerequisite(quest);
                            this.quest.save("quest_prerequisites");
                            this.guiManager.showGUI(this.player, new GuiQuestPrereqs(this.plugin, this.player, this.quest));
                        }
                    });
        }
    }
}
