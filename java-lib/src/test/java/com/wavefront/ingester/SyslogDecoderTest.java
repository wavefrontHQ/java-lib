package com.wavefront.ingester;

import com.wavefront.common.Clock;
import org.junit.Before;
import org.junit.Test;
import wavefront.report.Annotation;
import wavefront.report.ReportLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.function.Supplier;

import static junit.framework.TestCase.*;

public class SyslogDecoderTest {
    private final String defaultHost = "unitTest";
    private final Supplier<String> defaultHostSupplier = () -> defaultHost;

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

    private ReportableEntityDecoder<String, ReportLog> decoder;

    private List<ReportLog> reportLogs;

    @Before
    public void setup() {
        reportLogs = new ArrayList<>();
    }

    @Test
    public void testTraditionalFrameTypeSyslogMessage() {
        SyslogDecoder decoder = new SyslogDecoder(defaultHostSupplier, null, null, null, null, null, null, null);
        decoder.decode(TRADITIONAL_FRAME_SYSLOG, reportLogs, "none", null);
        assertEquals(1, reportLogs.size());

        ReportLog log = reportLogs.get(0);
        assertEquals(1666779024000L, log.getTimestamp());
        assertEquals("Some message", log.getMessage());
        assertEquals(4, log.getAnnotations().size());

        assertEquals(Annotation.newBuilder().setKey("syslog_host").setValue("some-hostname").build(), log.getAnnotations().get(0));
        assertEquals(Annotation.newBuilder().setKey("key1").setValue("val1").build(), log.getAnnotations().get(1));
        assertEquals(Annotation.newBuilder().setKey("key2").setValue("val2").build(), log.getAnnotations().get(2));
        assertEquals(Annotation.newBuilder().setKey("key3").setValue("val3").build(), log.getAnnotations().get(3));
    }

    @Test
    public void testOctetCountFrameTypeSyslogMessage() {
        SyslogDecoder decoder = new SyslogDecoder(defaultHostSupplier, null, null, null, null, null, null, null);
        decoder.decode(OCTET_COUNT_FRAME_SYSLOG, reportLogs, "none", null);
        assertEquals(1, reportLogs.size());

        ReportLog log = reportLogs.get(0);
        assertEquals(1640384401438L, log.getTimestamp());
        assertEquals("Another message", log.getMessage());
        assertEquals("another-hostname", log.getHost());
        assertEquals(2, log.getAnnotations().size());

        assertEquals(Annotation.newBuilder().setKey("syslog_host").setValue("another-hostname").build(), log.getAnnotations().get(0));
        assertEquals(Annotation.newBuilder().setKey("key1").setValue("val1").build(), log.getAnnotations().get(1));
    }

    @Test
    public void testNoHostUsesDefault() {
        SyslogDecoder decoder = new SyslogDecoder(defaultHostSupplier, null, null, null, null, null, null, null);

        String customTagsSyslog =
                "<14>1 2021-12-24T22:20:01.438069+00:00 " +
                        "- app-name ABC message-id - " +
                        "Some message";

        decoder.decode(customTagsSyslog, reportLogs, "none", null);
        assertEquals(1, reportLogs.size());

        ReportLog log = reportLogs.get(0);
        assertEquals(1640384401438L, log.getTimestamp());
        assertEquals("Some message", log.getMessage());
        assertEquals(defaultHostSupplier.get(), log.getHost());
    }

    @Test
    public void testNoHostWithNoDefault() {
        SyslogDecoder decoder = new SyslogDecoder(null, null, null, null, null, null, null, null);

        String customTagsSyslog =
                "<14>1 2021-12-24T22:20:01.438069+00:00 " +
                        "- app-name ABC message-id - " +
                        "Some message";

        decoder.decode(customTagsSyslog, reportLogs, "none", null);
        assertEquals(1, reportLogs.size());

        ReportLog log = reportLogs.get(0);
        assertEquals(1640384401438L, log.getTimestamp());
        assertEquals("Some message", log.getMessage());
        assertEquals("unknown", log.getHost());
    }

    @Test
    public void testCustomTags() {
        SyslogDecoder decoder = new SyslogDecoder(defaultHostSupplier, Collections.singletonList("customHost"),
                Collections.singletonList("customTimestamp"), Collections.singletonList("customMessage"),
                Collections.singletonList("customApplication"), Collections.singletonList("customService"),
                Collections.singletonList("customLevel"), Collections.singletonList("customException"));

        long curTime = Clock.now();
        String customTagsSyslog =
                "<14>1 2021-12-24T22:20:01.438069+00:00 " +
                        "- app-name ABC message-id " +
                        "[tags@12345 " +
                        "customHost=\"custom host\" " +
                        "customTimestamp=\"" + curTime + "\" " +
                        "customMessage=\"custom message\" " +
                        "customApplication=\"custom application\" " +
                        "customService=\"custom service\" " +
                        "customLevel=\"custom level\" " +
                        "customException=\"custom exception\"] " +
                        "Base message";

        decoder.decode(customTagsSyslog, reportLogs, "none", null);
        assertEquals(1, reportLogs.size());

        ReportLog log = reportLogs.get(0);
        // Will be the syslog timestamp due to the syslog parsing library not accepting null for timestamps
        assertEquals(1640384401438L, log.getTimestamp());
        // Will be the syslog message due to the syslog parsing library not accepting null for messages
        assertEquals("Base message", log.getMessage());
        assertEquals("custom host", log.getHost());

        assertEquals(8, log.getAnnotations().size());

        assertEquals(Annotation.newBuilder().setKey("syslog_host").setValue("-").build(), log.getAnnotations().get(0));
        // Unlike other custom tags, the custom host tag doesn't get removed when turning it into the top level host field
        assertEquals(Annotation.newBuilder().setKey("customHost").setValue("custom host").build(), log.getAnnotations().get(1));
        assertEquals(Annotation.newBuilder().setKey("customTimestamp").setValue(Long.toString(curTime)).build(), log.getAnnotations().get(2));
        assertEquals(Annotation.newBuilder().setKey("customMessage").setValue("custom message").build(), log.getAnnotations().get(3));
        assertEquals(Annotation.newBuilder().setKey("application").setValue("custom application").build(), log.getAnnotations().get(4));
        assertEquals(Annotation.newBuilder().setKey("service").setValue("custom service").build(), log.getAnnotations().get(5));
        assertEquals(Annotation.newBuilder().setKey("log_level").setValue("custom level").build(), log.getAnnotations().get(6));
        assertEquals(Annotation.newBuilder().setKey("error_name").setValue("custom exception").build(), log.getAnnotations().get(7));
    }

    @Test
    public void testInvalidSyslogMessage() {
        SyslogDecoder decoder = new SyslogDecoder(defaultHostSupplier, null, null, null, null, null, null, null);

        String notASyslogMessage = "invalid";
        decoder.decode(notASyslogMessage, reportLogs, "none", null);
        assertEquals(reportLogs.size(), 1);
        ReportLog log = reportLogs.get(0);
        assertNull(log);
    }

    @Test
    public void testInvalidOctetCountSyslogMessage() {
        SyslogDecoder decoder = new SyslogDecoder(defaultHostSupplier, null, null, null, null, null, null, null);

        String invalidOctetCount =
                "10 <14>1 2021-12-24T22:20:01.438069+00:00 " +
                        "hostname app-name ABC message-id - " +
                        "Base message";
        decoder.decode(invalidOctetCount, reportLogs, "none", null);
        assertEquals(reportLogs.size(), 1);
        ReportLog log = reportLogs.get(0);
        assertNull(log);
    }

    // This is a valid syslog message, but our parsing library doesn't currently support it
    @Test
    public void testInvalidNoTimestampSyslogMessages() {
        SyslogDecoder decoder = new SyslogDecoder(defaultHostSupplier, null, null, null, null, null, null, null);

        String noTimestamp =
                "<14>1 - " +
                        "hostname app-name ABC message-id - " +
                        "Base message";
        decoder.decode(noTimestamp, reportLogs, "none", null);
        assertEquals(reportLogs.size(), 1);
        ReportLog log = reportLogs.get(0);
        assertNull(log);
    }

    // This is a valid syslog message, but our parsing library doesn't currently support it
    @Test
    public void testInvalidNoMessageSyslogMessages() {
        SyslogDecoder decoder = new SyslogDecoder(defaultHostSupplier, null, null, null, null, null, null, null);

        String noMessage =
                "<14>1 2021-12-24T22:20:01.438069+00:00 " +
                        "hostname app-name ABC message-id -";
        decoder.decode(noMessage, reportLogs, "none", null);
        assertEquals(reportLogs.size(), 1);
        ReportLog log = reportLogs.get(0);
        assertNull(log);
    }
}
