package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.string.StringDecoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author Mike Heath
 */
public class SyslogFrameDecoderTest {

	@Test
	public void frameDecoding() {
		final String octetCount = "RFC 6587 3.5.1 Octet counting encoded frame";
		final String lfTerminated = "RFC 6587 3.5.1 LF terminated frame";
		final String nulTerminated = "RFC 6587 3.5.1 NUL terminated frame";
		final String crLfTerminated = "RFC 6587 3.5.1 CR LF terminated frame ";
		final String octetCount2 = "Another Octet counted frame";

		final ByteBuf buffer = Unpooled.buffer(4096);

		// Encode octetCount frame
		writeString(buffer, Integer.toString(octetCount.length()));
		buffer.writeByte(' ');
		writeString(buffer, octetCount);

		// Encode lfTerminated frame
		writeString(buffer, lfTerminated);
		buffer.writeByte('\n');

		// Encode nulTerminated frame
		writeString(buffer, nulTerminated);
		buffer.writeByte(0);

		// Encode crLfTerminated frame
		writeString(buffer, crLfTerminated);
		buffer.writeByte('\r');
		buffer.writeByte('\n');

		// Encode second octetCount frame
		writeString(buffer, Integer.toString(octetCount2.length()));
		buffer.writeByte(' ');
		writeString(buffer, octetCount2);

		// Run codec test
		new CodecTester()
				.fragmentBuffer(true)
				.decoderHanlders(new SyslogFrameDecoder(), new StringDecoder())
				.expect(buffer, octetCount, lfTerminated, nulTerminated, crLfTerminated, octetCount2)
				.verify();
	}

	private void writeString(ByteBuf buffer, String string) {
		buffer.writeBytes(string.getBytes(StandardCharsets.US_ASCII));
	}

}
