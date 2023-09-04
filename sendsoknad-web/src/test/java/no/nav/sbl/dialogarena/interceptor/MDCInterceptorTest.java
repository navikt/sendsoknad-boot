package no.nav.sbl.dialogarena.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collections;

import static no.nav.modig.common.MDCOperations.MDC_INNSENDINGS_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MDCInterceptorTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MDCInterceptor mdcInterceptor;

    @BeforeEach
    public void setUp() {
        MDC.clear();
        mdcInterceptor = new MDCInterceptor();

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void shouldSetMDCFromHeader() throws Exception {
        // Given
        String headerName = "X-behandlingsId";
        String headerValue = "header123456";

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.emptyMap());
        request.addHeader(headerName, headerValue);

        // When
        mdcInterceptor.preHandle(request, response, new Object());
        // Then
        assertEquals(headerValue, MDC.get(MDC_INNSENDINGS_ID));
    }

    @Test
    public void shouldSetMDCFromPathVariable() throws Exception {
        // Given
        String pathVarName = "behandlingsId";
        String pathVarValue = "path123456";

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap(pathVarName, pathVarValue));

        // When
        mdcInterceptor.preHandle(request, response, new Object());

        // Then
        assertEquals(pathVarValue, MDC.get(MDC_INNSENDINGS_ID));

    }

    @Test
    public void shouldResetMDCAfterRequestIsCompleted() throws Exception {
        // Given
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.emptyMap());

        // When
        mdcInterceptor.preHandle(request, response, new Object());

        // Then
        assertNull(MDC.get(MDC_INNSENDINGS_ID));
    }

    @Test
    public void shouldNotSetMDCWhenHeaderAndPathVariableAreNull() throws Exception {
        // Given: Neither header nor path variable is set
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.emptyMap());

        // When
        mdcInterceptor.preHandle(request, response, new Object());

        // Then
        assertNull(MDC.get(MDC_INNSENDINGS_ID));
    }
}