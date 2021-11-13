import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerApp {
 // private static   BDAuthenticationProvider authenticationProvider;

    public static void main(String[] args) {
        new Server().start();
//        authenticationProvider = new BDAuthenticationProvider();
//        authenticationProvider.connectBD();
//        System.out.println("eeee");
    }
}
