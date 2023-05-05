package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.IkkeFunnetException;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadDataFletterIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.BrukernotifikasjonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.UUID;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.AVBRUTT_AUTOMATISK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SoknadDataFletterIntegrationTestContext.class)
public class SoknadServiceIntegrasjonsTest {

    private final String BEHANDLINGSID = "EN_BEHANDLINGSID";
    private WebSoknad soknad;
    private final String uuid = "uuid";
    private String skjemaNummer = "NAV 11-12.10";
    private long soknadId;

    private SoknadService soknadService;

    @Autowired
    private TekstHenter tekstHenter;
    @Autowired
    private InnsendingService innsendingService;
    @Autowired
    private Filestorage filestorage;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SoknadRepository lokalDb;
    @Autowired
    private FaktaService faktaService;
    @Autowired
    private HendelseRepository hendelseRepository;
    @Autowired
    private VedleggRepository vedleggRepository;
    @Autowired
    private BrukernotifikasjonService brukernotifikasjonService;

    private final Brukernotifikasjon brukernotifikasjon = mock(Brukernotifikasjon.class);

    @Before
    public void setup() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();
        SoknadMetricsService soknadMetricsService = mock(SoknadMetricsService.class);
        WebSoknadConfig config = new WebSoknadConfig(lokalDb);

        SoknadDataFletter soknadDataFletter = new SoknadDataFletter(applicationContext,
                vedleggRepository, faktaService, lokalDb, hendelseRepository, config,
                new AlternativRepresentasjonService(config, tekstHenter), soknadMetricsService,
                innsendingService, filestorage, null, brukernotifikasjonService);

        soknadService = new SoknadService(lokalDb, null, null,
                soknadDataFletter, soknadMetricsService, brukernotifikasjon);

        soknadDataFletter.initBolker();
    }

    @After
    public void teardown() {
        if (soknad != null)
            lokalDb.slettSoknad(soknad.medId(soknadId), AVBRUTT_AUTOMATISK);
    }

    @Test
    public void startSoknadOppretterILokalDb() {
        String behandlingsId = soknadService.startSoknad("NAV 11-12.12", "aktorId");
        soknad = soknadService.hentSoknad(behandlingsId, false, false);

        assertNotNull(soknad);
    }

    @Test
    public void hentSoknadFraLokalDbReturnererPopulertSoknad() {
        skjemaNummer = "NAV 11-12.12";
        Long soknadId = opprettOgPersisterSoknad(BEHANDLINGSID, "aktor");

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);

        assertThat(webSoknad.getBrukerBehandlingId()).isEqualTo(BEHANDLINGSID);
        assertThat(webSoknad.getAktoerId()).isEqualTo("aktor");
        assertThat(webSoknad.getUuid()).isEqualTo(uuid);
        assertThat(webSoknad.getDelstegStatus()).isEqualTo(DelstegStatus.OPPRETTET);
        assertThat(webSoknad.getskjemaNummer()).isEqualTo(skjemaNummer);
    }

    @Test
    public void settDelstegPersistererNyttDelstegTilDb() {
        Long soknadId = opprettOgPersisterSoknad(BEHANDLINGSID, "aktor");

        soknadService.settDelsteg(BEHANDLINGSID, DelstegStatus.SAMTYKKET);

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad.getDelstegStatus()).isEqualTo(DelstegStatus.SAMTYKKET);
    }

    @Test
    public void settJournalforendeEnhetPersistererNyJournalforendeEnhetTilDb() {
        Long soknadId = opprettOgPersisterSoknad(BEHANDLINGSID, "aktor");

        soknadService.settJournalforendeEnhet(BEHANDLINGSID, "NAV UTLAND");

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad.getJournalforendeEnhet()).isEqualTo("NAV UTLAND");
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraLokalDb() {
        String behandlingsId = UUID.randomUUID().toString();
        Long soknadId = opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.avbrytSoknad(behandlingsId);

        soknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(soknad).isNull();
        verify(brukernotifikasjon, times(1)).cancelNotification(anyString(), anyString(), anyBoolean(), anyString());
    }

    @Test
    public void hentingAvSoknadSomIkkeErILokalDbKasterException() {
        String ikkeEksisterendeBehandlingsId = UUID.randomUUID().toString();

        assertThrows(IkkeFunnetException.class, () -> soknadService.hentSoknad(ikkeEksisterendeBehandlingsId, false, false));
    }

    @Test
    public void sendSoknadSkalLagreEnFilTilSoknadsfillager() {
        skjemaNummer = "NAV 10-07.40";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(filestorage, times(1)).store(eq(behandlingsId), any());
    }

    @Test
    public void sendSoknadSkalLagreToFilerTilSoknadsfillager() {
        skjemaNummer = "NAV 11-12.12";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknadMedData(behandlingsId, "aktor");
        lokalDb.opprettFaktum(soknadId, maalgruppeFaktum(), true);

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(filestorage, times(2)).store(eq(behandlingsId), any());
    }


    private String nyBehandlnigsId() {
        return UUID.randomUUID().toString();
    }

    private Long opprettOgPersisterSoknad(String behId, @SuppressWarnings("SameParameterValue") String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medBehandlingskjedeId(behId)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(skjemaNummer)
                .medOppretteDato(now());
        soknadId = lokalDb.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        return soknadId;
    }

    private void opprettOgPersisterSoknadMedData(String behId, @SuppressWarnings("SameParameterValue") String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medBehandlingskjedeId(behId)
                .medVersjon(0)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(skjemaNummer)
                .medOppretteDato(now());

        soknadId = lokalDb.opprettSoknad(soknad);
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.aarsak")
                .medValue("fasteboutgifter"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.periode")
                .medProperty("fom", "2015-07-22")
                .medProperty("tom", "2015-10-22"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.kommunestotte"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.adresseutgifter.aktivitetsadresse")
                .medProperty("utgift", "2000"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.adresseutgifter.hjemstedsaddresse"));

        soknad.setSoknadId(soknadId);
    }

    private Faktum maalgruppeFaktum() {
        return new Faktum()
                .medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                .medKey("maalgruppe")
                .medProperty("kodeverkVerdi", "ARBSOKER")
                .medProperty("fom", "2015-01-01");
    }
}
