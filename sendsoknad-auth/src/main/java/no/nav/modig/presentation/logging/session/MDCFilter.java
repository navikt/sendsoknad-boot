package no.nav.modig.presentation.logging.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.nav.modig.common.MDCOperations;
import no.nav.modig.core.context.SubjectHandler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Se <a href=http://confluence.adeo.no/display/Modernisering/MDCFilter>Utviklerhåndbok - Logging - Sporingslogging -
 * MDCFilter</a> for informasjon om filteret og hvordan det skal brukes.
 */
public class MDCFilter extends OncePerRequestFilter {
    protected static final Logger log = LoggerFactory.getLogger(MDCFilter.class.getName());

    private SubjectHandler subjectHandler;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        subjectHandler = SubjectHandler.getSubjectHandler();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("Entering filter to extract values and put on MDC for logging");

        Map<String, String> pathMap = (Map<String,String>)httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);  
        
        String behandlingsId= (pathMap!=null && pathMap.containsKey("behandlingsId")) ? pathMap.get("behandlingsId") : "";
        
        if (!StringUtils.isEmpty(behandlingsId)) {
        	subjectHandler.setBehandlingsId(behandlingsId);
        }
        log.info("Behandlings id extracted from " + httpServletRequest.getRequestURL() + " behandlingsId is " + behandlingsId);
        
        String consumerId = subjectHandler.getConsumerId() != null ? subjectHandler.getConsumerId() : "";
        behandlingsId = subjectHandler.getBehandlingsId() != null ? subjectHandler.getBehandlingsId() : "";
        String callId = MDCOperations.generateCallId();

        MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, callId);
        MDCOperations.putToMDC(MDCOperations.MDC_CONSUMER_ID, consumerId);
        MDCOperations.putToMDC(MDCOperations.MDC_BEHANDLINGS_ID, behandlingsId);
        log.debug("Values added");

        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            MDCOperations.remove(MDCOperations.MDC_CALL_ID);
            MDCOperations.remove(MDCOperations.MDC_CONSUMER_ID);
            MDCOperations.remove(MDCOperations.MDC_BEHANDLINGS_ID);
            log.debug("Cleared MDC session");
        }
    }
}
