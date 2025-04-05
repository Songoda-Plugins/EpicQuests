package com.songoda.epicquests;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.configuration.Config;
import com.songoda.core.data.DatabaseManager;
import com.songoda.core.gui.GuiManager;
import com.songoda.epicquests.Region.SelectionManager;
import com.songoda.epicquests.dialog.DialogManager;
import com.songoda.epicquests.listeners.BlockListeners;
import com.songoda.epicquests.listeners.EntityListeners;
import com.songoda.epicquests.listeners.InteractListeners;
import com.songoda.epicquests.listeners.ItemListeners;
import com.songoda.epicquests.settings.Settings;
import com.songoda.epicquests.story.StoryManager;
import com.songoda.epicquests.story.player.PlayerManager;
import com.songoda.epicquests.story.quest.action.ActionManager;
import com.songoda.epicquests.tasks.QuestTask;
import com.songoda.epicquests.tasks.RegionTask;
import com.songoda.epicquests.tasks.VisualizeTask;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.commands.CommandAdmin;
import com.songoda.epicquests.commands.CommandQuestBook;
import com.songoda.epicquests.commands.CommandResetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public class EpicQuests extends SongodaPlugin {
    private final GuiManager guiManager = new GuiManager(this);
    private StoryManager storyManager;
    private PlayerManager playerManager;
    private ActionManager actionManager;
    private CommandManager commandManager;
    private DialogManager dialogManager;
    private SelectionManager selectionManager;

    private DatabaseManager databaseManager;

    private QuestTask questTask;

    /**
     * @deprecated Use {@link #getPlugin(Class)} instead
     */
    @Deprecated
    public static EpicQuests getInstance() {
        return getPlugin(EpicQuests.class);
    }

    @Override
    public void onPluginLoad() {
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 364, XMaterial.COMPASS);

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        databaseManager = new DatabaseManager(this);

        // Init Managers
        this.storyManager = new StoryManager();
        this.playerManager = new PlayerManager(this.storyManager);
        this.actionManager = new ActionManager(this);
        this.dialogManager = new DialogManager();
        this.selectionManager = new SelectionManager();

        // Init Commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addMainCommand("EpicQuests")
                .addSubCommands(new CommandAdmin(this),
                        new CommandResetPlayer(this),
                        new CommandResetPlayer(this),
                        new CommandQuestBook(this));

        // Init Listeners
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new ItemListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new BlockListeners(this), this);

        // Start Tasks
        this.questTask = QuestTask.startTask(this);
        RegionTask.startTask(this);
        VisualizeTask.startTask(this);

        databaseManager.load("Story", () -> storyManager.loadData());
        databaseManager.load("Action", () -> actionManager.loadData());
        databaseManager.load("Player", () -> playerManager.loadData());
        databaseManager.load("Dialog", () -> dialogManager.loadData());
    }

    @Override
    public void onPluginDisable() {
        this.questTask.flush();
    }

    @Override
    public void onDataLoad() {
    }

    @Override
    public void onConfigReload() {
    }

    @Override
    public List<Config> getExtraConfig() {
        return null;
    }

    public SelectionManager getSelectionManager() {
        return this.selectionManager;
    }

    public StoryManager getStoryManager() {
        return this.storyManager;
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public ActionManager getActionManager() {
        return this.actionManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public GuiManager getGuiManager() {
        return this.guiManager;
    }

    public DialogManager getDialogManager() {
        return this.dialogManager;
    }

    public QuestTask getQuestTask() {
        return this.questTask;
    }
}
