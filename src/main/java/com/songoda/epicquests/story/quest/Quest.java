package com.songoda.epicquests.story.quest;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.data.SavesData;
import com.songoda.epicquests.Region.Region;
import com.songoda.epicquests.story.Story;
import com.songoda.epicquests.story.quest.reward.AbstractReward;
import com.songoda.third_party.org.jooq.DSLContext;
import org.bukkit.entity.Player;

import java.util.*;

public class Quest implements SavesData {

    private  int id = -1;
    private transient Story story;
    private String name = "Unnamed Quest";
    private final List<Integer> questPrerequisites = new LinkedList<>();
    private final List<AbstractReward> rewards = new LinkedList<>();

    private Region region;

    private boolean active = false;
    private boolean ordered = false;

    private final Set<Objective> objectives = new LinkedHashSet<>();

    public Quest(Story story) {
        this.story = story;
    }

    public Objective addObjective(Objective objective) {
        this.objectives.add(objective);
        return objective;
    }

    public Objective getObjective(int position) {
        return objectives.stream().sorted(Comparator.comparingInt(Objective::getStartPosition))
                .skip(position).findFirst().orElse(null);
    }

    public void removeObjective(Objective objective) {
        this.objectives.remove(objective);
    }

    public Set<Objective> getObjectives() {
        return Collections.unmodifiableSet(objectives);
    }

    public Story getStory() {
        return this.story;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public void addQuestPrerequisite(Quest quest) {
        addQuestPrerequisite(quest.getId());
    }

    public void addQuestPrerequisite(int quest) {
        this.questPrerequisites.add(quest);
    }

    public void removeQuestPrerequisite(Quest quest) {
        removeQuestPrerequisite(quest.getId());
    }

    public void removeQuestPrerequisite(int quest) {
        this.questPrerequisites.remove(quest);
    }

    public List<Integer> getQuestPrerequisites() {
        return Collections.unmodifiableList(this.questPrerequisites);
    }

    public void addReward(AbstractReward reward) {
        this.rewards.add(reward);
    }

    public void removeReward(AbstractReward reward) {
        this.rewards.remove(reward);
    }

    public List<AbstractReward> getRewards() {
        return Collections.unmodifiableList(this.rewards);
    }

    public void giveRewards(Player player) {
        for (AbstractReward reward : this.rewards) {
            reward.give(player);
        }
    }

    public boolean isOrdered() {
        return this.ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public Region getRegion() {
        return this.region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void clearRegion() {
        this.region = null;
    }

    public void setQuestPrerequisites(List<Integer> questPrerequisites) {
        this.questPrerequisites.clear();
        this.questPrerequisites.addAll(questPrerequisites);
    }

    @Override
    public void saveImpl(DSLContext dslContext, String... strings) {

        SQLInsert.create(dslContext).insertInto("quest")
                .withField("id", id, id == -1)
                .withField("story_id", story.getId())
                .withField("name", name)
                .withField("active", active)
                .withField("ordered", ordered)
                .withField("quest_prerequisites", questPrerequisites.isEmpty() ? null : questPrerequisites.stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElse(""))
                .withField("region", region == null ? null : region.serialize())
                .onDuplicateKeyUpdate(strings)
                .execute();

        if (id == -1) {
            this.id = lastInsertedId("quest", dslContext);
        }

    }

    @Override
    public void deleteImpl(DSLContext dslContext) {
        SQLDelete.create(dslContext).delete("quest", "id", id);
        for (Objective objective : objectives)
            objective.delete();
        for (AbstractReward reward : rewards)
            reward.delete();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quest quest = (Quest) o;
        return id == quest.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getEndingPosition() {
        return objectives.stream().mapToInt(Objective::getEndPosition).max().orElse(0);
    }
}
