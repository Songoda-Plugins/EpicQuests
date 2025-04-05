package com.songoda.epicquests.story.quest.action;

import com.songoda.third_party.com.cryptomorin.xseries.XSound;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.data.ActionDataStore;
import com.songoda.epicquests.dialog.Speech;
import com.songoda.epicquests.story.player.StoryPlayer;
import com.songoda.epicquests.story.quest.ActiveQuest;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.requirement.AbstractRequirement;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.List;

public abstract class AbstractAction {

    protected final EpicQuests plugin;
    private final ActionType type;
    private final boolean isSingleAmount;

    public AbstractAction(EpicQuests plugin, ActionType type, boolean isSingleAmount) {
        this.plugin = plugin;
        this.type = type;
        this.isSingleAmount = isSingleAmount;
    }

    public abstract List<String> getDescription(ActionDataStore actionDataStore);

    public abstract ActiveAction setup(Player player, Objective objective);

    public void onInteract(PlayerInteractEvent event, ActiveAction activeAction) {
    }

    public boolean onInteractWithEntity(PlayerInteractAtEntityEvent event, ActiveAction activeAction) {
        return false;
    }

    public void onPickup(PlayerPickupItemEvent event, ActiveAction activeAction) {
    }

    public void onDrop(PlayerDropItemEvent event, ActiveAction activeAction) {
    }

    public void onEntityKill(EntityDeathEvent event, ActiveAction activeAction) {
    }

    public void onBlockBreak(BlockBreakEvent event, ActiveAction action) {
    }

    public void moveTick(Player player, ActiveAction action) {
    }

    protected boolean performAction(ActiveAction activeAction, int amount, Player player) {
        StoryPlayer storyPlayer = this.plugin.getPlayerManager().getPlayer(player);
        for (ActiveQuest activeQuest : storyPlayer.getActiveQuests()) {
            if (activeQuest.getActiveQuest() != activeAction.getObjective().getQuest().getId()) {
                continue;
            }

            if (activeAction.getObjective() == null) {
                this.plugin.getActionManager().removeActiveAction(activeAction);
                continue;
            }

            // Check if the active action's objective start position matches the current position of the active quest
            if (activeQuest.getCurrentPosition() != activeAction.getObjective().getStartPosition()) {
                continue;
            }

            List<AbstractRequirement> requirements = activeAction.getObjective().getRequirements();
            for (AbstractRequirement requirement : requirements) {
                if (!requirement.isMet(player)) {
                    requirement.reject(player);
                    return false;
                }
            }

            for (AbstractRequirement requirement : requirements)
                requirement.execute(player);

            Speech speech = this.plugin.getDialogManager().getSpeech(activeAction.getObjective().getAttachedSpeech());
            if (activeQuest.completeAction(activeAction, amount)) {
                if (speech != null) {
                    speech.sendMessages(player, speech.getDialog().getCitizen());
                }
                // Update the current position to the next objective
                activeQuest.setCurrentPosition(activeAction.getObjective().getEndPosition());

                if (activeQuest.getCurrentPosition()
                        == activeAction.getObjective().getQuest().getEndingPosition()) {
                    plugin.getLocale().getMessage("event.quest.complete")
                            .processPlaceholder("name", activeAction.getObjective().getQuest().getName())
                            .sendPrefixedMessage(player);
                } else {
                    XSound.ITEM_BOOK_PAGE_TURN.play(player);
                    plugin.getLocale().getMessage("event.objective.complete")
                            .sendPrefixedMessage(player);
                }

                activeQuest.clearActions();
                activeQuest.save("current_position", "actions");
                return true;
            }

            storyPlayer.toggleAllFocusedOff();
            activeQuest.setFocused(true);
            activeQuest.save();
            return false;
        }
        return false;
    }

    public boolean isSingleAmount() {
        return this.isSingleAmount;
    }

    public ActionType getType() {
        return this.type;
    }
}
