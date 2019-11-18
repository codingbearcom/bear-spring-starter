/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A filter that logs requests received and responses sent by the server.
 */
@ConstrainedTo(RuntimeType.SERVER)
@Priority(Integer.MIN_VALUE)
@Component
@Provider
public class ServerLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

    private static final String REQUEST_MDC_KEY = "request-id";
    private static final String ENTITY_LOGGER_PROPERTY = "ENTITY_LOGGER_PROPERTY";
    private static final String RESPONSE_LOGGED_PROPERTY = "RESPONSE_LOGGED_PROPERTY";

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    /**
     * Prefix will be printed before requests.
     */
    private static final String REQUEST_PREFIX = "> ";
    /**
     * Prefix will be printed before response.
     */
    private static final String RESPONSE_PREFIX = "< ";

    private static final String NOTIFICATION_PREFIX = "* ";

    private static final String SECRET_TEXT = "-- secret --";

    private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR =
            (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey());

    private static final MediaType TEXT_MEDIA_TYPE = new MediaType("text", "*");

    private static final Set<MediaType> READABLE_APP_MEDIA_TYPES = new HashSet<>();
    private static final String REQUEST_NOTE = "Server has received a request";
    private static final String RESPONSE_NOTE = "Server responded with a response";

    static {
        READABLE_APP_MEDIA_TYPES.add(TEXT_MEDIA_TYPE);
        READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_ATOM_XML_TYPE);
        READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_JSON_TYPE);
        READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_SVG_XML_TYPE);
        READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_XHTML_XML_TYPE);
        READABLE_APP_MEDIA_TYPES.add(MediaType.APPLICATION_XML_TYPE);
    }

    private final Logger log = LoggerFactory.getLogger(ServerLoggingFilter.class);
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final Config config;

    /**
     * Constructor.
     *
     * @param config logging filter configuration.
     */
    @Autowired
    public ServerLoggingFilter(Config config) {
        this.config = config;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!log.isDebugEnabled() || !shouldLog(requestContext)) {
            return;
        }

        MDC.put(REQUEST_MDC_KEY, UUID.randomUUID().toString());

        // a flag indicating if the logging is turned on for this exchange,
        // so we don't have to evaluate the same conditions in the response
        requestContext.setProperty(RESPONSE_LOGGED_PROPERTY, "");

        StringBuilder loggedMessage = new StringBuilder();

        printRequestLine(loggedMessage, requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());
        printPrefixedHeaders(loggedMessage, REQUEST_PREFIX, requestContext.getHeaders());

        if (config.getLogBody() && hasEntity(requestContext) && isReadable(requestContext.getMediaType())) {
            Charset charset = getCharset(requestContext.getMediaType());

            Integer contentLength = getContentLength(requestContext);

            InputStream stream = new IncomingLoggingStream(loggedMessage,
                    requestContext.getEntityStream(),
                    config,
                    charset,
                    log,
                    contentLength);
            requestContext.setEntityStream(stream);
        } else if (log.isDebugEnabled()){
            log.debug(loggedMessage.toString());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (requestContext.getProperty(RESPONSE_LOGGED_PROPERTY) == null) {
            return;
        }

        StringBuilder loggedMessage = new StringBuilder();

        printResponseLine(loggedMessage, responseContext.getStatus());
        printPrefixedHeaders(loggedMessage, RESPONSE_PREFIX, responseContext.getStringHeaders());

        if (config.getLogBody() && responseContext.hasEntity() && isReadable(responseContext.getMediaType())) {
            Charset charset = getCharset(responseContext.getMediaType());
            OutputStream stream = new OutgoingLoggingStream(loggedMessage,
                    responseContext.getEntityStream(),
                    config,
                    charset,
                    log);
            responseContext.setEntityStream(stream);
            requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
        } else if (log.isDebugEnabled()) {
            log.debug(loggedMessage.toString());
        }

        MDC.remove(REQUEST_MDC_KEY);
    }

    private boolean shouldLog(ContainerRequestContext context) {
        if (config.getIgnoredUrls() == null) {
            return true;
        }

        String path = context.getUriInfo().getPath(true);

        return config.getIgnoredUrls().stream().noneMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    private void printRequestLine(StringBuilder loggedMessage, String method, URI uri) {
        loggedMessage.append(NOTIFICATION_PREFIX)
                .append(REQUEST_NOTE)
                .append("\n");
        loggedMessage.append(REQUEST_PREFIX)
                .append(method)
                .append(" ")
                .append(uri.toASCIIString())
                .append("\n");
    }

    private void printResponseLine(StringBuilder loggedMessage, int status) {
        loggedMessage.append(NOTIFICATION_PREFIX)
                .append(RESPONSE_NOTE)
                .append("\n");
        loggedMessage.append(RESPONSE_PREFIX)
                .append(status)
                .append("\n");
    }

    private void printPrefixedHeaders(StringBuilder loggedMessage,
                                      String prefix,
                                      MultivaluedMap<String, String> headers) {
        for (Map.Entry<String, List<String>> headerEntry : getSortedHeaders(headers.entrySet())) {
            List<?> val = headerEntry.getValue();
            String header = headerEntry.getKey();

            if (val.size() == 1) {
                if (header.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                    loggedMessage.append(prefix).append(header).append(": ").append(SECRET_TEXT).append("\n");
                } else {
                    loggedMessage.append(prefix).append(header).append(": ").append(val.get(0)).append("\n");
                }
            } else {
                String headersList = val.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
                loggedMessage.append(prefix).append(header).append(": ").append(headersList).append("\n");
            }
        }
    }

    private Set<Map.Entry<String, List<String>>> getSortedHeaders(Set<Map.Entry<String, List<String>>> headers) {
        TreeSet<Map.Entry<String, List<String>>> sortedHeaders = new TreeSet<>(COMPARATOR);
        sortedHeaders.addAll(headers);
        return sortedHeaders;
    }

    private boolean isReadable(MediaType mediaType) {
        if (mediaType != null) {
            for (MediaType readableMediaType : READABLE_APP_MEDIA_TYPES) {
                if (readableMediaType.isCompatible(mediaType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasEntity(ContainerRequestContext requestContext) {
        // responseContext.hasEntity() cannot be relied upon, because it just check if the response has a content type
        // some clients send a content type header even in requests without body

        Integer contentLength = getContentLength(requestContext);

        if (contentLength != null) {
            if (contentLength.compareTo(0) == 0) {
                return false;
            }

            if (contentLength > 0) {
                return true;
            }
        }

        List<String> values = requestContext.getHeaders().get("Transfer-Encoding");

        if (values != null) {
            return values.stream().anyMatch(value -> value.equalsIgnoreCase("Chunked"));
        }

        return false;
    }

    private Integer getContentLength(ContainerRequestContext requestContext) {
        String contentLengthStr = requestContext.getHeaders().getFirst("Content-Length");

        Integer contentLength = null;

        if (contentLengthStr != null) {
            try {
                contentLength = Integer.valueOf(contentLengthStr);
            } catch (NumberFormatException e) {
                log.warn("Could not parse 'Content-length' header value", e);
            }
        }

        return contentLength;
    }

    private Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        return (name == null) ? UTF8 : Charset.forName(name);
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException {
        OutgoingLoggingStream loggingStream = (OutgoingLoggingStream) context.getProperty(ENTITY_LOGGER_PROPERTY);
        context.proceed();
        if (loggingStream != null) {
            loggingStream.logMessage();
        }
    }

    private static String doLogMessage(
            StringBuilder loggedMessage,
            ByteArrayOutputStream buffer,
            int maxEntitySize,
            Charset charset) {

        // write entity to the builder
        byte[] entity = buffer.toByteArray();

        loggedMessage.append(new String(entity, 0, Math.min(entity.length, maxEntitySize), charset));
        if (entity.length > maxEntitySize) {
            loggedMessage.append("...more...");
        }

        loggedMessage.append('\n');
        return loggedMessage.toString();
    }

    private static class OutgoingLoggingStream extends FilterOutputStream {

        private final StringBuilder loggedMessage;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final Config config;
        private final Charset charset;
        private final Logger log;

        OutgoingLoggingStream(StringBuilder loggedMessage,
                              OutputStream wrappedStream,
                              Config config,
                              Charset charset,
                              Logger log) {
            super(wrappedStream);

            this.loggedMessage = loggedMessage;
            this.config = config;
            this.charset = charset;
            this.log = log;
        }

        void logMessage() {
            final String loggedOutput = doLogMessage(loggedMessage, buffer, config.getMaxEntitySize(), charset);
            if (log.isDebugEnabled()) {
                log.debug(loggedOutput);
            }
        }

        @Override
        public void write(int i) throws IOException {
            if (buffer.size() <= config.getMaxEntitySize()) {
                buffer.write(i);
            }

            out.write(i);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                if (buffer.size() >= config.getMaxEntitySize()) {
                    break;
                }

                buffer.write(b[off + i]);
            }

            out.write(b, off, len);
        }
    }

    /**
     * An incoming  entity stream wrapper that stores entity bytes for logging purposes and logs the entity
     * when it has been fully read.
     * The entity has been read when at least one of the following conditions is fulfilled:
     * <ul>
     * <li>The wrapped input stream returns -1</li>
     * <li>A number of bytes that is equal to 'Content-length' header value has been read</li>
     * <li>The stream is closed</li>
     * </ul>
     */
    private static class IncomingLoggingStream extends FilterInputStream {

        private final StringBuilder loggedMessage;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final Config config;
        private final Charset charset;
        private final Logger log;
        private boolean messageLogged = false;
        private int sizeRemaining;

        IncomingLoggingStream(StringBuilder loggedMessage,
                              InputStream wrappedStream,
                              Config config,
                              Charset charset,
                              Logger log,
                              Integer contentLength) {
            super(wrappedStream);

            this.loggedMessage = loggedMessage;
            this.config = config;
            this.charset = charset;
            this.log = log;

            if (contentLength == null) {
                sizeRemaining = -1;
            } else {
                sizeRemaining = contentLength;
            }
        }

        @Override
        public int read() throws IOException {
            int b = in.read();

            if (b == -1) {
                logMessage();
                return b;
            }

            if (buffer.size() <= config.getMaxEntitySize()) {
                buffer.write(b);
            }

            sizeRemaining--;

            if (sizeRemaining == 0) {
                logMessage();
            }

            return b;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int read = in.read(b, 0, b.length);

            if (read == -1) {
                logMessage();
                return read;
            }

            for (int i = 0; i < read; i++) {
                if (buffer.size() >= config.getMaxEntitySize()) {
                    break;
                }

                buffer.write(b[i]);
            }

            sizeRemaining -= read;

            if (sizeRemaining == 0) {
                logMessage();
            }

            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = in.read(b, 0, b.length);

            if (read == -1) {
                logMessage();
                return read;
            }

            for (int i = 0; i < read; i++) {
                if (buffer.size() >= config.getMaxEntitySize()) {
                    break;
                }

                buffer.write(b[off + i]);
            }

            sizeRemaining -= read;

            if (sizeRemaining == 0) {
                logMessage();
            }

            return read;
        }

        @Override
        public void close() throws IOException {
            logMessage();
            in.close();
        }

        private void logMessage() {
            if (messageLogged) {
                return;
            }

            messageLogged = true;
            final String loggedOutput = doLogMessage(loggedMessage, buffer, config.getMaxEntitySize(), charset);
            if (log.isDebugEnabled()) {
                log.debug(loggedOutput);
            }
        }
    }

    /**
     * Logging filter configuration.
     */
    @Component
    @ConfigurationProperties(prefix = "server-logging")
    @EnableConfigurationProperties
    public static class Config {

        private int maxEntitySize = 1000;
        private boolean logBody = true;
        /**
         * A list of URLs in Ant-style path patterns (see {@link org.springframework.util.AntPathMatcher}).
         * Request and responses whose URL matches one of the patters, will not be logged.
         */
        private List<String> ignoredUrls;

        /**
         * Validates configuration parsed from config file.
         */
        @PostConstruct
        public void validate() {
            if (ignoredUrls != null) {
                // the patterns will be matched with URIs starting with '/, so they must start with '/', too
                ignoredUrls = ignoredUrls.stream()
                        .map(pattern -> {
                            if (pattern.startsWith("/")) {
                                return pattern;
                            }

                            return "/" + pattern;
                        }).collect(Collectors.toList());
            }
        }

        public int getMaxEntitySize() {
            return maxEntitySize;
        }

        public void setMaxEntitySize(int maxEntitySize) {
            this.maxEntitySize = maxEntitySize;
        }

        public boolean getLogBody() {
            return logBody;
        }

        public void setLogBody(boolean logBody) {
            this.logBody = logBody;
        }

        public List<String> getIgnoredUrls() {
            return ignoredUrls;
        }

        public void setIgnoredUrls(List<String> ignoredUrls) {
            this.ignoredUrls = ignoredUrls;
        }
    }
}
