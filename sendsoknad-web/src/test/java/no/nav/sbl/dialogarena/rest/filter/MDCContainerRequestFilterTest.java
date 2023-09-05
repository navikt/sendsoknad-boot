package no.nav.sbl.dialogarena.rest.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import static no.nav.modig.common.MDCOperations.MDC_INNSENDINGS_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class MDCContainerRequestFilterTest {

    @Mock
    private ContainerRequestContext containerRequestContext;

    @Mock
    private UriInfo mockUriInfo;

    private MDCContainerRequestFilter mdcContainerRequestFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        MDC.clear();
        mdcContainerRequestFilter = new MDCContainerRequestFilter();
    }

    @Test
    public void shouldSetMDCFromHeader() {
        // Given
        String headerName = "X-behandlingsId";
        String headerValue = "header123456";

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();

        headers.add(headerName, headerValue);

        when(mockUriInfo.getPathParameters()).thenReturn(pathParameters);
        when(containerRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(containerRequestContext.getHeaders()).thenReturn(headers);

        // When
        mdcContainerRequestFilter.filter(containerRequestContext);

        // Then
        assertEquals(headerValue, MDC.get(MDC_INNSENDINGS_ID));
    }

    @Test
    public void shouldSetMDCFromPathVariable() {
        // Given
        String pathVarName = "behandlingsId";
        String pathVarValue = "path123456";

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();

        pathParameters.add(pathVarName, pathVarValue);

        when(mockUriInfo.getPathParameters()).thenReturn(pathParameters);
        when(containerRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(containerRequestContext.getHeaders()).thenReturn(headers);

        // When
        mdcContainerRequestFilter.filter(containerRequestContext);

        // Then
        assertEquals(pathVarValue, MDC.get(MDC_INNSENDINGS_ID));
    }

    @Test
    public void shouldNotSetMDCWhenHeaderAndPathVariableAreNull() {
        // Given
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();

        when(mockUriInfo.getPathParameters()).thenReturn(pathParameters);
        when(containerRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(containerRequestContext.getHeaders()).thenReturn(headers);

        // When
        mdcContainerRequestFilter.filter(containerRequestContext);

        // Then
        assertNull(MDC.get(MDC_INNSENDINGS_ID));
    }
}
