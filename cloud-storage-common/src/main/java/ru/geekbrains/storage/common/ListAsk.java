package ru.geekbrains.storage.common;

public class ListAsk implements Message {
    private String login;

    public ListAsk(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.LIST_ASK;
    }
}