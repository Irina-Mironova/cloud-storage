package ru.geekbrains.storage.common;

public class FileAsk implements Message {
    private String fileName;
    private String login;
    private String dirDestination;

    public FileAsk(String fileName, String dirDestination, String login) {
        this.fileName = fileName;
        this.dirDestination = dirDestination;
        this.login = login;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDirDestination() {
        return dirDestination;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.FILE_ASK;
    }
}
