package com.craftaro.epicrpg.gui;

import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicrpg.EpicRPG;
import com.craftaro.epicrpg.dialog.Speech;
import com.craftaro.epicrpg.story.quest.Objective;
import com.craftaro.epicrpg.story.quest.action.Action;
import com.craftaro.epicrpg.story.quest.action.ActionManager;
import com.craftaro.epicrpg.story.quest.action.ActiveAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiObjective extends Gui {
    private final EpicRPG plugin;
    private final Player player;
    private final ActionManager actionManager;
    private final Objective objective;

    public GuiObjective(EpicRPG plugin, Player player, Objective objective) {
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
                                    response -> this.objective.setTitle(response.getMessage()))
                            .setOnClose(() -> this.guiManager.showGUI(this.player, new GuiObjective(this.plugin, this.player, this.objective)));
                });

        setButton(0, 1, GuiUtils.createButtonItem(XMaterial.BLUE_DYE, "Add Action"),
                (event) -> this.guiManager.showGUI(this.player, new GuiActionTypes(this.plugin, this.player, this.objective)));

        setButton(0, 2, GuiUtils.createButtonItem(XMaterial.PINK_DYE, "Modify Requirements"),
                (event) -> {
                    this.guiManager.showGUI(this.player, new GuiRequirements(this.plugin, this.player, this.objective));
                    show();
                });

        Speech speech = this.plugin.getDialogManager().getSpeech(this.objective.getAttachedSpeech());
        setButton(0, 3, GuiUtils.createButtonItem(XMaterial.RED_DYE, "Attach Speech",
                        TextUtils.formatText("&fAttached to: &6" + (speech == null ? "NONE" : speech.getDialog().getCitizen().getName()))),
                (event) -> {
                    this.guiManager.showGUI(this.player, new GuiDialogs(this.plugin, this.player, this.objective));
                    show();
                });

        setButton(0, 4, GuiUtils.createButtonItem(XMaterial.PURPLE_DYE, this.objective.isVisible() ? "Visible" : "Invisible"),
                (event) -> {
                    this.objective.setVisible(!this.objective.isVisible());
                    show();
                });

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> this.guiManager.showGUI(this.player, new GuiQuest(this.plugin, this.player, this.objective.getQuest())));

        List<ActiveAction> actions = this.actionManager.getActiveActions().stream()
                .filter(a -> a.getObjective() == this.objective).collect(Collectors.toList());
        for (int i = 0; i < actions.size(); i++) {
            ActiveAction activeAction = actions.get(i);
            Action action = activeAction.getAction();

            List<String> lore = new ArrayList<>(action.getDescription(activeAction.getActionDataStore()));
            lore.add(TextUtils.formatText("&fAmount: &6" + activeAction.getAmount()));
            lore.addAll(Arrays.asList("",
                    TextUtils.formatText("&fLeft-Click: &6to setup"),
                    TextUtils.formatText("&fRight-Click: &6to delete")));
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER,
                            action.getType(), lore),
                    (event) -> {
                        if (event.clickType == ClickType.LEFT) {
                            this.player.closeInventory();
                            this.actionManager.addActiveAction(action.setup(this.player, this.objective));
                            this.actionManager.removeActiveAction(activeAction);
                            show();
                        } else if (event.clickType == ClickType.RIGHT) {
                            this.actionManager.removeActiveAction(activeAction);
                            show();
                        }
                    });
        }
    }
}
