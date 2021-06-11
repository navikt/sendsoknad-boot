package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPGjenopptakInformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.MigrasjonHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStatus;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EttersendingServiceTest {

    @Mock(name = "lokalDb")
    private SoknadRepository lokalDb;
    @Mock
    private HendelseRepository hendelseRepository;
    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private FaktaService faktaService;
    @Mock
    private PersonaliaBolk personaliaBolk;
    @Mock
    private BarnBolk barnBolk;
    @Mock
    private ArbeidsforholdBolk arbeidsforholdBolk;
    @Mock
    private MigrasjonHandterer migrasjonHandterer;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private VedleggService vedleggService;
    @Mock
    private WebSoknadConfig config;
    @InjectMocks
    private SoknadDataFletter soknadServiceUtil;

    @InjectMocks
    private EttersendingService ettersendingService;

    private static final String SKJEMANUMMER = new AAPGjenopptakInformasjon().getSkjemanummer().get(0);


    @Before
    public void before() {
        Map<String, BolkService> bolker = new HashMap<>();
        bolker.put(PersonaliaBolk.class.getName(), personaliaBolk);
        bolker.put(BarnBolk.class.getName(), barnBolk);
        bolker.put(ArbeidsforholdBolk.class.getName(), arbeidsforholdBolk);
        when(applicationContext.getBeansOfType(BolkService.class)).thenReturn(bolker);

        soknadServiceUtil.initBolker();
        when(hendelseRepository.hentVersjon(anyString())).thenReturn(1);
    }

    @Test
    public void skalStarteForsteEttersending() {
        String behandlingsId = "soknadBehandlingId";
        String ettersendingsBehandlingId = "ettersendingBehandlingId";
        String aktorId = "aktorId";

        DateTime innsendingsDato = DateTime.now();

        WSBehandlingskjedeElement behandlingsKjedeElement = new WSBehandlingskjedeElement()
                .withBehandlingsId(behandlingsId)
                .withInnsendtDato(innsendingsDato)
                .withStatus(WSStatus.FERDIG.toString());

        WSHentSoknadResponse orginalInnsending = new WSHentSoknadResponse()
                .withBehandlingsId(behandlingsId)
                .withStatus(WSStatus.FERDIG.toString())
                .withInnsendtDato(innsendingsDato)
                .withAny(new XMLMetadataListe()
                        .withMetadata(
                                new XMLHovedskjema().withUuid("uidHovedskjema"),
                                new XMLVedlegg().withSkjemanummer("MittSkjemaNummer")));

        WSHentSoknadResponse ettersendingResponse = new WSHentSoknadResponse()
                .withBehandlingsId(ettersendingsBehandlingId)
                .withStatus(WSStatus.UNDER_ARBEID.toString())
                .withAny(new XMLMetadataListe()
                        .withMetadata(
                                new XMLHovedskjema().withUuid("uidHovedskjema"),
                                new XMLVedlegg().withSkjemanummer("MittSkjemaNummer").withInnsendingsvalg(Vedlegg.Status.SendesSenere.name())));

        when(henvendelsesConnector.hentSoknad(ettersendingsBehandlingId)).thenReturn(ettersendingResponse);
        when(henvendelsesConnector.hentSoknad(behandlingsId)).thenReturn(orginalInnsending);
        when(henvendelsesConnector.hentBehandlingskjede(behandlingsId)).thenReturn(Collections.singletonList(behandlingsKjedeElement));
        when(henvendelsesConnector.startEttersending(eq(orginalInnsending), eq(aktorId))).thenReturn(ettersendingsBehandlingId);

        Long soknadId = 11L;
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);

        String ettersendingBehandlingsId = ettersendingService.start(behandlingsId, aktorId);

        verify(faktaService).lagreSystemFaktum(anyLong(), any(Faktum.class));
        assertNotNull(ettersendingBehandlingsId);
    }

    @Test(expected = SendSoknadException.class)
    public void skalIkkeKunneStarteEttersendingPaaUferdigSoknad() {
        String behandlingsId = "UferdigSoknadBehandlingId";

        WSBehandlingskjedeElement behandlingskjedeElement = new WSBehandlingskjedeElement()
                .withBehandlingsId(behandlingsId)
                .withStatus(WSStatus.UNDER_ARBEID.toString());

        WSHentSoknadResponse orginalInnsending = new WSHentSoknadResponse()
                .withBehandlingsId(behandlingsId)
                .withStatus(WSStatus.UNDER_ARBEID.toString());
        when(henvendelsesConnector.hentBehandlingskjede(behandlingsId)).thenReturn(Collections.singletonList(behandlingskjedeElement));
        when(henvendelsesConnector.hentSoknad(behandlingsId)).thenReturn(orginalInnsending);

        ettersendingService.start(behandlingsId, "aktorId");
    }

    @Test
    public void skalKunLagreSystemfakumPersonaliaForEttersendingerVedHenting() {
        WebSoknad soknad = new WebSoknad().medBehandlingId("123")
                .medskjemaNummer(SKJEMANUMMER)
                .medDelstegStatus(ETTERSENDING_OPPRETTET)
                .medId(1L);
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);
        when(migrasjonHandterer.handterMigrasjon(any(WebSoknad.class))).thenReturn(soknad);

        soknadServiceUtil.hentSoknad("123", true, true);

        verify(personaliaBolk, times(1)).genererSystemFakta(isNull(), anyLong());
        verify(barnBolk, never()).genererSystemFakta(anyString(), anyLong());
    }
}
