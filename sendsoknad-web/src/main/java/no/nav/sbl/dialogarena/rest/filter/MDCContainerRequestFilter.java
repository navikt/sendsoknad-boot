package no.nav.sbl.dialogarena.rest.filter;

import no.nav.modig.common.MDCOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.modig.common.MDCOperations.MDC_INNSENDINGS_ID;

@Provider
public class MDCContainerRequestFilter implements ContainerRequestFilter {
    protected static final Logger log = LoggerFactory.getLogger(MDCContainerRequestFilter.class.getName());

    // Adds behandlingsId/innsendingsId to MDC from path variables or headers
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        log.info("Entering filter to extract values and put on MDC for logging");

        MultivaluedMap<String, String> pathVariables = containerRequestContext.getUriInfo().getPathParameters();
        List<String> headerNames = new ArrayList<>(containerRequestContext.getHeaders().keySet());

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
        Arrays.stream(variations)
                .filter(variation -> headerNames.stream().anyMatch(header -> header.equalsIgnoreCase(variation)))
                .findFirst()
                .map(containerRequestContext::getHeaderString).ifPresent(innsendingsIdHeader -> {
                    log.info("innsendingsIdHeader: {}", innsendingsIdHeader);
                    MDCOperations.putToMDC(MDC_INNSENDINGS_ID, innsendingsIdHeader);
                });
    }
}

