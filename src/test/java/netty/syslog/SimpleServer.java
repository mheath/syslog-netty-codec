/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

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
