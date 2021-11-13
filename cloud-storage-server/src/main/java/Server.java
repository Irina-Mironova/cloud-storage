import com.sun.xml.internal.ws.message.StringHeader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Server {
    private BDAuthenticationProvider authenticationProvider;

//    public AuthenticationProvider getAuthenticationProvider() {
//        return authenticationProvider;
//    }

    public void start(){
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(1);
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.channel(NioServerSocketChannel.class)
            .group(auth,worker)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
//                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
//                                    new ObjectEncoder(),
//                                    new StringEncoder(),
//                                    new StringHandler(authenticationProvider)
                                    new MessageHandler(authenticationProvider)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(8189).sync();
            log.debug("Server started ...");
            authenticationProvider = new BDAuthenticationProvider();
            authenticationProvider.connectBD();

            future.channel().closeFuture().sync();

        } catch (Exception e){
          log.error("e",e);
        } finally{
            authenticationProvider.disconnectBD();
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
