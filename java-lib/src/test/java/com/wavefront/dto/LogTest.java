package com.wavefront.dto;

import junit.framework.TestCase;
import org.junit.Test;
import wavefront.report.Annotation;
import wavefront.report.ReportLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class LogTest {
    @Test
    public void testBasicLog() {

        Log log1 = new Log(new ReportLog(1234567L, "oops", "myHost", new ArrayList<>()));

        assertEquals("{\"log_timestamp\":1234567, \"text\":\"oops\", \"source\":\"myHost\"}", log1.toString());
    }

    @Test
    public void testLogWithAnnotations() {

        Log log1 = new Log(new ReportLog(1234567L, "oops", "myHost", Collections.singletonList(
                new Annotation("key1", "value1")
        )));

        assertEquals("{\"key1\":\"value1\", \"log_timestamp\":1234567, \"text\":\"oops\", \"source\":\"myHost\"}", log1.toString());
    }
}