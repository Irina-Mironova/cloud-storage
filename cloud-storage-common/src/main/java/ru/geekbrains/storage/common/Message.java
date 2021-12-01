package ru.geekbrains.storage.common;

import java.io.Serializable;

public interface Message extends Serializable {
    TypeMessage getTypeMessage();
}
