package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.FssProxyApiKey;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.SakOgAktivitetWSConfig;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpCallback;
import org.mockserver.model.HttpRequest;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static org.assertj.core.api.Assertions.assertThat;

public class AktivitetServiceIntegrationTest {
    private static int PORT = 10000 + (int) (Math.random() * 1000);
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(PORT, this);
    private AktivitetService service;
    private MockServerClient client;

    private SakOgAktivitetV1 sakOgAktivitetEndpoint(SakOgAktivitetV1 prod) {
        System.setProperty("test", "false");
        System.setProperty("tillatmock", "true");
        return createSwitcher(prod, prod, "test", SakOgAktivitetV1.class);
    }

    @Before
    public void setup() {
        MDC.put("callId", "apa bepa");
        SakOgAktivitetWSConfig config = new SakOgAktivitetWSConfig();
        
        ReflectionTestUtils.setField(config, "sakOgAktivitetEndpoint", "http://localhost:" + PORT);
        ReflectionTestUtils.setField(config, "apiKey", "dummy");
        SakOgAktivitetV1 aktivitetWebService = sakOgAktivitetEndpoint(config.factory().get());
        service = new AktivitetService(aktivitetWebService);
    }

    @Test
    public void shouldThrowException() {
        client.when(HttpRequest.request().withMethod("POST"))
                .callback(new HttpCallback().withCallbackClass(AktivitetServiceIntegrationCallback.class.getCanonicalName()));
        List<Faktum> response = service.hentAktiviteter("123");
        assertThat(response).isEmpty();
    }
}
