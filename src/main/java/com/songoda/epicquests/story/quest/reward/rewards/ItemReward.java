package com.songoda.epicquests.story.quest.reward.rewards;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.gui.Gui;
import com.songoda.core.utils.ItemSerializer;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.gui.GuiItems;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.epicquests.story.quest.reward.AbstractReward;
import com.songoda.epicquests.story.quest.reward.RewardType;
import com.songoda.epicquests.utils.ItemHolder;
import com.songoda.third_party.org.jooq.DSLContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ItemReward extends AbstractReward implements ItemHolder {
    private final EpicQuests plugin;

    private final List<ItemStack> items = new ArrayList<>();

    public ItemReward(Quest quest) {
        super(quest);
        this.plugin = JavaPlugin.getPlugin(EpicQuests.class);
    }

    @Override
    public RewardType getType() {
        return RewardType.ITEM;
    }

    @Override
    public void setup(Player player, Gui back, Runnable callback, Runnable onDelete) {
        this.plugin.getGuiManager().showGUI(player, new GuiItems(this.plugin, player, this, back, callback, onDelete));
    }

    @Override
    public void give(Player player) {
        Map<Integer, ItemStack> overfilled = player.getInventory().addItem(this.items.toArray(new ItemStack[0]));
        for (ItemStack item : overfilled.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public void clearItems() {
        this.items.clear();
    }

    public void addItem(ItemStack item) {
        this.items.add(item);
    }

    public void removeItem(ItemStack item) {
        this.items.remove(item);
    }

    @Override
    public void saveImpl(DSLContext ctx, String... columns) {
        SQLInsert.create(ctx).insertInto("item_reward")
                .withField("id", id, id == -1)
                .withField("items", ItemSerializer.toBase64(items))
                .withField("quest", this.getQuest().getId())
                .onDuplicateKeyUpdate(columns)
                .execute();

        if (id == -1)
            this.id = lastInsertedId("item_reward", ctx);
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("item_reward", "id", id);
    }
}
