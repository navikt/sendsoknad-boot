package no.nav.sbl.dialogarena.interceptor;

import no.nav.modig.common.MDCOperations;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static no.nav.modig.common.MDCOperations.MDC_INNSENDINGS_ID;

@Component
public class MDCInterceptor implements HandlerInterceptor {

    protected static final Logger log = LoggerFactory.getLogger(MDCInterceptor.class.getName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Entering interceptor to extract values and put on MDC for logging");

        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        List<String> headerNames = Collections.list(request.getHeaderNames());

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
                .map(request::getHeader).ifPresent(innsendingsIdHeader -> {
                    log.info("innsendingsIdHeader: {}", innsendingsIdHeader);
                    MDCOperations.putToMDC(MDC_INNSENDINGS_ID, innsendingsIdHeader);
                });

        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex)  {
        MDCOperations.remove(MDCOperations.MDC_INNSENDINGS_ID);
    }
}
