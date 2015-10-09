package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Mike Heath <elcapo@gmail.com>
 */
public class MessageDecoderTest {

	@Test
	public void decode() throws Exception {

		final Message message = new Message.MessageBuilder()
				.facility(Message.Facility.USER_LEVEL)
				.severity(Message.Severity.INFORMATION)
				.timestamp(ZonedDateTime.parse("2014-03-20T20:14:14Z"))
				.hostname("loggregator")
				.applicationName("20d38e29-85bb-4833-81c8-99ba7d0c1b09")
				.processId("[App/0]")
				.content(Unpooled.wrappedBuffer("SHLVL : 1".getBytes()))
				.build();
		new CodecTester()
				.decoderHanlders(new MessageDecoder())
				.expect(Unpooled.wrappedBuffer("<14>1 2014-03-20T20:14:14+00:00 loggregator 20d38e29-85bb-4833-81c8-99ba7d0c1b09 [App/0] - - SHLVL : 1".getBytes()), message)
				.assertExpectations();
	}

}
