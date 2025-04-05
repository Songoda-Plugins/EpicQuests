package com.songoda.epicquests.story.quest.action;

import com.songoda.core.data.LoadsData;
import com.songoda.core.data.SQLSelect;
import com.songoda.core.utils.ItemSerializer;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.third_party.org.jooq.DSLContext;
import com.songoda.third_party.org.jooq.impl.DSL;
import com.songoda.third_party.org.jooq.impl.SQLDataType;
import com.songoda.epicquests.story.quest.action.actions.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public class ActionManager implements LoadsData {

    private final EpicQuests plugin;

    private final Map<ActionType, AbstractAction> registeredActions = new HashMap<>();
    private final Set<ActiveAction> activeActions = new HashSet<>();

    public ActionManager(EpicQuests plugin) {
        this.plugin = plugin;
        registerActions(new RightClickCitizen(plugin),
                new EnterRegion(plugin),
                new PickupItem(plugin),
                new KillEntityType(plugin),
                new RightClickBlock(plugin),
                new RightClickEntity(plugin),
                new MineBlock(plugin),
                new DropItem(plugin));

        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            registerAction(new KillMythicMob(plugin));
        }
    }

    public AbstractAction getAction(ActionType type) {
        return this.registeredActions.get(type);
    }

    public List<AbstractAction> getActions() {
        return new LinkedList<>(this.registeredActions.values());
    }

    public Set<ActiveAction> getActiveActions() {
        return Collections.unmodifiableSet(this.activeActions);
    }

    public List<ActiveAction> getActiveActionsByObjective(Objective objective) {
        return this.activeActions.stream().filter(a -> a.getObjective() == objective).collect(Collectors.toList());
    }

    public void registerAction(AbstractAction action) {
        this.registeredActions.put(action.getType(), action);
    }

    public void registerActions(AbstractAction... actions) {
        for (AbstractAction action : actions) {
            registerAction(action);
        }
    }

    public ActiveAction addActiveAction(ActiveAction activeAction) {
        this.activeActions.add(activeAction);
        return activeAction;
    }

    public void removeActiveAction(ActiveAction action) {
        this.activeActions.remove(action);
    }

    @Override
    public void loadDataImpl(DSLContext ctx) {

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "item").from("action_drop_item", result -> {
            int id = result.get("id").asInt();
            int objectiveId = result.get("objective_id").asInt();
            int amount = result.get("amount").asInt();
            byte[] item = result.get("item").asBytes();

            Objective objective = plugin.getStoryManager().getObjective(objectiveId);
            if (objective == null)
                return;

            DropItem.DropItemDataStore dataStore = new DropItem.DropItemDataStore(objective);
            dataStore.setId(id);
            dataStore.setAmount(amount);

            dataStore.setItemStack(ItemSerializer.deserializeItem(item));
            ActiveAction activeAction = new ActiveAction(getAction(ActionType.DROP_ITEM), dataStore);
            addActiveAction(activeAction);
        });

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "pos1_world", "pos1_x", "pos1_y", "pos1_z", "pos2_world", "pos2_x", "pos2_y", "pos2_z")
                .from("action_enter_region", result -> {
                    int id = result.get("id").asInt();
                    int objectiveId = result.get("objective_id").asInt();
                    int amount = result.get("amount").asInt();
                    String pos1World = result.get("pos1_world").asString();
                    double pos1X = result.get("pos1_x").asDouble();
                    double pos1Y = result.get("pos1_y").asDouble();
                    double pos1Z = result.get("pos1_z").asDouble();
                    String pos2World = result.get("pos2_world").asString();
                    double pos2X = result.get("pos2_x").asDouble();
                    double pos2Y = result.get("pos2_y").asDouble();
                    double pos2Z = result.get("pos2_z").asDouble();

                    Objective objective = plugin.getStoryManager().getObjective(objectiveId);
                    if (objective == null)
                        return;

                    EnterRegion.EnterRegionDataStore dataStore = new EnterRegion.EnterRegionDataStore(objective);
                    dataStore.setId(id);
                    dataStore.setAmount(amount);

                    if (pos1World != null && pos2World != null) {
                        Location pos1 = new Location(Bukkit.getWorld(pos1World), pos1X, pos1Y, pos1Z);
                        Location pos2 = new Location(Bukkit.getWorld(pos2World), pos2X, pos2Y, pos2Z);
                        dataStore.setPos1(pos1);
                        dataStore.setPos2(pos2);
                    }

                    ActiveAction activeAction = new ActiveAction(getAction(ActionType.ENTER_REGION), dataStore);
                    addActiveAction(activeAction);
                });

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "entity_type").from("action_kill_entity_type", result -> {
            int id = result.get("id").asInt();
            int objectiveId = result.get("objective_id").asInt();
            int amount = result.get("amount").asInt();
            String entityTypeName = result.get("entity_type").asString();

            Objective objective = plugin.getStoryManager().getObjective(objectiveId);
            if (objective == null)
                return;

            KillEntityType.KillEntityTypeDataStore dataStore = new KillEntityType.KillEntityTypeDataStore(objective);
            dataStore.setId(id);
            dataStore.setAmount(amount);

            EntityType entityType = EntityType.valueOf(entityTypeName);
            dataStore.setEntityType(entityType);

            ActiveAction activeAction = new ActiveAction(getAction(ActionType.KILL_ENTITY_TYPE), dataStore);
            addActiveAction(activeAction);
        });

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "mob_type").from("action_kill_mythic_mob", result -> {
            int id = result.get("id").asInt();
            int objectiveId = result.get("objective_id").asInt();
            int amount = result.get("amount").asInt();
            String mobType = result.get("mob_type").asString();

            Objective objective = plugin.getStoryManager().getObjective(objectiveId);
            if (objective == null)
                return;

            KillMythicMob.KillMythicMobDataStore dataStore = new KillMythicMob.KillMythicMobDataStore(objective);
            dataStore.setId(id);
            dataStore.setAmount(amount);
            dataStore.setMobType(mobType);

            ActiveAction activeAction = new ActiveAction(getAction(ActionType.KILL_MYTHIC_MOB), dataStore);
            addActiveAction(activeAction);
        });

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "material").from("action_mine_block", result -> {
            int id = result.get("id").asInt();
            int objectiveId = result.get("objective_id").asInt();
            int amount = result.get("amount").asInt();
            String material = result.get("material").asString();

            Objective objective = plugin.getStoryManager().getObjective(objectiveId);
            if (objective == null)
                return;

            MineBlock.RightClickBlockDataStore dataStore = new MineBlock.RightClickBlockDataStore(objective);
            dataStore.setId(id);
            dataStore.setAmount(amount);
            dataStore.setMaterial(XMaterial.valueOf(material));

            ActiveAction activeAction = new ActiveAction(getAction(ActionType.MINE_BLOCK), dataStore);
            addActiveAction(activeAction);
        });

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "item").from("action_pickup_item", result -> {
            int id = result.get("id").asInt();
            int objectiveId = result.get("objective_id").asInt();
            int amount = result.get("amount").asInt();
            byte[] item = result.get("item").asBytes();

            Objective objective = plugin.getStoryManager().getObjective(objectiveId);
            if (objective == null)
                return;

            PickupItem.PickupItemDataStore dataStore = new PickupItem.PickupItemDataStore(objective);
            dataStore.setId(id);
            dataStore.setAmount(amount);

            dataStore.setItemStack(ItemSerializer.deserializeItem(item));
            ActiveAction activeAction = new ActiveAction(getAction(ActionType.PICKUP_ITEM), dataStore);
            addActiveAction(activeAction);
        });

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "world", "x", "y", "z").from("action_right_click_block", result -> {
            int id = result.get("id").asInt();
            int objectiveId = result.get("objective_id").asInt();
            int amount = result.get("amount").asInt();
            String world = result.get("world").asString();
            double x = result.get("x").asDouble();
            double y = result.get("y").asDouble();
            double z = result.get("z").asDouble();

            Objective objective = plugin.getStoryManager().getObjective(objectiveId);
            if (objective == null)
                return;

            RightClickBlock.RightClickBlockDataStore dataStore = new RightClickBlock.RightClickBlockDataStore(objective);
            dataStore.setId(id);
            dataStore.setAmount(amount);

            if (world != null) {
                Location location = new Location(Bukkit.getWorld(world), x, y, z);
                dataStore.setLocation(location);
            }

            ActiveAction activeAction = new ActiveAction(getAction(ActionType.RIGHT_CLICK_BLOCK), dataStore);
            addActiveAction(activeAction);
        });

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "citizen_id").from("action_right_click_citizen", result -> {
            int id = result.get("id").asInt();
            int objectiveId = result.get("objective_id").asInt();
            int amount = result.get("amount").asInt();
            int citizenId = result.get("citizen_id").asInt();

            Objective objective = plugin.getStoryManager().getObjective(objectiveId);
            if (objective == null)
                return;

            RightClickCitizen.RightClickCitizenDataStore dataStore = new RightClickCitizen.RightClickCitizenDataStore(objective);
            dataStore.setId(id);
            dataStore.setAmount(amount);
            dataStore.setCitizenId(citizenId);

            ActiveAction activeAction = new ActiveAction(getAction(ActionType.RIGHT_CLICK_CITIZEN), dataStore);
            addActiveAction(activeAction);
        });

        SQLSelect.create(ctx).select("id", "objective_id", "amount", "entity").from("action_right_click_entity", result -> {
            int id = result.get("id").asInt();
            int objectiveId = result.get("objective_id").asInt();
            int amount = result.get("amount").asInt();
            String entityUUID = result.get("entity").asString();

            Objective objective = plugin.getStoryManager().getObjective(objectiveId);
            if (objective == null)
                return;

            RightClickEntity.RightClickEntityDataStore dataStore = new RightClickEntity.RightClickEntityDataStore(objective);
            dataStore.setId(id);
            dataStore.setAmount(amount);
            dataStore.setEntity(UUID.fromString(entityUUID));

            ActiveAction activeAction = new ActiveAction(getAction(ActionType.RIGHT_CLICK_ENTITY), dataStore);
            addActiveAction(activeAction);
        });
    }

    @Override
    public void setupTables(DSLContext ctx) {

        ctx.createTableIfNotExists("action_drop_item")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("item", SQLDataType.BLOB(1000).nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("action_enter_region")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("pos1_world", SQLDataType.VARCHAR(36).nullable(true))
                .column("pos1_x", SQLDataType.DOUBLE.nullable(true))
                .column("pos1_y", SQLDataType.DOUBLE.nullable(true))
                .column("pos1_z", SQLDataType.DOUBLE.nullable(true))
                .column("pos2_world", SQLDataType.VARCHAR(36).nullable(true))
                .column("pos2_x", SQLDataType.DOUBLE.nullable(true))
                .column("pos2_y", SQLDataType.DOUBLE.nullable(true))
                .column("pos2_z", SQLDataType.DOUBLE.nullable(true))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("action_kill_entity_type")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("entity_type", SQLDataType.VARCHAR(255).nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("action_kill_mythic_mob")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("mob_type", SQLDataType.VARCHAR(255).nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("action_mine_block")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("material", SQLDataType.VARCHAR(255).nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("action_pickup_item")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("item", SQLDataType.BLOB(1000).nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("action_right_click_block")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("world", SQLDataType.VARCHAR(36).nullable(true))
                .column("x", SQLDataType.DOUBLE.nullable(true))
                .column("y", SQLDataType.DOUBLE.nullable(true))
                .column("z", SQLDataType.DOUBLE.nullable(true))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("action_right_click_citizen")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("citizen_id", SQLDataType.INTEGER.nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("action_right_click_entity")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("objective_id", SQLDataType.INTEGER.nullable(false))
                .column("amount", SQLDataType.INTEGER.nullable(false))
                .column("entity", SQLDataType.VARCHAR(36).nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();
    }
}
