public interface AuthenticationProvider {
    String getUsernameByLoginAndPassword(String login, String password);
    boolean isLoginUsed(String login);
    void connectBD();
    void disconnectBD();
}
