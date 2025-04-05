package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicquests.story.quest.reward.AbstractReward;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.epicquests.story.quest.reward.RewardType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class GuiRewardTypes extends Gui {
    public GuiRewardTypes(EpicQuests plugin, Player player, Quest quest) {
        setRows(6);
        setDefaultItem(null);

        setTitle("Pick a reward type");

        setActionForRange(0, 53, null);

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.BARRIER, "Cancel"),
                (event) -> {
                    this.guiManager.showGUI(player, new GuiRewards(plugin, player, quest));
                });

        List<RewardType> rewards = Arrays.asList(RewardType.values());
        for (int i = 0; i < rewards.size(); i++) {
            RewardType rewardType = rewards.get(i);
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, rewardType.name()),
                    (event) -> {
                        player.closeInventory();
                        AbstractReward reward = rewardType.init(quest);
                        quest.addReward(reward);
                        if (reward != null) {
                            reward.setup(player, this, () -> {
                                reward.save();
                                guiManager.showGUI(player, this);
                            }, null);
                        }
                    });
        }
    }
}
