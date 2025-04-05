package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicquests.dialog.Speech;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.dialog.Dialog;
import com.songoda.epicquests.dialog.DialogManager;
import com.songoda.epicquests.utils.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiDialogs extends Gui {
    private final EpicQuests plugin;
    private final DialogManager dialogManager;
    private final Player player;
    private final Consumer<Speech> attachedSpeech;

    public GuiDialogs(EpicQuests plugin, Player player, Consumer<Speech> attachedSpeech) {
        this.plugin = plugin;
        this.dialogManager = plugin.getDialogManager();
        this.player = player;
        this.attachedSpeech = attachedSpeech;
        setRows(6);
        setDefaultItem(null);

        setTitle("Dialog");

        show();
    }

    public void show() {
        reset();

        if (this.attachedSpeech == null) {
            setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Create Dialog"),
                    (event) -> {
                        this.plugin.getPlayerManager().getPlayer(this.player).setInDialogCreation(true);
                        this.player.sendMessage("Click a citizen to create a new dialog.");
                        this.player.closeInventory();
                    });

            setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                    (event) -> this.guiManager.showGUI(this.player, new GuiMain(this.plugin, this.player)));
        }

        List<Dialog> dialogs = this.dialogManager.getDialogs();
        for (int i = 0; i < dialogs.size(); i++) {
            Dialog dialog = dialogs.get(i);

            if (dialog.getCitizen() == null) {
                this.dialogManager.removeDialog(dialog);
                dialog.delete();
                continue;
            }

            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, dialog.getCitizen().getName(),
                            "",
                            TextUtils.formatText("&fLeft-Click: &6to edit"),
                            TextUtils.formatText("&fRight-Click: &6to delete")),
                    (event) -> {
                        if (event.clickType == ClickType.LEFT) {
                            this.guiManager.showGUI(this.player, new GuiDialog(this.plugin, this.player, dialog, this.attachedSpeech));
                        } else if (event.clickType == ClickType.RIGHT) {
                            System.out.println(dialogManager.getDialogs().stream().map(Dialog::getId).collect(Collectors.toList()));
                            dialogManager.removeDialog(dialog);
                            System.out.println(dialogManager.getDialogs().stream().map(Dialog::getId).collect(Collectors.toList()));
                            System.out.println("Removed dialog: " + dialog.getId());
                            dialog.delete();
                            show();
                        }
                    });
        }
    }
}
