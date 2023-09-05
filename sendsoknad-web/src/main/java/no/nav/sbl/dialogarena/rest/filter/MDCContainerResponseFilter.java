package no.nav.sbl.dialogarena.rest.filter;

import no.nav.modig.common.MDCOperations;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class MDCContainerResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MDCOperations.remove(MDCOperations.MDC_INNSENDINGS_ID);
    }
}