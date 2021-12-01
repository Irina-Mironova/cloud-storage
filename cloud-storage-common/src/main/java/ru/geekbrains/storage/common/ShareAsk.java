package ru.geekbrains.storage.common;

public class ShareAsk implements Message {

    private String fileName;
    private String login;
    private String email;

    public ShareAsk(String fileName, String login,  String email) {
        this.fileName = fileName;
        this.login  = login;
        this.email = email;
    }

    public String getFileName() {
        return fileName;
    }

    public String getEmail() {
        return email;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.SHARE_ASK;
    }
}

