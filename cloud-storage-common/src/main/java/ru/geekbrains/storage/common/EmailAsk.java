package ru.geekbrains.storage.common;

public class EmailAsk implements Message{
   private String email;

    public EmailAsk(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
public TypeMessage getTypeMessage() {
    return TypeMessage.EMAIL_ASK;
}
}
