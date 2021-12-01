package ru.geekbrains.storage.common;

public class RegAsk implements Message {
    private String lastname;
    private String name;
    private String email;
    private String login;
    private String password;

    public RegAsk(String lastname, String name, String email, String login, String password) {
        this.lastname = lastname;
        this.name = name;
        this.email = email;
        this.login = login;
        this.password = password;
    }

    public String getLastname() {
        return lastname;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.REG_ASK;
    }
}