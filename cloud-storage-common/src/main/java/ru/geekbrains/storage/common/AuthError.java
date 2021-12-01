package ru.geekbrains.storage.common;

public class AuthError implements Message {
    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.AUTH_ERROR;
    }
}
