package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.utils.ItemHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GuiItems extends Gui {
    private EpicQuests plugin;
    private Player player;
    private final ItemHolder holder;
    private final Gui back;
    private final Runnable callback;
    private final Runnable onDelete;

    public GuiItems(EpicQuests plugin, Player player, ItemHolder holder, Gui back, Runnable callback, Runnable onDelete) {
        this.holder = holder;
        this.back = back;
        this.callback = callback;
        this.onDelete = onDelete;
        init(plugin, player);
    }

    public void init(EpicQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        setRows(6);
        setDefaultItem(null);
        setAcceptsItems(true);
        setTitle("Item");
        setOnClose((event) -> {
        });
        setUnlockedRange(1, 0, 6, 9);
        show();
    }

    public void save() {
        this.holder.clearItems();
        for (int i = 9; i < this.inventory.getSize(); i++) {
            ItemStack itemStack = getItem(i);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                this.holder.addItem(itemStack);
            }
        }
    }

    public void show() {
        reset();

        if (onDelete != null)
            setButton(0, 0, GuiUtils.createButtonItem(XMaterial.REDSTONE_BLOCK, "Delete"),
                    (event) -> {
                        onDelete.run();
                    });

        setButton(0, 7, GuiUtils.createButtonItem(XMaterial.EMERALD_BLOCK, "Confirm"),
                (event) -> {
                    save();
                    callback.run();
                });

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> guiManager.showGUI(player, back));

        List<ItemStack> items = this.holder.getItems();
        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);
            setItem(i + 9, itemStack);
        }
    }
}
