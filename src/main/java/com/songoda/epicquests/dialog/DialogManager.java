package com.songoda.epicquests.dialog;

import com.songoda.core.data.LoadsData;
import com.songoda.core.data.SQLSelect;
import com.songoda.third_party.org.jooq.DSLContext;
import com.songoda.third_party.org.jooq.impl.DSL;
import com.songoda.third_party.org.jooq.impl.SQLDataType;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DialogManager implements LoadsData {
    private final Map<Integer, Dialog> registeredDialog = new LinkedHashMap<>();

    public Dialog getDialog(int id) {
        return this.registeredDialog.get(id);
    }

    public List<Dialog> getDialogs() {
        return new LinkedList<>(this.registeredDialog.values());
    }

    public Dialog addDialog(Dialog dialog) {
        if (getDialogByCitizenId(dialog.getCitizenId()) != null)
            return dialog;
        for (Speech speech : dialog.getMessages())
            speech.setDialog(dialog);
        this.registeredDialog.put(dialog.getId(), dialog);
        return dialog;
    }

    public void removeDialog(Dialog dialog) {
        this.registeredDialog.remove(dialog.getId());
    }

    public Speech getSpeech(int id) {
        for (Dialog dialog : this.registeredDialog.values()) {
            for (Speech speech : dialog.getMessages()) {
                if (speech.getId() == id) {
                    return speech;
                }
            }
        }
        return null;
    }

    public Dialog getDialogByCitizenId(int id) {
        for (Dialog dialog : this.registeredDialog.values()) {
            if (dialog.getCitizenId() == id) {
                return dialog;
            }
        }
        return null;
    }


    @Override
    public void loadDataImpl(DSLContext ctx) {
        SQLSelect.create(ctx).select("id", "citizen_id").from("dialog", result -> {
            int id = result.get("id").asInt();
            int citizenId = result.get("citizen_id").asInt();

            Dialog dialog = new Dialog(citizenId);
            dialog.setId(id);
            addDialog(dialog);
        });

        SQLSelect.create(ctx).select("id", "dialog_id", "default_dialog", "messages", "quest_prerequisites")
                .from("speech", result -> {
                    int id = result.get("id").asInt();
                    int dialogId = result.get("dialog_id").asInt();
                    boolean defaultDialog = result.get("default_dialog").asBoolean();
                    String messagesString = result.get("messages").asString();
                    String questPrerequisitesString = result.get("quest_prerequisites").asString();

                    Dialog dialog = getDialog(dialogId);
                    if (dialog == null) {
                        return;
                    }

                    Speech speech = new Speech(dialog);
                    speech.setId(id);
                    speech.setDefaultDialog(defaultDialog);
                    if (messagesString != null && !messagesString.isEmpty())
                        for (String msg : messagesString.split("\n"))
                            speech.addMessage(msg);
                    if (questPrerequisitesString != null && !questPrerequisitesString.isEmpty())
                        for (String questPrerequisite : questPrerequisitesString.split(","))
                            if (!questPrerequisite.isEmpty())
                                speech.addQuestPrerequisite(Integer.parseInt(questPrerequisite));

                    dialog.addMessage(speech);
                });
    }

    @Override
    public void setupTables(DSLContext ctx) {
        ctx.createTableIfNotExists("dialog")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("citizen_id", SQLDataType.INTEGER.nullable(false))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

        ctx.createTableIfNotExists("speech")
                .column("id", SQLDataType.INTEGER.nullable(false).identity(true))
                .column("dialog_id", SQLDataType.INTEGER.nullable(false))
                .column("default_dialog", SQLDataType.BOOLEAN.nullable(false))
                .column("messages", SQLDataType.VARCHAR(1024))
                .column("quest_prerequisites", SQLDataType.VARCHAR(1024))
                .constraint(DSL.constraint().primaryKey("id"))
                .execute();

    }
}
