package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.Story;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class GuiStory extends Gui {
    private final EpicQuests plugin;
    private final Player player;
    private final Story story;

    public GuiStory(EpicQuests plugin, Player player, Story story) {
        this.plugin = plugin;
        this.player = player;
        this.story = story;
        setRows(6);
        setDefaultItem(null);

        setTitle(story.getName());

        show();
    }

    public void show() {
        reset();

        setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Rename Story"),
                (event) -> ChatPrompt.showPrompt(this.plugin, this.player,
                                "Enter a story name.",
                                response -> {
                                    story.setName(response.getMessage());
                                    story.save("name");
                                })
                        .setOnClose(() -> this.guiManager.showGUI(this.player, new GuiStory(this.plugin, this.player, this.story))));

        setButton(0, 1, GuiUtils.createButtonItem(XMaterial.BLUE_DYE, "Create Quest"),
                (event) -> {
                    Quest quest = story.addQuest(new Quest(this.story));
                    quest.save();
                    show();
                });

        setButton(0, 2, GuiUtils.createButtonItem(XMaterial.RED_DYE, this.story.isActive() ? "Active" : "Inactive"),
                (event) -> {
                    story.setActive(!this.story.isActive());
                    story.save("active");
                    show();
                });

        setItem(0, 7, GuiUtils.createButtonItem(XMaterial.OAK_SIGN, TextUtils.formatText("&7Quests are tasks that players",
                "&7can complete to progress through", "&7a story.")));

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> this.guiManager.showGUI(this.player, new GuiStories(this.plugin, this.player)));


        List<Quest> quests = this.story.getQuests();
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, quest.getName(),
                            "",
                            TextUtils.formatText("&fLeft-Click: &6to view"),
                            TextUtils.formatText("&fRight-Click: &6to delete")),
                    (event) -> {
                        if (event.clickType == ClickType.LEFT) {
                            this.guiManager.showGUI(this.player, new GuiQuest(this.plugin, this.player, quest));
                        } else if (event.clickType == ClickType.RIGHT) {
                            ChatPrompt.showPrompt(this.plugin, this.player,
                                            "Type in 'DELETE' to confirm.",
                                            response -> {
                                                if (response.getMessage().trim().equalsIgnoreCase("delete")) {
                                                    story.removeQuest(quest);
                                                    story.delete();
                                                }
                                            })
                                    .setOnClose(() -> this.guiManager.showGUI(this.player, new GuiStory(this.plugin, this.player, this.story)));
                        }
                    });
        }
    }
}
