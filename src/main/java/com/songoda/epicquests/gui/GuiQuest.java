package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.Region.Region;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.Quest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiQuest extends Gui {
    private final EpicQuests plugin;
    private final Player player;
    private final Quest quest;

    public GuiQuest(EpicQuests plugin, Player player, Quest quest) {
        this.plugin = plugin;
        this.player = player;
        this.quest = quest;
        setRows(6);
        setDefaultItem(null);

        setTitle(quest.getName());

        show();
    }

    public void show() {
        reset();

        setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Rename Quest"),
                (event) -> ChatPrompt.showPrompt(this.plugin, this.player,
                                "Enter a Quest name.",
                                response -> {
                                    quest.setName(response.getMessage());
                                    quest.save("name");
                                })
                        .setOnClose(() -> this.guiManager.showGUI(this.player, new GuiQuest(this.plugin, this.player, this.quest))));

        setButton(0, 1, GuiUtils.createButtonItem(XMaterial.BLUE_DYE, "Create Objective"),
                (event) -> {
                    Objective objective = quest.addObjective(new Objective(this.quest));
                    objective.setStartPosition(quest.getObjectives().size() - 1);
                    objective.setEndPosition(quest.getObjectives().size());
                    objective.save();
                    show();
                });

        setButton(0, 2, GuiUtils.createButtonItem(XMaterial.RED_DYE, this.quest.isActive() ? "Active" : "Inactive"),
                (event) -> {
                    quest.setActive(!quest.isActive());
                    quest.save("active");
                    show();
                });

        setButton(0, 3, GuiUtils.createButtonItem(XMaterial.PURPLE_DYE, "Modify Prerequisites"),
                (event) -> {
                    this.guiManager.showGUI(this.player, new GuiQuestPrereqs(this.plugin, this.player, this.quest));
                    show();
                });

        setButton(0, 4, GuiUtils.createButtonItem(XMaterial.YELLOW_DYE, "Modify Rewards"),
                (event) -> {
                    this.guiManager.showGUI(this.player, new GuiRewards(this.plugin, this.player, this.quest));
                    show();
                });

        Region region = this.quest.getRegion();
        setButton(0, 5, GuiUtils.createButtonItem(XMaterial.ORANGE_DYE, "Modify Focus Region",
                        region != null ? TextUtils.formatText(Arrays.asList("&fRight-Click: &6to show region", "&fLeft-Click: &6to clear"))
                                : Collections.singletonList(TextUtils.formatText("&6Click to set region."))),
                (event) -> {
                    if (region == null) {
                        this.plugin.getSelectionManager().addActiveSelection(this.player, this.quest);
                        close();
                        this.player.sendMessage("Select your first region position.");
                    } else {
                        if (event.clickType == ClickType.RIGHT) {
                            this.plugin.getSelectionManager().addActiveView(this.player, region);
                            close();
                            this.player.sendMessage("Showing region for 15 seconds.");
                            Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                                            this.plugin.getSelectionManager().removeActiveView(this.player),
                                    20 * 15);
                        } else if (event.clickType == ClickType.LEFT) {
                            this.quest.setRegion(null);
                            show();
                        }
                    }
                });


        setItem(0, 7, GuiUtils.createButtonItem(XMaterial.OAK_SIGN, com.songoda.epicquests.utils.TextUtils.formatText("&7Objectives are tasks that players",
                "&7must complete to progress through", "&7a quest.")));

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> {
                    this.guiManager.showGUI(this.player, new GuiStory(this.plugin, this.player, this.quest.getStory()));
                });

        // sort by start position
        List<Objective> objectives = this.quest.getObjectives().stream()
                .sorted(Comparator.comparingInt(Objective::getStartPosition))
                .collect(Collectors.toList());
        for (int i = 0; i < objectives.size(); i++) {
            Objective objective = objectives.get(i);
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, objective.getTitle(),
                            "",
                            TextUtils.formatText("&fLeft-Click: &6to view"),
                            TextUtils.formatText("&fRight-Click: &6to delete")),
                    (event) -> {
                        if (event.clickType == ClickType.LEFT) {
                            this.guiManager.showGUI(this.player, new GuiObjective(this.plugin, this.player, objective));
                        } else if (event.clickType == ClickType.RIGHT) {
                            this.quest.removeObjective(objective);
                            objective.delete();
                            show();
                        }
                    });
        }
    }
}
