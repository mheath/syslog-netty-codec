package netty.syslog;

import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Mike Heath <elcapo@gmail.com>
 */
public class MessageDecoderTest {

	@Test
	public void decode() throws Exception {
		final MessageDecoder decoder = new MessageDecoder();
		final List<Object> messageList = new ArrayList<>();

		decoder.decode(null, Unpooled.wrappedBuffer("<14>1 2014-03-20T20:14:14+00:00 loggregator 20d38e29-85bb-4833-81c8-99ba7d0c1b09 [App/0] - - SHLVL : 1".getBytes()), messageList);
		assertEquals(messageList.size(), 1);
		final Message message = ((Message) messageList.remove(0));
		assertEquals(message.getFacility(), Message.Facility.USER_LEVEL);
		assertEquals(message.getSeverity(), Message.Severity.INFORMATION);
		assertEquals(message.getTimestamp(), ZonedDateTime.parse("2014-03-20T20:14:14Z"));
		assertEquals(message.getHostname(), "loggregator");
		assertEquals(message.getApplicationName(), "20d38e29-85bb-4833-81c8-99ba7d0c1b09");
		assertEquals(message.getProcessId(), "[App/0]");
		assertNull(message.getMessageId());
		assertEquals(message.getStructuredData().size(), 0);
		assertEquals(message.content().toString(StandardCharsets.UTF_8), "SHLVL : 1");
	}

	@Test
	public void decodeWithStructuredData() throws Exception {
		final MessageDecoder decoder = new MessageDecoder();
		final List<Object> messageList = new ArrayList<>();

    decoder.decode(
        null,
        Unpooled.wrappedBuffer(
            "<14>1 2014-03-20T20:14:14+00:00 loggregator 20d38e29-85bb-4833-81c8-99ba7d0c1b09 [App/0] - [sdid@121 testKey1=\"testValue1\" testKey2=\"testValue2\"] SHLVL : 1"
                .getBytes()),
        messageList);
    assertEquals(messageList.size(), 1);
    final Message message = ((Message) messageList.remove(0));
    assertEquals(message.getFacility(), Message.Facility.USER_LEVEL);
    assertEquals(message.getSeverity(), Message.Severity.INFORMATION);
    assertEquals(message.getTimestamp(), ZonedDateTime.parse("2014-03-20T20:14:14Z"));
    assertEquals(message.getHostname(), "loggregator");
    assertEquals(message.getApplicationName(), "20d38e29-85bb-4833-81c8-99ba7d0c1b09");
    assertEquals(message.getProcessId(), "[App/0]");
    assertNull(message.getMessageId());
    assertTrue(message.getStructuredData().containsKey("sdid@121"));
    assertTrue(message.getStructuredData().get("sdid@121").containsKey("testKey1"));
    assertEquals(message.getStructuredData().get("sdid@121").get("testKey1"), "testValue1");
    assertTrue(message.getStructuredData().get("sdid@121").containsKey("testKey2"));
    assertEquals(message.getStructuredData().get("sdid@121").get("testKey2"), "testValue2");
    assertEquals(message.getStructuredData().get("sdid@121").size(), 2);
    assertEquals(message.getStructuredData().size(), 1);
    assertEquals(message.content().toString(StandardCharsets.UTF_8), "SHLVL : 1");
	}

}
