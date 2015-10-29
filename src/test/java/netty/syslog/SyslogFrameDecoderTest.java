package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
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
		ByteBufUtil.writeUtf8(buffer, Integer.toString(octetCount.length()));
		buffer.writeByte(' ');
		ByteBufUtil.writeUtf8(buffer, octetCount);

		// Encode lfTerminated frame
		ByteBufUtil.writeUtf8(buffer, lfTerminated);
		buffer.writeByte('\n');

		// Encode nulTerminated frame
		ByteBufUtil.writeUtf8(buffer, nulTerminated);
		buffer.writeByte(0);

		// Encode crLfTerminated frame
		ByteBufUtil.writeUtf8(buffer, crLfTerminated);
		buffer.writeByte('\r');
		buffer.writeByte('\n');

		// Encode second octetCount frame
		ByteBufUtil.writeUtf8(buffer, Integer.toString(octetCount2.length()));
		buffer.writeByte(' ');
		ByteBufUtil.writeUtf8(buffer, octetCount2);

		// Run codec test
		new CodecTester()
				.fragmentBuffer(true)
				.decoderHandlers(new SyslogFrameDecoder(), new StringDecoder())
				.expect(buffer, octetCount, lfTerminated, nulTerminated, crLfTerminated, octetCount2)
				.verify();
	}

}
