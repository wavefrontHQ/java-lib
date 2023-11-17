package com.wavefront.ingester;

import com.wavefront.data.ParseException;
import org.springframework.integration.syslog.RFC5424SyslogParser;
import org.springframework.integration.syslog.SyslogHeaders;
import wavefront.report.Annotation;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Doesn't support nil values for the timestamp and message syslog fields
// due to the syslog parsing library not currently supporting those cases
public class SyslogParser {
    private static final String NILVALUE = "-";

    private final RFC5424SyslogParser parser = new RFC5424SyslogParser();

    private Pattern structuredDataTagRegex;

    public SyslogParser() {
        structuredDataTagRegex = Pattern.compile(" (?:([^=]+)=\\\"([^\\\"]*)\\\")");
    }

    public void parse(String syslogMessage, List<Annotation> annotations) {
        char firstChar = syslogMessage.charAt(0);
        if (firstChar != '<' && java.lang.Character.isDigit(firstChar)) {
            int whiteSpaceIndex = syslogMessage.indexOf(' ');
            int octetCount = Integer.parseInt(syslogMessage.substring(0, whiteSpaceIndex));
            syslogMessage = syslogMessage.substring(whiteSpaceIndex);

            // Octet count is a count of the entire message including the space
            if (syslogMessage.getBytes(StandardCharsets.UTF_8).length != octetCount) {
                throw new ParseException("Mismatched syslog message octet count");
            }

            // Syslog message starts after the octet count and space
            int lessthanIndex = syslogMessage.indexOf('<');
            syslogMessage = syslogMessage.substring(lessthanIndex);
        }

        Map<String, ?> fields = parser.parse(syslogMessage, 0, false);

        if (fields.get(SyslogHeaders.DECODE_ERRORS).toString().equals("true")) {
            throw new ParseException(fields.get(SyslogHeaders.ERRORS).toString());
        }

        if (fields.get(SyslogHeaders.TIMESTAMP) == null) {
            throw new ParseException("Timestamp is a required syslog field");
        }

        String logTimestamp = fields.get(SyslogHeaders.TIMESTAMP).toString();
        Instant timestamp = ZonedDateTime.parse(logTimestamp).toInstant();
        long timestampEpochMilliSecond = timestamp.toEpochMilli();
        annotations.add(Annotation.newBuilder().setKey("timestamp").setValue(Long.toString(timestampEpochMilliSecond)).build());

        if (fields.get(SyslogHeaders.MESSAGE) == null) {
            throw new ParseException("Message is a required syslog field");
        }

        String logMessage = fields.get(SyslogHeaders.MESSAGE).toString();
        annotations.add(Annotation.newBuilder().setKey("message").setValue(logMessage).build());

        String host = fields.get(SyslogHeaders.HOST).toString();
        if (!NILVALUE.equals(fields.get(SyslogHeaders.HOST).toString())) {
            annotations.add(Annotation.newBuilder().setKey("host").setValue(host).build());
        }

        // Adding as a separate tag so we still have access to it as a tag
        annotations.add(Annotation.newBuilder().setKey("syslog_host").setValue(host).build());

        if (fields.get(SyslogHeaders.STRUCTURED_DATA) != null) {
            String structuredData = fields.get(SyslogHeaders.STRUCTURED_DATA).toString();
            Matcher m = structuredDataTagRegex.matcher(structuredData);

            while (m.find()) {
                annotations.add(Annotation.newBuilder().setKey(m.group(1)).setValue(m.group(2)).build());
            }
        }
    }
}
