package ru.geekbrains.storage.common;

public class AuthOK implements Message {
    private String username;
    private String login;

    public AuthOK(String username, String login) {
        this.username = username;
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.AUTH_OK;
    }
}
