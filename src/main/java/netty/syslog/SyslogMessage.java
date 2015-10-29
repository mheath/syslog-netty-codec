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
		private ZonedDateTime timestamp = ZonedDateTime.now();
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

		public MessageBuilder addStructuredDataElement(CharSequence name, CharSequence parameterName, String parameterValue) {
			final AsciiString asciiName = toAsciiString(name);
			if (structuredData == null) {
				structuredData = new HashMap<>();
			}
			Map<AsciiString, String> params = structuredData.get(asciiName);
			if (params == null) {
				params = new HashMap<>();
				structuredData.put(asciiName, params);
			}
			params.put(toAsciiString(parameterName), parameterValue);
			return this;
		}

		public MessageBuilder content(ByteBuf message) {
			this.content = message;
			return this;
		}

		public SyslogMessage build() {
			return new SyslogMessage(this);
		}

		private AsciiString toAsciiString(CharSequence string) {
			if (string == null) {
				return null;
			}
			return (string instanceof AsciiString) ? (AsciiString) string : new AsciiString(string);
		}

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

	public Map<AsciiString, Map<AsciiString, String>> getStructuredData() {
		return structuredData;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
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
		return Objects.hash(facility, severity, timestamp, hostname, applicationName, processId, messageId, structuredData);
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
