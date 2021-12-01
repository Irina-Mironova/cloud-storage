package ru.geekbrains.storage.common;

public class RegError implements Message {
    private String login;
    private String email;

    public RegError(String login, String email) {
        this.login = login;
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.REG_ERROR;
    }
}
