package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.string.StringEncoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author Mike Heath
 */
public class SyslogFrameEncoderTest {

	@Test
	public void frameEncoder() {
		final String frame1 = "Have a nice day.";
		final String frame2 = "Your mom goes to college";
		final String frame3 = "This is just a test.";

		final ByteBuf expectedBuffer = Unpooled.buffer();
		writeFrame(expectedBuffer, frame1);
		writeFrame(expectedBuffer, frame2);
		writeFrame(expectedBuffer, frame3);

		new CodecTester()
				.encoderHandlers(new StringEncoder(StandardCharsets.UTF_8), new SyslogFrameEncoder())
				.expect(expectedBuffer, frame1, frame2, frame3)
				.verify();
	}

	private void writeFrame(ByteBuf buffer, String frame) {
		ByteBufUtil.writeUtf8(buffer, Integer.toString(frame.length()) + " " + frame);
	}
}
