package ru.geekbrains.storage.server;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class BDAuthenticationProvider implements AuthenticationProvider {
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;

    static final String USER = "postgres";
    static final String PASS = "postgres";


    //получение имени польз-ля по логину и паролю
    @Override
    public String getUsernameByLogin(String login) {
        try {
            ps = connection.prepareStatement("Select * From users Where login = ?");
            ps.setString(1, login);

            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getString(2) + " " + rs.getString(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД");
            return null;
        }
        return null;
    }

    //получение пароля по логину
    @Override
    public String getPasswordByLogin(String login) {
        try {
            ps = connection.prepareStatement("Select * From users Where login = ?");
            ps.setString(1, login);
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getString(6);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД");
            return null;
        }
        return null;
    }


    //получение login по email
    @Override
    public String getLoginByEmail(String email) {
        try {
            ps = connection.prepareStatement("Select * From users Where email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getString(5);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД");
            return null;
        }
        return null;
    }

    //создание нового пользователя
    public boolean newUser(String lastname, String name, String email, String login, String password, String uuid) {
        try {
            ps = connection.prepareStatement("Insert Into users (lastname, name, email, login, password, uuid) " +
                    " Values(?,?,?,?,?,?)");
            ps.setString(1, lastname);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, login);
            ps.setString(5, password);
            ps.setString(6, uuid);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД. Создание нового пользователя не удалось");
            return false;
        }
    }

    //проверка: не занят ли логин
    @Override
    public boolean isLoginUsed(String login) {
        try {
            ps = connection.prepareStatement("Select * From users Where login = ?");
            ps.setString(1, login);
            rs = ps.executeQuery();
            while (rs.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД. Поиск пользователя не удался");
            return true;
        }
    }

    //проверка: не занят ли email
    @Override
    public boolean isEmailUsed(String email) {
        try {
            ps = connection.prepareStatement("Select * From users Where email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            while (rs.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД. Поиск пользователя не удался");
            return true;
        }
    }

    //получение названия директории пользователя по логину
    @Override
    public String getUuidByLogin(String login) {
        try {
            ps = connection.prepareStatement("Select * from users Where login = ?");
            ps.setString(1, login);
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getString(7);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД. Поиск папки UUID не удался");
            return null;
        }
        return null;
    }


    //подключение к БД
    @Override
    public void connectBD() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/userDB?currentSchema=public", USER, PASS);
            log.error("Соединение с базой данных установлено");
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка соединения с БД");
        }

    }

    //отключение от БД
    @Override
    public void disconnectBD() {
        log.debug("Соединение с БД разорвано");
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
