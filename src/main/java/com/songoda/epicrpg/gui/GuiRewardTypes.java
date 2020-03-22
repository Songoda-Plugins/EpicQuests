package com.songoda.epicrpg.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicrpg.EpicRPG;
import com.songoda.epicrpg.story.quest.Quest;
import com.songoda.epicrpg.story.quest.reward.Reward;
import com.songoda.epicrpg.story.quest.reward.RewardType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class GuiRewardTypes extends Gui {

    public GuiRewardTypes(EpicRPG plugin, Player player, Quest quest) {
        setRows(6);
        setDefaultItem(null);

        setTitle("Pick a reward type");

        setActionForRange(0, 53, null);

        setButton(0, 8, GuiUtils.createButtonItem(CompatibleMaterial.BARRIER, "Cancel"),
                (event) -> {
                    guiManager.showGUI(player, new GuiRewards(plugin, player, quest));
                });


        List<RewardType> rewards = Arrays.asList(RewardType.values());
        for (int i = 0; i < rewards.size(); i++) {
            RewardType rewardType = rewards.get(i);
            setButton(i + 9, GuiUtils.createButtonItem(CompatibleMaterial.PAPER, rewardType.name()),
                    (event) -> {
                        player.closeInventory();
                        Reward reward = rewardType.init(quest);
                        quest.addReward(reward);
                        reward.setup(player);
                    });
        }
    }
}