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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mike Heath
 */
public class CodecTester {

	private boolean fragmentBuffer = false;
	private ChannelHandler[] encoderHandlers;
	private ChannelHandler[] decoderHandlers;
	private Collection<Expectation> expectations = new ArrayList<>();

	public CodecTester encoderHandlers(ChannelHandler... encoderHandlers) {
		this.encoderHandlers = encoderHandlers;
		return this;
	}

	public CodecTester decoderHandlers(ChannelHandler... decoderHandlers) {
		this.decoderHandlers = decoderHandlers;
		return this;
	}

	public CodecTester expect(ByteBuf buffer, Object... messages) {
		expectations.add(new Expectation(buffer, messages));
		return this;
	}

	public CodecTester fragmentBuffer(boolean fragmentBuffer) {
		this.fragmentBuffer =fragmentBuffer;
		return this;
	}

	public void verify() {
		if (encoderHandlers == null && decoderHandlers == null) {
			fail("No encoder/decoder handlers to test.");
		}
		if (expectations.size() == 0) {
			fail("No expectations to test");
		}
		if (decoderHandlers != null) {
			verifyDecoders();
		}
		if (encoderHandlers != null) {
			verifyEncoders();
		}
	}

	private void verifyDecoders() {
		final EmbeddedChannel channel = new EmbeddedChannel(decoderHandlers);
		expectations.forEach(expectation -> {
			expectation.buffer.markReaderIndex();
			try {
				if (fragmentBuffer) {
					// Write 1 byte at a time to ensure the decoder handles pagment fragmentation
					while (expectation.buffer.readableBytes() > 0) {
						channel.writeInbound(expectation.buffer.readBytes(1));
					}
				} else {
					channel.writeInbound(expectation.buffer);
				}
				expectation.messages.forEach(m -> assertEquals(m, channel.readInbound()));
			} finally {
				expectation.buffer.resetReaderIndex();
			}
		});
	}

	private void verifyEncoders() {
		final EmbeddedChannel channel = new EmbeddedChannel(encoderHandlers);
		expectations.forEach(expectation -> {
			expectation.messages.forEach(channel::writeOutbound);
			final ByteBuf outboundBuffer = Unpooled.buffer(expectation.buffer.readableBytes());
			channel.outboundMessages().forEach(buffer -> outboundBuffer.writeBytes((ByteBuf) buffer));
			assertEquals(expectation.buffer, outboundBuffer);
		});
	}

	private static class Expectation {
		private final ByteBuf buffer;
		private final List<Object> messages;

		public Expectation(ByteBuf buffer, Object[] message) {
			this.buffer = buffer;
			this.messages = Arrays.asList(message);
		}
	}
}
