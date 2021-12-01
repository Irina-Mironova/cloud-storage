package ru.geekbrains.storage.common;

public class RenameAsk implements Message {

    private String oldName;
    private String newName;
    private String login;

    public RenameAsk(String oldName, String newName, String login) {
        this.oldName = oldName;
        this.newName = newName;
        this.login = login;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.RENAME_ASK;
    }


}

