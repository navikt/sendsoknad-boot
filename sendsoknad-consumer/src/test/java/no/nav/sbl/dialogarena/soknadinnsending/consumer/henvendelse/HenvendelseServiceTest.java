package no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.IKKE_VALGT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType.SEND_SOKNAD;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType.SEND_SOKNAD_ETTERSENDING;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HenvendelseServiceTest {

    private static final String BEHANDLINGSKJEDE_ID = "A";
    private static final String BEHANDLINGS_ID = "B";
    private static final String FODSELSNUMMER = "12345678910";
    private static final String TEST_SKJEMANUMMER = "NAV 10-07.40";
    private static final String TEST_TEMA = "BIL";
    private static final String TEST_UUID = UUID.randomUUID().toString();
    private static final SoknadType SOKNADS_TYPE_SEND_SOKNAD = SEND_SOKNAD;
    private static final SoknadType SOKNADS_TYPE_SEND_SOKNAD_ETTERSENDING = SEND_SOKNAD_ETTERSENDING;

    private static ArgumentCaptor<WSStartSoknadRequest> argument = ArgumentCaptor.forClass(WSStartSoknadRequest.class);
    private static WSHentSoknadResponse respons = new WSHentSoknadResponse();


    @Mock
    private SendSoknadPortType sendSoknadEndpoint;
    @InjectMocks
    private HenvendelseService service;


    @Before
    public void setup() {
        when(sendSoknadEndpoint.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(new WSBehandlingsId());
    }

    @Test
    public void testAvStartSoknadSenderRiktigDataVedOpprettelseAvSoknad() {
        service.startSoknad(FODSELSNUMMER, TEST_SKJEMANUMMER, TEST_TEMA, TEST_UUID, SOKNADS_TYPE_SEND_SOKNAD);

        verify(sendSoknadEndpoint).startSoknad(argument.capture());
        WSStartSoknadRequest request = argument.getValue();

        bekreftRitigeRequest(request, SOKNADS_TYPE_SEND_SOKNAD.toString());
        bekreftVedleggData(request);
    }

    @Test
    public void testAvStartSoknadSenderRiktigDataVedInnsendingAvEttersendelseTilSoknad() {
        service.startSoknad(FODSELSNUMMER, TEST_SKJEMANUMMER, TEST_TEMA, TEST_UUID, SOKNADS_TYPE_SEND_SOKNAD_ETTERSENDING);

        verify(sendSoknadEndpoint).startSoknad(argument.capture());
        WSStartSoknadRequest request = argument.getValue();

        bekreftRitigeRequest(request, SOKNADS_TYPE_SEND_SOKNAD_ETTERSENDING.toString());
        bekreftVedleggData(request);
    }

    @Test
    public void testEttersendingBrukerBehandlingskjedeIdDersomDenErSatt() {
        respons.setBehandlingskjedeId(BEHANDLINGSKJEDE_ID);
        respons.setBehandlingsId(BEHANDLINGS_ID);

        service.startEttersending(respons, "aktorId");

        verify(sendSoknadEndpoint).startSoknad(argument.capture());
        assertEquals(BEHANDLINGSKJEDE_ID, argument.getValue().getBehandlingskjedeId());
    }

    @Test
    public void testEttersendingBrukerBehandlingsIdDersomBehandlingskjedeIdIkkeErSatt() {
        respons.setBehandlingsId(BEHANDLINGS_ID);

        service.startEttersending(respons, "aktorId");

        verify(sendSoknadEndpoint).startSoknad(argument.capture());
        assertEquals(BEHANDLINGS_ID, argument.getValue().getBehandlingskjedeId());
    }


    private void bekreftRitigeRequest(WSStartSoknadRequest request, String forventetSoknadsType) {
        assertEquals(FODSELSNUMMER, request.getFodselsnummer());
        assertEquals(forventetSoknadsType, request.getType());
        assertEquals("", request.getBehandlingskjedeId());
    }

    private void bekreftVedleggData(WSStartSoknadRequest request) {
        XMLMetadataListe metadata = (XMLMetadataListe) request.getAny();
        assertEquals(1, metadata.getMetadata().size());
        XMLVedlegg vedlegg = (XMLVedlegg) metadata.getMetadata().get(0);

        assertEquals(TEST_SKJEMANUMMER, vedlegg.getSkjemanummer());
        assertEquals(TEST_UUID, vedlegg.getUuid());
        assertEquals(IKKE_VALGT.toString(), vedlegg.getInnsendingsvalg());
        assertEquals(TEST_TEMA, vedlegg.getTilleggsinfo());
    }
}
