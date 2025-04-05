package com.songoda.epicquests.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.gui.GuiMain;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandAdmin extends AbstractCommand {
    private final EpicQuests plugin;

    public CommandAdmin(EpicQuests plugin) {
        super(CommandType.PLAYER_ONLY, "admin");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.plugin.getGuiManager().showGUI((Player) sender, new GuiMain(this.plugin, (Player) sender));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicquests.admin";
    }

    @Override
    public String getSyntax() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Open the admin panel.";
    }
}
