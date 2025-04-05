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
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Collections;
import java.util.List;

public class KillMythicMob extends AbstractAction {
    public KillMythicMob(EpicQuests plugin) {
        super(plugin, ActionType.KILL_MYTHIC_MOB, false);
    }

    @Override
    public List<String> getDescription(ActionDataStore actionDataStore) {
        KillMythicMobDataStore dataStore = (KillMythicMobDataStore) actionDataStore;

        MythicMob mob = MythicBukkit.inst()
                .getMobManager()
                .getMobTypes()
                .stream()
                .filter(type -> type.getInternalName().equals(dataStore.mobType))
                .findFirst()
                .orElse(null);

        if (mob != null) {
            return Collections.singletonList(TextUtils.formatText("&fMythic Mob Name: &6" + mob.getDisplayName()));
        }
        return Collections.singletonList(TextUtils.formatText("&cThe attached mob could not be found!"));
    }


    @Override
    public boolean onInteractWithEntity(PlayerInteractAtEntityEvent event, ActiveAction activeAction) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        KillMythicMobDataStore dataStore = (KillMythicMobDataStore) activeAction.getActionDataStore();

        MobExecutor mobManager = MythicBukkit.inst().getMobManager();

        if (!mobManager.isActiveMob(event.getRightClicked().getUniqueId())
                || !dataStore.isBeingSetup(event.getPlayer())) {
            return false;
        }

        dataStore.setMobType(mobManager.getActiveMob(entity.getUniqueId()).get().getMobType());
        dataStore.finishSetup(this.plugin, player, activeAction);
        return false;
    }

    @Override
    public void onEntityKill(EntityDeathEvent event, ActiveAction activeAction) {
        KillMythicMobDataStore dataStore = (KillMythicMobDataStore) activeAction.getActionDataStore();
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }

        MobExecutor mobManager = MythicBukkit.inst().getMobManager();

        if (!mobManager.isActiveMob(event.getEntity().getUniqueId())) {
            return;
        }

        ActiveMob mob = mobManager.getActiveMob(event.getEntity().getUniqueId()).orElse(null);

        if (mob == null) {
            return;
        }

        if (mob.getMobType().equals(dataStore.mobType)) {
            performAction(activeAction, 1, event.getEntity().getKiller());
        }
    }

    @Override
    public ActiveAction setup(Player player, Objective objective) {
        player.sendMessage("Right click a mythic mob to assign this action.");
        KillMythicMobDataStore dataStore = new KillMythicMobDataStore(objective);
        dataStore.startSetup(player);
        return new ActiveAction(this, dataStore);
    }

    public static class KillMythicMobDataStore extends ActionDataStore {
        private String mobType;

        public KillMythicMobDataStore(Objective objective) {
            super(objective);
        }

        public String getMobType() {
            return this.mobType;
        }

        public void setMobType(String mobType) {
            this.mobType = mobType;
        }


        @Override
        public void saveImpl(DSLContext ctx, String... columns) {
            SQLInsert.create(ctx)
                    .insertInto("action_kill_mythic_mob")
                    .withField("id", id, id == -1)
                    .withField("objective_id", objective.getId())
                    .withField("amount", amount)
                    .withField("mob_type", mobType)
                    .onDuplicateKeyUpdate()
                    .execute();
        }

        @Override
        public void deleteImpl(DSLContext ctx) {
            SQLDelete.create(ctx).delete("action_kill_mythic_mob", "id", id);
        }
    }
}
