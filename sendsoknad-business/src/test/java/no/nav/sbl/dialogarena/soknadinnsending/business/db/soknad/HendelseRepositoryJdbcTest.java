package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.digipost.time.ControllableClock;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class HendelseRepositoryJdbcTest {

    public static final int FEMTISEKS_DAGER = 56;
    public static final int EN_DAG = 1;
    public static final int FEMTI_DAGER = 50;
    public static final String BEHANDLINGS_ID_1 = "99";
    @Autowired
    private HendelseRepository hendelseRepository;

    @Autowired
    private TestSupport support;

    @Autowired
    private Clock systemClock;
    private ControllableClock controllableClock;


    @Before
    public void setUp() {
        controllableClock = (ControllableClock) systemClock;
    }

    @After
    public void teardown() {
        support.getJdbcTemplate().execute("DELETE FROM hendelse");
    }

    @Test
    public void skalHenteVersjonForOpprettetSoknad() {
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));

        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(1);
    }

    @Test
    public void skalGiDefaultversjonForSoknadUtenHendelser() {
        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(0);
        assertThat(hendelseRepository.hentVersjon("XX")).isEqualTo(0);
    }


    @Test
    public void skalHenteVersjonForMigrertSoknad() {
        String behandlingsId1 = "ABD";
        hendelseRepository.registrerOpprettetHendelse(soknad(behandlingsId1));
        controllableClock.set(LocalDateTime.now().plusMinutes(1));
        hendelseRepository.registrerMigrertHendelse(soknad(behandlingsId1).medVersjon(2));

        assertThat(hendelseRepository.hentVersjon(behandlingsId1)).isEqualTo(2);
    }

    @Test
    public void skalHenteVersjonForMellomlagretSoknad() {
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));
        hendelseRepository.registrerHendelse(new WebSoknad().medBehandlingId(BEHANDLINGS_ID_1), HendelseType.LAGRET_I_HENVENDELSE);

        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(1);
    }

    @Test
    public void skalHenteVersjonForOpphentetMigrertSoknad() {
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));
        hendelseRepository.registrerHendelse(new WebSoknad().medBehandlingId(BEHANDLINGS_ID_1), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerHendelse(new WebSoknad().medBehandlingId(BEHANDLINGS_ID_1), HendelseType.HENTET_FRA_HENVENDELSE);
        controllableClock.set(LocalDateTime.now().plusMinutes(1));
        hendelseRepository.registrerMigrertHendelse(soknad(BEHANDLINGS_ID_1).medVersjon(2));

        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(2);
    }

    @Test
    public void skalHenteForGammelUnderArbeidSak() {
        stillTidenTilbake(FEMTISEKS_DAGER);

        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }



    @Test
    public void skalIkkeHenteNyereUnderArbeidSak() {
        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalBehandleMigertSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));
        hendelseRepository.registrerMigrertHendelse(soknad(BEHANDLINGS_ID_1).medVersjon(2));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }

    @Test
    public void skalBehandleMellomlagretSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));
        hendelseRepository.registrerHendelse(soknad(BEHANDLINGS_ID_1), HendelseType.LAGRET_I_HENVENDELSE);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }

    @Test
    public void skalBehandleOpphentetSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));
        hendelseRepository.registrerHendelse(soknad(BEHANDLINGS_ID_1), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerHendelse(soknad(BEHANDLINGS_ID_1), HendelseType.HENTET_FRA_HENVENDELSE);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErAvbruttAvBruker() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));
        hendelseRepository.registrerHendelse(soknad(BEHANDLINGS_ID_1), HendelseType.AVBRUTT_AV_BRUKER);

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(soknad("2"));
        hendelseRepository.registrerHendelse(soknad("2"), HendelseType.AVBRUTT_AV_BRUKER);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErAvbruttAutomatisk() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));
        hendelseRepository.registrerHendelse(soknad(BEHANDLINGS_ID_1), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerAutomatiskAvsluttetHendelse(BEHANDLINGS_ID_1);

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(soknad("2"));
        hendelseRepository.registrerHendelse(soknad("2"), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerAutomatiskAvsluttetHendelse("2");

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErInnsendt() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad(BEHANDLINGS_ID_1));
        hendelseRepository.registrerHendelse(soknad(BEHANDLINGS_ID_1), HendelseType.INNSENDT);

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(soknad("2"));
        hendelseRepository.registrerHendelse(soknad("2"), HendelseType.INNSENDT);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }


    private WebSoknad soknad(String behandlingsId) {
        return new WebSoknad().medskjemaNummer("NAV-01").medBehandlingId(behandlingsId).medVersjon(1);
    }

    private void stillTidenTilbake(int antallDager) {
        controllableClock.set(LocalDateTime.now().minusDays(antallDager));
    }
}
