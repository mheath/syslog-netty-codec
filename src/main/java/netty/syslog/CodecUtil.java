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
import io.netty.handler.codec.DecoderException;
import io.netty.util.AsciiString;

class CodecUtil {
    static int readDigit(ByteBuf buffer) {
        int digit = 0;
        while (buffer.readableBytes() > 0 && Character.isDigit(peek(buffer))) {
            digit = digit * 10 + buffer.readByte() - '0';
        }
        return digit;
    }

    static ByteBuf writeDigit(ByteBuf buf, int digit) {
        buf.writeBytes(Integer.toString(digit).getBytes());
        return buf;
    }

    static byte peek(ByteBuf buffer) {
        return buffer.getByte(buffer.readerIndex());
    }

    static void expect(ByteBuf buffer, char c) {
        if (buffer.readByte() != c) {
            throw new DecoderException("Expected " + c + " at index " + buffer.readerIndex());
        }
    }

    static AsciiString readAsciiStringToSpace(ByteBuf buf, boolean checkNull) {
        return readAsciiStringToChar(buf, ' ', checkNull);
    }

    static AsciiString readAsciiStringToChar(ByteBuf buffer, char c, boolean checkNull) {
        if (checkNull && peek(buffer) == '-') {
            buffer.readerIndex(buffer.readerIndex() + 2);
            return null;
        }
        int length = -1;
        for (int i = buffer.readerIndex(); i < buffer.capacity(); i++) {
            if (buffer.getByte(i) == c) {
                length = i - buffer.readerIndex();
                break;
            }
        }
        if (length < 0) {
            length = buffer.readableBytes();
        }
        final AsciiString string = new AsciiString(buffer.readBytes(length).array(), false);
        buffer.readByte();
        return string;
    }
}
