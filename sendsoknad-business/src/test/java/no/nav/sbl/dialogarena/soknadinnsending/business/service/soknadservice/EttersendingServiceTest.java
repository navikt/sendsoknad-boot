package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
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
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggHentOgPersistService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.*;

import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
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
    private ApplicationContext applicationContext;
    @Mock
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private WebSoknadConfig config;
    @Mock
    private VedleggHentOgPersistService vedleggHentOgPersistService;
    @Mock
    private Brukernotifikasjon brukernotifikasjon;
    @Mock
    private Filestorage filestorage;

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
        ReflectionTestUtils.setField(soknadServiceUtil, "sendToSoknadsfillager", true);
    }

    @Test
    public void skalStarteForsteEttersending() {
        String behandlingsId = "soknadBehandlingId";
        String ettersendingsBehandlingId = "ettersendingBehandlingId";
        String aktorId = "aktorId";
        Long soknadId = 11L;
        Long faktumId = 123L;
        Long ettersendingssoknadId = 124L;
        String hovedskjemanr = "hovedskjemanr";
        String vedleggsnr = "vedleggsnr";

        DateTime innsendingsDato = DateTime.now();
        Calendar calendar = Calendar.getInstance();
        java.util.Date currentTime = calendar.getTime();
        long time = currentTime.getTime();
        Timestamp timestamp = new Timestamp(time);


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

        Vedlegg hovedVedlegg = new Vedlegg(soknadId, faktumId, hovedskjemanr, Vedlegg.Status.LastetOpp);
        Vedlegg kvittering = new Vedlegg(soknadId, faktumId, SKJEMANUMMER_KVITTERING, Vedlegg.Status.LastetOpp);
        Vedlegg pakrevdVedlegg = new Vedlegg(soknadId, faktumId, vedleggsnr, Vedlegg.Status.SendesSenere);
        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(hovedVedlegg);
        vedlegg.add(kvittering);
        vedlegg.add(pakrevdVedlegg);

        WebSoknad nyesteInnsendtSoknad = WebSoknad.startSoknad()
                .medAktorId(aktorId)
                .medskjemaNummer(hovedskjemanr)
                .medBehandlingId(behandlingsId)
                .medInnsendtDato(timestamp)
                .medStatus(SoknadInnsendingStatus.FERDIG)
                .medVedlegg(vedlegg);

        List<Vedlegg> ettersendingsVedlegg = new ArrayList<>();
        ettersendingsVedlegg.add(pakrevdVedlegg);
        WebSoknad ettersendingsSoknad = WebSoknad.startSoknad()
                .medAktorId(aktorId)
                .medBehandlingskjedeId(behandlingsId)
                .medInnsendtDato(null)
                .medStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .medVedlegg(ettersendingsVedlegg);

        lenient().when(henvendelsesConnector.hentSoknad(ettersendingsBehandlingId)).thenReturn(ettersendingResponse);
        lenient().when(henvendelsesConnector.hentSoknad(behandlingsId)).thenReturn(orginalInnsending);
        lenient().when(henvendelsesConnector.hentBehandlingskjede(behandlingsId)).thenReturn(Collections.singletonList(behandlingsKjedeElement));
        lenient().when(henvendelsesConnector.startEttersending(eq(orginalInnsending), eq(aktorId))).thenReturn(ettersendingsBehandlingId);
        when(lokalDb.hentNyesteSoknadGittBehandlingskjedeId(eq(behandlingsId))).thenReturn(nyesteInnsendtSoknad);
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(ettersendingssoknadId);

        String ettersendingBehandlingsId = ettersendingService.start(behandlingsId, aktorId);

        verify(faktaService).lagreSystemFaktum(anyLong(), any(Faktum.class));
        verify(brukernotifikasjon, times(1)).newNotification(any(), eq(ettersendingBehandlingsId), eq(behandlingsId), eq(true), eq(aktorId));
        verify(soknadMetricsService, times(1)).startetSoknad(any(), eq(true));
        verify(faktaService, times(1)).lagreSystemFaktum(any(),any());
        verify(vedleggHentOgPersistService, times(1)).persisterVedlegg(any());
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
        lenient().when(henvendelsesConnector.hentBehandlingskjede(behandlingsId)).thenReturn(Collections.singletonList(behandlingskjedeElement));
        lenient().when(henvendelsesConnector.hentSoknad(behandlingsId)).thenReturn(orginalInnsending);

        ettersendingService.start(behandlingsId, "aktorId");
    }

    @Test
    public void skalKunLagreSystemfakumPersonaliaForEttersendingerVedHenting() {
        String behandlingsId = "123";
        WebSoknad soknad = new WebSoknad().medBehandlingId(behandlingsId)
                .medskjemaNummer(SKJEMANUMMER)
                .medDelstegStatus(ETTERSENDING_OPPRETTET)
                .medId(1L);
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);

        soknadServiceUtil.hentSoknad(behandlingsId, true, true);

        verify(filestorage, times(1)).getFileMetadata(eq(behandlingsId), any());
        verify(personaliaBolk, times(1)).genererSystemFakta(isNull(), anyLong());
        verify(config, times(2)).getSoknadTypePrefix(eq(1L));
        verify(barnBolk, never()).genererSystemFakta(anyString(), anyLong());
    }
}
