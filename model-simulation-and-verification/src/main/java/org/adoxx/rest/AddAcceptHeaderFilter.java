package org.adoxx.rest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class AddAcceptHeaderFilter implements ContainerRequestFilter {

    private static final String DEFAULT_ACCEPT = "application/json";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Add Accept if missing
        if (requestContext.getHeaderString("Accept") == null) {
            System.out.println("Accept header is null");
            requestContext.getHeaders().putSingle("Accept", DEFAULT_ACCEPT);
        }
    }
}
