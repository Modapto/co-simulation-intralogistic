package org.adoxx;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;
import java.util.*;

public class AddAcceptHeaderFilter implements Filter {

    private final String defaultAccept;

    public AddAcceptHeaderFilter() {
        this.defaultAccept = "application/json"; // default value
    }

    public AddAcceptHeaderFilter(String defaultAccept) {
        this.defaultAccept = defaultAccept;
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("Accept".equalsIgnoreCase(name)) {
                    String header = super.getHeader(name);
                    if (header == null) {
                        return Collections.enumeration(Collections.singleton(defaultAccept));
                    }
                }
                return super.getHeaders(name);
            }

            @Override
            public String getHeader(String name) {
                if ("Accept".equalsIgnoreCase(name)) {
                    String header = super.getHeader(name);
                    if (header == null) {
                        return defaultAccept;
                    }
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                if (!names.stream().anyMatch(n -> "Accept".equalsIgnoreCase(n))) {
                    names.add("Accept");
                }
                return Collections.enumeration(names);
            }
        };

        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {}
}
