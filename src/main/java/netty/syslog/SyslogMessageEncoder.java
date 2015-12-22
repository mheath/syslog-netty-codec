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
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AsciiString;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static netty.syslog.CodecUtil.*;

public class SyslogMessageEncoder extends MessageToByteEncoder<SyslogMessage> {

    public static final DateTimeFormatter SYSLOG_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'kk:mm:ss.SSSSSSX");

    @Override
    protected void encode(ChannelHandlerContext ctx, SyslogMessage msg, ByteBuf out) throws Exception {
        // Encode PRI
        final int pri = msg.getFacility().ordinal() * 8 + msg.getSeverity().ordinal();
        out.writeByte('<');
        writeDigit(out, pri).writeByte('>');

        // Encode version
        writeDigit(out, 1);
        out.writeByte(' ');

        final String timestamp;
        if (msg.getTimestamp() == null) {
            timestamp = null;
        } else {
            timestamp = msg.getTimestamp().format(SYSLOG_DATETIME_FORMATTER);
        }
        writeNilableString(out, timestamp, 255);

        writeNilableString(out, msg.getHostname(), SyslogMessage.MAX_HOSTNAME_LENGTH);
        writeNilableString(out, msg.getApplicationName(), SyslogMessage.MAX_APPLICATION_NAME_LENGTH);
        writeNilableString(out, msg.getProcessId(), SyslogMessage.MAX_PROCESS_ID_LENGTH);
        writeNilableString(out, msg.getMessageId(), SyslogMessage.MAX_MESSAGE_ID_LENGTH);

        if (msg.getStructuredData().size() == 0) {
            out.writeByte('-');
        } else {
            encodeStructuredData(out, msg.getStructuredData());
        }

        if (msg.content() != null && msg.content().readableBytes() > 0) {
            out.writeByte(' ');
            out.writeBytes(msg.content());
        }
    }

    private void encodeStructuredData(ByteBuf out, Map<AsciiString, Map<AsciiString, String>> structuredData) {
        structuredData.forEach((key, values) -> {
            out.writeByte('[');
            writeSdName(out, key);
            if (values.size() == 0) {
                out.writeByte(' ').writeByte('-');
            } else {
                values.forEach((name, value) -> {
                    out.writeByte(' ');
                    writeSdName(out, name);
                    out.writeByte('=').writeByte('"');
                    writeSdValue(out, value);
                    out.writeByte('"');
                });
            }
            out.writeByte(']');
        });
    }

    private void writeSdValue(ByteBuf out, String value) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                builder.append('\\');
            }
            builder.append(c);
        }
        ByteBufUtil.writeUtf8(out, builder);
    }

    private void writeSdName(ByteBuf out, AsciiString value) {
        final int length = Math.min(value.length(), SyslogMessage.MAX_SD_NAME_LENGTH);
        for (int i = 0; i < length; i++) {
            final char c = value.charAt(i);
            if (SyslogMessage.isPrintableUsAscii(c) && c != '=' && c != ']' && c != '"') {
                out.writeByte(c);
            } else {
                throw new EncoderException("Invalid structured data name " + value);
            }
        }
    }

    void writeNilableString(ByteBuf out, CharSequence string, int maxLength) {
        if (string == null || string.length() == 0) {
            out.writeByte('-');
        } else {
            final int length = Math.min(string.length(), maxLength);
            for (int i = 0; i < length; i++) {
                final char c = string.charAt(i);
                if (SyslogMessage.isPrintableUsAscii(c)) {
                    out.writeByte(c);
                } else {
                    throw new EncoderException("Non-US ASCII character " + c + " in string: " + string);
                }
            }
        }
        out.writeByte(' ');
    }

}
