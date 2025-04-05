package com.songoda.epicquests.story.player;

import com.songoda.core.data.LoadsData;
import com.songoda.core.data.SQLSelect;
import com.songoda.epicquests.story.Story;
import com.songoda.epicquests.story.StoryManager;
import com.songoda.epicquests.story.quest.ActiveQuest;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.third_party.org.jooq.DSLContext;
import com.songoda.third_party.org.jooq.impl.DSL;
import com.songoda.third_party.org.jooq.impl.SQLDataType;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class PlayerManager implements LoadsData {
    private final StoryManager storyManager;

    private final Map<UUID, StoryPlayer> registeredPlayers = new HashMap<>();

    public PlayerManager(StoryManager storyManager) {
        this.storyManager = storyManager;
    }

    public StoryPlayer getPlayer(OfflinePlayer player) {
        return this.registeredPlayers.computeIfAbsent(player.getUniqueId(), uuid -> new StoryPlayer(player.getUniqueId()));
    }

    public StoryPlayer getPlayer(UUID uniqueId) {
        return this.registeredPlayers.computeIfAbsent(uniqueId, uuid -> new StoryPlayer(uniqueId));
    }

    public List<StoryPlayer> getPlayers() {
        return new ArrayList<>(this.registeredPlayers.values());
    }


    public void discoverQuests(StoryPlayer contender) {
        for (Story story : this.storyManager.getStories()) {
            for (Quest quest : story.getEnabledQuests()) {
                if (contender.getCompletedQuests().stream().anyMatch(q -> quest.getId() == q)
                        || contender.getActiveQuests().stream().anyMatch(q -> q.getActiveQuest() == quest.getId())) {
                    continue;
                }
                if (contender.getCompletedQuests().containsAll(quest.getQuestPrerequisites())) {
                    contender.addActiveQuest(quest).save();
                }
            }
        }
    }

    @Override
    public void loadDataImpl(DSLContext ctx) {
        SQLSelect.create(ctx).select("id", "completed_quests").from("player", result -> {
            UUID id = UUID.fromString(result.get("id").asString());
            String completedQuests = result.get("completed_quests").asString();

            StoryPlayer player = getPlayer(id);
            for (String quest : completedQuests.split(",")) {
                player.addCompletedQuest(Integer.parseInt(quest));
            }

        });

        SQLSelect.create(ctx).select("player_id", "active_quest", "current_position", "actions").from("active_quest", result -> {
            UUID playerId = UUID.fromString(result.get("player_id").asString());
            int activeQuestId = result.get("active_quest").asInt();

            Quest quest = storyManager.getQuest(activeQuestId);
            if (quest == null)
                return;

            StoryPlayer player = getPlayer(playerId);
            if (player == null)
                return;

            int currentPosition = result.get("current_position").asInt();
            String remainingActionsString = result.get("actions").asString();

            System.out.println("currentPosition: " + currentPosition + ", remainingActionsString: " + remainingActionsString);
            Map<Integer, Integer> actions = new HashMap<>();
            if (remainingActionsString != null && !remainingActionsString.isEmpty()) {
                String[] actionPairs = remainingActionsString.split(", ");
                for (String actionPair : actionPairs) {
                    String[] actionData = actionPair.split(":");
                    int actionId = Integer.parseInt(actionData[0]);
                    int amount = Integer.parseInt(actionData[1]);
                    System.out.println("actionId: " + actionId + ", amount: " + amount);
                    actions.put(actionId, amount);
                }
            }

            ActiveQuest activeQuest = new ActiveQuest(playerId, quest);

            activeQuest.setCurrentPosition(currentPosition);
            activeQuest.setActions(actions);

            player.addActiveQuest(activeQuest);
        });
    }

    @Override
    public void setupTables(DSLContext ctx) {
        ctx.createTableIfNotExists("player")
                .column("id", SQLDataType.UUID.nullable(false))
                .column("completed_quests", SQLDataType.VARCHAR(512).nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("active_quest")
                .column("player_id", SQLDataType.UUID.nullable(false))
                .column("active_quest", SQLDataType.INTEGER.nullable(false))
                .column("current_position", SQLDataType.INTEGER.nullable(false))
                .column("focused", SQLDataType.BOOLEAN.nullable(false).defaultValue(false))
                .column("actions", SQLDataType.VARCHAR(1024))
                .constraint(DSL.constraint().primaryKey("player_id", "active_quest"))
                .execute();
    }
}
