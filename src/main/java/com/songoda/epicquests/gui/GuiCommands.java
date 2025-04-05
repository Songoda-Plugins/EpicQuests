package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.story.quest.reward.rewards.CommandReward;
import com.songoda.epicquests.utils.TextUtils;
import org.bukkit.entity.Player;

import java.util.List;

public class GuiCommands extends Gui {
    private EpicQuests plugin;
    private Player player;
    private final CommandReward reward;
    private final Runnable callback;
    private final Runnable onDelete;
    private final Gui back;

    public GuiCommands(EpicQuests plugin, Player player, CommandReward reward, Gui back, Runnable callback, Runnable onDelete) {
        this.reward = reward;
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
        setTitle("Commands");
        show();
    }

    public void show() {
        reset();

        setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Add command"),
                (event) -> ChatPrompt.showPrompt(this.plugin, this.player,
                                "Enter a Command. (You can use: @p)",
                                response -> this.reward.addCommand(response.getMessage()))
                        .setOnClose(() -> this.guiManager.showGUI(this.player, this)));

        setButton(0, 7, GuiUtils.createButtonItem(XMaterial.EMERALD_BLOCK, "Confirm"),
                (event) -> {
                    reward.save();
                    callback.run();
                });

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> {
                    guiManager.showGUI(player, back);
                });

        List<String> commands = this.reward.getCommands();
        for (int i = 0; i < commands.size(); i++) {
            String command = commands.get(i);
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, TextUtils.condense(command)),
                    (event) -> {
                        this.reward.removeCommand(command);
                        show();
                    });
        }
    }
}
