package ru.geekbrains.storage.common;

public class DeleteAsk implements Message {

    private String fileName;
    private String login;

    public DeleteAsk(String fileName, String login) {
        this.fileName = fileName;
        this.login = login;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.DELETE_ASK;
    }
}
