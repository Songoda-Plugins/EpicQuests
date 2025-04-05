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
import java.util.Optional;

public class GuiQuestPrereqs extends Gui {
    private EpicQuests plugin;
    private Player player;
    private StoryManager storyManager;
    private Quest quest;
    private Speech speech;

    public GuiQuestPrereqs(EpicQuests plugin, Player player, Quest quest) {
        this.quest = quest;
        init(plugin, player);
    }

    public GuiQuestPrereqs(EpicQuests plugin, Player player, Speech speech) {
        this.speech = speech;
        init(plugin, player);
    }

    public void init(EpicQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.storyManager = plugin.getStoryManager();
        setRows(6);
        setDefaultItem(null);

        setTitle((this.quest == null ? "Dialog" : this.quest.getName()) + " - prereqs");

        show();
    }

    public void show() {
        reset();

        setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Add Quest"),
                (event) -> {
                    if (this.quest == null) {
                        this.guiManager.showGUI(this.player, new GuiPickQuest(this.plugin, this.player, this.speech));
                    } else {
                        this.guiManager.showGUI(this.player, new GuiPickQuest(this.plugin, this.player, this.quest));
                    }
                });

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> {
                    if (this.quest == null) {
                        this.guiManager.showGUI(this.player, new GuiSpeech(this.plugin, this.player, this.speech));
                    } else {
                        this.guiManager.showGUI(this.player, new GuiQuest(this.plugin, this.player, this.quest));
                    }
                });


        List<Integer> prereqs = this.quest == null ? this.speech.getQuestPrerequisites() : this.quest.getQuestPrerequisites();
        for (int i = 0; i < prereqs.size(); i++) {
            int id = prereqs.get(i);
            Optional<Quest> optional = this.storyManager.getQuests().stream().filter(q -> q.getId() == id).findFirst();
            if (!optional.isPresent()) {
                continue;
            }
            Quest quest = optional.get();

            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, quest.getName()),
                    (event) -> {
                        if (this.quest == null) {
                            this.speech.removeQuestPrerequisite(id);
                            this.speech.save("quest_prerequisites");
                        } else {
                            this.quest.removeQuestPrerequisite(id);
                            this.quest.save("quest_prerequisites");
                        }
                        show();
                    });
        }
    }
}
