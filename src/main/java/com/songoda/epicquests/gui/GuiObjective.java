package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.dialog.Speech;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.action.AbstractAction;
import com.songoda.epicquests.story.quest.action.ActionManager;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiObjective extends Gui {
    private final EpicQuests plugin;
    private final Player player;
    private final ActionManager actionManager;
    private final Objective objective;

    public GuiObjective(EpicQuests plugin, Player player, Objective objective) {
        this.plugin = plugin;
        this.player = player;
        this.actionManager = plugin.getActionManager();
        this.objective = objective;
        setRows(6);
        setDefaultItem(null);

        setTitle(objective.getTitle());

        show();
    }

    public void show() {
        reset();

        setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Retitle Objective"),
                (event) -> {
                    ChatPrompt.showPrompt(this.plugin, this.player,
                                    "Enter a title.",
                                    response -> {
                                        objective.setTitle(response.getMessage());
                                        objective.save("title");
                                    })
                            .setOnClose(() -> this.guiManager.showGUI(this.player, new GuiObjective(this.plugin, this.player, this.objective)));
                });

        setButton(0, 1, GuiUtils.createButtonItem(XMaterial.BLUE_DYE, "Add Action"),
                (event) -> this.guiManager.showGUI(this.player, new GuiActionTypes(this.plugin, this.player, this.objective)));

        setButton(0, 2, GuiUtils.createButtonItem(XMaterial.PINK_DYE, "Modify Requirements"),
                (event) -> {
                    this.guiManager.showGUI(this.player, new GuiRequirements(this.plugin, this.player, this.objective));
                    show();
                });

        Speech currentSpeech = this.plugin.getDialogManager().getSpeech(this.objective.getAttachedSpeech());
        setButton(0, 3, GuiUtils.createButtonItem(XMaterial.RED_DYE, "Attach Speech",
                        TextUtils.formatText("&fAttached to: &6" + (currentSpeech == null ? "NONE" : currentSpeech.getDialog().getCitizen().getName()))),
                (event) -> {
                    this.guiManager.showGUI(this.player, new GuiDialogs(this.plugin, this.player, speech -> {
                        objective.setAttachedSpeech(speech.getId());
                        objective.save("attached_speech");
                        guiManager.showGUI(player, this);
                    }));
                    show();
                });

        setButton(0, 4, GuiUtils.createButtonItem(XMaterial.PURPLE_DYE, this.objective.isVisible() ? "Visible" : "Invisible"),
                (event) -> {
                    this.objective.setVisible(!this.objective.isVisible());
                    this.objective.save("visible");
                    show();
                });

        // set position
        setButton(0, 5, GuiUtils.createButtonItem(XMaterial.LIME_DYE, "Set Position",
                        TextUtils.formatText("&fCurrent: &6" + this.objective.getStartPosition() + " &f- &6" + this.objective.getEndPosition(),
                        "",
                        "&fLeft-Click: &6Set Start",
                        "&fRight-Click: &6Set End")),
                (event) -> {
                    if (event.clickType == ClickType.LEFT) {
                        this.objective.setStartPosition(this.objective.getQuest().getObjectives().size());
                        this.objective.save("start_position");
                    } else if (event.clickType == ClickType.RIGHT) {
                        this.objective.setEndPosition(this.objective.getQuest().getObjectives().size());
                        this.objective.save("end_position");
                    }
                    show();
                });

        setItem(0, 7, GuiUtils.createButtonItem(XMaterial.OAK_SIGN, TextUtils.formatText("&7Actions are tasks that players",
                "&7can complete to progress through", "&7an objective.")));

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> this.guiManager.showGUI(this.player, new GuiQuest(this.plugin, this.player, this.objective.getQuest())));

        List<ActiveAction> actions = this.actionManager.getActiveActions().stream()
                .filter(a -> a.getObjective() == this.objective).collect(Collectors.toList());
        for (int i = 0; i < actions.size(); i++) {
            ActiveAction activeAction = actions.get(i);
            AbstractAction action = activeAction.getAction();

            List<String> lore = new ArrayList<>(action.getDescription(activeAction.getActionDataStore()));
            lore.add(TextUtils.formatText("&fAmount: &6" + activeAction.getAmount()));
            lore.addAll(Arrays.asList("",
                    TextUtils.formatText("&fLeft-Click: &6to setup"),
                    TextUtils.formatText("&fRight-Click: &6to delete")));
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER,
                            action.getType().name(), lore),
                    (event) -> {
                        if (event.clickType == ClickType.LEFT) {
                            this.player.closeInventory();
                            this.actionManager.addActiveAction(action.setup(this.player, this.objective));
                            this.actionManager.removeActiveAction(activeAction);
                            activeAction.getActionDataStore().delete();
                            show();
                        } else if (event.clickType == ClickType.RIGHT) {
                            this.actionManager.removeActiveAction(activeAction);
                            activeAction.getActionDataStore().delete();
                            show();
                        }
                    });
        }
    }
}
