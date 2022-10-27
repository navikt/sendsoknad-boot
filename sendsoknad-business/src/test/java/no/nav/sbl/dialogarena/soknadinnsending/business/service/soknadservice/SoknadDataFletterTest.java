package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggFraHenvendelsePopulator;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.BrukernotifikasjonService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.activation.DataHandler;
import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SoknadDataFletterTest {

    private static final String AAP = "NAV 11-13.05";
    private static final String SKJEMA_NUMMER = "NAV 11-12.12";
    private static final List<String> SKJEMANUMMER_TILLEGGSSTONAD = asList("NAV 11-12.12", "NAV 11-12.13");
    private static final Vedlegg KVITTERING_REF = new Vedlegg()
            .medVedleggId(3L)
            .medFillagerReferanse("kvitteringRef")
            .medSkjemaNummer(SKJEMANUMMER_KVITTERING)
            .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
            .medStorrelse(3L)
            .medAntallSider(1);

    @Mock(name = "lokalDb")
    private SoknadRepository lokalDb;
    @Mock
    private HendelseRepository hendelseRepository;
    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private FillagerService fillagerService;
    @Mock
    private VedleggFraHenvendelsePopulator vedleggService;
    @Mock
    private FaktaService faktaService;
    @Mock
    private WebSoknadConfig config;
    @Mock
    private PersonaliaBolk personaliaBolk;
    @Mock
    private BarnBolk barnBolk;
    @Mock
    private ArbeidsforholdBolk arbeidsforholdBolk;
    @Mock
    private ApplicationContext applicationContex;
    @Mock
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private LegacyInnsendingService legacyInnsendingService;
    @Mock
    private InnsendingService innsendingService;
    @Mock
    private Filestorage filestorage;
    @Mock
    private BrukernotifikasjonService brukernotifikasjonService;


    @InjectMocks
    private SoknadDataFletter soknadServiceUtil;

    @InjectMocks
    private AlternativRepresentasjonService alternativRepresentasjonService;


    @Before
    public void setup() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();

        ReflectionTestUtils.setField(soknadServiceUtil, "sendDirectlyToSoknadsmottaker", true);
        ReflectionTestUtils.setField(soknadServiceUtil, "sendToSoknadsfillager", true);

        when(personaliaBolk.tilbyrBolk()).thenReturn(PersonaliaBolk.BOLKNAVN);
        when(barnBolk.tilbyrBolk()).thenReturn(BarnBolk.BOLKNAVN);
        when(arbeidsforholdBolk.tilbyrBolk()).thenReturn(ArbeidsforholdBolk.BOLKNAVN);

        Map<String, BolkService> bolker = new HashMap<>();
        bolker.put(PersonaliaBolk.class.getName(), personaliaBolk);
        bolker.put(BarnBolk.class.getName(), barnBolk);
        bolker.put(ArbeidsforholdBolk.class.getName(), arbeidsforholdBolk);
        when(applicationContex.getBeansOfType(BolkService.class)).thenReturn(bolker);

        soknadServiceUtil.initBolker();
        soknadServiceUtil.alternativRepresentasjonService = alternativRepresentasjonService;
        when(config.hentStruktur(any(String.class))).thenReturn(new SoknadStruktur());
    }


    @Test
    public void skalStarteSoknad() {
        String tema = "TSO";
        String tittel = "Søknad om tilleggsstønader";
        String behandlingsId = "123";
        final long soknadId = 69L;
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            lenient().when(henvendelsesConnector.startSoknad(anyString(), anyString(), anyString(), anyString(), any(SoknadType.class))).thenReturn(behandlingsId);
        }
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);
        String expectedTilleggsinfo = "{\"tittel\":\"" + tittel + "\",\"tema\":\"" + tema + "\"}";
        String bruker = "aktorId";

        soknadServiceUtil.startSoknad(SKJEMA_NUMMER, bruker);

        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            ArgumentCaptor<String> uid = ArgumentCaptor.forClass(String.class);
            verify(henvendelsesConnector).startSoknad(eq(bruker), eq(tittel), eq(expectedTilleggsinfo), uid.capture(), any(SoknadType.class));
        }
        ArgumentCaptor<WebSoknad> lagretSoknad = ArgumentCaptor.forClass(WebSoknad.class);
        WebSoknad soknad = new WebSoknad()
                .medId(soknadId)
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(SKJEMA_NUMMER)
                .medAktorId(bruker)
                .medOppretteDato(new DateTime())
                .medStatus(UNDER_ARBEID)
                .medDelstegStatus(OPPRETTET);
        verify(lokalDb).opprettSoknad(lagretSoknad.capture());
        assertThat(soknad.getskjemaNummer().equals(lagretSoknad.getValue().getskjemaNummer()));

        verify(brukernotifikasjonService, times(1)).newNotification(eq(tittel), eq(lagretSoknad.getValue().getBrukerBehandlingId()), eq(lagretSoknad.getValue().getBrukerBehandlingId()), eq(false), eq(bruker));
        verify(faktaService, atLeastOnce()).lagreFaktum(anyLong(), any(Faktum.class));
        DateTimeUtils.setCurrentMillisSystem();
    }


    @Test
    public void skalSendeSoknad() {
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medVedleggId(1L)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(2L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medVedleggId(2L)
                        .medSkjemaNummer("L8")
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        String behandlingsId = "123";
        WebSoknad webSoknad = new WebSoknad().medId(1L)
                .medAktorId("123456")
                .medBehandlingId(behandlingsId)
                .medUuid("uidHovedskjema")
                .medskjemaNummer(AAP)
                .medFaktum(new Faktum().medKey("personalia"))
                .medJournalforendeEnhet("enhet")
                .medVedlegg(vedlegg);

        when(lokalDb.hentSoknadMedVedlegg(behandlingsId)).thenReturn(webSoknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(webSoknad);
        when(vedleggService.hentVedleggOgKvittering(webSoknad)).thenReturn(mockHentVedleggForventninger(webSoknad));

        soknadServiceUtil.sendSoknad(behandlingsId, new byte[]{1, 2, 3}, new byte[]{4,5,6});

        verify(filestorage, times(2)).store(eq(behandlingsId), any());
        verify(innsendingService, times(1)).sendSoknad(any(), any(), any(), any(), any(), any());
        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            verify(legacyInnsendingService, times(1 /*TODO: Change to 0*/)).sendSoknad(any(), any(), any(), any(), any());
            verify(lokalDb, times(1)).slettSoknad(any(WebSoknad.class), any());
        }
        verify(hendelseRepository, times(1)).hentVersjon(eq(behandlingsId));
        verify(soknadMetricsService, times(1)).sendtSoknad(eq(AAP), eq(false));
    }

    @Test
    public void skalKunLagreSystemfakumPersonaliaForEttersendingerVedHenting() {
        WebSoknad soknad = new WebSoknad().medBehandlingId("123")
                .medskjemaNummer(SKJEMA_NUMMER)
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medId(1L);
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);

        soknadServiceUtil.hentSoknad("123", true, true);

        verify(personaliaBolk, times(1)).genererSystemFakta(isNull(), anyLong());
        verify(barnBolk, never()).genererSystemFakta(anyString(), anyLong());
    }

    @Test
    @Ignore
    public void skalPopulereFraHenvendelseNaarSoknadIkkeFinnes() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg").medInnsendingsvalg(Vedlegg.Status.LastetOpp);
        Vedlegg vedleggCheck = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg").medData(new byte[]{1, 2, 3});
        WebSoknad soknad = new WebSoknad().medBehandlingId("123").medskjemaNummer(SKJEMA_NUMMER).medId(11L)
                .medVedlegg(Collections.singletonList(vedlegg)).medStatus(UNDER_ARBEID);
        WebSoknad soknadCheck = new WebSoknad().medBehandlingId("123").medskjemaNummer(SKJEMA_NUMMER).medId(11L)
                .medVedlegg(Collections.singletonList(vedleggCheck));

        when(henvendelsesConnector.hentSoknad("123")).thenReturn(
                new WSHentSoknadResponse()
                        .withBehandlingsId("123")
                        .withStatus(WSStatus.UNDER_ARBEID.toString())
                        .withAny(new XMLMetadataListe()
                                .withMetadata(
                                        new XMLHovedskjema().withUuid("uidHovedskjema"),
                                        new XMLVedlegg().withUuid("uidVedlegg")))
        );
        when(lokalDb.hentSoknad("123")).thenReturn(null, soknad, soknad);
        when(lokalDb.hentSoknadMedVedlegg("123")).thenReturn(soknad, soknad);
        when(lokalDb.hentSoknadMedData(11L)).thenReturn(soknad);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(soknad, baos);
        DataHandler handler = mock(DataHandler.class);
        when(fillagerService.hentFil("uidHovedskjema"))
                .thenReturn(baos.toByteArray());
        when(fillagerService.hentFiler("123"))
                .thenReturn(Collections.singletonList(
                        new WSInnhold().withUuid("uidVedlegg").withInnhold(handler)
                ));

        WebSoknad webSoknad = soknadServiceUtil.hentSoknad("123", true, false);
        soknadServiceUtil.hentSoknad("123", true, false);

        verify(lokalDb, atMost(1)).populerFraStruktur(eq(soknadCheck));
        assertThat(webSoknad.getSoknadId()).isEqualTo(11L);
    }

    @Test
    public void lagreSystemfakumSomDefinertForSoknadVedHenting() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(SKJEMA_NUMMER)
                .medId(1L);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(soknad);

        soknadServiceUtil.hentSoknad("123", true, true);

        verify(personaliaBolk, times(1)).genererSystemFakta(isNull(), anyLong());
        verify(barnBolk, times(1)).genererSystemFakta(isNull(), anyLong());
        verify(arbeidsforholdBolk, never()).genererSystemFakta(anyString(), anyLong());
    }

    @Test
    public void skalSetteDelstegTilUtfyllingVedUgyldigDatoVerdiForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(1L)
                .medFaktum(
                        new Faktum()
                            .medKey("informasjonsside.stonad.bostotte")
                            .medValue("true")
                )
                .medFaktum(
                        new Faktum()
                                .medKey("bostotte.samling")
                                .medProperty("fom", "NaN-aN-aN")
                                .medProperty("tom", "NaN-aN-aN"));
        soknad = soknadServiceUtil.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
        assertThat(soknad.getDelstegStatus()).isEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalSetteDelstegTilUtfyllingVedNullVerdiForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(1L)
                .medFaktum(
                        new Faktum()
                                .medKey("informasjonsside.stonad.bostotte")
                                .medValue("true")
                )
                .medFaktum(
                        new Faktum()
                                .medKey("bostotte.samling")
                                .medProperty("fom", null)
                                .medProperty("tom", null)
                );
        soknad = soknadServiceUtil.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
        assertThat(soknad.getDelstegStatus()).isEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalIkkeSetteDelstegTilUtfyllingVedGyldigeDatoVerdierForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(1L)
                .medFaktum(
                        new Faktum()
                                .medKey("informasjonsside.stonad.bostotte")
                                .medValue("true")
                )
                .medFaktum(
                        new Faktum()
                                .medKey("bostotte.samling")
                                .medProperty("fom", "2017-01-01")
                                .medProperty("tom", "2017-02-02"));
        soknad = soknadServiceUtil.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
        assertThat(soknad.getDelstegStatus()).isNotEqualTo(DelstegStatus.UTFYLLING);
    }

    private static List<Vedlegg> mockHentVedleggForventninger(WebSoknad soknad) {

        List<Vedlegg> vedleggForventninger = soknad.getVedlegg();
        Vedlegg kvittering = KVITTERING_REF;
        if (kvittering != null) {
            vedleggForventninger.add(kvittering);
        }
        return vedleggForventninger;
    }
}
