package ru.geekbrains.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import ru.geekbrains.storage.common.*;
import ru.geekbrains.storage.util.FilesUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {
    private BDAuthenticationProvider authenticationProvider;
    private FilesUtils filesUtils;

    public MessageHandler(BDAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connected...");
        filesUtils = new FilesUtils();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnected...");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        //обработка входящего сообщения в зависимости от его типа
        switch (msg.getTypeMessage()) {
            //если пришло сообщение о попытке авторизации пользователя с указанным логином и паролем
            case AUTH_ASK:
                AuthAsk authAsk = (AuthAsk) msg;
                log.debug("Запрос клиента об авторизации");
                //обращаемся к БД для получения пароля по логину
                String password = authenticationProvider.getPasswordByLogin(authAsk.getLogin());

                //если пароль от пользователя совпадает с сохраненным паролем в БД,то
                if (BCrypt.checkpw(authAsk.getPassword(), password)) {
                    //обращаемся к БД для получения фамилии и имени указанного польз-ля
                    String username = authenticationProvider.getUsernameByLogin(authAsk.getLogin());
                    //если пользователь найден, то отправляем клиенту сообщение об удачной авторизации
                    if (username != null) {
                        ctx.writeAndFlush(new AuthOK(username, authAsk.getLogin()));
                        log.debug("Сообщение от сервера: авторизация пользователя " + authAsk.getLogin() + " успешна");
                    }
                } else { //в противном случае, отправляем клиенту сообщение о неудачной авторизации
                    ctx.writeAndFlush(new AuthError());
                    log.error("Сообщение от сервера: ошибка авторизации пользователя " + authAsk.getLogin());
                }
                break;

            // если пришло сообщение о запросе регистрации нового польз-ля
            case REG_ASK:
                RegAsk regAsk = (RegAsk) msg;
                //проверяем в БД, что пользователя с указанным login и email в БД нет,
                //в противном случае отправляем клиенту сообщение о неудачной регистрации
                if (authenticationProvider.isLoginUsed(regAsk.getLogin())) {
                    ctx.writeAndFlush(new RegError(regAsk.getLogin(), ""));
                    break;
                }
                if (authenticationProvider.isEmailUsed(regAsk.getEmail())) {
                    ctx.writeAndFlush(new RegError("", regAsk.getEmail()));
                    break;
                }
                //в случае, если login и email свободны, то в БД добавляем нового польз-ля
                //и отправляем клиенту сообщение об удачной регистрации
                if (authenticationProvider.newUser(regAsk.getLastname(), regAsk.getName(), regAsk.getEmail(),
                        regAsk.getLogin(), regAsk.getPassword(), regAsk.getLogin())) {
                    ctx.writeAndFlush(new RegOK());
                }
                break;

            // если пришло сообщение о запросе списка файлов клиента
            case LIST_ASK:
                ListAsk listAsk = (ListAsk) msg;
                log.debug("Запрос клиента о списке файлов");
                sendListFiles(ctx, listAsk.getLogin());
                break;

            // если пришло сообщение о запросе выгрузки файла с сервера
            case FILE_ASK:
                FileAsk fileAsk = (FileAsk) msg;
                log.debug("Запрос клиента о выгрузке файла с сервера");
                Path file = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server",
                        "server", fileAsk.getLogin());
                file = file.resolve(fileAsk.getFileName());
                if (file.toFile().exists()) {
                    //разбиваем файл на части и отправляем клиенту
                    filesUtils.sendFile(file.toString(), fileAsk.getLogin(), fileAsk.getDirDestination(), null, ctx);
                }
                break;

            // если пришло сообщение с частью файла
            case FILE_MESSAGE:
                FileMessage fileMessage = (FileMessage) msg;
                Path dirTmp = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server",
                        "server", "tmp", fileMessage.getLogin());
                Path dirDestination = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server",
                        "server", fileMessage.getLogin());
                //сохраняем файл
                if (filesUtils.saveFile(dirTmp, dirDestination, fileMessage)) {
                    //отправляем новый список файлов клиенту
                    sendListFiles(ctx, fileMessage.getLogin());
                }
                break;

            // если пришел запрос на удаление файла
            case DELETE_ASK:
                DeleteAsk deleteAsk = (DeleteAsk) msg;
                Path delFile = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server",
                        "server", deleteAsk.getLogin());
                delFile = delFile.resolve(deleteAsk.getFileName());
                //удаляем файл
                filesUtils.deleteFile(delFile.toString());
                //отправляем новый список файлов клиенту
                sendListFiles(ctx, deleteAsk.getLogin());
                break;

            // если пришел запрос на переименование файла
            case RENAME_ASK:
                RenameAsk renameAsk = (RenameAsk) msg;
                Path oldName = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server",
                        "server", renameAsk.getLogin());
                oldName = oldName.resolve(renameAsk.getOldName());
                Path newName = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server",
                        "server", renameAsk.getLogin());
                newName = newName.resolve(renameAsk.getNewName());
                filesUtils.renameFile(oldName.toString(), newName.toString());
                //отправляем новый список файлов клиенту
                sendListFiles(ctx, renameAsk.getLogin());
                break;

            // если пришел запрос о расшаривании файла
            case SHARE_ASK:
                ShareAsk shareAsk = (ShareAsk) msg;
                //проверяем в БД, что пользователь с указанным email существует
                String loginForShare = authenticationProvider.getLoginByEmail(shareAsk.getEmail());
                if (loginForShare != null) {
                    Path source = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server",
                            "server", shareAsk.getLogin());
                    source = source.resolve(shareAsk.getFileName());
                    Path destination = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server",
                            "server", loginForShare);
                    filesUtils.createDirectory(destination);
                    destination = destination.resolve(shareAsk.getFileName());
                    //копируем в папку указанного пользователя расшаренный файл
                    filesUtils.shareFile(destination.toString(), source.toString());
                }
                break;
        }
    }


    //создание списка файлов в папке польз-ля
    private List<String> getListFiles(String uuid) {
        Path path = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server", "server", uuid);
        filesUtils.createDirectory(path);

        List<String> listFile = new ArrayList<>();
        File dir = new File(path.toString());
        File[] arrFiles = dir.listFiles();
        for (File file : arrFiles) {
            listFile.add(file.getName());
        }
        return listFile;
    }


    private void sendListFiles(ChannelHandlerContext ctx, String login) {
        // ищем в БД имя папки пользователя (uuid)
        String uuid = authenticationProvider.getUuidByLogin(login);
        // если папка найдена, отправляем клиенту сообщение, содержащее список файлов его папки
        if (uuid != null) {
            ctx.writeAndFlush(new ListMessage(getListFiles(uuid)));
            log.debug("Сообщение от сервера: пользователю " + login + " отправлен список файлов");
        }
    }
}