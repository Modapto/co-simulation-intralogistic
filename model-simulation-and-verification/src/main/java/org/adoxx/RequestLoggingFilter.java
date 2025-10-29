package org.adoxx;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class RequestLoggingFilter implements Filter {

    private static final int MAX_LOG_SIZE = 4096;

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

        // Wrap request to allow reading body multiple times
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);

        // Wrap response to capture body
        BufferedResponseWrapper responseWrapper = new BufferedResponseWrapper(httpResponse);

        // Read request body
        String requestBody = cachedRequest.getBody();
        //if (requestBody.length() > MAX_LOG_SIZE) {
        //    requestBody = requestBody.substring(0, MAX_LOG_SIZE) + "...[truncated]";
        //}

        // Log request
        System.out.println("Request " + httpRequest.getMethod() + " " +
                httpRequest.getRequestURI() +
                " query=\"" + (httpRequest.getQueryString() != null ? httpRequest.getQueryString() : "") + "\"" +
                " body: " + requestBody);

        // Continue the filter chain with wrapped request and response
        chain.doFilter(cachedRequest, responseWrapper);

        // Read response body
        String responseBody = new String(responseWrapper.getResponseData(), httpResponse.getCharacterEncoding());
        //if (responseBody.length() > MAX_LOG_SIZE) {
        //    responseBody = responseBody.substring(0, MAX_LOG_SIZE) + "...[truncated]";
        //}

        // Log response
        System.out.println("Response for " + httpRequest.getMethod() + " " +
                httpRequest.getRequestURI() + ": " + responseBody);

        // Copy response back to the client
        ServletOutputStream out = httpResponse.getOutputStream();
        out.write(responseWrapper.getResponseData());
        out.flush();
    }

    @Override
    public void destroy() {}

    // -------------------
    // Helper: Request wrapper
    // -------------------
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            InputStream is = request.getInputStream();
            cachedBody = readInputStream(is);
        }

        private byte[] readInputStream(InputStream is) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() { return true; }

                @Override
                public void setReadListener(ReadListener readListener) {}
            };
        }

        public String getBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
    }

    // -------------------
    // Helper: Response wrapper
    // -------------------
    private static class BufferedResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private ServletOutputStream outputStream;
        private PrintWriter writer;

        public BufferedResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (outputStream == null) {
                outputStream = new ServletOutputStream() {
                    @Override
                    public void write(int b) {
                        buffer.write(b);
                        //return b;
                    }

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
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding()));
            }
            return writer;
        }

        public byte[] getResponseData() throws IOException {
            if (writer != null) writer.flush();
            if (outputStream != null) outputStream.flush();
            return buffer.toByteArray();
        }
    }
}
