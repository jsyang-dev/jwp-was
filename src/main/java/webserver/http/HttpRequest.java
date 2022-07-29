package webserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HttpUtils;
import utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class HttpRequest {
    public static final String VALIDATION_MESSAGE = "잘못된 HTTP 요청입니다.";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);
    private static final Pattern HEADER_DELIMITER = Pattern.compile(": ");

    private final RequestLine requestLine;
    private final Map<String, Object> headers;
    private final Map<String, String> attributes;

    public HttpRequest(InputStream in) {
        final BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.requestLine = makeRequestLine(br);
        this.headers = makeHeaders(br);
        this.attributes = makeAttributes(br);
        LOGGER.debug(requestLine.toString());
    }

    private RequestLine makeRequestLine(BufferedReader br) {
        final String line = readLine(br);
        validate(line);
        return new RequestLine(line);
    }

    private Map<String, Object> makeHeaders(BufferedReader br) {
        Map<String, Object> headers = new HashMap<>();
        String line;
        while (!"".equals(line = readLine(br))) {
            final String[] splitLine = HEADER_DELIMITER.split(line);
            headers.put(splitLine[0], splitLine[1]);
        }
        return headers;
    }

    private String readLine(BufferedReader br) {
        String line;
        try {
            line = br.readLine();
        } catch (IOException e) {
            throw new IllegalArgumentException(VALIDATION_MESSAGE, e);
        }
        return line;
    }

    private void validate(String line) {
        if (line == null) {
            throw new IllegalArgumentException(VALIDATION_MESSAGE);
        }
    }

    private Map<String, String> makeAttributes(BufferedReader br) {
        if (requestLine.isGet()) {
            return new HashMap<>();
        }

        try {
            final int contentLength = getContentLength();
            final String payload = IOUtils.readData(br, contentLength);
            return HttpUtils.parseToMap(payload);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequest that = (HttpRequest) o;
        return Objects.equals(requestLine, that.requestLine) && Objects.equals(headers, that.headers) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestLine, headers, attributes);
    }

    public RequestLine getRequestLine() {
        return requestLine;
    }

    public Object getHeader(String key) {
        return headers.get(key);
    }

    public int getContentLength() {
        try {
            return Integer.parseInt((String) getHeader("Content-Length"));
        } catch (Exception e) {
            return 0;
        }
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public String getPath() {
        return requestLine.getPath();
    }


    public HttpProtocol getHttpProtocol() {
        return requestLine.getHttpProtocol();
    }

    public boolean isGet() {
        return requestLine.isGet();
    }

    public boolean isPost() {
        return requestLine.isPost();
    }
}
