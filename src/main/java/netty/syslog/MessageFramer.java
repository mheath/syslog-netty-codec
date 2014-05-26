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
 * @author Mike Heath <elcapo@gmail.com>
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
