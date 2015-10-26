package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

	public CodecTester decoderHanlders(ChannelHandler... decoderHandlers) {
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
			assertDecoders();
		}
		if (encoderHandlers != null) {
			assertEncoders();
		}
	}

	private void assertDecoders() {
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

	private void assertEncoders() {
		fail("Not yet implemented.");
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
