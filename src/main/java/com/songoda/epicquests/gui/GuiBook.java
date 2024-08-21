package com.songoda.epicquests.gui;

import com.craftaro.core.gui.CustomizableGui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.Story;
import com.songoda.epicquests.story.StoryManager;
import com.songoda.epicquests.story.player.StoryPlayer;
import com.songoda.epicquests.story.quest.ActiveQuest;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.Quest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GuiBook extends CustomizableGui {
    private final EpicQuests plugin;
    private final Player player;
    private final StoryPlayer storyPlayer;
    private final StoryManager storyManager;

    private final int storyCount;

    public GuiBook(EpicQuests plugin, Player player) {
        super(plugin, "book");
        this.plugin = plugin;
        this.player = player;
        this.storyPlayer = plugin.getPlayerManager().getPlayer(player);
        this.storyManager = plugin.getStoryManager();
        setRows(6);
        setDefaultItem(null);

        this.storyCount = Math.toIntExact(this.storyManager.getStories().stream().filter(Story::isActive).count());

        setTitle(this.plugin.getLocale().getMessage("book.title.regular").getMessage());

        show();
    }

    public void show() {
        reset();

        List<Story> stories = this.storyManager.getStories();
        for (int i = 0; i < stories.size(); i++) {
            Story story = stories.get(i);
            if (this.storyCount == 1) {
                showQuests(story, false);
                break;
            }
            if (story.isActive()) {
                setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, story.getName()),
                        (event) -> showQuests(story, false));
            }
        }
    }

    public void showQuests(Story story, boolean completed) {
        reset();

        setButton("objectives", 4, GuiUtils.createButtonItem(XMaterial.BELL,
                        this.plugin.getLocale().getMessage(this.storyPlayer.isSilent() ? "book.objectives.show" : "book.objectives.hide").getMessage()),
                (event) -> {
                    this.storyPlayer.setSilent(!this.storyPlayer.isSilent());
                    showQuests(story, completed);
                });

        if (this.storyCount != 1) {
            setButton("back", 5, 3, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("book.back").getMessage()),
                    (event) -> show());
        }
        setButton("exit", 5, this.storyCount == 1 ? 3 : 4, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, this.plugin.getLocale().getMessage("book.exit").getMessage()),
                (event) -> this.player.closeInventory());


        long amountCompleted = story.getEnabledQuests().stream()
                .filter(q -> this.storyPlayer.getCompletedQuests().contains(q.getId())).count();

        List<String> lore = new ArrayList<>();
        if (completed) {
            lore.addAll(Arrays.asList(
                    this.plugin.getLocale().getMessage("book.quests.ongoing.lore1").toText(),
                    this.plugin.getLocale().getMessage("book.quests.ongoing.lore2").toText(),
                    "",
                    this.plugin.getLocale().getMessage("book.quests.ongoing.click").toText()));
        } else {
            lore.addAll(Arrays.asList(
                    this.plugin.getLocale().getMessage("book.quests.completed.lore1").toText(),
                    this.plugin.getLocale().getMessage("book.quests.completed.lore2").toText(),
                    this.plugin.getLocale().getMessage("book.quests.completed.lore3").toText(),
                    "",
                    this.plugin.getLocale().getMessage("book.quests.completed.amount")
                            .processPlaceholder("amount", Long.toString(amountCompleted)).toText(),
                    "",
                    this.plugin.getLocale().getMessage("book.quests.completed.click").toText()));

        }

        setButton("quests", 5, 5, GuiUtils.createButtonItem(XMaterial.BOOK,
                        this.plugin.getLocale().getMessage(completed ? "book.quests.ongoing.title" : "book.quests.completed.title").toText(), lore),
                (event) -> {
                    setTitle(this.plugin.getLocale().getMessage(completed ? "book.title.regular" : "book.title.completed").toText());
                    showQuests(story, !completed);
                });

        int i = 9;
        for (Quest quest : story.getEnabledQuests()) {
            boolean questCompleted = this.storyPlayer.getCompletedQuests().contains(quest.getId());
            if (completed && !questCompleted || !completed && questCompleted
                    || !this.storyPlayer.getActiveQuests().stream()
                    .map(ActiveQuest::getActiveQuest).collect(Collectors.toList())
                    .contains(quest.getId()) && !this.storyPlayer.getCompletedQuests().contains(quest.getId())) {
                continue;
            }

            ActiveQuest activeQuest = this.storyPlayer.getActiveQuest(quest);
            int currentPosition = activeQuest != null ? activeQuest.getCurrentPosition() : 0;

            List<String> objectivesLore = new ArrayList<>(Collections.singletonList(""));
            for (Objective objective : quest.getObjectives()) {
                boolean isObjectiveCompleted = objective.isObjectiveBefore(currentPosition);

                if (objective.getStartPosition() <= currentPosition || completed)
                    objectivesLore.add(this.plugin.getLocale().getMessage(
                                    isObjectiveCompleted ? "book.quest.objective.completed" : (objective.getStartPosition() < currentPosition ? "book.quest.objective.current" : "book.quest.objective.incomplete"))
                            .processPlaceholder("objective", objective.getTitle()).toText());
            }
            if (this.storyPlayer.isFocused(quest)) {
                objectivesLore.addAll(Arrays.asList("", this.plugin.getLocale().getMessage("book.quest.focused").toText()));
            }

            setButton(i, GuiUtils.createButtonItem(XMaterial.PAPER,
                    this.plugin.getLocale().getMessage("book.quest.title")
                            .processPlaceholder("name", quest.getName()).toText(),
                    objectivesLore), (event) -> {
                if (!questCompleted) {
                    if (!storyPlayer.isFocused(quest)) {
                        storyPlayer.toggleAllFocusedOff();
                        storyPlayer.toggleFocus(quest);
                        storyPlayer.save("focused");
                        showQuests(story, false);
                    }
                }
            });
            i++;
        }
    }
}
