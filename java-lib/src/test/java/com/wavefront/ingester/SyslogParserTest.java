package com.wavefront.ingester;

import com.wavefront.data.ParseException;
import org.junit.Before;
import org.junit.Test;
import wavefront.report.Annotation;

import java.util.*;

import static org.junit.Assert.*;

public class SyslogParserTest {
    private static final String TRADITIONAL_FRAME_SYSLOG =
            "<44>1 2022-10-26T10:10:24+00:00 " +
                    "some-hostname process-name proc-id message-id " +
                    "[instance@12345 " +
                    "key1=\"val1\" " +
                    "key2=\"val2\" " +
                    "key3=\"val3\"] " +
                    "Some message";

    private static final String OCTET_COUNT_FRAME_SYSLOG =
            "129 <14>1 2021-12-24T22:20:01.438069+00:00 " +
                    "another-hostname another-app-name ABC message-id " +
                    "[tags@12345 " +
                    "key1=\"val1\"] " +
                    "Another message";
    private SyslogParser parser;
    private List<Annotation> annotations;

    @Before
    public void setUp() {
        parser = new SyslogParser();
        annotations = new ArrayList<>();
    }

    @Test
    public void testTraditionalFrameTypeSyslogMessage() {
        parser.parse(TRADITIONAL_FRAME_SYSLOG, annotations);

        assertEquals(7, annotations.size());

        assertEquals(Annotation.newBuilder().setKey("timestamp").setValue("1666779024000").build(), annotations.get(0));
        assertEquals(Annotation.newBuilder().setKey("message").setValue("Some message").build(), annotations.get(1));
        assertEquals(Annotation.newBuilder().setKey("host").setValue("some-hostname").build(), annotations.get(2));
        assertEquals(Annotation.newBuilder().setKey("syslog_host").setValue("some-hostname").build(), annotations.get(3));

        assertEquals(Annotation.newBuilder().setKey("key1").setValue("val1").build(), annotations.get(4));
        assertEquals(Annotation.newBuilder().setKey("key2").setValue("val2").build(), annotations.get(5));
        assertEquals(Annotation.newBuilder().setKey("key3").setValue("val3").build(), annotations.get(6));
    }

    @Test
    public void testOctetCountFrameTypeSyslogMessage() {
        parser.parse(OCTET_COUNT_FRAME_SYSLOG, annotations);

        assertEquals(5, annotations.size());

        assertEquals(Annotation.newBuilder().setKey("timestamp").setValue("1640384401438").build(), annotations.get(0));
        assertEquals(Annotation.newBuilder().setKey("message").setValue("Another message").build(), annotations.get(1));
        assertEquals(Annotation.newBuilder().setKey("host").setValue("another-hostname").build(), annotations.get(2));
        assertEquals(Annotation.newBuilder().setKey("syslog_host").setValue("another-hostname").build(), annotations.get(3));

        assertEquals(Annotation.newBuilder().setKey("key1").setValue("val1").build(), annotations.get(4));
    }

    @Test
    public void testNoHost() {
        String noHostSyslogMessage = "<14>1 2021-12-24T22:20:01.438069+00:00 - app-name proc-id message-id [tags@12345 key1=val1] message";

        parser.parse(noHostSyslogMessage, annotations);

        assertEquals(3, annotations.size());

        assertEquals(Annotation.newBuilder().setKey("timestamp").setValue("1640384401438").build(), annotations.get(0));
        assertEquals(Annotation.newBuilder().setKey("message").setValue("message").build(), annotations.get(1));
        assertEquals(Annotation.newBuilder().setKey("syslog_host").setValue("-").build(), annotations.get(2));
    }

    @Test
    public void testNoStructuredData() {
        String noStructuredDataSyslogMessage = "<14>1 2021-12-24T22:20:01.438069+00:00 hostname app-name proc-id message-id - message";

        parser.parse(noStructuredDataSyslogMessage, annotations);

        assertEquals(4, annotations.size());

        assertEquals(Annotation.newBuilder().setKey("timestamp").setValue("1640384401438").build(), annotations.get(0));
        assertEquals(Annotation.newBuilder().setKey("message").setValue("message").build(), annotations.get(1));
        assertEquals(Annotation.newBuilder().setKey("host").setValue("hostname").build(), annotations.get(2));
        assertEquals(Annotation.newBuilder().setKey("syslog_host").setValue("hostname").build(), annotations.get(3));
    }

    @Test
    public void testOctetCountDiffersFromCharacterCount() {
        // The message character count is 111, but the octet count is 115 due to characters being 1-4 bytes
        String syslogMessageWithMultibyteCharacters = "115 <44>1 2022-10-26T10:10:24.727681+00:00 some-hostname process-name proc-id message-id [tags@12345 key1=val1] 世界";
        parser.parse(syslogMessageWithMultibyteCharacters, annotations);

        assertEquals(4, annotations.size());

        assertEquals(Annotation.newBuilder().setKey("timestamp").setValue("1666779024727").build(), annotations.get(0));
        assertEquals(Annotation.newBuilder().setKey("message").setValue("世界").build(), annotations.get(1));
        assertEquals(Annotation.newBuilder().setKey("host").setValue("some-hostname").build(), annotations.get(2));
        assertEquals(Annotation.newBuilder().setKey("syslog_host").setValue("some-hostname").build(), annotations.get(3));
    }

    @Test
    public void testOctetCountIsIncorrectlySetToCharacterCountThrowsException() {
        // The message character count is 111, but the octet count is 115 due to characters being 1-4 bytes
        String invalidSyslogMessageWithMultibyteCharacters = "111 <44>1 2022-10-26T10:10:24.727681+00:00 some-hostname process-name proc-id message-id [tags@12345 key1=val1] 世界";
        Exception e = assertThrows(ParseException.class, () -> parser.parse(invalidSyslogMessageWithMultibyteCharacters, annotations));
        assertEquals("Mismatched syslog message octet count", e.getMessage());
    }

    // This is a valid syslog message, but our parsing library doesn't currently support it
    @Test
    public void testNoTimestampThrowsException() {
        String noTimestampSyslogMessage = "<44>1 - hostname app-name proc-id - - some message";
        Exception e = assertThrows(ParseException.class, () -> parser.parse(noTimestampSyslogMessage, annotations));
        assertEquals("Timestamp is a required syslog field", e.getMessage());
    }

    // This is a valid syslog message, but our parsing library doesn't currently support it
    @Test
    public void testNoMessageThrowsException() {
        String noMessageSyslogMessage = "<14>1 2021-12-24T22:20:01.438069+00:00 hostname app-name proc-id - -";
        assertThrows(ParseException.class, () -> parser.parse(noMessageSyslogMessage, annotations));
    }

    @Test
    public void testInvalidOctetCountThrowsException() {
        // Correct octet_count is 85
        String invalidOctetCountSyslogMessage = "10 <44>1 2022-10-26T10:10:24.727681+00:00 some-hostname process-name - - - Some message";
        Exception e = assertThrows(ParseException.class, () -> parser.parse(invalidOctetCountSyslogMessage, annotations));
        assertEquals("Mismatched syslog message octet count", e.getMessage());
    }

    @Test
    public void testInvalidSyslogMessageThrowsException() {
        String invalidSyslogMessage = "invalid data";
        assertThrows(ParseException.class, () -> parser.parse(invalidSyslogMessage, annotations));
    }
}
