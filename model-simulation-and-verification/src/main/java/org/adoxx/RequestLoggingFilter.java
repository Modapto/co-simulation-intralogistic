package org.adoxx;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestLoggingFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Wrap request and response
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
        BufferedResponseWrapper responseWrapper = new BufferedResponseWrapper(httpResponse);

        // --- REQUEST LOG ---
        
        String method = httpRequest.getMethod();
        String query = httpRequest.getQueryString() != null ? httpRequest.getQueryString() : "";
        String requestBody = cachedRequest.getBody();

        StringBuilder requestHeaders = new StringBuilder();
        Collections.list(httpRequest.getHeaderNames())
                .forEach(name -> requestHeaders.append(name).append(": ")
                        .append(httpRequest.getHeader(name)).append("; "));

        System.out.println("Request " + httpRequest.getScheme() + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort() + " " + method + " HTTP/" + httpRequest.getProtocol() + " from " + httpRequest.getRemoteAddr() + ":" + httpRequest.getRemotePort()  + " " + httpRequest.getRequestURI() +
                " query=\"" + query + "\" headers=[" + requestHeaders + "] body encoding: " + httpRequest.getCharacterEncoding() + " body: " + requestBody);

        // --- CONTINUE CHAIN ---
        chain.doFilter(cachedRequest, responseWrapper);

        // --- RESPONSE LOG ---
        byte[] responseData = responseWrapper.getResponseData();
        String responseBody = new String(responseData, httpResponse.getCharacterEncoding());

        StringBuilder responseHeaders = new StringBuilder();
        for (String name : responseWrapper.getHeaderNames()) {
            responseHeaders.append(name).append(": ").append(responseWrapper.getHeader(name)).append("; ");
        }

        System.out.println("Response for " + method + " " + httpRequest.getRequestURI() +
                " headers=[" + responseHeaders + "] body: " + responseBody);

        // Copy response to client
        ServletOutputStream out = httpResponse.getOutputStream();
        out.write(responseData);
        out.flush();
    }

    @Override
    public void destroy() {}

    // -------------------
    // Cached request wrapper
    // -------------------
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            cachedBody = request.getInputStream().readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() { return byteArrayInputStream.read(); }
                @Override
                public boolean isFinished() { return byteArrayInputStream.available() == 0; }
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setReadListener(ReadListener listener) {}
            };
        }

        public String getBody() { return new String(cachedBody, StandardCharsets.UTF_8); }
    }

    // -------------------
    // Buffered response wrapper
    // -------------------
    private static class BufferedResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private ServletOutputStream outputStream;
        private PrintWriter writer;

        public BufferedResponseWrapper(HttpServletResponse response) { super(response); }

        @Override
        public ServletOutputStream getOutputStream() {
            if (outputStream == null) {
                outputStream = new ServletOutputStream() {
                    @Override
                    public void write(int b) { buffer.write(b); }
                    @Override
                    public boolean isReady() { return true; }
                    @Override
                    public void setWriteListener(WriteListener listener) {}
                };
            }
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer == null) writer = new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding()));
            return writer;
        }

        public byte[] getResponseData() throws IOException {
            if (writer != null) writer.flush();
            if (outputStream != null) outputStream.flush();
            return buffer.toByteArray();
        }
    }
}
