package com.songoda.epicquests.story.quest.action.actions;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.utils.LocationUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.data.ActionDataStore;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.action.AbstractAction;
import com.songoda.epicquests.story.quest.action.ActionType;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import com.songoda.third_party.org.jooq.DSLContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;
import java.util.List;

public class RightClickBlock extends AbstractAction {
    public RightClickBlock(EpicQuests plugin) {
        super(plugin, ActionType.RIGHT_CLICK_BLOCK, true);
    }

    @Override
    public List<String> getDescription(ActionDataStore actionDataStore) {
        RightClickBlockDataStore dataStore = (RightClickBlockDataStore) actionDataStore;
        Location location = dataStore.getLocation();
        return Collections.singletonList(TextUtils.formatText("&fBlock: &6"
                + (dataStore.getLocation() == null ? "Undefined" : location.getX() + ":" + location.getY() + ":" + location.getZ())));
    }

    @Override
    public void onInteract(PlayerInteractEvent event, ActiveAction activeAction) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        RightClickBlockDataStore dataStore = (RightClickBlockDataStore) activeAction.getActionDataStore();

        if (dataStore.isBeingSetup(event.getPlayer())) {
            dataStore.setLocation(block.getLocation());
            dataStore.finishSetup(this.plugin, player, activeAction);
            return;
        }

        if (LocationUtils.isLocationMatching(block.getLocation(), dataStore.location)) {
            performAction(activeAction, 1, player);
        }
    }

    @Override
    public ActiveAction setup(Player player, Objective objective) {
        player.sendMessage("Right click a block to assign this action.");
        RightClickBlockDataStore dataStore = new RightClickBlockDataStore(objective);
        dataStore.startSetup(player);
        return new ActiveAction(this, dataStore);
    }

    public static class RightClickBlockDataStore extends ActionDataStore {
        private Location location;

        public RightClickBlockDataStore(Objective objective) {
            super(objective);
        }

        public Location getLocation() {
            if (this.location == null) {
                return null;
            }
            return this.location.clone();
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        @Override
        public void saveImpl(DSLContext ctx, String... columns) {
            SQLInsert.create(ctx)
                    .insertInto("action_right_click_block")
                    .withField("id", id, id == -1)
                    .withField("objective_id", objective.getId())
                    .withField("amount", amount)
                    .withField("world", location != null ? location.getWorld().getName() : null)
                    .withField("x", location != null ? location.getX() : null)
                    .withField("y", location != null ? location.getY() : null)
                    .withField("z", location != null ? location.getZ() : null)
                    .onDuplicateKeyUpdate()
                    .execute();
        }

        @Override
        public void deleteImpl(DSLContext ctx) {
            SQLDelete.create(ctx).delete("action_right_click_block", "id", id);
        }
    }
}
