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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Collections;
import java.util.List;

public class KillEntityType extends AbstractAction {
    public KillEntityType(EpicQuests plugin) {
        super(plugin, ActionType.KILL_ENTITY_TYPE, false);
    }

    @Override
    public List<String> getDescription(ActionDataStore actionDataStore) {
        KillEntityTypeDataStore dataStore = (KillEntityTypeDataStore) actionDataStore;
        return Collections.singletonList(TextUtils.formatText("&fEntity Type: &6"
                + (dataStore.getEntityType() == null ? "Undefined" : dataStore.getEntityType().name())));
    }

    @Override
    public boolean onInteractWithEntity(PlayerInteractAtEntityEvent event, ActiveAction activeAction) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        KillEntityTypeDataStore dataStore = (KillEntityTypeDataStore) activeAction.getActionDataStore();

        if (dataStore.isBeingSetup(event.getPlayer())) {
            dataStore.setEntityType(entity.getType());
            dataStore.finishSetup(this.plugin, player, activeAction);
        }
        return false;
    }

    @Override
    public void onEntityKill(EntityDeathEvent event, ActiveAction activeAction) {
        KillEntityTypeDataStore dataStore = (KillEntityTypeDataStore) activeAction.getActionDataStore();
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }

        if (dataStore.getEntityType() == event.getEntityType()) {
            performAction(activeAction, 1, event.getEntity().getKiller());
        }
    }

    @Override
    public ActiveAction setup(Player player, Objective objective) {
        player.sendMessage("Right click the entity type you want to assign to this action.");
        KillEntityTypeDataStore dataStore = new KillEntityTypeDataStore(objective);
        dataStore.startSetup(player);
        return new ActiveAction(this, dataStore);
    }

    public static class KillEntityTypeDataStore extends ActionDataStore {
        private EntityType entityType;

        public KillEntityTypeDataStore(Objective objective) {
            super(objective);
        }

        public EntityType getEntityType() {
            return this.entityType;
        }

        public void setEntityType(EntityType entityType) {
            this.entityType = entityType;
        }

        @Override
        public void saveImpl(DSLContext ctx, String... columns) {
            SQLInsert.create(ctx)
                    .insertInto("action_kill_entity_type")
                    .withField("id", id, id == -1)
                    .withField("objective_id", objective.getId())
                    .withField("amount", amount)
                    .withField("entity_type", entityType.name())
                    .onDuplicateKeyUpdate()
                    .execute();
        }

        @Override
        public void deleteImpl(DSLContext ctx) {
            SQLDelete.create(ctx).delete("action_kill_entity_type", "id", id);
        }
    }
}
