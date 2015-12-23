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
    static int readDigit(ByteBuf buf) {
        int digit = 0;
        while (buf.readableBytes() > 0 && Character.isDigit(peek(buf))) {
            digit = digit * 10 + buf.readByte() - '0';
        }
        return digit;
    }

    static ByteBuf writeDigit(ByteBuf buf, int digit) {
        buf.writeBytes(Integer.toString(digit).getBytes());
        return buf;
    }

    static byte peek(ByteBuf buf) {
        return buf.getByte(buf.readerIndex());
    }

    static void expect(ByteBuf buf, char c) {
        if (buf.readByte() != c) {
            throw new DecoderException("Expected " + c + " at index " + buf.readerIndex());
        }
    }

    static AsciiString readAsciiStringToSpace(ByteBuf buf, boolean checkNull) {
        return readAsciiStringToChar(buf, ' ', checkNull);
    }

    static AsciiString readAsciiStringToChar(ByteBuf buf, char c, boolean checkNull) {
        if (checkNull && peek(buf) == '-') {
            buf.readerIndex(buf.readerIndex() + 2);
            return null;
        }
        int length = -1;
        for (int i = buf.readerIndex(); i < buf.capacity(); i++) {
            if (buf.getByte(i) == c) {
                length = i - buf.readerIndex();
                break;
            }
        }
        if (length < 0) {
            length = buf.readableBytes();
        }
        final AsciiString string = new AsciiString(buf.readBytes(length).array(), false);
        buf.readByte();
        return string;
    }
}
