import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringHandler extends SimpleChannelInboundHandler<String> {
private BDAuthenticationProvider authenticationProvider;

    public StringHandler(BDAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("e", cause);
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
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        log.debug("received: {}", message);
        String[] tokens = message.split("\\s");
        if (message.startsWith("/auth")){    //  /auth login password
            String username = authenticationProvider.getUsernameByLoginAndPassword(tokens[1],tokens[2]);
            if (username != null) {
//                ctx.write("/authOK " + username);
//                ctx.flush();
                ctx.writeAndFlush("/authOK " + username,null);
                log.debug("send: {}", "/authOK " + username);
            }
            else {
                ctx.writeAndFlush("/authError");
                log.debug("send: {}", "/authError");
            }
        }

        //ctx.writeAndFlush("From server: " + s);
    }
}
