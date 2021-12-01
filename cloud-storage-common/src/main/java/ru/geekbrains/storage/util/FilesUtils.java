package ru.geekbrains.storage.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;
import ru.geekbrains.storage.common.FileMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FilesUtils {
    private final int BUF_SIZE = 1000000; //размер буфера

    public void sendFile(String fileName, String login, String dirDestination, ObjectEncoderOutputStream os, ChannelHandlerContext ctx) {
        File file = new File(fileName);
        float fileLen = file.length();
        log.debug("Подготовка к отправке файла " + file.getName() + " ...");
        log.debug("Размер файла: " + fileLen);
        int count = (int) Math.ceil(fileLen / (BUF_SIZE));
        log.debug("Кол-во пакетов для отправки: " + count);
        int number = 1;

        //разбиваем файл и отправляем его частями размером BUF_SIZE
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buf = new byte[BUF_SIZE];
            int c;
            while ((c = in.read(buf)) > 0) {
                if (c < buf.length) {
                    buf = Arrays.copyOf(buf, c);
                }
                // отправляем файл(часть) серверу
                if (os != null) {
                    os.writeObject(new FileMessage(login, file.getName(), null, buf, number, count));
                    log.debug("На сервер отправлен файл " + number + " из " + count);
                } else //отправляем файл(часть) клиенту
                    ctx.writeAndFlush(new FileMessage(login, file.getName(), dirDestination, buf, number, count));
                log.debug("На клиент отправлен файл " + number + " из " + count);
                number++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке файла");
        }
    }

    //создание директории
    public void createDirectory(Path path) {
        if (!Files.exists(path)) {
            log.debug("Директория " + path.toString() + " не существует");
            try {
                Files.createDirectory(path);
                log.debug("Создана директория " + path.toString());
            } catch (IOException e) {
                log.error("Ошибка при создании директории " + path.toString());
            }
        }
    }

    // сохранение файла (по частям)
    public boolean saveFile(Path dirTmp, Path dirDestination, FileMessage fileMessage) {
        //создаем директорию для временных файлов
        createDirectory(dirTmp);

        Path fileNameTmp = dirTmp.resolve(fileMessage.getFileName() + ".tmp" + fileMessage.getNumberPackage());

        //создаем tmp-файл в папке для временных файлов
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileNameTmp.toString()))) {
            out.write(fileMessage.getArrayInfoBytes());
        } catch (IOException ioException) {
            ioException.printStackTrace();
            log.error("Ошибка при записи файла " + fileNameTmp + " на сервер");
        }

        //подсчет количества файлов(частей) tmp в папке dirTMP
        try {
            List<String> filesList;
            filesList = Files.list(dirTmp)
                    .map(p -> p.getFileName().toString())
                    .filter(i -> i.contains(fileMessage.getFileName()))
                    .sorted()
                    .collect(Collectors.toList());

            //если кол-во файлов tmp = числу пакетов, из которых состоит файл,
            //то объединяем файлы
            if (filesList.size() == fileMessage.getCountPackage()) {
                Path fileDestination = dirDestination.resolve(fileMessage.getFileName());

                try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileDestination.toString(), true))) {
                    for (String filename : filesList) {
                        //копируем инф-ю из tmp-файла
                        joinFiles(out, dirTmp.resolve(filename).toString());
                        //и удаляем его
                        deleteFile(dirTmp.resolve(filename).toString());
                    }
                }
            } else {
                return false;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            log.error("Ошибка при записи файла на сервер");
            return false;
        }
        return true;
    }

    //объединение частей файлов
    private void joinFiles(BufferedOutputStream out, String source) throws IOException {
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(source), BUF_SIZE)) {
            int c;
            while ((c = is.read()) != -1) {
                out.write(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при объединении файлов");
        }
    }

    //удаление файла
    public void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
            log.debug("Файл " + fileName + " удален");
        } else {
            log.error("Файл " + fileName + " не найден");
        }
    }

    //переименование файла
    public void renameFile(String oldName, String newName) {
        File file = new File(oldName);
        if (file.exists()) {
            file.renameTo(new File(newName));
            log.debug("Файл " + file.getName() + " переименован в " + newName);
        } else {
            log.error("Файл " + oldName + " не найден");
        }
    }

    //поделиться файлом
    public void shareFile(String destination, String source) {
        File file = new File(source);
        if (file.exists()) {
            try {
                Files.copy(Paths.get(source), Paths.get(destination));
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Ошибка при копировании файла");
            }
            log.debug("Файл " + source + " скопирован в " + destination);
        } else {
            log.error("Файл " + source + " не найден");
        }
    }
}



