package com.songoda.epicquests.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicquests.EpicQuests;
import com.songoda.epicquests.dialog.Speech;
import com.songoda.epicquests.utils.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.List;

public class GuiSpeech extends Gui {
    private final EpicQuests plugin;
    private final Player player;
    private final Speech speech;

    public GuiSpeech(EpicQuests plugin, Player player, Speech speech) {
        this.plugin = plugin;
        this.player = player;
        this.speech = speech;
        setRows(6);
        setDefaultItem(null);

        setTitle("Speech");

        show();
    }

    public void show() {
        reset();

        setButton(0, 0, GuiUtils.createButtonItem(XMaterial.GREEN_DYE, "Add Message"),
                (event) -> {
                    ChatPrompt.showPrompt(this.plugin, this.player,
                                    "Enter a Message. You may use @p to include the players name.",
                                    response -> {
                        speech.addMessage(response.getMessage());
                        speech.save("messages");
                                    })
                            .setOnClose(() -> this.guiManager.showGUI(this.player, new GuiSpeech(this.plugin, this.player, this.speech)));
                });

        setButton(0, 1, GuiUtils.createButtonItem(XMaterial.RED_DYE,
                        this.speech.isDefaultDialog() ? "Default" : "Not default"),
                (event) -> {
                    this.speech.setDefaultDialog(!this.speech.isDefaultDialog());
                    if (this.speech.isDefaultDialog()) {
                        this.speech.clearQuestPrerequisites();
                    }
                    speech.save("default_dialog");
                    show();
                });

        if (!this.speech.isDefaultDialog()) {
            setButton(0, 2, GuiUtils.createButtonItem(XMaterial.PURPLE_DYE, "Modify Prerequisites"),
                    (event) -> {
                        this.guiManager.showGUI(this.player, new GuiQuestPrereqs(this.plugin, this.player, this.speech));
                        show();
                    });
        }

        setButton(0, 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back"),
                (event) -> {
                    this.guiManager.showGUI(this.player, new GuiDialog(this.plugin, this.player, this.speech.getDialog(), null));
                });

        List<String> messages = this.speech.getMessages();

        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            List<String> lore = TextUtils.condense(message);
            lore.addAll(Arrays.asList("", TextUtils.formatText("&fLeft-Click: &6to move to the end"), TextUtils.formatText("&fRight-Click: &6to delete")));
            setButton(i + 9, GuiUtils.createButtonItem(XMaterial.PAPER, lore),
                    (event) -> {
                        if (event.clickType == ClickType.RIGHT) {
                            this.speech.removeMessage(message);
                        } else if (event.clickType == ClickType.LEFT) {
                            this.speech.moveMessageToEnd(message);
                        }
                        speech.save("messages");
                        show();
                    });
        }
    }
}
