package ru.geekbrains.storage.common;

import java.util.List;

public class ListMessage implements Message{
    private List<String> listFiles;

    public ListMessage(List<String> listFiles) {
        this.listFiles = listFiles;
    }

    public List<String> getListFiles() {
        return listFiles;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.LIST_MESSAGE;
    }
}
