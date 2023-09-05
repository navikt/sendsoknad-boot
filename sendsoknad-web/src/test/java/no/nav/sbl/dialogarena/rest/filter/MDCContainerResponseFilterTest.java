package no.nav.sbl.dialogarena.rest.filter;

import no.nav.modig.common.MDCOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MDCContainerResponseFilterTest {


    @Mock
    private ContainerRequestContext mockRequestContext;

    @Mock
    private ContainerResponseContext mockResponseContext;

    @InjectMocks
    private MDCContainerResponseFilter mdcContainerResponseFilter;

    @BeforeEach
    public void setUp() {
        MDC.clear();
    }

    @Test
    public void shouldRemoveMDCEntryAfterResponse() throws Exception {
        // Given
        String mdcValue = "sampleValue";
        MDC.put(MDCOperations.MDC_INNSENDINGS_ID, mdcValue);
        assertEquals(mdcValue, MDC.get(MDCOperations.MDC_INNSENDINGS_ID));

        // When
        mdcContainerResponseFilter.filter(mockRequestContext, mockResponseContext);

        // Then
        assertNull(MDC.get(MDCOperations.MDC_INNSENDINGS_ID));
    }
}
