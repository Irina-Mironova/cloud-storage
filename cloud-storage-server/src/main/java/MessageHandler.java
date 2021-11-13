import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println("сообщение от клиента ))");
        switch (msg.getTypeMessage()) {
            case TEXT_MESSAGE:
                TextMessage textMessage = (TextMessage) msg;
                log.debug("received: {}", textMessage);
                System.out.println("От клиента: " + textMessage.getTextMessage());
                String[] tokens = textMessage.getTextMessage().split("\\s");
                if (textMessage.getTextMessage().startsWith("/auth")){    //  /auth login password
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
                break;
            case FILE_MESSAGE:
                break;
            case LIST_MESSAGE:
                break;
        }

        //
       // log.debug("received: {}", message);


        //ctx.writeAndFlush("From server: " + s);


    }
}
