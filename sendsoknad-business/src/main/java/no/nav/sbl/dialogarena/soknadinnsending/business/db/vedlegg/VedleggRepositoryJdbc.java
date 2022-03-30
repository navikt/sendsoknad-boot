package no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.slf4j.LoggerFactory.getLogger;

@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)
@Component("vedleggRepository")
public class VedleggRepositoryJdbc extends JdbcDaoSupport implements VedleggRepository {
    private static final Logger logger = getLogger(VedleggRepositoryJdbc.class);

    private final DefaultLobHandler lobHandler;

    public VedleggRepositoryJdbc() {
        lobHandler = new DefaultLobHandler();
    }


    @Autowired
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public List<Vedlegg> hentVedleggUnderBehandling(String behandlingsId, String fillagerReferanse) {
        return getJdbcTemplate().query("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, opprinneliginnsendingsvalg, storrelse, opprettetdato, " +
                        "antallsider, fillagerReferanse, aarsak, filnavn, mimetype from Vedlegg where soknad_id = (select soknad_id from SOKNAD where brukerbehandlingid = ?) " +
                        "and fillagerreferanse = ? and innsendingsvalg = 'UnderBehandling'",
                new VedleggRowMapper(false), behandlingsId, fillagerReferanse);
    }

    @Override
    public List<Vedlegg> hentVedlegg(String behandlingsId) {
        List<Vedlegg> vedlegg = getJdbcTemplate().query("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, opprinneliginnsendingsvalg, storrelse, opprettetdato," +
                " antallsider, fillagerReferanse, aarsak, filnavn, mimetype from Vedlegg" +
                        " where soknad_id = (select soknad_id from SOKNAD where brukerbehandlingid = ?) and innsendingsvalg != 'UnderBehandling' ",
                new VedleggRowMapper(false), behandlingsId);
        return vedlegg.stream()
                .filter(IKKE_KVITTERING)
                .collect(toList());
    }

    @Override
    public void opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(Vedlegg vedlegg) {
        if(vedlegg.getVedleggId() == null) {
            opprettEllerEndreVedlegg(vedlegg, null);
        } else {
            lagreVedlegg(vedlegg.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
        }
    }

    @Override
    public Long opprettEllerEndreVedlegg(final Vedlegg vedlegg, final byte[] content) {
        if (vedlegg.getVedleggId() == null) {
            vedlegg.setVedleggId(getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("VEDLEGG_ID_SEQ"), Long.class));
        }
        if (vedlegg.getNavn() == null || "".equals(vedlegg.getNavn())) {
            logger.warn("I opprettEllerEndreVedlegg er ikke Vedleggsnavn satt for skjemaNummer = "+ vedlegg.getSkjemaNummer() + " for søknadId " + vedlegg.getSoknadId());
        }
        getJdbcTemplate().execute("insert into vedlegg(vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, opprinneliginnsendingsvalg, storrelse, antallsider," +
                        " fillagerReferanse, data, opprettetdato, aarsak, filnavn, mimetype) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate, ?, ?, ?)",

                new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                    @Override
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                        Vedlegg.Status opprinneligInnsendingsvalg = vedlegg.getOpprinneligInnsendingsvalg();

                        ps.setLong(1, vedlegg.getVedleggId());
                        ps.setLong(2, vedlegg.getSoknadId());
                        ps.setObject(3, vedlegg.getFaktumId());
                        ps.setString(4, getSkjemanummerMedTillegg(vedlegg));
                        ps.setString(5, vedlegg.getNavn());
                        ps.setString(6, vedlegg.getInnsendingsvalg().toString());
                        ps.setString(7, opprinneligInnsendingsvalg != null ? opprinneligInnsendingsvalg.toString() : null);
                        ps.setLong(8, vedlegg.getStorrelse());
                        ps.setLong(9, vedlegg.getAntallSider());
                        ps.setString(10, vedlegg.getFillagerReferanse());
                        lobCreator.setBlobAsBytes(ps, 11, content);
                        ps.setString(12, vedlegg.getAarsak());
                        ps.setString(13, vedlegg.getFilnavn()) ;
                        ps.setString(14, vedlegg.getMimetype());
                    }
                });
        return vedlegg.getVedleggId();
    }

    @Override
    public void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg) {
        getJdbcTemplate().update("update vedlegg set navn = ?, innsendingsvalg = ?, aarsak = ? where soknad_id = ? and vedlegg_id = ?",
                vedlegg.getNavn(), vedlegg.getInnsendingsvalg().toString(), vedlegg.getAarsak(), soknadId, vedleggId);
    }

    @Override
    public void lagreVedleggMedData(final Long soknadId, final Long vedleggId, final Vedlegg vedlegg, byte[] data) {

        try {
            getJdbcTemplate().update("update vedlegg set innsendingsvalg = ?, storrelse = ?, antallsider = ?, aarsak = ?, data = ?, filnavn = ?, mimetype = ? " +
                    "where soknad_id = ? and vedlegg_id = ?", new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement) throws SQLException {
                    preparedStatement.setString(1, vedlegg.getInnsendingsvalg().toString());
                    preparedStatement.setLong(2, vedlegg.getStorrelse());
                    preparedStatement.setLong(3, vedlegg.getAntallSider());
                    preparedStatement.setString(4, vedlegg.getAarsak());
                    preparedStatement.setBinaryStream(5, new ByteArrayInputStream(data), data.length);
                    preparedStatement.setString(6, vedlegg.getFilnavn());
                    preparedStatement.setString(7, vedlegg.getMimetype());
                    preparedStatement.setLong(8, soknadId);
                    preparedStatement.setLong(9, vedleggId);
                }
            });
        } catch (DataAccessException e) {
            throw new SendSoknadException("kunne ikke lagre vedlegg", e);
        }
    }

    public byte[] hentVedleggData(Long vedleggId) {
        List<InputStream> query = getJdbcTemplate().query("select data from Vedlegg where vedlegg_id = ?", new VedleggDataRowMapper(), vedleggId);
        if (!query.isEmpty()) {
            try {
                return IOUtils.toByteArray(query.get(0));
            } catch (IOException e) {
                throw new RuntimeException("Kunne ikke hente ut datainnhold", e);
            }
        }
        return new byte[]{};
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        Vedlegg v = hentVedlegg(vedleggId);
        if (v.getInnsendingsvalg().er(Vedlegg.Status.UnderBehandling)) {
            getJdbcTemplate().update("delete from vedlegg where soknad_id = ? and vedlegg_id = ?", soknadId, vedleggId);
        } else {
            getJdbcTemplate().update("update vedlegg set data = null, storrelse = 0, innsendingsvalg='VedleggKreves' where soknad_id = ? and vedlegg_id = ?", soknadId, vedleggId);
        }
    }

    @Override
    public void slettVedleggOgData(Long soknadId, Vedlegg vedlegg) {
        getJdbcTemplate().update("delete from vedlegg where soknad_id = ? and faktum = ? and skjemaNummer = ?",
                soknadId, vedlegg.getFaktumId(), getSkjemanummerMedTillegg(vedlegg));
    }

    @Override
    public String hentBehandlingsIdTilVedlegg(Long vedleggId) {
        final String sql = "select brukerbehandlingId from soknad where soknad_id = (select soknad_id from vedlegg where vedlegg_id = ?)";
        List<String> strings = getJdbcTemplate().queryForList(sql, String.class, vedleggId);
        if (!strings.isEmpty()) {
            return strings.get(0);
        } else {

            logger.debug("Fant ikke behandlingsId for vedleggId {}", vedleggId);
            return null;
        }
    }

    @Override
    public void slettVedleggUnderBehandling(Long soknadId, Long faktumId, String skjemaNummer, String skjemanummerTillegg) {
        String skjermaQuery = skjemaNummer + (skjemanummerTillegg != null? "|" + skjemanummerTillegg: "");
        if(faktumId == null){
            getJdbcTemplate().update("delete from vedlegg where soknad_id = ? and faktum  is null and skjemaNummer = ? and innsendingsvalg = 'UnderBehandling'", soknadId, skjermaQuery);
        } else {
            getJdbcTemplate().update("delete from vedlegg where soknad_id = ? and faktum = ? and skjemaNummer = ? and innsendingsvalg = 'UnderBehandling'", soknadId, faktumId, skjermaQuery);
        }
    }

    @Override
    public Vedlegg hentVedlegg(Long vedleggId) {
        return getJdbcTemplate().queryForObject("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, opprinneliginnsendingsvalg, storrelse, antallsider," +
                " fillagerReferanse, opprettetdato, aarsak, filnavn, mimetype from Vedlegg where vedlegg_id = ?", new VedleggRowMapper(false), vedleggId);
    }

    @Override
    public Vedlegg hentVedleggForskjemaNummer(Long soknadId, Long faktumId, String skjemaNummer) {
        if (faktumId == null) {
            return hentEtObjectAv("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, opprinneliginnsendingsvalg, storrelse, antallsider," +
                            " fillagerReferanse, opprettetdato, aarsak, filnavn, mimetype from Vedlegg" +
                            " where soknad_id = ? and faktum is null and skjemaNummer = ? and innsendingsvalg != 'UnderBehandling'",
                    new VedleggRowMapper(false), soknadId, skjemaNummer);
        } else {
            return hentEtObjectAv("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, opprinneliginnsendingsvalg, storrelse, antallsider," +
                            " fillagerReferanse, opprettetdato, aarsak, filnavn, mimetype from Vedlegg" +
                            " where soknad_id = ? and faktum = ? and skjemaNummer = ? and innsendingsvalg != 'UnderBehandling'",
                    new VedleggRowMapper(false), soknadId, faktumId, skjemaNummer);
        }

    }

    @Override
    public Vedlegg hentVedleggMedInnhold(Long vedleggId) {
        return getJdbcTemplate().queryForObject("select * from Vedlegg where vedlegg_Id = ?", new VedleggRowMapper(true), vedleggId);
    }

    @Override
    public List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktumId) {
        return getJdbcTemplate().query("select vedlegg_id, soknad_id,faktum, skjemaNummer, navn, innsendingsvalg, opprinneliginnsendingsvalg, storrelse, opprettetdato, antallsider," +
                        " fillagerReferanse, aarsak, filnavn, mimetype from Vedlegg where soknad_id = ? and faktum=? and innsendingsvalg in " +
                        "('VedleggKreves', 'LastetOpp', 'VedleggSendesAvAndre', 'VedleggSendesIkke', 'SendesSenere','SendesIkke', 'VedleggAlleredeSendt') ",
                new VedleggRowMapper(false), soknadId, faktumId);
    }

    private static final Predicate<? super Vedlegg> IKKE_KVITTERING = (Predicate<Vedlegg>) vedlegg -> !SKJEMANUMMER_KVITTERING.equalsIgnoreCase(vedlegg.getSkjemaNummer());

    private static String getSkjemanummerMedTillegg(Vedlegg vedlegg) {
        String skjemanummerMedTillegg = vedlegg.getSkjemaNummer();
        String tillegg = vedlegg.getSkjemanummerTillegg();

        if (tillegg != null && !tillegg.isEmpty()) {
            skjemanummerMedTillegg = skjemanummerMedTillegg + "|" + vedlegg.getSkjemanummerTillegg();
        }
        return skjemanummerMedTillegg;
    }

    private <T> T hentEtObjectAv(String sql, RowMapper<T> mapper, Object... args) {
        List<T> objekter = getJdbcTemplate().query(sql, mapper, args);
        if (!objekter.isEmpty()) {
            return objekter.get(0);
        }
        return null;
    }
}
