package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
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
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.BrukernotifikasjonService;
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

    private static final String BEHANDLINGSID = "71";
    private static final long SOKNADSID = 68L;
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
    private InnsendingService innsendingService;
    @Mock
    private Filestorage filestorage;
    @Mock
    private BrukernotifikasjonService brukernotifikasjonService;


    @InjectMocks
    private SoknadDataFletter soknadDataFletter;

    @InjectMocks
    private AlternativRepresentasjonService alternativRepresentasjonService;


    @Before
    public void setup() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();

        when(personaliaBolk.tilbyrBolk()).thenReturn(PersonaliaBolk.BOLKNAVN);
        when(barnBolk.tilbyrBolk()).thenReturn(BarnBolk.BOLKNAVN);
        when(arbeidsforholdBolk.tilbyrBolk()).thenReturn(ArbeidsforholdBolk.BOLKNAVN);

        Map<String, BolkService> bolker = new HashMap<>();
        bolker.put(PersonaliaBolk.class.getName(), personaliaBolk);
        bolker.put(BarnBolk.class.getName(), barnBolk);
        bolker.put(ArbeidsforholdBolk.class.getName(), arbeidsforholdBolk);
        when(applicationContex.getBeansOfType(BolkService.class)).thenReturn(bolker);

        soknadDataFletter.initBolker();
        soknadDataFletter.alternativRepresentasjonService = alternativRepresentasjonService;
        when(config.hentStruktur(any(String.class))).thenReturn(new SoknadStruktur());
    }


    @Test
    public void skalStarteSoknad() {
        String tittel = "Søknad om tilleggsstønader";
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(SOKNADSID);
        String bruker = "aktorId";

        soknadDataFletter.startSoknad(SKJEMA_NUMMER, bruker);

        ArgumentCaptor<WebSoknad> lagretSoknad = ArgumentCaptor.forClass(WebSoknad.class);
        WebSoknad soknad = new WebSoknad()
                .medId(SOKNADSID)
                .medBehandlingId(BEHANDLINGSID)
                .medskjemaNummer(SKJEMA_NUMMER)
                .medAktorId(bruker)
                .medOppretteDato(new DateTime())
                .medStatus(UNDER_ARBEID)
                .medDelstegStatus(OPPRETTET);
        verify(lokalDb).opprettSoknad(lagretSoknad.capture());

        assertThat(soknad.getskjemaNummer()).isEqualTo(lagretSoknad.getValue().getskjemaNummer());
        String expectedBehandlingsId = lagretSoknad.getValue().getBrukerBehandlingId();

        verify(brukernotifikasjonService, times(1))
                .newNotification(eq(tittel), eq(expectedBehandlingsId), eq(expectedBehandlingsId), eq(false), eq(bruker));
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

        WebSoknad webSoknad = new WebSoknad()
                .medId(SOKNADSID)
                .medAktorId("123456")
                .medBehandlingId(BEHANDLINGSID)
                .medUuid("uidHovedskjema")
                .medskjemaNummer(AAP)
                .medFaktum(new Faktum().medKey("personalia"))
                .medJournalforendeEnhet("enhet")
                .medVedlegg(vedlegg);

        when(lokalDb.hentSoknadMedVedlegg(eq(BEHANDLINGSID))).thenReturn(webSoknad);
        when(lokalDb.hentSoknadMedData(eq(SOKNADSID))).thenReturn(webSoknad);
        when(vedleggService.hentVedleggOgKvittering(webSoknad)).thenReturn(mockHentVedleggForventninger(webSoknad));

        soknadDataFletter.sendSoknad(BEHANDLINGSID, new byte[]{1, 2, 3}, new byte[]{4,5,6});

        verify(filestorage, times(2)).store(eq(BEHANDLINGSID), any());
        verify(innsendingService, times(1)).sendSoknad(any(), any(), any(), any(), any(), any());
        verify(hendelseRepository, times(1)).hentVersjon(eq(BEHANDLINGSID));
        verify(soknadMetricsService, times(1)).sendtSoknad(eq(AAP), eq(false));
    }

    @Test
    public void hentSoknad_hentSoknadMedData_skalKunLagreSystemfakumPersonaliaForEttersendingerVedHenting() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId(BEHANDLINGSID)
                .medskjemaNummer(SKJEMA_NUMMER)
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medId(SOKNADSID);
        when(lokalDb.hentSoknadMedVedlegg(eq(BEHANDLINGSID))).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(eq(SOKNADSID))).thenReturn(soknad);

        soknadDataFletter.hentSoknad(BEHANDLINGSID, true, true);

        verify(personaliaBolk, times(1)).genererSystemFakta(isNull(), anyLong());
        verify(barnBolk, never()).genererSystemFakta(anyString(), anyLong());
    }

    @Test
    @Ignore // TODO: Henvendelsetest
    public void skalPopulereFraHenvendelseNaarSoknadIkkeFinnes() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg").medInnsendingsvalg(Vedlegg.Status.LastetOpp);
        Vedlegg vedleggCheck = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg").medData(new byte[]{1, 2, 3});
        WebSoknad soknad = new WebSoknad().medBehandlingId("123").medskjemaNummer(SKJEMA_NUMMER).medId(11L)
                .medVedlegg(Collections.singletonList(vedlegg)).medStatus(UNDER_ARBEID);
        WebSoknad soknadCheck = new WebSoknad().medBehandlingId("123").medskjemaNummer(SKJEMA_NUMMER).medId(11L)
                .medVedlegg(Collections.singletonList(vedleggCheck));

        when(lokalDb.hentSoknad("123")).thenReturn(null, soknad, soknad);
        when(lokalDb.hentSoknadMedVedlegg("123")).thenReturn(soknad, soknad);
        when(lokalDb.hentSoknadMedData(11L)).thenReturn(soknad);

        /*
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(soknad, baos);
        DataHandler handler = mock(DataHandler.class);
        when(fillagerService.hentFil("uidHovedskjema"))
                .thenReturn(baos.toByteArray());
        when(fillagerService.hentFiler("123"))
                .thenReturn(Collections.singletonList(
                        new WSInnhold().withUuid("uidVedlegg").withInnhold(handler)
                ));
         */

        WebSoknad webSoknad = soknadDataFletter.hentSoknad("123", true, false);

        verify(lokalDb, atMost(1)).populerFraStruktur(eq(soknadCheck));
        assertThat(webSoknad.getSoknadId()).isEqualTo(11L);
    }

    @Test
    public void hentSoknad_hentSoknadMedData_lagreSystemfakumSomDefinertForSoknadVedHenting() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId(BEHANDLINGSID)
                .medskjemaNummer(SKJEMA_NUMMER)
                .medId(SOKNADSID);
        when(lokalDb.hentSoknadMedVedlegg(eq(BEHANDLINGSID))).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(eq(SOKNADSID))).thenReturn(soknad);

        soknadDataFletter.hentSoknad(BEHANDLINGSID, true, true);

        verify(personaliaBolk, times(1)).genererSystemFakta(isNull(), anyLong());
        verify(barnBolk, times(1)).genererSystemFakta(isNull(), anyLong());
        verify(arbeidsforholdBolk, never()).genererSystemFakta(anyString(), anyLong());
    }

    @Test
    public void skalSetteDelstegTilUtfyllingVedUgyldigDatoVerdiForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId(BEHANDLINGSID)
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(SOKNADSID)
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
        soknad = soknadDataFletter.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
        assertThat(soknad.getDelstegStatus()).isEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalSetteDelstegTilUtfyllingVedNullVerdiForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId(BEHANDLINGSID)
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(SOKNADSID)
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
        soknad = soknadDataFletter.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
        assertThat(soknad.getDelstegStatus()).isEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalIkkeSetteDelstegTilUtfyllingVedGyldigeDatoVerdierForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId(BEHANDLINGSID)
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(SOKNADSID)
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
        soknad = soknadDataFletter.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
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
