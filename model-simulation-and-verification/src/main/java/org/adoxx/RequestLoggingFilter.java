package org.adoxx;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RequestLoggingFilter implements Filter {

    private static final int MAX_LOG_SIZE = 4096;

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String method = httpRequest.getMethod();

            // Wrap request to read body safely (body may be empty for GET)
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);

            String body = cachedRequest.getBody();
            if (body.length() > MAX_LOG_SIZE) {
                body = body.substring(0, MAX_LOG_SIZE) + "...[truncated]";
            }

            System.out.println("Request " + method + " " + httpRequest.getRequestURI() +
                    " query=\"" + httpRequest.getQueryString() + "\" body: " + body);

            chain.doFilter(cachedRequest, response);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    // Helper wrapper to cache the request body
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
                public void setReadListener(ReadListener readListener) {}
            };
        }

        public String getBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
    }
}
