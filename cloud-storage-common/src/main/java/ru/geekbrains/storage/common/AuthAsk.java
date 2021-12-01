package ru.geekbrains.storage.common;

public class AuthAsk implements Message {
    private String login;
    private String password;

    public AuthAsk(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.AUTH_ASK;
    }
}
