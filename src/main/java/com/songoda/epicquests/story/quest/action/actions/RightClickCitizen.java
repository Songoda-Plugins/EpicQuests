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
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Collections;
import java.util.List;

public class RightClickCitizen extends AbstractAction {
    public RightClickCitizen(EpicQuests plugin) {
        super(plugin, ActionType.RIGHT_CLICK_CITIZEN, true);
    }

    @Override
    public List<String> getDescription(ActionDataStore actionDataStore) {
        RightClickCitizenDataStore dataStore = (RightClickCitizenDataStore) actionDataStore;
        NPC npc = CitizensAPI.getNPCRegistry().getById(dataStore.citizenId);
        if (npc != null) {
            return Collections.singletonList(TextUtils.formatText("&fCitizen Name: &6" + CitizensAPI.getNPCRegistry().getById(dataStore.citizenId).getName()));
        } else {
            return Collections.singletonList(TextUtils.formatText("&cThe attached citizen could not be found!"));
        }
    }

    @Override
    public boolean onInteractWithEntity(PlayerInteractAtEntityEvent event, ActiveAction activeAction) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        RightClickCitizenDataStore dataStore = (RightClickCitizenDataStore) activeAction.getActionDataStore();

        if (!CitizensAPI.getNPCRegistry().isNPC(entity)) {
            return false;
        }

        if (dataStore.isBeingSetup(event.getPlayer())) {
            dataStore.setCitizenId(CitizensAPI.getNPCRegistry().getNPC(entity).getId());
            dataStore.finishSetup(this.plugin, player, activeAction);
            return false;
        }

        System.out.println("Checking if NPC is the right one");
        if (CitizensAPI.getNPCRegistry().getNPC(entity).getId() == dataStore.getCitizenId()) {
            System.out.println("Performing action");
            return performAction(activeAction, 1, player);
        }
        return false;
    }

    @Override
    public ActiveAction setup(Player player, Objective objective) {
        player.sendMessage("Right click a citizen to assign this action.");
        RightClickCitizenDataStore dataStore = new RightClickCitizenDataStore(objective);
        dataStore.startSetup(player);
        return new ActiveAction(this, dataStore);
    }

    public static class RightClickCitizenDataStore extends ActionDataStore {
        private int citizenId;

        public RightClickCitizenDataStore(Objective objective) {
            super(objective);
        }

        public int getCitizenId() {
            return this.citizenId;
        }

        public void setCitizenId(int citizenId) {
            this.citizenId = citizenId;
        }

        @Override
        public void saveImpl(DSLContext ctx, String... columns) {
            SQLInsert.create(ctx)
                    .insertInto("action_right_click_citizen")
                    .withField("id", id, id == -1)
                    .withField("objective_id", objective.getId())
                    .withField("amount", amount)
                    .withField("citizen_id", citizenId)
                    .onDuplicateKeyUpdate()
                    .execute();
        }

        @Override
        public void deleteImpl(DSLContext ctx) {
            SQLDelete.create(ctx).delete("action_right_click_citizen", "id", id);
        }
    }
}
