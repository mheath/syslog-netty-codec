package netty.syslog;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Mike Heath
 */
public class SimpleServer {
	public static void main(String[] args) throws Exception {
		final ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap
				.group(new NioEventLoopGroup(), new NioEventLoopGroup())
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel channel) throws Exception {
						final ChannelPipeline pipeline = channel.pipeline();

						pipeline.addLast("framer", new SyslogFrameDecoder());
						pipeline.addLast("decoder", new SyslogMessageDecoder());
						pipeline.addLast("handler", new ChannelInboundHandlerAdapter() {
							@Override
							public void channelActive(ChannelHandlerContext ctx) throws Exception {
								System.out.println("Session started");
							}

							@Override
							public void channelInactive(ChannelHandlerContext ctx) throws Exception {
								System.out.println("Session stopped");
							}

							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								final SyslogMessage message = (SyslogMessage) msg;
								System.out.println(ctx.channel().remoteAddress() + ":" + message);
								message.release();
							}
						});
					}
				});

		bootstrap.bind(12345).sync().channel();
		System.out.println("Let's roll!");
	}
}
