package ru.geekbrains.storage.common;

public class FileMessage implements Message {
    private  String login;
    private  String fileName;
    private  String  dirDestination;
    private byte[] arrayInfoBytes; //часть файла в байтах
    private int numberPackage; //номер пакета
    private int countPackage; // общее количество пакетов

    public FileMessage(String login,  String fileName, String dirDestination,  byte[] arrayInfoBytes,
                       int numberPackage, int countPackage) {
        this.login = login;
        this.fileName = fileName;
        this.dirDestination = dirDestination;
        this.arrayInfoBytes = arrayInfoBytes;
        this.numberPackage = numberPackage;
        this.countPackage = countPackage;
    }

    public String getLogin() {
        return login;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getArrayInfoBytes() {
        return arrayInfoBytes;
    }

    public int getNumberPackage() {
        return numberPackage;
    }

    public int getCountPackage() {
        return countPackage;
    }

    public String getDirDestination() {
        return dirDestination;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.FILE_MESSAGE;
    }
}
