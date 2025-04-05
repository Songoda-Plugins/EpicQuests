package com.songoda.epicquests.story.quest.action.actions;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.utils.ItemSerializer;
import com.songoda.core.utils.TextUtils;
import com.songoda.third_party.org.jooq.DSLContext;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.data.ActionDataStore;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.action.AbstractAction;
import com.songoda.epicquests.story.quest.action.ActionType;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class DropItem extends AbstractAction {
    public DropItem(EpicQuests plugin) {
        super(plugin, ActionType.DROP_ITEM, false);
    }

    @Override
    public List<String> getDescription(ActionDataStore actionDataStore) {
        DropItemDataStore dataStore = (DropItemDataStore) actionDataStore;
        return dataStore.getItemStack() == null ? Collections.singletonList("None") : Collections.singletonList(TextUtils.formatText("&fItem: &6" + dataStore.getItemStack().getType().name()));
    }

    @Override
    public void onDrop(PlayerDropItemEvent event, ActiveAction activeAction) {
        DropItemDataStore dataStore = (DropItemDataStore) activeAction.getActionDataStore();
        Player player = event.getPlayer();

        ItemStack item = event.getItemDrop().getItemStack();

        if (item.isSimilar(dataStore.getItemStack())) {
            performAction(activeAction, item.getAmount(), player);
        }

        if (!dataStore.isBeingSetup(player)) {
            return;
        }
        dataStore.setItemStack(event.getItemDrop().getItemStack());
        dataStore.finishSetup(this.plugin, player, activeAction);
    }

    @Override
    public ActiveAction setup(Player player, Objective objective) {
        player.sendMessage("Drop the item you would like assigned to this action.");
        DropItemDataStore dataStore = new DropItemDataStore(objective);
        dataStore.startSetup(player.getUniqueId());

        // Do setup here.
        return new ActiveAction(this, dataStore);
    }

    public static class DropItemDataStore extends ActionDataStore {
        private ItemStack itemStack;

        public DropItemDataStore(Objective objective) {
            super(objective);
        }

        public ItemStack getItemStack() {
            return this.itemStack;
        }

        public void setItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public void saveImpl(DSLContext ctx, String... columns) {
            SQLInsert.create(ctx)
                    .insertInto("action_drop_item")
                    .withField("id", id, id == -1)
                    .withField("objective_id", objective.getId())
                    .withField("amount", amount)
                    .withField("item_stack", ItemSerializer.serializeItem(itemStack))
                    .onDuplicateKeyUpdate()
                    .execute();
        }

        @Override
        public void deleteImpl(DSLContext ctx) {
            SQLDelete.create(ctx).delete("action_drop_item", "id", id);
        }
    }
}
