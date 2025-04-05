package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.dialog.Speech;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.requirement.AbstractRequirement;
import com.songoda.epicquests.utils.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class GuiRequirements extends Gui {
    private final EpicQuests plugin;
    private final Player player;
    private final Objective objective;

    public GuiRequirements(EpicQuests plugin, Player player, Objective objective) {
        this.plugin = plugin;
        this.player = player;
        this.objective = objective;
        setRows(6);
        setDefaultItem(null);

        setTitle(objective.getTitle() + " - requirements");

        show();
    }

    public void show() {
        reset();

        setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Add Requirement"),
                (event) -> this.guiManager.showGUI(this.player, new GuiRequirementTypes(this.plugin, this.player, this, this.objective, requirement -> {
                    objective.addRequirement(requirement);
                    requirement.save();
                    guiManager.showGUI(player, this);
                    show();
                })));

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> this.guiManager.showGUI(this.player, new GuiObjective(this.plugin, this.player, this.objective)));


        List<AbstractRequirement> requirements = this.objective.getRequirements();
        for (int i = 0; i < requirements.size(); i++) {
            AbstractRequirement requirement = requirements.get(i);

            String citizenName = null;
            if (requirement.getAttachedSpeech() != -1) {
                Speech speech = plugin.getDialogManager().getSpeech(requirement.getAttachedSpeech());
                if (speech != null)
                    citizenName = speech.getDialog().getCitizen().getName();
            }
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, requirement.getType().name(),
                            TextUtils.formatText("&7" + requirement.getDescription().toLowerCase()),
                            com.songoda.core.utils.TextUtils.formatText("&fRejection Message: &6" + (citizenName == null ? "NONE" : citizenName)),
                    "",
                    TextUtils.formatText("&fRight-Click: &6to attach dialog"),
                    TextUtils.formatText("&fLeft-Click: &6to setup")),
                    (event) -> {
                        if (event.clickType == ClickType.RIGHT) {
                            this.guiManager.showGUI(this.player, new GuiDialogs(this.plugin, this.player, speech -> {
                                requirement.setAttachedSpeech(speech.getId());
                                requirement.save("reject");
                                guiManager.showGUI(player, this);
                            }));
                        } else if (event.clickType == ClickType.LEFT) {
                            requirement.setup(this.player, this, () -> {
                                        requirement.save();
                                        guiManager.showGUI(player, this);
                                        show();
                                    },
                                    () -> {
                                        this.objective.removeRequirement(requirement);
                                        requirement.delete();
                                        guiManager.showGUI(player, this);
                                        show();
                                    });
                        }
                    });
        }
    }
}
