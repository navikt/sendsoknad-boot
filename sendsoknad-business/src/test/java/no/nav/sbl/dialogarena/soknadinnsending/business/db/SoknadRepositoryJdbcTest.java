package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Interval;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SoknadRepositoryJdbcTest {

    @Autowired
    private SoknadRepository soknadRepository;
    @Autowired
    private HendelseRepository hendelseRepository;
    @Autowired
    private RepositoryTestSupport soknadRepositoryTestSupport;

    private WebSoknad soknad;
    private Long soknadId;

    private static final String AKTOR_ID = "1";
    private static final String BEHANDLINGS_ID = "1";
    private static final int VERSJONSNR = 1;
    private static final String SKJEMA_NUMMER = "skjemaNummer";
    private static final String test_UUID = "123";

    @After
    public void cleanUp() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Vedlegg");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from faktumegenskap");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from soknadbrukerdata");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Soknad");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Hendelse");
    }

    @Test
    public void skalKunneOppretteSoknad() {
        opprettOgPersisterSoknad();
    }

    @Test
    public void skalSetteSistLagret() {
        DateTimeUtils.setCurrentMillisFixed(new Date().getTime());

        opprettOgPersisterSoknad();
        soknadRepository.settSistLagretTidspunkt(soknadId);
        WebSoknad endret = soknadRepository.hentSoknad(soknadId);
        System.out.println(new DateTime());
        System.out.println(new DateTime(endret.getSistLagret()));
        Interval endretIntervall = new Interval(new DateTime().minusMillis(1000), new DateTime().plusMillis(1000));
        assertTrue(endretIntervall.contains(endret.getSistLagret()));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenAktorId() {
        soknad = WebSoknad.startSoknad()
                .medUuid(test_UUID)
                .medBehandlingId(BEHANDLINGS_ID)
                .medskjemaNummer(SKJEMA_NUMMER)
                .medOppretteDato(now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenBehandlingId() {
        soknad = WebSoknad.startSoknad()
                .medUuid(test_UUID)
                .medAktorId(AKTOR_ID)
                .medskjemaNummer(SKJEMA_NUMMER)
                .medOppretteDato(now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void skalIkkeKunneOppretteUtenskjemaNummer() {
        soknad = WebSoknad.startSoknad()
                .medUuid(test_UUID)
                .medAktorId(AKTOR_ID)
                .medBehandlingId(BEHANDLINGS_ID)
                .medOppretteDato(now());

        soknadRepository.opprettSoknad(soknad);
    }

    @Test
    public void skalKunneHenteOpprettetSoknad() {
        opprettOgPersisterSoknad();

        WebSoknad opprettetSoknad = soknadRepository.hentSoknad(soknadId);
        assertNotNull(opprettetSoknad);
        assertEquals(SoknadInnsendingStatus.UNDER_ARBEID, opprettetSoknad.getStatus());
        assertEquals(AKTOR_ID, opprettetSoknad.getAktoerId());
        assertEquals(BEHANDLINGS_ID, opprettetSoknad.getBrukerBehandlingId());
        assertEquals(SKJEMA_NUMMER, opprettetSoknad.getskjemaNummer());
    }

    @Test
    public void skalKunneHenteOpprettetSoknadMedBehandlingsId() {
        String behId = randomUUID().toString();
        opprettOgPersisterSoknad(behId, "aktor-3");

        WebSoknad opprettetSoknad = soknadRepository.hentSoknad(behId);

        assertNotNull(opprettetSoknad);
        assertEquals(SoknadInnsendingStatus.UNDER_ARBEID, opprettetSoknad.getStatus());
        assertEquals("aktor-3", opprettetSoknad.getAktoerId());
        assertEquals(behId, opprettetSoknad.getBrukerBehandlingId());
        assertEquals(SKJEMA_NUMMER, opprettetSoknad.getskjemaNummer());
    }

    @Test
    public void skalKunneHenteTidligereInnsendteGittBehandlingsKjedeId() {
        String behId = randomUUID().toString();
        opprettOgPersisterSoknad(behId, "aktor-3");

        WebSoknad opprettetSoknad = soknadRepository.hentSoknad(behId);
        opprettetSoknad.medStatus(SoknadInnsendingStatus.FERDIG);
        opprettetSoknad.setInnsendtDato(new DateTime().now());

        soknadRepository.oppdaterSoknadEtterInnsending(opprettetSoknad);

        WebSoknad hentOpprinneligInnsendt = soknadRepository.hentOpprinneligInnsendtSoknad(behId);

        assertNotNull(hentOpprinneligInnsendt);
        assertNotNull(hentOpprinneligInnsendt.getInnsendtDato());

        WebSoknad innsendtSoknad = soknadRepository.hentNyesteSoknadGittBehandlingskjedeId(behId);

        assertNotNull(innsendtSoknad);
        assertEquals(innsendtSoknad.getStatus().FERDIG, innsendtSoknad.getStatus());
        assertEquals("aktor-3", innsendtSoknad.getAktoerId());
        assertEquals(behId, innsendtSoknad.getBrukerBehandlingId());
        assertEquals(SKJEMA_NUMMER, innsendtSoknad.getskjemaNummer());
    }

    @Test
    @Ignore
    public void skalReturnereNullDersomIngenFerdigSoknadMedGittBehandlingsKjedeId() {
        String behId = randomUUID().toString();
        opprettOgPersisterSoknad(behId, "aktor-3");

        WebSoknad opprettetSoknad = soknadRepository.hentSoknad(behId);

        WebSoknad innsendtSoknad = soknadRepository.hentNyesteSoknadGittBehandlingskjedeId(behId);

        assertNull(innsendtSoknad);
    }

    @Test
    public void skalFaaNullVedUkjentBehandlingsId() {
        String behId = randomUUID().toString();
        WebSoknad soknad = soknadRepository.hentSoknad(behId);
        Assert.assertNull(soknad);
    }

    @Test
    public void skalKunneLagreBrukerData() {
        String key = "Key";
        String value = "Value";

        opprettOgPersisterSoknad();
        lagreData(key, null, value);
    }

    @Test
    public void skalKunneHenteLagretBrukerData() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        lagreData("key2", null, "value2");
        lagreData("key3", null, "value3");


        List<Faktum> soknadBrukerData = soknadRepository.hentAlleBrukerData(BEHANDLINGS_ID);

        assertNotNull(soknadBrukerData);
        assertEquals(3, soknadBrukerData.size());
    }

    @Test
    public void skalHenteSystemfaktum() {
        Faktum faktum = new Faktum().medKey("personalia").medSoknadId(12L).medProperty("fno", "123").medType(SYSTEMREGISTRERT);
        Faktum result = new Faktum().medKey("personalia").medSoknadId(11L).medProperty("fno", "123").medType(SYSTEMREGISTRERT);
        result.setFaktumId(soknadRepository.opprettFaktum(11L, faktum, true));

        List<Faktum> personalia = soknadRepository.hentSystemFaktumList(11L, "personalia");
        assertEquals(result, personalia.get(0));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void skalSletteFaktum() {
        opprettOgPersisterSoknad();
        Long id = lagreData("key", null, "value");
        Faktum faktum = soknadRepository.hentFaktum(id);
        assertNotNull(faktum);
        soknadRepository.slettBrukerFaktum(soknadId, id);
        soknadRepository.hentFaktum(id);
        fail("ikke slettet");
    }

    @Test
    public void skalKunneHenteFaktum() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        Long faktumId = lagreData("key2", null, "value2");
        lagreData("key3", null, "value3");

        soknadRepository.hentFaktum(faktumId);
    }

    @Test
    public void skalKunneHenteVersjon() {
        opprettOgPersisterSoknad();
        int versjon = hendelseRepository.hentVersjon(BEHANDLINGS_ID);
        assertEquals(1, versjon);
    }

    @Test
    public void skalFinneBehandlingsIdTilSoknadFraFaktumId() {
        Long soknadId = opprettOgPersisterSoknad("123abc", "aktor");
        Long faktumId = lagreData(soknadId, "key", null, "value");
        String behandlingsIdTilFaktum = soknadRepository.hentBehandlingsIdTilFaktum(faktumId);
        assertEquals("123abc", behandlingsIdTilFaktum);
    }

    @Test
    public void skalReturnereNullHvisFaktumIdIkkeFinnes() {
        String behandlingsIdTilFaktum = soknadRepository.hentBehandlingsIdTilFaktum(999L);
        assertNull(behandlingsIdTilFaktum);
    }


    @Test
    public void skalTaVarePaaSystemproperties() {
        soknadId = opprettOgPersisterSoknad();
        soknadRepository.opprettFaktum(soknadId, new Faktum().medSoknadId(soknadId).medKey("system1").medType(SYSTEMREGISTRERT));
    }

    @Test
    public void skalHenteSoknadType() {
        opprettOgPersisterSoknad();
        String s = soknadRepository.hentSoknadType(soknadId);
        assertEquals(soknad.getskjemaNummer(), s);
    }

    @Test
    public void skalSetteDelstegstatus() {
        opprettOgPersisterSoknad();
        soknadRepository.settDelstegstatus(soknadId, DelstegStatus.SAMTYKKET);
        assertEquals(DelstegStatus.SAMTYKKET, soknadRepository.hentSoknad(soknadId).getDelstegStatus());
    }

    @Test
    public void skalSetteDelstegstatusMedBehandlingsId() {
        opprettOgPersisterSoknad();
        soknadRepository.settDelstegstatus(BEHANDLINGS_ID, DelstegStatus.SAMTYKKET);
        assertEquals(DelstegStatus.SAMTYKKET, soknadRepository.hentSoknad(soknadId).getDelstegStatus());
    }

    @Test
    public void skalSetteJournalforendeEnhet() {
        opprettOgPersisterSoknad();
        soknadRepository.settJournalforendeEnhet(BEHANDLINGS_ID, "NAV EØS");
        assertEquals("NAV EØS", soknadRepository.hentSoknad(BEHANDLINGS_ID).getJournalforendeEnhet());
    }

    @Test
    public void skalKunneOppdatereLagretBrukerData() {
        String key = "key";
        String value = "value";
        String oppdatertValue = "oppdatert";

        opprettOgPersisterSoknad();
        Long faktumId = lagreData(key, null, value);


        Faktum ikkeOppdaterData = soknadRepository.hentAlleBrukerData(BEHANDLINGS_ID).get(0);
        assertNotNull(ikkeOppdaterData);
        assertEquals(value, ikkeOppdaterData.getValue());


        lagreData(key, faktumId, oppdatertValue);
        Faktum oppdaterData = soknadRepository.hentAlleBrukerData(BEHANDLINGS_ID).get(0);
        assertNotNull(oppdaterData);
        assertEquals(oppdatertValue, oppdaterData.getValue());
    }

    @Test
    public void skalKunneHenteSoknadMedBrukerData() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        lagreData("key2", null, "value2");
        lagreData("key3", null, "value3");

        WebSoknad soknadMedData = soknadRepository.hentSoknadMedData(soknadId);

        assertNotNull(soknadMedData);
        assertNotNull(soknadMedData.getFakta());
        assertEquals(3, soknadMedData.getFakta().size());
    }

    @Test
    public void skalReturnereNullOmSoknadMedBehandlingsIdIkkeFinnes() {
        opprettOgPersisterSoknad();
        WebSoknad soknadMedData = soknadRepository.hentSoknadMedVedlegg("soknadSomIkkeFinnes");
        assertNull(soknadMedData);
    }

    @Test
    public void skalReturnereNullOmSoknadMedSoknadIdIkkeFinnes() {
        opprettOgPersisterSoknad();
        WebSoknad soknadMedData = soknadRepository.hentSoknadMedData(1000000000L);
        assertNull(soknadMedData);
    }

    @Test
    public void plukkerRiktigeSoknaderPaaTversAvAlleTraader() throws InterruptedException {
        List<Long> soknaderSomSkalMellomlagres = lagreXSoknader(15, 3);
        lagreXSoknader(5, 0); // legger til søknader som ikke skal taes med

        final List<Long> soknaderSomBleMellomlagret = Collections.synchronizedList(new ArrayList<>());
        int numberOfThreads = 10;
        ExecutorService threadpool = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            threadpool.submit((Callable<Void>) () -> {
                while (true) {
                    Optional<WebSoknad> soknad = soknadRepository.plukkSoknadTilMellomlagring();
                    if (soknad.isPresent()) {
                        soknaderSomBleMellomlagret.add(soknad.get().getSoknadId());
                    } else {
                        break;
                    }
                }
                return null;
            });
        }
        threadpool.shutdown();
        threadpool.awaitTermination(1, TimeUnit.MINUTES);

        sort(soknaderSomSkalMellomlagres);
        sort(soknaderSomBleMellomlagret);
        assertEquals(soknaderSomSkalMellomlagres, soknaderSomBleMellomlagret);
    }

    @Test
    public void skalKunneSletteSoknad() {
        opprettOgPersisterSoknad();

        soknadRepository.slettSoknad(soknad, HendelseType.AVBRUTT_AV_BRUKER);

        WebSoknad soknad = soknadRepository.hentSoknad(soknadId);
        assertNull("Soknad deleted by user should be deleted", soknad);
        List<Hendelse> hendelser = hendelseRepository.hentHendelser(BEHANDLINGS_ID);
        assertNotNull(hendelser);
        assertTrue(!hendelser.stream().filter(f->f.getHendelseType().equals(HendelseType.PERMANENT_SLETTET_AV_BRUKER)).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void arkiverteSoknaderSkalSlettes() {
        Long id = opprettOgPersisterSoknad();

        WebSoknad webSoknad = soknadRepository.hentSoknad(id);
        webSoknad.setInnsendtDato(DateTime.now().minusDays(2));
        webSoknad.medStatus(SoknadInnsendingStatus.FERDIG);
        soknadRepository.oppdaterSoknadEtterInnsending(webSoknad);
        soknadRepository.updateArkiveringsStatus(webSoknad.getBrukerBehandlingId(), SoknadArkiveringsStatus.Arkivert);

        soknadRepository.finnOgSlettDataTilArkiverteSoknader(1);
        WebSoknad arkivertSoknad = soknadRepository.hentSoknad(id);
        assertNull(arkivertSoknad);
        List<Hendelse> hendelser = hendelseRepository.hentHendelser(BEHANDLINGS_ID);
        assertNotNull(hendelser);
        assertTrue(!hendelser.stream().filter(f->f.getHendelseType().equals(HendelseType.PERMANENT_SLETTET_AV_SYSTEM)).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void skalKunneSletteSoknadPermanent() {
        opprettOgPersisterSoknad();

        soknadRepository.slettGamleSoknaderPermanent(-1);

        WebSoknad soknad = soknadRepository.hentSoknad(soknadId);
        assertNull("Soknad should be be deleted", soknad);
        List<Hendelse> hendelser = hendelseRepository.hentHendelser(BEHANDLINGS_ID);
        assertNotNull(hendelser);
        assertTrue(!hendelser.stream().filter(f->f.getHendelseType().equals(HendelseType.PERMANENT_SLETTET_AV_SYSTEM)).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void skalKunneLeggeTilbake() {
        opprettOgPersisterSoknad();
        lagreData("key1", null, "value1");
        lagreData("key2", null, "value2");

        WebSoknad soknad = soknadRepository.hentSoknadMedData(soknadId);
        soknadRepository.leggTilbake(soknad);
        List<Faktum> soknadBrukerData = soknadRepository.hentAlleBrukerData(BEHANDLINGS_ID);
        assertNotNull(soknadBrukerData);
    }

    @Test
    public void skalRepopulereDatabaseOgSetteSistLagret() {
        soknad = WebSoknad.startSoknad()
                .medId(101L)
                .medUuid(test_UUID)
                .medAktorId("123123")
                .medBehandlingId("AH123")
                .medskjemaNummer(SKJEMA_NUMMER)
                .medOppretteDato(now())
                .medVersjon(0)
                .leggTilFaktum(new Faktum().medSoknadId(101L).medFaktumId(11L).medKey("key1").medValue("val1").medType(BRUKERREGISTRERT).medProperty("test", "test"))
                .leggTilFaktum(new Faktum().medSoknadId(101L).medFaktumId(12L).medKey("key2").medValue("val2").medType(SYSTEMREGISTRERT).medProperty("test2", "test2"))
                .medVedlegg(singletonList(new Vedlegg(101L, 11L, "L6", Vedlegg.Status.LastetOpp).medOpprettetDato(System.currentTimeMillis())));

        soknadRepository.populerFraStruktur(soknad);
        WebSoknad res = soknadRepository.hentSoknadMedData(soknad.getSoknadId());

        soknad.getVedlegg().get(0).setOpprettetDato(res.getVedlegg().get(0).getOpprettetDato());
        soknad.setSistLagret(res.getSistLagret());

        assertEquals(soknad, res);
        assertNotNull(res.getSistLagret());
    }

    @Test
    public void skalKunneHenteUtEttersendingMedBehandlingskjedeId() {
        opprettOgPersisterEttersending();

        Optional<WebSoknad> res = soknadRepository.hentEttersendingMedBehandlingskjedeId(BEHANDLINGS_ID);

        assertTrue(res.isPresent());
        assertEquals(DelstegStatus.ETTERSENDING_OPPRETTET, res.get().getDelstegStatus());
    }

    @Test
    public void skalKunneAvbryteGamleIkkeInnsendteSoknader() {
        WebSoknad nySoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.UNDER_ARBEID, new DateTime().minusDays(2),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad gammelIkkeInnsendtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.UNDER_ARBEID, new DateTime().minusDays(7*8 + 1),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad gammelAvbrudtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.AVBRUTT_AUTOMATISK, new DateTime().minusDays(7*8 + 2),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad innsendtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.FERDIG, new DateTime().minusDays(1 ),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad innsendtOgArkivertSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.FERDIG, new DateTime().minusDays(1 ),SoknadArkiveringsStatus.Arkivert);

        List<WebSoknad> slettedeSoknader = soknadRepository.slettGamleIkkeInnsendteSoknader(7*8);
        assertEquals(1, slettedeSoknader.size());
        assertEquals(gammelIkkeInnsendtSoknad.getSoknadId(), slettedeSoknader.get(0).getSoknadId());

        WebSoknad soknad = soknadRepository.hentSoknad(gammelIkkeInnsendtSoknad.getSoknadId());
        assertNotNull("Soknad should not be deleted", soknad);
        List<Hendelse> hendelser = hendelseRepository.hentHendelser(gammelIkkeInnsendtSoknad.getBrukerBehandlingId());
        assertNotNull(hendelser);
        assertTrue(!hendelser.stream().filter(f->f.getHendelseType().equals(HendelseType.AVBRUTT_AUTOMATISK)).collect(Collectors.toList()).isEmpty());

        WebSoknad ikkeSlettetSoknad = soknadRepository.hentSoknad(nySoknad.getSoknadId());
        assertNotNull("Soknad should not be deleted", soknad);
        assertEquals(nySoknad.getBrukerBehandlingId(), ikkeSlettetSoknad.getBrukerBehandlingId());

    }

    @Test
    public void skalSlettePermanentAlleGamleSoknader() {
        WebSoknad nySoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.UNDER_ARBEID, new DateTime().minusDays(2),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad avbrudtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.AVBRUTT_AUTOMATISK, new DateTime().minusDays(7*8 + 1),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad gammelAvbrudtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.AVBRUTT_AUTOMATISK, new DateTime().minusDays(7*26 + 2),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad nyInnsendtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.FERDIG, new DateTime().minusDays(1 ),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad gammelInnsendtIkkeArkivertSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.FERDIG, new DateTime().minusDays(7*26 + 1 ),SoknadArkiveringsStatus.ArkiveringFeilet);

        List<WebSoknad> slettedeSoknader = soknadRepository.slettGamleSoknaderPermanent(7*26);
        assertEquals(2, slettedeSoknader.size());
        assertTrue(slettedeSoknader.stream().map(s-> s.getBrukerBehandlingId()).toList().contains(gammelAvbrudtSoknad.getBrukerBehandlingId()));

        WebSoknad soknad = soknadRepository.hentSoknad(nyInnsendtSoknad.getSoknadId());
        assertNotNull("Soknad should not be deleted", soknad);

        List<Hendelse> hendelser = hendelseRepository.hentHendelser(gammelInnsendtIkkeArkivertSoknad.getBrukerBehandlingId());
        assertNotNull(hendelser);
        assertTrue(!hendelser.stream().filter(f->f.getHendelseType().equals(HendelseType.PERMANENT_SLETTET_AV_SYSTEM)).collect(Collectors.toList()).isEmpty());

    }


    @Test
    public void finnArkiverteSoknader() {
        int days = 3*7;
        WebSoknad nySoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.UNDER_ARBEID, new DateTime().minusDays(2),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad avbrudtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.AVBRUTT_AUTOMATISK, new DateTime().minusDays(7*8 + 1),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad gammelAvbrudtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.AVBRUTT_AUTOMATISK, new DateTime().minusDays(7*26 + 2),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad nyInnsendtSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.FERDIG, new DateTime().minusDays(1 ),SoknadArkiveringsStatus.IkkeSatt);
        WebSoknad gammelInnsendtIkkeArkivertSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.FERDIG, new DateTime().minusDays(days + 2 ),SoknadArkiveringsStatus.ArkiveringFeilet);
        WebSoknad gammelInnsendtArkivertSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.FERDIG, new DateTime().minusDays(days + 1 ),SoknadArkiveringsStatus.Arkivert);
        WebSoknad nyereArkivertSoknad = opprettOgPersisterSoknad(UUID.randomUUID().toString(), "123456789012", SoknadInnsendingStatus.FERDIG, new DateTime().minusDays(days - 1 ),SoknadArkiveringsStatus.Arkivert);

        List<WebSoknad> gamleArkiverteSoknader = soknadRepository.finnArkiverteSoknader(days);
        assertEquals(1, gamleArkiverteSoknader.size());
        assertTrue(gamleArkiverteSoknader.stream().map(s-> s.getBrukerBehandlingId()).toList().contains(gammelInnsendtArkivertSoknad.getBrukerBehandlingId()));

        WebSoknad soknad = soknadRepository.slettSoknadPermanent(gammelInnsendtArkivertSoknad.getSoknadId(), HendelseType.PERMANENT_SLETTET_AV_SYSTEM);

        List<Hendelse> hendelser = hendelseRepository.hentHendelser(gammelInnsendtArkivertSoknad.getBrukerBehandlingId());
        assertNotNull(hendelser);
        assertTrue(!hendelser.stream().filter(f->f.getHendelseType().equals(HendelseType.PERMANENT_SLETTET_AV_SYSTEM)).collect(Collectors.toList()).isEmpty());

    }

    @Test
    public void skalFaaNullDersomManProverAHenteEttersendingMedBehandlingskjedeIdOgDetIkkeFinnesNoen() {
        Optional<WebSoknad> res = soknadRepository.hentEttersendingMedBehandlingskjedeId(BEHANDLINGS_ID);

        assertFalse(res.isPresent());
    }


    private List<Long> lagreXSoknader(int antall, int timerSidenLagring) {
        List<Long> soknadsIder = new ArrayList<>(antall);
        for (int i = 0; i < antall; i++) {
            Long id = opprettOgPersisterSoknad();
            soknadRepositoryTestSupport.getJdbcTemplate().update("update soknad set sistlagret = CURRENT_TIMESTAMP - (INTERVAL '" + timerSidenLagring + "' HOUR) where soknad_id = ?", soknadId);
            soknadsIder.add(id);
        }
        return soknadsIder;
    }

    private Long opprettOgPersisterSoknad() {
        return opprettOgPersisterSoknad(BEHANDLINGS_ID, AKTOR_ID);
    }

    private void opprettOgPersisterEttersending() {
        soknad = WebSoknad.startEttersending(BEHANDLINGS_ID)
                .medUuid(test_UUID)
                .medAktorId(AKTOR_ID)
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medBehandlingskjedeId(BEHANDLINGS_ID)
                .medskjemaNummer(SKJEMA_NUMMER).medOppretteDato(now());
        soknadId = soknadRepository.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
    }

    private Long opprettOgPersisterSoknad(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(test_UUID)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medVersjon(VERSJONSNR)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(SKJEMA_NUMMER).medOppretteDato(now());
        soknadId = soknadRepository.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        return soknadId;
    }

    private WebSoknad opprettOgPersisterSoknad(String behId, String aktor, SoknadInnsendingStatus status, DateTime opprettetDato, SoknadArkiveringsStatus arkivert) {
        soknad = WebSoknad.startSoknad()
                .medUuid(test_UUID)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medVersjon(VERSJONSNR)
                .medStatus(status)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(SKJEMA_NUMMER)
                .medOppretteDato(opprettetDato)
                .medArkivStatus(arkivert);
        if (soknad.getArkiveringsStatus().equals(SoknadArkiveringsStatus.Arkivert))
            soknad.setInnsendtDato(soknad.getOpprettetDato());
        soknadId = soknadRepository.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        return soknad;
    }

    private Long lagreData(String key, Long faktumId, String value) {
        return lagreData(soknadId, key, faktumId, value);
    }

    private Long lagreData(Long soknadId, String key, Long faktumId, String value) {
        if (faktumId != null) {
            return soknadRepository.oppdaterFaktum(new Faktum().medSoknadId(soknadId).medFaktumId(faktumId).medKey(key).medValue(value).medType(BRUKERREGISTRERT));
        }
        return soknadRepository.opprettFaktum(soknadId, new Faktum().medSoknadId(soknadId).medKey(key).medValue(value).medType(BRUKERREGISTRERT));
    }
}