package ru.geekbrains.storage.common;

public class RegOK implements Message {
    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.REG_OK;
    }
}
