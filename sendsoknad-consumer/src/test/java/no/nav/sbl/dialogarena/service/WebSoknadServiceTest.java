package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class WebSoknadServiceTest {

    private static final String BEHANDLINGS_ID = "222222222";

    @Mock
 //   @Named("sendSoknadEndpoint")
    SendSoknadPortType webservice;
    @InjectMocks
    HenvendelseService service;

    @Test
    public void skalKunneStarteSoknad() {
        when(webservice.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(lagResultatFraStartSoknad());
        String behandlingsId = service.startSoknad("11111111111", "NAV-123", "BIL","123", SoknadType.SEND_SOKNAD);
        assertEquals(BEHANDLINGS_ID, behandlingsId);
    }

    private WSBehandlingsId lagResultatFraStartSoknad() {
        return new WSBehandlingsId().withBehandlingsId(BEHANDLINGS_ID);
    }
}
