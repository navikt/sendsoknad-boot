package no.nav.modig.presentation.logging.session;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import no.nav.modig.common.MDCOperations;
import no.nav.modig.core.domain.ConsumerId;
import org.springframework.web.servlet.HandlerMapping;

import static no.nav.modig.common.MDCOperations.MDC_INNSENDINGS_ID;

/**
 * Se <a href=http://confluence.adeo.no/display/Modernisering/MDCFilter>Utviklerhåndbok - Logging - Sporingslogging -
 * MDCFilter</a> for informasjon om filteret og hvordan det skal brukes.
 */
public class MDCFilter extends OncePerRequestFilter {
    protected static final Logger log = LoggerFactory.getLogger(MDCFilter.class.getName());

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Entering filter to extract values and put on MDC for logging");

        //@TODO gå en gang til gjennom consumerId og hvordan den settes.
        
        String consumerId = new ConsumerId().getConsumerId();
        String callId = MDCOperations.generateCallId();

        MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, callId);
        MDCOperations.putToMDC(MDCOperations.MDC_CONSUMER_ID, consumerId);
        Map<String, String> pathVariables = (Map<String, String>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        List<String> headerNames = Collections.list(httpServletRequest.getHeaderNames());

        String[] variations = {"x-innsendingid", "x-innsendingsid", "innsendingid", "innsendingsid", "x-behandlingsid", "behandlingsid"};

        log.info("pathVariables: {}, headerNames: {}", pathVariables, headerNames);

        if (pathVariables != null) {
            pathVariables.keySet().stream()
                    .filter(key -> Arrays.stream(variations).anyMatch(key::equalsIgnoreCase))
                    .findFirst()
                    .map(pathVariables::get).ifPresent(innsendingsIdPathVariable -> {
                        log.info("innsendingsIdPathVariable: {}", innsendingsIdPathVariable);
                        MDCOperations.putToMDC(MDC_INNSENDINGS_ID, innsendingsIdPathVariable);
                    });
        }


        Arrays.stream(variations)
                .filter(variation -> headerNames.stream().anyMatch(header -> header.equalsIgnoreCase(variation)))
                .findFirst()
                .map(httpServletRequest::getHeader).ifPresent(innsendingsIdHeader -> {
                            log.info("innsendingsIdHeader: {}", innsendingsIdHeader);
                            MDCOperations.putToMDC(MDC_INNSENDINGS_ID, innsendingsIdHeader);
                        });

        log.info("Values added");

        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            MDCOperations.remove(MDCOperations.MDC_CALL_ID);
            MDCOperations.remove(MDCOperations.MDC_CONSUMER_ID);
            MDCOperations.remove(MDCOperations.MDC_BEHANDLINGS_ID);
            MDCOperations.remove(MDCOperations.MDC_INNSENDINGS_ID);
            log.info("Cleared MDC session");
        }
    }
}
