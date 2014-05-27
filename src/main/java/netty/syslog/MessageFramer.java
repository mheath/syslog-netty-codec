/*
 *   Copyright (c) 2014 Intellectual Reserve, Inc.  All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;

import static netty.syslog.DecoderUtil.*;

import java.util.List;

/**
 * Frames syslog messages per RFC-6587.
 *
 * @author "Mike Heath <elcapo@gmail.com>"
 */
public class MessageFramer extends ReplayingDecoder<ByteBuf> {

	public static final int DEFAULT_MAX_MESSAGE_SIZE = 64 * 1024;

	private final int maxMessageSize;

	public MessageFramer() {
		this(DEFAULT_MAX_MESSAGE_SIZE);
	}

	public MessageFramer(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
		// Decode the content length
		final int length = readDigit(buffer);
		expect(buffer, ' ');
		if (length > maxMessageSize) {
			throw new TooLongFrameException("Received a message of length " + length + ", maximum message length is " + maxMessageSize);
		}

		final ByteBuf messageBuffer = buffer.readSlice(length);
		messageBuffer.retain();
		out.add(messageBuffer);
	}

}
