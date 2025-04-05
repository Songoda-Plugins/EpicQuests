package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicquests.story.quest.requirement.AbstractRequirement;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.requirement.RequirementType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GuiRequirementTypes extends Gui {
    public GuiRequirementTypes(EpicQuests plugin, Player player, Gui back, Objective objective, Consumer<AbstractRequirement> callback) {
        setRows(6);
        setDefaultItem(null);

        setTitle("Pick a requirement type");

        setActionForRange(0, 53, null);

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.BARRIER, "Cancel"),
                (event) -> {
                    this.guiManager.showGUI(player, back);
                });


        List<RequirementType> requirements = Arrays.asList(RequirementType.values());
        for (int i = 0; i < requirements.size(); i++) {
            RequirementType requirementType = requirements.get(i);
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, requirementType.name()),
                    (event) -> {
                        player.closeInventory();
                        AbstractRequirement requirement = requirementType.init(objective);
                        requirement.setup(player, back, () -> callback.accept(requirement), null);
                    });
        }
    }
}
