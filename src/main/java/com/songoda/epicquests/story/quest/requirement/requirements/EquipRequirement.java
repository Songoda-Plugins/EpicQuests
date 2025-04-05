package com.songoda.epicquests.story.quest.requirement.requirements;

import com.songoda.core.data.SQLDelete;
import com.songoda.core.data.SQLInsert;
import com.songoda.core.gui.Gui;
import com.songoda.core.utils.ItemSerializer;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.gui.GuiItems;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.requirement.AbstractRequirement;
import com.songoda.epicquests.story.quest.requirement.RequirementType;
import com.songoda.epicquests.utils.ItemHolder;
import com.songoda.third_party.org.jooq.DSLContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EquipRequirement extends AbstractRequirement implements ItemHolder {
    private final EpicQuests plugin;

    private final List<ItemStack> items = new ArrayList<>();

    public EquipRequirement(Objective objective) {
        super(objective);
        this.plugin = JavaPlugin.getPlugin(EpicQuests.class);
    }

    @Override
    public RequirementType getType() {
        return RequirementType.EQUIP;
    }

    @Override
    public boolean isMet(Player player) {
        if (player.getEquipment() == null) {
            return false;
        }

        top:
        for (ItemStack item : this.items) {
            ItemStack[] contents = player.getEquipment().getArmorContents();
            for (ItemStack it : contents) {
                if (it.isSimilar(item)) {
                    continue top;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void execute(Player player) {
        for (ItemStack itemStack : this.items) {
            player.getInventory().removeItem(itemStack);
        }
    }

    @Override
    public void setup(Player player, Gui back, Runnable callback, Runnable onDelete) {
        this.plugin.getGuiManager().showGUI(player, new GuiItems(this.plugin, player, this, back, callback, onDelete));
    }

    @Override
    public String getDescription() {
        return this.items.stream().map(item -> "&7- &6" + item.getAmount() + "x " + item.getType().name()).collect(Collectors.joining("\n"));
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
        SQLInsert.create(ctx).insertInto("equip_requirement")
                .withField("id", id, id == -1)
                .withField("items", ItemSerializer.toBase64(items))
                .withField("objective", this.objective.getId())
                .withField("reject", this.reject)
                .onDuplicateKeyUpdate(columns)
                .execute();

        if (id == -1)
            this.id = lastInsertedId("equip_requirement", ctx);
    }

    @Override
    public void deleteImpl(DSLContext ctx) {
        SQLDelete.create(ctx).delete("equip_requirement", "id", id);
    }
}
