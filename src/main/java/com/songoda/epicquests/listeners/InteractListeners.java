package com.songoda.epicquests.listeners;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.Region.ActiveSelection;
import com.songoda.epicquests.dialog.Dialog;
import com.songoda.epicquests.gui.GuiDialogs;
import com.songoda.epicquests.story.player.StoryPlayer;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InteractListeners implements Listener {
    private final EpicQuests plugin;

    public InteractListeners(EpicQuests plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)
                && event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = event.getPlayer();
        ActiveSelection activeSelection = this.plugin.getSelectionManager().getActiveSelection(player);

        if (activeSelection != null && event.hasBlock()) {
            activeSelection.commit(player, event.getClickedBlock().getLocation(), this.plugin);
            event.setCancelled(true);
            return;
        }

        for (ActiveAction action : this.plugin.getActionManager().getActiveActions()) {
            action.getAction().onInteract(event, action);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)
                && event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        boolean skipDefault = false;
        for (ActiveAction action : this.plugin.getActionManager().getActiveActions()) {
            if (action.getAction().onInteractWithEntity(event, action)) {
                System.out.println("Skipping default interaction2");
                skipDefault = true;
            }
        }

        if (skipDefault)
            return;

        StoryPlayer storyPlayer = this.plugin.getPlayerManager().getPlayer(event.getPlayer());
        Entity rightClicked = event.getRightClicked();
        if (CitizensAPI.getNPCRegistry().isNPC(rightClicked)) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(rightClicked);
            if (npc == null) {
                return;
            }
            if (storyPlayer.isInDialogCreation()) {
                Dialog dialog = new Dialog(npc.getId());
                dialog.save(() -> {
                    plugin.getDialogManager().addDialog(dialog);
                    plugin.getGuiManager().showGUI(event.getPlayer(), new GuiDialogs(this.plugin, event.getPlayer(), null));
                });
                storyPlayer.setInDialogCreation(false);
                return;
            }
            System.out.println("Interacted with NPC: " + npc.getId());
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                Dialog dialog = this.plugin.getDialogManager().getDialogByCitizenId(npc.getId());
                System.out.println("Dialog: " + dialog);
                if (dialog != null) {
                    dialog.sendMessages(event.getPlayer(), storyPlayer);
                }
            }, 10L);
        }
    }
}
