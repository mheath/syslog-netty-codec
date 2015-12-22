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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Frames outbound {@link ByteBuf} objects using the Syslog message framing described in RFC-6587.
 *
 * @author Mike Heath
 */
public class SyslogFrameEncoder extends MessageToMessageEncoder<ByteBuf> {
	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		final String msgLength = Integer.toString(msg.readableBytes());
		final ByteBuf lengthBuf = ctx.alloc().buffer(msgLength.length() + 1);
		ByteBufUtil.writeAscii(lengthBuf, msgLength);
		lengthBuf.writeByte(' ');
		out.add(Unpooled.wrappedBuffer(lengthBuf, msg.retain()));
	}
}
