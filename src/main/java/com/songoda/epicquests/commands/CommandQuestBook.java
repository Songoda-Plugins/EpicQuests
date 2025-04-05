package com.songoda.epicquests.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.gui.GuiBook;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandQuestBook extends AbstractCommand {
    private final EpicQuests plugin;

    public CommandQuestBook(EpicQuests plugin) {
        super(CommandType.PLAYER_ONLY, "book");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.plugin.getGuiManager().showGUI((Player) sender, new GuiBook(this.plugin, (Player) sender));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicquests.book";
    }

    @Override
    public String getSyntax() {
        return "book";
    }

    @Override
    public String getDescription() {
        return "Open the quest book.";
    }
}
