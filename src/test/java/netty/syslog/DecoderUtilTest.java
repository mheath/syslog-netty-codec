package netty.syslog;

import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class DecoderUtilTest {

  @Test
  public void testReadStringToSpace() {
    assertEquals(
        DecoderUtil.readStringToSpace(Unpooled.wrappedBuffer("This is a test".getBytes()), true),
        "This");
		assertEquals(
			DecoderUtil.readStringToSpace(Unpooled.wrappedBuffer("This\\ is\\ a test".getBytes()), true),
			"This is a");
  }

}
