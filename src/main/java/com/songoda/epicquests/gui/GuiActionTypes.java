package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.action.AbstractAction;
import com.songoda.epicquests.story.quest.action.ActionManager;
import org.bukkit.entity.Player;

import java.util.List;

public class GuiActionTypes extends Gui {
    public GuiActionTypes(EpicQuests plugin, Player player, Objective objective) {
        setRows(6);
        setDefaultItem(null);
        ActionManager actionManager = plugin.getActionManager();

        setTitle("Pick an action type");

        setActionForRange(0, 53, null);

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.BARRIER, "Cancel"),
                (event) -> this.guiManager.showGUI(player, new GuiObjective(plugin, player, objective)));

        List<AbstractAction> actions = actionManager.getActions();
        for (int i = 0; i < actions.size(); ++i) {
            AbstractAction action = actions.get(i);
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, action.getType().name()),
                    (event) -> {
                        player.closeInventory();
                        actionManager.addActiveAction(action.setup(player, objective)).getActionDataStore();
                    });
        }
    }
}
