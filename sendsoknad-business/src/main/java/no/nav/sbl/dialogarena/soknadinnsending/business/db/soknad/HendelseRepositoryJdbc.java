package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.Hendelse;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.MIGRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.whereLimit;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@Transactional
public class HendelseRepositoryJdbc extends NamedParameterJdbcDaoSupport implements HendelseRepository {

    private static final Logger logger = getLogger(HendelseRepositoryJdbc.class);

    @Autowired
    private Clock clock;

    public HendelseRepositoryJdbc() {
    }


    @Autowired
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    public void registrerOpprettetHendelse(WebSoknad soknad) {
        insertHendelse(soknad.getBrukerBehandlingId(), OPPRETTET.name(), soknad.getVersjon(), soknad.getskjemaNummer());
    }

    public void registrerHendelse(WebSoknad soknad, HendelseType hendelse) {
        insertHendelse(soknad.getBrukerBehandlingId(), hendelse.name(), soknad.getVersjon(), soknad.getskjemaNummer());
    }

    public List<Hendelse> hentHendelser(String behandlingsid) {
        String sql = "select * from hendelse where behandlingsid=?";
        return getJdbcTemplate().query(sql, new HendelseRowMapper(), behandlingsid);
    }

    @Transactional(readOnly = true)
    public Integer hentVersjon(String behandlingsId) {
        try {
            Object[] args = {OPPRETTET.name(), MIGRERT.name(), behandlingsId};
            return getJdbcTemplate().queryForObject(
                    "SELECT * FROM (" + "SELECT versjon FROM hendelse WHERE (hendelse_type = ? or hendelse_type = ?) AND behandlingsid = ?" + " ORDER BY hendelse_tidspunkt DESC)"
                            + whereLimit(1),
                    args, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            /*
             * Dersom det ikke finnes noen hendelser hvor det er satt versjon antar vi at soknader er opprettet før
             * migrering var en mulighet og gir den dermed defaultversjonen. Når man skal gjøre en migrering
             * vil det være viktig å avstemme søknadene under arbeid i henvendelse med innholdet i hendelsene for å sjekke
             * de ligger i hendelsetabellen med rett versjon
             **/
            return KravdialogInformasjon.DEFAULT_VERSJON;
        }
    }


    private void insertHendelse(String behandlingsid, String hendelse_type, Integer versjon, String skjemanummer) {
        logger.info("{}: lagre hendelse {}", behandlingsid, hendelse_type);
        getJdbcTemplate()
                .update("update hendelse set SIST_HENDELSE = 0 where BEHANDLINGSID = ? AND SIST_HENDELSE=1", behandlingsid);
        getJdbcTemplate()
                .update("insert into hendelse (BEHANDLINGSID, HENDELSE_TYPE, HENDELSE_TIDSPUNKT, VERSJON, SKJEMANUMMER, SIST_HENDELSE)" +
                                " values (?,?,?,?,?, 1)",
                        behandlingsid,
                        hendelse_type,
                        new Timestamp(clock.instant().toEpochMilli()),
                        versjon,
                        skjemanummer);
    }
}
