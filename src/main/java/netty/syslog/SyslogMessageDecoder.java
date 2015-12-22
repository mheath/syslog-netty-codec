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
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.util.AsciiString;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import static netty.syslog.CodecUtil.*;

public class SyslogMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> objects) throws Exception {
        if (buffer.readableBytes() < 1) {
            return;
        }
        final SyslogMessage.MessageBuilder messageBuilder = SyslogMessage.MessageBuilder.create();

        // Decode PRI
        expect(buffer, '<');
        final int pri = readDigit(buffer);
        if (pri < 0 || pri > 191) {
            throw new DecoderException("Invalid PRIVAL " + pri);
        }
        final int facility = pri / 8;
        final int severity = pri % 8;

        messageBuilder.facility(SyslogMessage.Facility.values()[facility]);
        messageBuilder.severity(SyslogMessage.Severity.values()[severity]);

        expect(buffer, '>');

        // Decode VERSION
        if (buffer.readByte() != '1') {
            throw new DecoderException("Expected a version 1 syslog message");
        }
        expect(buffer, ' ');

        // Decode TIMESTAMP
        final ZonedDateTime timestamp;
        final AsciiString timeStampString = readAsciiStringToSpace(buffer, true);
        if (timeStampString == null) {
            timestamp = null;
        } else {
            timestamp = ZonedDateTime.parse(timeStampString);
        }
        messageBuilder.timestamp(timestamp);

        // Decode HOSTNAME
        messageBuilder.hostname(readAsciiStringToSpace(buffer, true));

        // Decode APP-NAME
        messageBuilder.applicationName(readAsciiStringToSpace(buffer, true));

        // Decode PROC-ID
        messageBuilder.processId(readAsciiStringToSpace(buffer, true));

        // Decode MSGID
        messageBuilder.messageId(readAsciiStringToSpace(buffer, true));

        final byte structuredData = buffer.readByte();
        if (structuredData == '[') {
            // Decode STRUCTURED-DATA
            decodeStructuredData(messageBuilder, buffer);
        } else if (structuredData != '-') {
            throw new DecoderException("Invalid structured data field. Expected '[' or '-', got " + (char)structuredData);
        }

        final int length = buffer.readableBytes();
        messageBuilder.content(buffer.readSlice(length).retain());

        objects.add(messageBuilder.build(false));
    }

    static void decodeStructuredData(SyslogMessage.MessageBuilder builder, ByteBuf buf) {
        byte termByte;
        do {
            final AsciiString element = readAsciiStringToSpace(buf, false);
            if (peek(buf) == '-') {
                buf.readByte();
                expect(buf, ']');
                builder.addStructuredDataElement(element);
            } else {
                do {
                    final AsciiString paramName = readAsciiStringToChar(buf, '=', false);
                    if (paramName == null) {
                        builder.addStructuredDataElement(element);
                    } else {
                        final String paramValue = readParamValue(buf);
                        builder.addStructuredDataElement(element, paramName, paramValue);
                    }
                    termByte = buf.readByte();
                } while (termByte != ']');
            }
            termByte = buf.readByte();
        } while (termByte == '[');
        if (termByte != ' ') {
            throw new DecoderException("Expected ']' found " + (char) termByte);
        }
    }

    private static String readParamValue(ByteBuf buf) {
        expect(buf, '"');
        final ByteBuf valueBuf = Unpooled.buffer(buf.readableBytes());
        try {
            byte b;
            while ((b = buf.readByte()) != '"') {
                if (b == '\\') {
                    b = buf.readByte();
                }
                valueBuf.writeByte(b);
            }
            return valueBuf.toString(StandardCharsets.UTF_8);
        } finally {
            valueBuf.release();
        }
    }

}
