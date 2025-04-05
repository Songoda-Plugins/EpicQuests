package com.songoda.epicquests.story.quest.reward.rewards;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.gui.Gui;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.NumberUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.gui.GuiRewards;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.epicquests.story.quest.reward.AbstractReward;
import com.songoda.epicquests.story.quest.reward.RewardType;
import com.songoda.third_party.org.jooq.DSLContext;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class XpReward extends AbstractReward {
    private final EpicQuests plugin;

    private int xp;

    public XpReward(Quest quest) {
        super(quest);

        this.plugin = JavaPlugin.getPlugin(EpicQuests.class);
    }

    @Override
    public RewardType getType() {
        return RewardType.ITEM;
    }

    @Override
    public void setup(Player player, Gui back, Runnable callback, Runnable onDelete) {

        ChatPrompt.showPrompt(this.plugin, player,
                        "Enter an xp amount.",
                        response -> {
                            String msg = response.getMessage();
                            if (NumberUtils.isInt(msg)) {
                                this.xp = Integer.parseInt(msg);
                            }
                            callback.run();
                        })
                .setOnClose(() -> this.plugin.getGuiManager().showGUI(player, back));
    }

    @Override
    public void give(Player player) {
        player.giveExp(this.xp);
    }

    @Override
    public void saveImpl(DSLContext ctx, String... columns) {
        SQLInsert.create(ctx).insertInto("xp_reward")
                .withField("id", id, id == -1)
                .withField("xp", this.xp)
                .withField("quest", this.getQuest().getId())
                .onDuplicateKeyUpdate(columns)
                .execute();

        if (id == -1)
            this.id = lastInsertedId("xp_reward", ctx);
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("xp_reward", "id", id);
    }

    public int getXp() {
        return this.xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}
