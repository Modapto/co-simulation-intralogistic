package org.adoxx;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RequestLoggingFilter implements Filter {

    // Max bytes to log (to avoid huge payloads)
    private static final int MAX_LOG_SIZE = 4096;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No init needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // Only log POST/PUT/DELETE (with body)
            String method = httpRequest.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {

                // Wrap the request to be able to read the body multiple times
                CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);

                String body = cachedRequest.getBody();
                if (body.length() > MAX_LOG_SIZE) {
                    body = body.substring(0, MAX_LOG_SIZE) + "...[truncated]";
                }

                System.out.println("Request " + method + " " + httpRequest.getRequestURI() + " body: " + body);

                chain.doFilter(cachedRequest, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup
    }

    /**
     * Helper wrapper class to cache request body
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            InputStream requestInputStream = request.getInputStream();
            cachedBody = readInputStream(requestInputStream);
        }

        private byte[] readInputStream(InputStream is) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
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
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // not needed
                }
            };
        }

        public String getBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
    }
}
