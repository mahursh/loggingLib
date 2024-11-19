package com.ada.logginglib.encoder;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.encoder.EncoderBase;

import java.nio.charset.StandardCharsets;

public class CustomAccessLogEncoder extends EncoderBase<IAccessEvent>
{
    @Override
    public byte[] headerBytes() {
        return new byte[0];
    }

    @Override
    public byte[] encode(IAccessEvent event) {
        StringBuilder logBuilder = new StringBuilder();

        // Add general log parts
        logBuilder.append("timestamp=").append(event.getTimeStamp()).append(" ");
        logBuilder.append("thread=").append(Thread.currentThread().getName()).append(" ");
//        logBuilder.append("logger=").append(event.getLoggerName()).append(" ");

        // Add request details
        logBuilder.append("method=").append(event.getMethod()).append(" ");
        logBuilder.append("url=").append(event.getRequestURL()).append(" ");
        logBuilder.append("headers=").append(event.getRequestHeaderMap()).append(" ");

        // Add response details
        logBuilder.append("status=").append(event.getStatusCode()).append(" ");
        logBuilder.append("responseHeaders=").append(event.getResponseHeaderMap()).append(" ");

        logBuilder.append(System.lineSeparator());
        return logBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] footerBytes() {
        return new byte[0];
    }
}
