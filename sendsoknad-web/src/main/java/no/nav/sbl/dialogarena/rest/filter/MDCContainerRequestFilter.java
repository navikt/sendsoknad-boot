package no.nav.sbl.dialogarena.rest.filter;

import no.nav.modig.common.MDCOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.modig.common.MDCOperations.MDC_INNSENDINGS_ID;

@Provider
public class MDCContainerRequestFilter implements ContainerRequestFilter {
    protected static final Logger log = LoggerFactory.getLogger(MDCContainerRequestFilter.class.getName());

    // Adds behandlingsId/innsendingsId to MDC from path variables or headers
    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        MultivaluedMap<String, String> pathVariables = containerRequestContext.getUriInfo().getPathParameters();
        var headers = containerRequestContext.getHeaders();
        List<String> headerNames = new ArrayList<>(headers.keySet());

        String[] variations = {"x-innsendingid", "x-innsendingsid", "innsendingid", "innsendingsid", "x-behandlingsid", "behandlingsid"};

        log.info("pathVariables: {}, headerNames: {}", pathVariables, headerNames);

        // Add from path variables
        pathVariables.keySet().stream()
                .filter(key -> Arrays.stream(variations).anyMatch(key::equalsIgnoreCase))
                .findFirst()
                .map(key -> pathVariables.get(key).get(0))
                .ifPresent(innsendingsIdPathVariable -> {
                    log.info("innsendingsIdPathVariable: {}", innsendingsIdPathVariable);
                    MDCOperations.putToMDC(MDC_INNSENDINGS_ID, innsendingsIdPathVariable);
                });

        // Add from headers
        headerNames.stream()
                .filter(header -> Arrays.stream(variations).anyMatch(header::equalsIgnoreCase))
                .findFirst()
                .map(headerName -> headers.get(headerName).get(0))
                .ifPresent(innsendingsIdHeader -> {
                    log.info("innsendingsIdHeader: {}", innsendingsIdHeader);
                    MDCOperations.putToMDC(MDC_INNSENDINGS_ID, innsendingsIdHeader);
                });


    }
}

