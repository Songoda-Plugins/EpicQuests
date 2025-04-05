package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.dialog.Dialog;
import com.songoda.epicquests.dialog.Speech;
import com.songoda.epicquests.utils.TextUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GuiDialog extends Gui {
    private final EpicQuests plugin;
    private final Dialog dialog;
    private final Player player;
    private final Consumer<Speech> attachedSpeech;

    public GuiDialog(EpicQuests plugin, Player player, Dialog dialog, Consumer<Speech> attachedSpeech) {
        this.plugin = plugin;
        this.dialog = dialog;

        this.player = player;
        this.attachedSpeech = attachedSpeech;
        setRows(6);
        setDefaultItem(null);

        setTitle(dialog.getCitizen().getName());

        show();
    }


    public void show() {
        reset();

        if (this.attachedSpeech == null) {
            setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Add Speech"),
                    (event) -> {
                        Speech speech = new Speech(this.dialog);
                        speech.save();
                        this.dialog.addMessage(speech);
                        show();
                    });
        }

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> this.guiManager.showGUI(this.player, new GuiDialogs(this.plugin, this.player, this.attachedSpeech)));

        List<Speech> messages = this.dialog.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            Speech speech = messages.get(i);
            List<String> lore = new ArrayList<>(Collections.singletonList(""));

            lore.addAll(speech.getMessages().isEmpty()
                    ? Collections.singletonList(TextUtils.formatText("&cNothing here..."))
                    : TextUtils.condense(speech.getMessages().get(0)));

            lore.addAll(Arrays.asList("", TextUtils.formatText(this.attachedSpeech == null ? "&fLeft-Click: &6to view" : "&fLeft-Click: &6to select"), TextUtils.formatText("&fRight-Click: &6to delete")));
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, "Speech " + (i + 1), lore),
                    (event) -> {
                        if (event.clickType == ClickType.RIGHT) {
                            this.dialog.removeSpeech(speech);
                            speech.delete();
                            show();
                            return;
                        }

                        if (this.attachedSpeech == null) {
                            this.guiManager.showGUI(this.player, new GuiSpeech(this.plugin, this.player, speech));
                            show();
                        } else {
                            attachedSpeech.accept(speech);
                        }
                    });
        }
    }
}
