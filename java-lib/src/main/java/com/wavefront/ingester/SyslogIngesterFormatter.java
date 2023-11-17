package com.wavefront.ingester;

import com.wavefront.data.ParseException;
import org.apache.commons.lang.StringUtils;
import org.springframework.integration.syslog.SyslogHeaders;
import wavefront.report.Annotation;
import wavefront.report.ReportLog;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyslogIngesterFormatter extends AbstractIngesterFormatter<ReportLog> {
    private final String NONE = "none";
    private final String APPLICATION = "application";
    private final String SERVICE = "service";
    private final String LOG_LEVEL = "log_level";
    private final String ERROR_NAME = "error_name";

    private final String SYSLOG_HOST = "syslog_host";

    private static final String NILVALUE = "-";

    private final SyslogParser parser = new SyslogParser();

    private SyslogIngesterFormatter(List<FormatterElement<ReportLog>> elements) {
        super(elements);
    }

    public static class ReportLogIngesterFormatBuilder extends IngesterFormatBuilder<ReportLog> {
        @Override
        public SyslogIngesterFormatter build() {
            return new SyslogIngesterFormatter(elements);
        }
    }

    public static IngesterFormatBuilder<ReportLog> newBuilder() {
        return new ReportLogIngesterFormatBuilder();
    }

    // Doesn't support customLogTimestampTags and customLogMessageTags
    // due to the syslog parsing library not currently supporting those cases
    @Override
    public ReportLog drive(
            String syslogMessage,
            @Nullable Supplier<String> defaultHostNameSupplier,
            String customerId,
            @Nullable List<String> customSourceTags,
            @Nullable List<String> customLogTimestampTags,
            @Nullable List<String> customLogMessageTags,
            List<String> customLogApplicationTags,
            List<String> customLogServiceTags,
            @Nullable List<String> customLogLevelTags,
            @Nullable List<String> customLogExceptionTags,
            @Nullable IngesterContext ingesterContext) {
        final ReportLog log = new ReportLog();
        List<Annotation> annotations = new ArrayList<>();

        try {
            parser.parse(syslogMessage, annotations);
            log.setAnnotations(annotations);

            String host = AbstractIngesterFormatter.getHostAndNormalizeTags(log.getAnnotations(), customSourceTags, false);
            if (host == null) {
                if (defaultHostNameSupplier == null) {
                    host = "unknown";
                } else {
                    host = defaultHostNameSupplier.get();
                }
            }
            log.setHost(host);

            // Will always be the timestamp from the syslog message as our syslog parsing library doesn't support it being nil
            Long timestamp = AbstractIngesterFormatter.getLogTimestamp(log.getAnnotations(), null);
            log.setTimestamp(timestamp);

            // Will always be the message from the syslog message as our syslog parsing library doesn't support it being nil
            String message = AbstractIngesterFormatter.getLogMessage(log.getAnnotations(), customLogMessageTags);
            log.setMessage(message);

            String application = AbstractIngesterFormatter.getLogApplication(log.getAnnotations(), customLogApplicationTags);
            if (!StringUtils.equalsIgnoreCase(application, NONE)) {
                log.getAnnotations().add(Annotation.newBuilder().setKey(APPLICATION).setValue(application).build());
            }

            String service = AbstractIngesterFormatter.getLogService(log.getAnnotations(), customLogServiceTags);
            if (!StringUtils.equalsIgnoreCase(service, NONE)) {
                log.getAnnotations().add(Annotation.newBuilder().setKey(SERVICE).setValue(service).build());
            }

            String level = AbstractIngesterFormatter.getLogLevel(log.getAnnotations(), customLogLevelTags);
            if (!StringUtils.equalsIgnoreCase(level, "")) {
                log.getAnnotations().add(Annotation.newBuilder().setKey(LOG_LEVEL).setValue(level).build());
            }

            String exception = AbstractIngesterFormatter.getLogException(log.getAnnotations(), customLogExceptionTags);
            if (!StringUtils.equalsIgnoreCase(exception, "")) {
                log.getAnnotations().add(Annotation.newBuilder().setKey(ERROR_NAME).setValue(exception).build());
            }

            return log;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
