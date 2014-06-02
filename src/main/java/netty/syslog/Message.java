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

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Syslog content as defined by RFC 5424. See http://tools.ietf.org/html/rfc5424#section-6.
 */
public class Message extends DefaultByteBufHolder {

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
		private String hostname;
		private String applicationName;
		private String processId;
		private String messageId;
		private Map<String, Map<String, String>> structuredData;
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

		public MessageBuilder hostname(String hostname) {
			this.hostname = hostname;
			return this;
		}

		public MessageBuilder applicationName(String applicationName) {
			this.applicationName = applicationName;
			return this;
		}

		public MessageBuilder processId(String processId) {
			this.processId = processId;
			return this;
		}

		public MessageBuilder messageId(String messageId) {
			this.messageId = messageId;
			return this;
		}

		public MessageBuilder addStructuredData(String name, String key, String value) {
			if (structuredData == null) {
				structuredData = new HashMap<>();
			}
			Map<String, String> params = structuredData.get(name);
			if (params == null) {
				params = new HashMap<>();
				structuredData.put(name, params);
			}
			params.put(key, value);
			return this;
		}

		public MessageBuilder content(ByteBuf message) {
			this.content = message;
			return this;
		}

		public Message build() {
			return new Message(facility, severity, timestamp, hostname, applicationName, processId, messageId, structuredData, content);
		}
	}

	private final Facility facility;
	private final Severity severity;
	private final ZonedDateTime timestamp;
	private final String hostname;
	private final String applicationName;
	private final String processId;
	private final String messageId;
	private final Map<String, Map<String, String>> structuredData;

	private Message(Facility facility, Severity severity, ZonedDateTime timestamp, String hostname, String applicationName, String processId, String messageId, Map<String, Map<String, String>> structuredData, ByteBuf message) {
		super(message);
		this.facility = facility;
		this.severity = severity;
		this.timestamp = timestamp;
		this.hostname = hostname;
		this.applicationName = applicationName;
		this.processId = processId;
		this.messageId = messageId;
		if (structuredData == null) {
			this.structuredData = Collections.emptyMap();
		} else {
			this.structuredData = structuredData;
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

	public String getHostname() {
		return hostname;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getProcessId() {
		return processId;
	}

	public String getMessageId() {
		return messageId;
	}

	public Map<String, Map<String, String>> getStructuredData() {
		return structuredData;
	}

	@Override
	public String toString() {
		return "Message{" +
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
