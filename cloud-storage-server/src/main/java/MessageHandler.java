import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {
    private BDAuthenticationProvider authenticationProvider;

    public MessageHandler(BDAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connected...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnected...");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println("сообщение от клиента");
        //обработка входящего сообщения в зависимости от его типа
        switch (msg.getTypeMessage()) {
            case TEXT_MESSAGE:  //пришло текстовое сообщение
                TextMessage textMessage = (TextMessage) msg;
                log.debug("received: {}", textMessage);
                System.out.println("От клиента: " + textMessage.getTextMessage());

                //разбиваем текстовое сообщение на отдельные слова по пробелам и сохраняем в массив
                String[] tokens = textMessage.getTextMessage().split("\\s");

                // если пришло сообщение о попытке авторизации пользователя с указанным логином и паролем
                if (textMessage.getTextMessage().startsWith("/auth")) {    //  /auth login password
                    //обращаемся к БД для получения фамилии и имени указанного польз-ля
                    String username = authenticationProvider.getUsernameByLoginAndPassword(tokens[1], tokens[2]);
                    //если пользователь найден, то отправляем клиенту сообщение об удачной авторизации
                    if (username != null) {
                        ctx.writeAndFlush(new TextMessage("/authOK " + username));
                        log.debug("send: {}", "/authOK " + username);
                    } else { //в противном случае отправляем клиенту сообщение о неудачной авторизации
                        ctx.writeAndFlush(new TextMessage("/authError"));
                        log.debug("send: {}", "/authError");
                    }
                }

                // если пришло сообщение о запросе списка файлов клиента
                if (textMessage.getTextMessage().startsWith("/list")) {    //  /list login
                    // ищем в БД имя папки пользователя (uuid)
                    String uuid = authenticationProvider.getUuidByLogin(tokens[1]);
                    // если папка найдена, отправляем клиенту сообщение, содержащее список файлов его папки
                    if (uuid != null) {
                        ctx.writeAndFlush(new ListMessage(getListFiles(uuid)));
                        log.debug("send: {}", "/list " + tokens[3]);
                    }
                }

                // если пришло сообщение о запросе регистрации нового польз-ля
                if (textMessage.getTextMessage().startsWith("/reg")) { // /reg lastname name email login password
                    //проверяем в БД, что пользователя с указанным login и email  БД нет,
                    //в противном случае отправляем клиенту сообщение о неудачной регистрации
                    if (authenticationProvider.isLoginUsed(tokens[4])) {
                        ctx.writeAndFlush(new TextMessage("/regError login"));
                        break;
                    }
                    if (authenticationProvider.isEmailUsed(tokens[3])) {
                        ctx.writeAndFlush(new TextMessage("/regError email"));
                        break;
                    }
                    //в случае, если login и email используется, то в БД добавляем нового польз-ля
                    //и отправляем клиенту сообщение об удачной регистрации
                    if (authenticationProvider.newUser(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[4])) {
                        ctx.writeAndFlush(new TextMessage("/regOK"));
                    }
                }
                break;
            case FILE_MESSAGE:
                break;
            case LIST_MESSAGE:
                break;
        }

    }

    //создание списка файлов в папке польз-ля
    private List<String> getListFiles(String uuid) {
        Path path = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-server", "server", uuid);
        if (!Files.exists(path)) {
            log.debug("Директория " + path.toString() + " не существует");
            try {
                Files.createDirectory(path);
                log.debug("Создана директория " + path.toString());
            } catch (IOException e) {
                log.error("Ошибка при создании директории " + path.toString());
            }
            return null;
        }

        List<String> listFile = new ArrayList<>();
        File dir = new File(path.toString());
        File[] arrFiles = dir.listFiles();
        for (File file : arrFiles) {
            listFile.add(file.getName());
        }
        return listFile;
    }
}
