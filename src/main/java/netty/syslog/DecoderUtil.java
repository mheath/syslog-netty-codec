/*
 *   Copyright (c) 2014 Intellectual Reserve, Inc.  All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

class DecoderUtil {
	static int readDigit(ByteBuf buffer) {
		int digit = 0;
		while (buffer.readableBytes() > 0 && Character.isDigit(peek(buffer))) {
			digit = digit * 10 + buffer.readByte() - '0';
		}
		return digit;
	}

	static byte peek(ByteBuf buffer) {
		return buffer.getByte(buffer.readerIndex());
	}

	static void expect(ByteBuf buffer, char c) {
		if (buffer.readByte() != c) {
			throw new DecoderException("Expected " + c + " at index " + buffer.readerIndex());
		}
	}

	static String readStringToSpace(ByteBuf buffer, boolean checkNull) {
		if (checkNull && peek(buffer) == '-') {
			buffer.readByte();
			return null;
		}
		int length = -1;
		for (int i = buffer.readerIndex(); i < buffer.capacity(); i++) {
			if (buffer.getByte(i) == ' ') {
				length = i - buffer.readerIndex();
				break;
			}
		}
		if (length < 0) {
			length = buffer.readableBytes();
		}
		return new String(buffer.readBytes(length).array());
	}
}
