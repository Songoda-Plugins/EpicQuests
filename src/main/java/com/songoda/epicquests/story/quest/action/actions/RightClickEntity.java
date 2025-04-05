package com.songoda.epicquests.story.quest.action.actions;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.data.ActionDataStore;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.action.AbstractAction;
import com.songoda.epicquests.story.quest.action.ActionType;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import com.songoda.third_party.org.jooq.DSLContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RightClickEntity extends AbstractAction {
    public RightClickEntity(EpicQuests plugin) {
        super(plugin, ActionType.RIGHT_CLICK_ENTITY, true);
    }

    @Override
    public List<String> getDescription(ActionDataStore actionDataStore) {
        RightClickEntityDataStore dataStore = (RightClickEntityDataStore) actionDataStore;
        if (dataStore.getEntity() != null && Bukkit.getEntity(dataStore.entity) != null)
            return Collections.singletonList(TextUtils.formatText("&fEntity UUID: &6" + dataStore.getEntity()));
        else
            return Collections.singletonList(TextUtils.formatText("&cThe attached entity could not be found!"));
    }

    @Override
    public boolean onInteractWithEntity(PlayerInteractAtEntityEvent event, ActiveAction activeAction) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        RightClickEntityDataStore dataStore = (RightClickEntityDataStore) activeAction.getActionDataStore();

        if (dataStore.isBeingSetup(event.getPlayer())) {
            dataStore.setEntity(entity.getUniqueId());
            dataStore.finishSetup(this.plugin, player, activeAction);
        }

        if (entity.getUniqueId().equals(dataStore.getEntity()))
            performAction(activeAction, 1, player);
        return false;
    }

    @Override
    public ActiveAction setup(Player player, Objective objective) {
        player.sendMessage("Right click an entity to assign this action.");
        RightClickEntityDataStore dataStore = new RightClickEntityDataStore(objective);
        dataStore.startSetup(player);
        return new ActiveAction(this, dataStore);
    }

    public static class RightClickEntityDataStore extends ActionDataStore {
        private UUID entity;

        public RightClickEntityDataStore(Objective objective) {
            super(objective);
        }

        public UUID getEntity() {
            return this.entity;
        }

        public void setEntity(UUID entity) {
            this.entity = entity;
        }

        @Override
        public void saveImpl(DSLContext ctx, String... columns) {
            SQLInsert.create(ctx)
                    .insertInto("action_right_click_entity")
                    .withField("id", id, id == -1)
                    .withField("objective_id", objective.getId())
                    .withField("amount", amount)
                    .withField("entity", entity.toString())
                    .onDuplicateKeyUpdate()
                    .execute();
        }

        @Override
        public void deleteImpl(DSLContext ctx) {
            SQLDelete.create(ctx).delete("action_right_click_entity", "id", id);
        }
    }
}
