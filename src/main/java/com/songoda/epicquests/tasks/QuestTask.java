package com.songoda.epicquests.tasks;

import com.songoda.core.compatibility.CompatibleParticleHandler;
import com.songoda.core.utils.LocationUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.Region.Region;
import com.songoda.epicquests.settings.Settings;
import com.songoda.epicquests.story.player.PlayerManager;
import com.songoda.epicquests.story.player.StoryPlayer;
import com.songoda.epicquests.story.quest.ActiveQuest;
import com.songoda.epicquests.story.quest.Objective;
import com.songoda.epicquests.story.quest.Quest;
import com.songoda.epicquests.story.quest.action.ActiveAction;
import com.songoda.epicquests.story.quest.action.actions.RightClickBlock;
import com.songoda.epicquests.story.quest.action.actions.RightClickCitizen;
import com.songoda.third_party.com.cryptomorin.xseries.XSound;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class QuestTask extends BukkitRunnable {
    private static QuestTask instance;
    private static EpicQuests plugin;
    private static PlayerManager playerManager;

    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();

    public QuestTask(EpicQuests plug) {
        plugin = plug;
    }

    public static QuestTask startTask(EpicQuests plug) {
        plugin = plug;
        playerManager = plugin.getPlayerManager();
        if (instance == null) {
            instance = new QuestTask(plugin);
            instance.runTaskTimerAsynchronously(plugin, 20, 5);
        }

        return instance;
    }

    @Override
    public void run() {
        if (plugin.getStoryManager().getQuests().stream().noneMatch(Quest::isActive))
            return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            StoryPlayer storyPlayer = playerManager.getPlayer(player.getUniqueId());
            plugin.getPlayerManager().discoverQuests(storyPlayer);

            boolean updatedBossBar = false;

            List<ActiveQuest> active = storyPlayer.getActiveQuests();
            for (ActiveQuest activeQuest : new HashSet<>(active).stream()
                    .sorted(Comparator.comparing(q -> !q.isFocused()))
                    .sorted(Comparator.comparing(q -> {
                        Region region = plugin.getStoryManager()
                                .getEnabledQuest(q.getActiveQuest()).getRegion();
                        return region == null
                                || !LocationUtils.isInArea(player.getLocation(), region.getPos1(), region.getPos2());
                    }))
                    .collect(Collectors.toCollection(LinkedHashSet::new))) {
                if (activeQuest == null) {
                    continue;
                }
                Quest quest = plugin.getStoryManager().getEnabledQuest(activeQuest.getActiveQuest());

                if (quest == null || quest.getObjectives() == null || quest.getObjectives().size() == 0) {
                    continue;
                }

                if (activeQuest.getCurrentPosition() == quest.getEndingPosition()) {
                    XSound.UI_TOAST_CHALLENGE_COMPLETE.play(player);
                    storyPlayer.completeQuest(quest);
                    storyPlayer.save("completed_quests");

                    storyPlayer.removeActiveQuest(activeQuest);
                    activeQuest.delete();

                    quest.giveRewards(player);
                    continue;
                }
                if (updatedBossBar) {
                    continue;
                }
                updatedBossBar = true;

                Objective currentObjective = quest.getObjective(activeQuest.getCurrentPosition());

                List<ActiveAction> activeActions = plugin.getActionManager().getActiveActionsByObjective(currentObjective);

                if (activeActions.isEmpty()) {
                    continue;
                }

                int current = activeQuest.getTotalAmount();
                int goal = activeActions.stream().mapToInt(ActiveAction::getAmount).sum();

                ActiveAction action = activeActions.get(0);
                // Effects
                if (action.getAction() instanceof RightClickCitizen) {
                    Location location = CitizensAPI.getNPCRegistry()
                            .getById(((RightClickCitizen.RightClickCitizenDataStore) action
                                    .getActionDataStore()).getCitizenId()).getStoredLocation();
                    if (player.getWorld() == location.getWorld()) {
                        CompatibleParticleHandler.redstoneParticles(location
                                        .add(0, 2.5, 0),
                                255, 85, 255, 1, 5, .1f, player);
                    }
                } else if (action.getAction() instanceof RightClickBlock) {
                    Location location = (((RightClickBlock.RightClickBlockDataStore) action
                            .getActionDataStore()).getLocation());
                    if (player.getWorld() == location.getWorld()) {
                        CompatibleParticleHandler.redstoneParticles(location.add(.5, .5, .5),
                                255, 85, 255, 1, 10, .5f, player);
                    }
                }

                String title = TextUtils.formatText(plugin.getLocale().getMessage("general.bossbar.objective").getMessage()
                        + currentObjective.getTitle());
                BossBar bossBar = this.activeBossBars.computeIfAbsent(player.getUniqueId(),
                        uuid -> Bukkit.createBossBar(title,
                                BarColor.valueOf(Settings.BOSS_BAR_COLOR.getString()),
                                BarStyle.SOLID));

                boolean silent = storyPlayer.isSilent();

                if (currentObjective.isVisible() && !silent) {
                    bossBar.addPlayer(player);
                } else {
                    bossBar.removePlayer(player);
                    this.activeBossBars.remove(player.getUniqueId());
                    bossBar.removeAll();
                }
                if (goal == 1 || goal == 0) {
                    bossBar.setTitle(title);
                    bossBar.setProgress(1);
                } else {
                    bossBar.setTitle(plugin.getLocale().getMessage("general.bossbar.amount")
                            .processPlaceholder("title", title)
                            .processPlaceholder("current", current)
                            .processPlaceholder("goal", goal).toText());
                    bossBar.setProgress((double) current / goal);
                }
            }

            if (!updatedBossBar && this.activeBossBars.containsKey(player.getUniqueId())) {
                BossBar bossBar = this.activeBossBars.remove(player.getUniqueId());
                bossBar.removeAll();
            }
        }
    }

    public void flush() {
        for (BossBar bossBar : this.activeBossBars.values()) {
            bossBar.removeAll();
        }
    }

    public void remove(Player player) {
        BossBar bossBar = this.activeBossBars.get(player.getUniqueId());
        if (bossBar == null) {
            return;
        }
        bossBar.removePlayer(player);
    }

}
