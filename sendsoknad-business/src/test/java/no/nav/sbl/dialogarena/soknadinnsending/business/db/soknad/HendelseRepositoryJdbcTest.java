package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class HendelseRepositoryJdbcTest {

    public static final String BEHANDLINGS_ID_1 = "99";

    @Autowired
    private HendelseRepository hendelseRepository;
    @Autowired
    private TestSupport support;


    @After
    public void teardown() {
        support.getJdbcTemplate().execute("DELETE FROM hendelse");
    }

    @Test
    public void skalHenteVersjonForOpprettetSoknad() {
        hendelseRepository.registrerOpprettetHendelse(soknad());

        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(1);
    }

    @Test
    public void skalGiDefaultversjonForSoknadUtenHendelser() {
        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(0);
        assertThat(hendelseRepository.hentVersjon("XX")).isEqualTo(0);
    }


    @Test
    public void skalHenteVersjonForMellomlagretSoknad() {
        hendelseRepository.registrerOpprettetHendelse(soknad());
        hendelseRepository.registrerHendelse(new WebSoknad().medBehandlingId(BEHANDLINGS_ID_1), HendelseType.LAGRET_I_HENVENDELSE);

        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(1);
    }


    private WebSoknad soknad() {
        return new WebSoknad().medskjemaNummer("NAV-01").medBehandlingId(BEHANDLINGS_ID_1).medVersjon(1);
    }
}
