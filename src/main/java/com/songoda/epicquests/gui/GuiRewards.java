package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicquests.story.quest.reward.AbstractReward;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.quest.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class GuiRewards extends Gui {
    private final EpicQuests plugin;
    private final Player player;
    private final Quest quest;

    public GuiRewards(EpicQuests plugin, Player player, Quest quest) {
        this.plugin = plugin;
        this.player = player;
        this.quest = quest;
        setRows(6);
        setDefaultItem(null);

        setTitle(quest.getName() + " - rewards");

        show();
    }

    public void show() {
        reset();

        setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Add Reward"),
                (event) -> this.guiManager.showGUI(this.player, new GuiRewardTypes(this.plugin, this.player, this.quest)));

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> this.guiManager.showGUI(this.player, new GuiQuest(this.plugin, this.player, this.quest)));


        List<AbstractReward> rewards = this.quest.getRewards();
        for (int i = 0; i < rewards.size(); i++) {
            AbstractReward reward = rewards.get(i);

            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, reward.getType().name()),
                    (event) -> {
                        if (event.clickType == ClickType.RIGHT) {
                            this.quest.removeReward(reward);
                            reward.delete();
                            show();
                        } else if (event.clickType == ClickType.LEFT) {
                            reward.setup(this.player, this, () -> {
                                reward.save();
                                show();
                            }, null);
                        }
                    });
        }
    }
}
