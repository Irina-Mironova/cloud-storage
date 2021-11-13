import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class BDAuthenticationProvider implements AuthenticationProvider{
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;

    static final String USER = "postgres";
    static final String PASS = "postgres";

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        try {
            ps = connection.prepareStatement("Select * From users Where login = ? and password = ?");
            ps.setString(1,login);
            ps.setString(2,password);
            rs = ps.executeQuery();
            while (rs.next()){
                return rs.getString(2) + " " +  rs.getString(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД");
            return null;
        }
        return null;
    }

    public void newUser(String lastname, String name, String email,  String login, String password, String uuid){
        try {
        ps = connection.prepareStatement("Insert Into users (lastname, name, email, login, password, uuid) " +
                                             " Values(?,?,?,?,?,?)");
        ps.setString(1,lastname);
        ps.setString(2,name);
        ps.setString(3,email);
        ps.setString(4,login);
        ps.setString(5,password);
        ps.setString(6,uuid);
        ps.executeUpdate();
    } catch (SQLException e){
        e.printStackTrace();
        log.error("Ошибка при работе с таблицей users в БД. Создание нового пользователя не удалось");}
    }

    @Override
    public boolean isLoginUsed(String login) {
        try {
            ps = connection.prepareStatement("Select * From users Where login = ?");
            ps.setString(1, login);
            rs = ps.executeQuery();
            while (rs.next()){
                return true;
            }
         return false;
        }catch (SQLException e){
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД. Поиск пользователя не удался");
            return true;
        }
    }

    @Override
    public void connectBD() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
                connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/userDB?currentSchema=public", USER,PASS);
                System.out.println("Соединение с базой данных установлено");
//                if (isLoginUsed("petrov1")) {
//                    System.out.println("login petrov1 уже используется");
//                } else System.out.println("login petrov1 свободен");

        //        newUser("Крылов", "Степан","kr@mail.ru","kr1","123","555");
//                ps = connection.prepareStatement("SELECT * FROM users WHERE id_user = ?;");
//                ps.setInt(1, 1);
//                rs = ps.executeQuery();
//                while (rs.next()){
//                    System.out.println("Петров = " + rs.getString(3));
//
//                }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка соединения с БД");
        }

    }

    @Override
    public void disconnectBD() {
        System.out.println("Соединение с БД разорвано");
        try {
           if(connection != null) {
               connection.close();
           }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
