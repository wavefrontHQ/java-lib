package com.wavefront.ingester;

import javax.annotation.Nullable;

import org.springframework.integration.syslog.RFC5424SyslogParser;
import wavefront.report.Annotation;
import wavefront.report.ReportLog;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyslogDecoder implements ReportableEntityDecoder<String, ReportLog> {
    private static final AbstractIngesterFormatter<ReportLog> FORMAT =
            SyslogIngesterFormatter.newBuilder().build();

    private final Supplier<String> hostNameSupplier;
    private List<String> customSourceTags;
    private List<String> customLogTimestampTags;
    private List<String> customLogMessageTags;
    private List<String> customApplicationTags;
    private List<String> customServiceTags;
    private List<String> customLevelTags;
    private List<String> customExceptionTags;

    public SyslogDecoder(@Nullable Supplier<String> hostNameSupplier,
                         List<String> customSourceTags, List<String> customLogTimestampTags, List<String> customLogMessageTags,
                         List<String> customApplicationTags, List<String> customServiceTags,
                         List<String> customLevelTags, List<String> customExceptionTags) {
        this.hostNameSupplier = hostNameSupplier;
        this.customSourceTags = customSourceTags;
        this.customLogTimestampTags = customLogTimestampTags;
        this.customLogMessageTags = customLogMessageTags;
        this.customApplicationTags = customApplicationTags;
        this.customServiceTags = customServiceTags;
        this.customLevelTags = customLevelTags;
        this.customExceptionTags = customExceptionTags;
    }

    @Override
    public void decode(String msg, List<ReportLog> out, String customerId, @Nullable IngesterContext ctx) {
        ReportLog log = FORMAT.drive(msg, hostNameSupplier, "default", customSourceTags, customLogTimestampTags,
                customLogMessageTags, customApplicationTags, customServiceTags, customLevelTags, customExceptionTags, ctx);

        if (out != null) {
            out.add(log);
        }
    }
}
