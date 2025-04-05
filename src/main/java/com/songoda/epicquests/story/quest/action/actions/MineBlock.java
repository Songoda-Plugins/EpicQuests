package com.songoda.epicquests.story.quest.action.actions;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.epicquests.story.quest.action.ActionType;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.data.ActionDataStore;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.action.AbstractAction;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import com.songoda.third_party.org.jooq.DSLContext;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collections;
import java.util.List;

public class MineBlock extends AbstractAction {
    public MineBlock(EpicQuests plugin) {
        super(plugin, ActionType.MINE_BLOCK, false);
    }

    @Override
    public List<String> getDescription(ActionDataStore actionDataStore) {
        RightClickBlockDataStore dataStore = (RightClickBlockDataStore) actionDataStore;
        XMaterial material = dataStore.getMaterial();
        return Collections.singletonList(TextUtils.formatText("&fBlock: &6" + (dataStore.getMaterial() == null ? "Undefined" : material.name())));
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, ActiveAction action) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        RightClickBlockDataStore dataStore = (RightClickBlockDataStore) action.getActionDataStore();

        if (dataStore.isBeingSetup(event.getPlayer())) {
            dataStore.setMaterial(CompatibleMaterial.getMaterial(block.getType()).get());
            dataStore.finishSetup(this.plugin, player, action);
            return;
        }

        if (CompatibleMaterial.getMaterial(block.getType()).orElse(null) == dataStore.getMaterial()) {
            performAction(action, 1, player);
        }
    }

    @Override
    public ActiveAction setup(Player player, Objective objective) {
        player.sendMessage("Break a block to assign this action.");
        RightClickBlockDataStore dataStore = new RightClickBlockDataStore(objective);
        dataStore.startSetup(player);
        return new ActiveAction(this, dataStore);
    }

    public static class RightClickBlockDataStore extends ActionDataStore {
        private XMaterial material;

        public RightClickBlockDataStore(Objective objective) {
            super(objective);
        }

        public XMaterial getMaterial() {
            return this.material;
        }

        public void setMaterial(XMaterial material) {
            this.material = material;
        }

        @Override
        public void saveImpl(DSLContext ctx, String... columns) {
            SQLInsert.create(ctx)
                    .insertInto("action_mine_block")
                    .withField("id", id, id == -1)
                    .withField("objective_id", objective.getId())
                    .withField("amount", amount)
                    .withField("material", material.name())
                    .onDuplicateKeyUpdate()
                    .execute();
        }

        @Override
        public void deleteImpl(DSLContext ctx) {
            SQLDelete.create(ctx).delete("action_mine_block", "id", id);
        }
    }
}
