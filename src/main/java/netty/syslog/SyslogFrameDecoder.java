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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;

import static netty.syslog.CodecUtil.*;

/**
 * Decodes Syslog frames per RFC-6587.
 */
public class SyslogFrameDecoder extends DelimiterBasedFrameDecoder {

    public static final int DEFAULT_MAX_MESSAGE_SIZE = 64 * 1024;
    private static final ByteBuf[] DELIMITERS = new ByteBuf[] {
            Unpooled.wrappedBuffer(new byte[] { 0 }),
            Unpooled.wrappedBuffer(new byte[] { '\r', '\n' }),
            Unpooled.wrappedBuffer(new byte[] { '\n' }),
    };

    private final int maxLength;

    private boolean readingLine = false;

    public SyslogFrameDecoder() {
        this(DEFAULT_MAX_MESSAGE_SIZE);
    }


    public SyslogFrameDecoder(int maxLength) {
        this(maxLength, true, false);
    }

    public SyslogFrameDecoder(int maxLength, boolean stripDelimiter, boolean failFast) {
        super(maxLength, stripDelimiter, failFast, DELIMITERS);
        this.maxLength = maxLength;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        if (buffer.readableBytes() == 0) {
            return null;
        }
        final int length;
        if (!readingLine && Character.isDigit(peek(buffer))) {
            buffer.markReaderIndex();
            // Decode the content length
            length = readDigit(buffer);
            if (buffer.readableBytes() == 0 || buffer.readableBytes() < length + 1) {
                // Received a buffer with an incomplete frame
                buffer.resetReaderIndex();
                return null;
            }
            expect(buffer, ' ');
            if (length > maxLength) {
                throw new TooLongFrameException(
                        "Received a message of length " + length + ", maximum message length is " + maxLength);
            }
            return buffer.readSlice(length).retain();
        } else {
            final Object lineFrame = super.decode(ctx, buffer);
            readingLine = lineFrame == null;
            return lineFrame;
        }
    }

}
