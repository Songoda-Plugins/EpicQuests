package com.songoda.epicquests.story;

import com.songoda.core.data.LoadsData;
import com.songoda.core.data.SQLSelect;
import com.songoda.core.utils.ItemSerializer;
import com.songoda.epicquests.Region.Region;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.epicquests.story.quest.requirement.AbstractRequirement;
import com.songoda.epicquests.story.quest.requirement.requirements.EquipRequirement;
import com.songoda.epicquests.story.quest.requirement.requirements.ItemRequirement;
import com.songoda.epicquests.story.quest.reward.AbstractReward;
import com.songoda.epicquests.story.quest.reward.rewards.CommandReward;
import com.songoda.epicquests.story.quest.reward.rewards.ItemReward;
import com.songoda.epicquests.story.quest.reward.rewards.XpReward;
import com.songoda.third_party.org.jooq.DSLContext;
import com.songoda.third_party.org.jooq.impl.DSL;
import com.songoda.third_party.org.jooq.impl.SQLDataType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class StoryManager implements LoadsData {
    private final List<Story> stories = new LinkedList<>();
    public Story addStory(Story story) {
        for (Quest quest : story.getQuests()) {
            quest.setStory(story);
            for (AbstractReward reward : quest.getRewards()) {
                reward.setQuest(quest);
            }
            for (Objective objective : quest.getObjectives()) {
                for (AbstractRequirement requirement : objective.getRequirements()) {
                    requirement.setObjective(objective);
                }
                objective.setQuest(quest);
            }
        }
        this.stories.add(story);
        return story;
    }

    public void removeStory(Story story) {
        this.stories.remove(story);
    }

    public List<Story> getStories() {
        return Collections.unmodifiableList(this.stories);
    }

    public List<Quest> getQuests() {
        List<Quest> quests = new ArrayList<>();
        for (Story story : this.stories) {
            quests.addAll(story.getQuests());
        }
        return quests;
    }

    public Quest getQuest(int activeQuest) {
        for (Quest quest : getQuests()) {
            if (activeQuest == quest.getId()) {
                return quest;
            }
        }
        return null;
    }

    public Quest getEnabledQuest(int activeQuest) {
        for (Quest quest : getQuests()) {
            if (activeQuest == quest.getId() && quest.isActive()) {
                return quest;
            }
        }
        return null;
    }

    public Objective getObjective(int id) {
        for (Quest quest : getQuests()) {
            for (Objective objective : quest.getObjectives()) {
                if (objective.getId() == id) {
                    return objective;
                }
            }
        }
        return null;
    }

    @Override
    public void loadDataImpl(DSLContext ctx) {

        SQLSelect.create(ctx).select("id", "name", "active").from("story", result -> {
            Story story = new Story();
            story.setId(result.get("id").asInt());
            story.setName(result.get("name").asString());
            story.setActive(result.get("active").asBoolean());
            this.addStory(story);
        });

        SQLSelect.create(ctx).select("id", "story_id", "name", "active", "ordered", "quest_prerequisites", "region").from("quest", result -> {
            Story story = this.getStories().stream().filter(s -> s.getId() == result.get("story_id").asInt()).findFirst().orElse(null);
            if (story == null)
                return;

            Quest quest = new Quest(story);
            quest.setId(result.get("id").asInt());
            quest.setName(result.get("name").asString());
            quest.setActive(result.get("active").asBoolean());
            quest.setOrdered(result.get("ordered").asBoolean());
            String preReqs = result.get("quest_prerequisites").asString();
            if (preReqs != null && !preReqs.isEmpty())
                quest.setQuestPrerequisites(Arrays.stream(preReqs.split(",")).map(Integer::parseInt).collect(Collectors.toList()));
            String region = result.get("region").asString();
            if (region != null && !region.isEmpty())
                quest.setRegion(Region.deserialize(region));
            story.addQuest(quest);
        });

        SQLSelect.create(ctx).select("id", "quest_id", "title", "visible", "attached_speech", "start_position", "end_position").from("objective", result -> {
            Quest quest = getQuests().stream()
                    .filter(q -> q.getId() == result.get("quest_id").asInt()).findFirst().orElse(null);
            if (quest == null) {
                System.out.println("Objective without quest (id: " + result.get("quest_id").asInt() + ")");
                return;
            }

            Objective objective = new Objective(quest);
            objective.setId(result.get("id").asInt());
            objective.setTitle(result.get("title").asString());
            objective.setVisible(result.get("visible").asBoolean());
            objective.setAttachedSpeech(result.get("attached_speech").asInt());
            objective.setStartPosition(result.get("start_position").asInt());
            objective.setEndPosition(result.get("end_position").asInt());
            quest.addObjective(objective);
        });

        // Requirements

        SQLSelect.create(ctx).select("id", "objective", "items", "reject").from("item_requirement", result -> {
            Objective objective = this.getQuests().stream().flatMap(q -> q.getObjectives().stream())
                    .filter(o -> o.getId() == result.get("objective").asInt()).findFirst().orElse(null);
            System.out.println("Objective: " + objective);

            if (objective == null)
                return;

            ItemRequirement requirement = new ItemRequirement(objective);

            requirement.setId(result.get("id").asInt());
            requirement.setReject(result.get("reject").asInt());
            String items = result.get("items").asString();
            if (items != null && !items.isEmpty())
                for (ItemStack item : ItemSerializer.fromBase64(items))
                    requirement.addItem(item);
            objective.addRequirement(requirement);
        });

        SQLSelect.create(ctx).select("id", "objective", "items", "reject").from("equip_requirement", result -> {
            Objective objective = this.getQuests().stream().flatMap(q -> q.getObjectives().stream())
                    .filter(o -> o.getId() == result.get("objective").asInt()).findFirst().orElse(null);
            if (objective == null)
                return;

            EquipRequirement requirement = new EquipRequirement(objective);

            requirement.setId(result.get("id").asInt());
            requirement.setReject(result.get("reject").asInt());
            String items = result.get("items").asString();
            if (items != null && !items.isEmpty())
                for (ItemStack item : ItemSerializer.fromBase64(items))
                    requirement.addItem(item);
            objective.addRequirement(requirement);
        });

        // Rewards

        SQLSelect.create(ctx).select("id", "commands", "quest").from("command_reward", result -> {
            Quest quest = this.getQuests().stream().filter(q -> q.getId() == result.get("quest").asInt()).findFirst().orElse(null);
            if (quest == null)
                return;

            CommandReward reward = new CommandReward(quest);
            reward.setId(result.get("id").asInt());

            String encodedCommands = result.get("commands").asString();
            if (encodedCommands != null && !encodedCommands.isEmpty()) {
                String decodedCommands = new String(Base64.getDecoder().decode(encodedCommands));
                String[] commands = decodedCommands.split("\n");
                for (String command : commands) {
                    reward.addCommand(command);
                }
            }

            quest.addReward(reward);
        });

        SQLSelect.create(ctx).select("id", "items", "quest").from("item_reward", result -> {
            Quest quest = this.getQuests().stream().filter(q -> q.getId() == result.get("quest").asInt()).findFirst().orElse(null);
            if (quest == null)
                return;

            ItemReward reward = new ItemReward(quest);
            reward.setId(result.get("id").asInt());

            String encodedItems = result.get("items").asString();
            if (encodedItems != null && !encodedItems.isEmpty()) {
                List<ItemStack> items = ItemSerializer.fromBase64(encodedItems);
                for (ItemStack item : items)
                    reward.addItem(item);
            }

            quest.addReward(reward);
        });

        SQLSelect.create(ctx).select("id", "xp", "quest").from("xp_reward", result -> {
            Quest quest = this.getQuests().stream().filter(q -> q.getId() == result.get("quest").asInt()).findFirst().orElse(null);
            if (quest == null)
                return;

            XpReward reward = new XpReward(quest);
            reward.setId(result.get("id").asInt());
            reward.setXp(result.get("xp").asInt());

            quest.addReward(reward);
        });
    }

    @Override
    public void setupTables(DSLContext ctx) {

        ctx.createTableIfNotExists("story")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("name", SQLDataType.VARCHAR(255).nullable(false))
                .column("active", SQLDataType.BOOLEAN.nullable(false).defaultValue(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("quest")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("story_id", SQLDataType.INTEGER.nullable(false))
                .column("name", SQLDataType.VARCHAR(255).nullable(false))
                .column("active", SQLDataType.BOOLEAN.nullable(false).defaultValue(false))
                .column("ordered", SQLDataType.BOOLEAN.nullable(false).defaultValue(false))
                .column("quest_prerequisites", SQLDataType.VARCHAR(255))
                .column("region", SQLDataType.VARCHAR(255))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("objective")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("quest_id", SQLDataType.INTEGER.nullable(false))
                .column("title", SQLDataType.VARCHAR(255).nullable(false))
                .column("visible", SQLDataType.BOOLEAN.nullable(false).defaultValue(false))
                .column("attached_speech", SQLDataType.INTEGER.nullable(false).defaultValue(-1))
                .column("start_position", SQLDataType.INTEGER.nullable(false).defaultValue(0))
                .column("end_position", SQLDataType.INTEGER.nullable(false).defaultValue(0))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        // Requirements

        ctx.createTableIfNotExists("item_requirement")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("items", SQLDataType.VARCHAR(1000).nullable(false))
                .column("objective", SQLDataType.INTEGER.nullable(false))
                .column("reject", SQLDataType.INTEGER.nullable(false).defaultValue(-1))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("equip_requirement")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("items", SQLDataType.VARCHAR(1000).nullable(false))
                .column("objective", SQLDataType.INTEGER.nullable(false))
                .column("reject", SQLDataType.INTEGER.nullable(false).defaultValue(-1))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        // Rewards

        ctx.createTableIfNotExists("command_reward")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("commands", SQLDataType.VARCHAR(1000).nullable(false))
                .column("quest", SQLDataType.INTEGER.nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("item_reward")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("items", SQLDataType.VARCHAR(1000).nullable(false))
                .column("quest", SQLDataType.INTEGER.nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("xp_reward")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("xp", SQLDataType.INTEGER.nullable(false))
                .column("quest", SQLDataType.INTEGER.nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

    }
}
