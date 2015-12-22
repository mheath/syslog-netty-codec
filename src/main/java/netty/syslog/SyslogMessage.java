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
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.util.AsciiString;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a Syslog content as defined by RFC 5424. See http://tools.ietf.org/html/rfc5424#section-6.
 */
public class SyslogMessage extends DefaultByteBufHolder {

    public static final int MAX_HOSTNAME_LENGTH = 255;
    public static final int MAX_APPLICATION_NAME_LENGTH = 48;
    public static final int MAX_PROCESS_ID_LENGTH = 128;
    public static final int MAX_MESSAGE_ID_LENGTH = 32;
    public static final int MAX_SD_NAME_LENGTH = 32;

    private static final int PRINTUSASCII_LOW = 33;
    private static final int PRINTUSASCII_HIGH = 126;

    private static final byte[] UTF8_BOM = {(byte)0xEF, (byte)0xBB, (byte)0xBF};

    @SuppressWarnings("unused")
    public enum Facility {
        KERNEL,
        USER_LEVEL,
        MAIL,
        SYSTEM_DAEMON,
        SECURITY,
        SYSLOGD,
        LINE_PRINTER,
        NETWORK_NEWS,
        UUCP,
        CLOCK,
        SECURITY2,
        FTP,
        NTP,
        LOG_AUDIT,
        LOG_ALERTY,
        CLOCK2,
        LOCAL0,
        LOCAL1,
        LOCAL2,
        LOCAL3,
        LOCAL4,
        LOCAL5,
        LOCAL6,
        LOCAL7
    }

    @SuppressWarnings("unused")
    public enum Severity {
        EMERGENCY,
        ALERT,
        CRITICAL,
        ERROR,
        WARNING,
        NOTICE,
        INFORMATION,
        DEBUG
    }

    public static class MessageBuilder {
        private Facility facility = Facility.USER_LEVEL;
        private Severity severity = Severity.INFORMATION;
        private ZonedDateTime timestamp;
        private AsciiString hostname;
        private AsciiString applicationName;
        private AsciiString processId;
        private AsciiString messageId;
        private Map<AsciiString, Map<AsciiString, String>> structuredData;
        private ByteBuf content = Unpooled.EMPTY_BUFFER;

        public static MessageBuilder create() {
            return new MessageBuilder();
        }

        public MessageBuilder facility(Facility facility) {
            this.facility = facility;
            return this;
        }

        public MessageBuilder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public MessageBuilder timestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MessageBuilder hostname(CharSequence hostname) {
            this.hostname = toAsciiString(hostname);
            return this;
        }

        public MessageBuilder applicationName(CharSequence applicationName) {
            this.applicationName = toAsciiString(applicationName);
            return this;
        }

        public MessageBuilder processId(CharSequence processId) {
            this.processId = toAsciiString(processId);
            return this;
        }

        public MessageBuilder messageId(AsciiString messageId) {
            this.messageId = toAsciiString(messageId);
            return this;
        }

        public MessageBuilder addStructuredDataElement(CharSequence id) {
            setStructuredDataElement(id);
            return this;
        }

        public MessageBuilder addStructuredDataElement(CharSequence id, CharSequence name, String value) {
            setStructuredDataElement(id).put(toAsciiString(name), value);
            return this;
        }

        private Map<AsciiString, String> setStructuredDataElement(CharSequence id) {
            final AsciiString asciiName = toAsciiString(id);
            if (structuredData == null) {
                structuredData = new HashMap<>();
            }
            Map<AsciiString, String> params = structuredData.get(asciiName);
            if (params == null) {
                params = new HashMap<>();
                structuredData.put(asciiName, params);
            }
            return params;
        }

        public MessageBuilder content(ByteBuf message) {
            this.content = message;
            return this;
        }

        /**
         * Sets the message content as a raw UTF-8 encoded string.
         *
         * @param content the string to use as the message's content
         * @return {@code this} for method chaining
         */
        public MessageBuilder content(CharSequence content) {
            final ByteBuf buf = Unpooled.buffer(content.length());
            ByteBufUtil.writeUtf8(buf, content);
            this.content = buf;
            return this;
        }

        /**
         * Sets the message content as a BOM prefixed UTF-8 encoded string, per
         * <a href="http://tools.ietf.org/html/rfc5424#section-6.4">RFC-5424 Section 6.4</a>.
         *
         * @param content the string to use as the message's content
         * @return {@code this} for method chaining
         */
        public MessageBuilder utf8Content(CharSequence content) {
            final ByteBuf buf = Unpooled.buffer(content.length() + 3);
            buf.writeBytes(UTF8_BOM);
            ByteBufUtil.writeUtf8(buf, content);
            this.content = buf;
            return this;
        }

        public SyslogMessage build(boolean validate) {
            if (validate) {
                validatePrintUsAscii(MAX_HOSTNAME_LENGTH, hostname);
                validatePrintUsAscii(MAX_APPLICATION_NAME_LENGTH, applicationName);
                validatePrintUsAscii(MAX_PROCESS_ID_LENGTH, processId);
                validatePrintUsAscii(MAX_MESSAGE_ID_LENGTH, messageId);

                if (structuredData != null) {
                    structuredData.forEach((key, value) -> validateSdName(key));
                }
            }
            return new SyslogMessage(this);
        }

        /**
         * Validates the string is a PRINTUSASCII string per RFC-5424 section 6.
         *
         * @param maxLength the maximum length of the string
         * @param string the string to validate
         */
        private void validatePrintUsAscii(int maxLength, AsciiString string) {
            if (string == null) {
                return;
            }
            if (string.length() > maxLength) {
                throw new IllegalArgumentException("String greather than " + maxLength + " characters: " + string);
            }
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isPrintableUsAscii(c)) {
                    throw new IllegalArgumentException("Invalid character '" + c + "' in string: " + string);
                }
            }
        }

        private void validateSdName(AsciiString string) {
            if (string == null) {
                return;
            }
            if (string.length() > MAX_SD_NAME_LENGTH) {
                throw new IllegalArgumentException("String is longer than 32 characters: " + string);
            }
            for (int i = 0; i < string.length(); i++) {
                final char c = string.charAt(i);
                if (!isPrintableUsAscii(c) || c == '=' || c == ' ' || c == ']' || c == '"') {
                    throw new IllegalArgumentException(
                            "Illegal character '" + c + "' at " + i + " in string: " + string);
                }
            }
        }

        private AsciiString toAsciiString(CharSequence string) {
            if (string == null) {
                return null;
            }
            return (string instanceof AsciiString)? (AsciiString) string : new AsciiString(string);
        }

    }

    static boolean isPrintableUsAscii(char c) {
        return c >= PRINTUSASCII_LOW && c <= PRINTUSASCII_HIGH;
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    private final Facility facility;
    private final Severity severity;
    private final ZonedDateTime timestamp;
    private final AsciiString hostname;
    private final AsciiString applicationName;
    private final AsciiString processId;
    private final AsciiString messageId;
    private final Map<AsciiString, Map<AsciiString, String>> structuredData;

    private SyslogMessage(MessageBuilder builder) {
        super(builder.content);
        this.facility = builder.facility;
        this.severity = builder.severity;
        this.timestamp = builder.timestamp;
        this.hostname = builder.hostname;
        this.applicationName = builder.applicationName;
        this.processId = builder.processId;
        this.messageId = builder.messageId;
        if (builder.structuredData == null) {
            this.structuredData = Collections.emptyMap();
        } else {
            this.structuredData = new HashMap<>(builder.structuredData);
        }
    }

    public Facility getFacility() {
        return facility;
    }

    public Severity getSeverity() {
        return severity;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public AsciiString getHostname() {
        return hostname;
    }

    public AsciiString getApplicationName() {
        return applicationName;
    }

    public AsciiString getProcessId() {
        return processId;
    }

    public AsciiString getMessageId() {
        return messageId;
    }

    public String contentAsUtf8() {
        final ByteBuf buf = content();
        final int idx = buf.readerIndex();
        if (buf.getByte(idx) == UTF8_BOM[0]
                && buf.getByte(idx + 1) == UTF8_BOM[1]
                && buf.getByte(idx + 2) == UTF8_BOM[2]) {
            buf.readerIndex(idx + 3);
        }
        return buf.toString(StandardCharsets.UTF_8);
    }

    public Map<AsciiString, Map<AsciiString, String>> getStructuredData() {
        return structuredData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SyslogMessage message = (SyslogMessage) o;
        return Objects.equals(facility, message.facility) &&
               Objects.equals(severity, message.severity) &&
               Objects.equals(timestamp, message.timestamp) &&
               Objects.equals(hostname, message.hostname) &&
               Objects.equals(applicationName, message.applicationName) &&
               Objects.equals(processId, message.processId) &&
               Objects.equals(messageId, message.messageId) &&
               Objects.equals(structuredData, message.structuredData);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(facility, severity, timestamp, hostname, applicationName, processId, messageId, structuredData);
    }

    @Override
    public String toString() {
        return "SyslogMessage{" +
               "facility=" + facility +
               ", severity=" + severity +
               ", timestamp=" + timestamp +
               ", hostname='" + hostname + '\'' +
               ", applicationName='" + applicationName + '\'' +
               ", processId='" + processId + '\'' +
               ", messageId='" + messageId + '\'' +
               ", structuredData=" + structuredData +
               ", message='" + content().duplicate().toString(StandardCharsets.UTF_8) + '\'' +
               '}';
    }
}
