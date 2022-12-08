package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * marker alle metoder som transactional. Alle operasjoner vil skje i en
 * transactional write context. Read metoder kan overstyre dette om det trengs.
 */
@Component("soknadInnsendingRepository")
@Transactional
public class SoknadRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadRepository {

    private static final Logger logger = getLogger(SoknadRepositoryJdbc.class);

    public static final String INSERT_FAKTUM = "insert into SOKNADBRUKERDATA (soknadbrukerdata_id, soknad_id, key, value, type, parrent_faktum, sistendret) values " +
            "(:faktumId, :soknadId, :key, :value, :typeString, :parrentFaktum, CURRENT_TIMESTAMP)";
    public static final String INSERT_FAKTUMEGENSKAP = "insert into FAKTUMEGENSKAP (soknad_id, faktum_id, key, value, systemegenskap) values (:soknadId, :faktumId, :key, :value, :systemEgenskap)";

    private static final FaktumRowMapper FAKTUM_ROW_MAPPER = new FaktumRowMapper();
    private static final FaktumEgenskapRowMapper FAKTUM_EGENSKAP_ROW_MAPPER = new FaktumEgenskapRowMapper();
    private static final SoknadRowMapper SOKNAD_ROW_MAPPER = new SoknadRowMapper();

    @Autowired
    private VedleggRepository vedleggRepository;

    @Autowired
    private HendelseRepository hendelseRepository;


    @Autowired
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    public Long opprettSoknad(WebSoknad soknad) {
        Long databasenokkel = getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_ID_SEQ"), Long.class);
        insertSoknad(soknad, databasenokkel);

        hendelseRepository.registrerOpprettetHendelse(soknad);
        return databasenokkel;
    }

    private void insertSoknad(WebSoknad soknad, Long databasenokkel) {
        getJdbcTemplate()
                .update("insert into soknad (soknad_id, uuid, brukerbehandlingid, navsoknadid, aktorid, opprettetdato, status, delstegstatus, behandlingskjedeid, journalforendeEnhet, sistlagret)" +
                                " values (?,?,?,?,?,?,?,?,?,?, CURRENT_TIMESTAMP)",
                        databasenokkel,
                        soknad.getUuid(),
                        soknad.getBrukerBehandlingId(),
                        soknad.getskjemaNummer(),
                        soknad.getAktoerId(),
                        soknad.getOpprettetDato().toDate(),
                        soknad.getStatus().name(),
                        soknad.getDelstegStatus().name(),
                        soknad.getBehandlingskjedeId(),
                        soknad.getJournalforendeEnhet());
    }



    public void populerFraStruktur(WebSoknad soknad) {
        logger.debug("I populerFraStruktur. Behandle soknadId = " + soknad.getSoknadId());
        insertSoknad(soknad, soknad.getSoknadId());
        hendelseRepository.registrerHendelse(soknad, HendelseType.HENTET_FRA_HENVENDELSE);

        List<FaktumEgenskap> egenskaper = soknad.getFakta().stream()
                .flatMap(faktum -> faktum.getFaktumEgenskaper().stream())
                .collect(toList());

        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUM, SqlParameterSourceUtils.createBatch(soknad.getFakta().toArray()));
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUMEGENSKAP, SqlParameterSourceUtils.createBatch(egenskaper.toArray()));
        for (Vedlegg vedlegg : soknad.getVedlegg()) {
            vedleggRepository.opprettEllerEndreVedlegg(vedlegg, null);
        }
    }

    public Optional<WebSoknad> hentEttersendingMedBehandlingskjedeId(String behandlingsId) {
        String sql = "select * from soknad where behandlingskjedeid = ? and status = 'UNDER_ARBEID'";
        return getJdbcTemplate().query(sql, SOKNAD_ROW_MAPPER, behandlingsId).stream().findFirst();
    }

    public Faktum hentFaktumMedKey(Long soknadId, String faktumKey) {
        final String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ?";
        Faktum faktum = hentEtObjectAv(sql, FAKTUM_ROW_MAPPER, soknadId, faktumKey);
        if (faktum == null) {
            return null;
        }
        return populerMedProperties(faktum);
    }

    public WebSoknad hentSoknad(Long id) {
        String sql = "select * from SOKNAD where soknad_id = ?";
        return hentEtObjectAv(sql, SOKNAD_ROW_MAPPER, id);
    }

    public WebSoknad hentSoknad(String behandlingsId) {
        String sql = "select * from SOKNAD where brukerbehandlingid = ?";
        return hentEtObjectAv(sql, SOKNAD_ROW_MAPPER, behandlingsId);
    }


    private <T> T hentEtObjectAv(String sql, RowMapper<T> mapper, Object... args) {
        List<T> objekter = getJdbcTemplate().query(sql, mapper, args);
        if (!objekter.isEmpty()) {
            return objekter.get(0);
        }
        return null;
    }

    public Optional<WebSoknad> plukkSoknadTilMellomlagring() {
        while (true) {
            String select = "select * from soknad where sistlagret < CURRENT_TIMESTAMP - (INTERVAL '1' HOUR) and batch_status = 'LEDIG'" + limit(1);
            Optional<WebSoknad> soknad = getJdbcTemplate().query(select, new SoknadRowMapper()).stream().findFirst();
            if (soknad.isEmpty()) {
                return Optional.empty();
            }
            String update = "update soknad set batch_status ='TATT' where soknad_id = ? and batch_status = 'LEDIG'";
            int rowsAffected = getJdbcTemplate().update(update, soknad.get().getSoknadId());
            if (rowsAffected == 1) {
                return Optional.ofNullable(hentSoknadMedData(soknad.get().getSoknadId()));
            }
        }
    }

    public void leggTilbake(WebSoknad webSoknad) {
        getJdbcTemplate().update("update soknad set batch_status = 'LEDIG' where soknad_id = ?", webSoknad.getSoknadId());
    }

    public WebSoknad hentSoknadMedData(Long id) {
        WebSoknad soknad = hentSoknad(id);
        if (soknad != null) {
            leggTilBrukerdataOgVedleggPaaSoknad(soknad, soknad.getBrukerBehandlingId());
        }
        return soknad;
    }

    public WebSoknad hentSoknadMedVedlegg(String behandlingsId) {
        WebSoknad soknad = hentSoknad(behandlingsId);
        if (soknad != null) {
            leggTilBrukerdataOgVedleggPaaSoknad(soknad, behandlingsId);
        }
        return soknad;
    }


    private void leggTilBrukerdataOgVedleggPaaSoknad(WebSoknad soknad, String behandlingsId) {
        soknad.medBrukerData(hentAlleBrukerData(behandlingsId)).medVedlegg(vedleggRepository.hentVedlegg(behandlingsId));
    }


    @Override
    public String hentBehandlingsIdTilFaktum(Long faktumId) {
        final String sql = "select brukerbehandlingId from soknad where soknad_id = (select soknad_id from soknadbrukerdata where soknadbrukerdata_id = ?)";
        List<String> strings = getJdbcTemplate().queryForList(sql, String.class, faktumId);
        if (!strings.isEmpty()) {
            return strings.get(0);
        } else {
            logger.debug("Fant ikke behandlingsId for faktumId {}", faktumId);
            return null;
        }
    }


    public List<Faktum> hentSystemFaktumList(Long soknadId, String key) {
        String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ? and type= ?";
        List<Faktum> fakta = getJdbcTemplate().query(sql, FAKTUM_ROW_MAPPER, soknadId, key, SYSTEMREGISTRERT.toString());
        List<FaktumEgenskap> egenskaper = select("select * from FAKTUMEGENSKAP where soknad_id = ?", FAKTUM_EGENSKAP_ROW_MAPPER, soknadId);

        Map<Long, Faktum> faktaMap = fakta.stream().collect(toMap(Faktum::getFaktumId, faktum -> faktum));

        for (FaktumEgenskap faktumEgenskap : egenskaper) {
            if (faktaMap.containsKey(faktumEgenskap.getFaktumId())) {
                faktaMap.get(faktumEgenskap.getFaktumId()).getProperties().put(faktumEgenskap.getKey(), faktumEgenskap.getValue());
            }
        }

        if (!fakta.isEmpty()) {
            return fakta;
        } else {
            return new ArrayList<>();
        }
    }

    public Long opprettFaktum(long soknadId, Faktum faktum) {
        return opprettFaktum(soknadId, faktum, false);
    }

    public Long opprettFaktum(long soknadId, Faktum faktum, Boolean systemLagring) {
        faktum.setSoknadId(soknadId);
        faktum.setFaktumId(getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class));
        getNamedParameterJdbcTemplate().update(INSERT_FAKTUM, forFaktum(faktum));
        Faktum lagretFaktum = hentFaktum(faktum.getFaktumId());
        lagreAlleEgenskaper(faktum,lagretFaktum ,systemLagring);
        return faktum.getFaktumId();
    }

    public Long oppdaterFaktum(Faktum faktum) {
        return oppdaterFaktum(faktum, false);
    }

    public Long oppdaterFaktum(Faktum faktum, Boolean systemLagring) {
        oppdaterBrukerData(faktum, systemLagring);
        return faktum.getFaktumId();
    }

    public void oppdaterFaktumBatched(List<Faktum> faktum) {
        oppdaterBrukerDataBatched(faktum);
    }

    @Override
    public List<Long> hentLedigeFaktumIder(int antall) {
        return getJdbcTemplate().queryForList(selectMultipleNextSequenceValues("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class, antall);
    }

    @Override
    public void batchOpprettTommeFakta(List<Faktum> fakta) {
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUM, SqlParameterSourceUtils.createBatch(fakta.toArray()));
    }

    private BeanPropertySqlParameterSource forFaktum(Faktum faktum) {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(faktum);
        parameterSource.registerSqlType("type", Types.VARCHAR);
        return parameterSource;
    }

    public Faktum hentFaktum(Long faktumId) {
        if (faktumId == null) {
            return null;
        }
        final String sql = "select * from SOKNADBRUKERDATA where soknadbrukerdata_id = ?";
        Faktum faktum = getJdbcTemplate().queryForObject(sql, FAKTUM_ROW_MAPPER, faktumId);
        populerMedProperties(faktum);
        return faktum;
    }


    private List<Faktum> hentAllFaktum(List<Long> faktumId) {
        if (faktumId.isEmpty()) {
            return emptyList();
        }
        final String sql = "select * from SOKNADBRUKERDATA where soknadbrukerdata_id in (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", faktumId);

        List<Faktum> faktum = getNamedParameterJdbcTemplate().query(sql, parameters, FAKTUM_ROW_MAPPER);
        populerMedProperties(faktum);
        return faktum;
    }

    private void populerMedProperties (List<Faktum> faktum) {
        List<Long> faktumId = faktum.stream()
                .map(Faktum::getFaktumId)
                .collect(toList());
        String propertiesSql = "select * from FAKTUMEGENSKAP where faktum_id in (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", faktumId);


        Map<Long, List<FaktumEgenskap>> faktumegenskaper = getNamedParameterJdbcTemplate()
                .query(propertiesSql, parameters, FAKTUM_EGENSKAP_ROW_MAPPER)
                .stream().collect(Collectors.groupingBy(FaktumEgenskap::getFaktumId));
        faktum.forEach(enFaktum -> {
                if (faktumegenskaper.containsKey(enFaktum.getFaktumId())) {
                    faktumegenskaper.get(enFaktum.getFaktumId()).forEach(enFaktum::medEgenskap);
                }
            }
        );
    }

    private Faktum populerMedProperties(Faktum faktum) {
        String propertiesSql = "select * from FAKTUMEGENSKAP where soknad_id = ? and faktum_id = ?";
        List<FaktumEgenskap> properties = getJdbcTemplate().query(propertiesSql, FAKTUM_EGENSKAP_ROW_MAPPER, faktum.getSoknadId(), faktum.getFaktumId());
        for (FaktumEgenskap faktumEgenskap : properties) {
            faktum.medEgenskap(faktumEgenskap);
        }
        return faktum;
    }

    private void oppdaterBrukerDataBatched(List<Faktum> faktumer) {
        Map<Long, Faktum> lagretFaktumer = hentAllFaktum(faktumer.stream()
                .map(Faktum::getFaktumId)
                .collect(toList())
        ).stream()
                .collect(Collectors.toMap(Faktum::getFaktumId, t -> t));


        var faktumPairs = faktumer.stream()
                .map(f -> new AbstractMap.SimpleEntry<>(f, lagretFaktumer.get(f.getFaktumId())))
                .collect(toList());

        faktumPairs.forEach(ent -> {
            var faktum = ent.getKey();
            var lagretFaktum = ent.getValue();
            if (faktum.getValue() != null && faktum.getValue().length() > 500) {
                logger.error("Prøver å opppdatere faktum med en value som overstiger 500 tegn. (Faktumkey: {}, Faktumtype: {}) ",
                        faktum.getKey(), faktum.getTypeString());
                faktum.setValue(faktum.getValue().substring(0, 500));
            }

            if (lagretFaktum == null) {
                if (faktum.getKey() == null) {
                    logger.error("Faktum (Faktumid: {}, Faktumkey: {}) forsøkt hentet, men returnerte null. Se SD-443", faktum.getFaktumId(), faktum.getKey());
                } else {
                    logger.info("Lagrer ikke faktum der FaktumKey er null");
                }
            }
        });

        List<Entry<Faktum, Faktum>> brukerGrensesnitFaktumer = faktumPairs.stream()
                .filter(ent -> ent.getValue() != null && ent.getValue().er(Faktum.FaktumType.BRUKERREGISTRERT))
                .collect(toList());


        String UPPDATE_BrukerGrensesnitFaktumer = "update soknadbrukerdata set value= ? where soknadbrukerdata_id = ? ";

        int [][] affectedRows = getJdbcTemplate().batchUpdate(UPPDATE_BrukerGrensesnitFaktumer, brukerGrensesnitFaktumer,brukerGrensesnitFaktumer.size() ,
                (ps, argument) -> {
                    ps.setString(1, argument.getKey().getValue());
                    ps.setLong(2, argument.getKey().getFaktumId());
                });
        int numberOfUpdatedRows = Arrays.stream(affectedRows).mapToInt(arr -> arr[0]).sum();
        logger.info("Affected rows during UPDATE BRUKERGRENSESNIT FAKTUM ER " + numberOfUpdatedRows);

        List<Entry<Faktum,Faktum>> lagreEgenskaperEntries = faktumPairs.stream()
                .filter(e -> e.getValue() != null && e.getKey().getKey() != null)
                .collect(toList());

        var DELETE_FAKTUM = "delete from faktumegenskap where faktum_id in (:ids)";

        var faktumIdToDelete = lagreEgenskaperEntries.stream()
                .map(e -> e.getKey().getFaktumId())
                .collect(toList());
        SqlParameterSource deleteParams = new MapSqlParameterSource("ids", faktumIdToDelete);

        getNamedParameterJdbcTemplate().update(DELETE_FAKTUM, deleteParams);

        lagreEgenskaperEntries.forEach(e -> {
            e.getKey().kopierFraProperies();
            if (e.getValue().er(SYSTEMREGISTRERT)) {
                e.getKey().kopierSystemlagrede(e.getValue());
            }
        });

        var allEgenskaper = lagreEgenskaperEntries.stream()
                .map(e -> e.getKey().getFaktumEgenskaper())
                .flatMap(Collection::stream)
                .collect(toList());

        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUMEGENSKAP,SqlParameterSourceUtils.createBatch(allEgenskaper));
    }


    private void oppdaterBrukerData(Faktum faktum, Boolean systemLagring) {
        Faktum lagretFaktum = hentFaktum(faktum.getFaktumId());
        // Siden faktum-value er endret fra CLOB til Varchar må vi få med oss om det skulle oppstå tilfeller
        // hvor dette lager problemer. Logges som kritisk
        if (faktum.getValue() != null && faktum.getValue().length() > 500) {
            logger.error("Prøver å opppdatere faktum med en value som overstiger 500 tegn. (Faktumkey: {}, Faktumtype: {}) ",
                    faktum.getKey(), faktum.getTypeString());
            faktum.setValue(faktum.getValue().substring(0, 500));
        }

        if (lagretFaktum == null) {
            if (faktum.getKey() == null) {
                logger.error("Faktum (Faktumid: {} ,Faktumkey: {}) forsøkt hentet, men returnerte null. Se SD-443", faktum.getFaktumId(), faktum.getKey());
            } else {
                logger.info("Lagrer ikke faktum der FaktumKey er null");
            }
        }
        if ((lagretFaktum != null && lagretFaktum.er(Faktum.FaktumType.BRUKERREGISTRERT)) || systemLagring) {
            getJdbcTemplate()
                    .update("update soknadbrukerdata set value=? where soknadbrukerdata_id = ? ",
                            faktum.getValue(), faktum.getFaktumId());
        }

        if (lagretFaktum != null && faktum.getKey() != null) {
            lagreAlleEgenskaper(faktum, lagretFaktum ,systemLagring);
        } else {
            logger.info("Lagrer ikke faktum der FaktumKey er null");
        }
    }

    private void lagreAlleEgenskaper(Faktum faktum, Faktum lagretFaktum, Boolean systemLagring) {

        if (systemLagring) {
            faktum.kopierFaktumegenskaper(lagretFaktum);
        } else {
            faktum.kopierFraProperies();
            if (lagretFaktum.er(SYSTEMREGISTRERT)) {
                faktum.kopierSystemlagrede(lagretFaktum);
            }
        }
        getJdbcTemplate().update("delete from faktumegenskap where faktum_id = ?", faktum.getFaktumId());
        getNamedParameterJdbcTemplate().batchUpdate(INSERT_FAKTUMEGENSKAP, SqlParameterSourceUtils.createBatch(faktum.getFaktumEgenskaper().toArray()));
    }

    public void slettBrukerFaktum(Long soknadId, Long faktumId) {
        getJdbcTemplate().update("delete from vedlegg where faktum in (select soknadbrukerdata_id " +
                "from SoknadBrukerdata sb where sb.soknad_id = ? and sb.parrent_faktum = ?)", soknadId, faktumId);
        getJdbcTemplate().update("delete from FaktumEgenskap where faktum_id in " +
                "( select soknadbrukerdata_id from SoknadBrukerdata sb " +
                "where sb.soknad_id = ? and sb.parrent_faktum = ?)", soknadId, faktumId);
        getJdbcTemplate().update("delete from FaktumEgenskap where soknad_id = ? and faktum_id = ?", soknadId, faktumId);
        getJdbcTemplate().update("delete from soknadbrukerdata where soknad_id = ? and soknadbrukerdata_id = ? and type = 'BRUKERREGISTRERT'", soknadId, faktumId);
        getJdbcTemplate().update("delete from SOKNADBRUKERDATA where soknad_id=? and parrent_faktum=?", soknadId, faktumId);
    }

    public void settSistLagretTidspunkt(Long soknadId) {
        getJdbcTemplate().update("update soknad set sistlagret = CURRENT_TIMESTAMP where soknad_id = ?", soknadId);
    }

    public List<Faktum> hentAlleBrukerData(String behandlingsId) {
        List<Faktum> fakta = select(
                "select * from SOKNADBRUKERDATA where soknad_id = (select soknad_id from SOKNAD where brukerbehandlingid = ?) order by soknadbrukerdata_id asc",
                FAKTUM_ROW_MAPPER,
                behandlingsId);
        List<FaktumEgenskap> egenskaper = select(
                "select * from FAKTUMEGENSKAP where soknad_id = (select soknad_id from SOKNAD where brukerbehandlingid = ?)",
                FAKTUM_EGENSKAP_ROW_MAPPER,
                behandlingsId);

        Map<Long, Faktum> faktaMap = fakta.stream().collect(toMap(Faktum::getFaktumId, faktum -> faktum));

        for (FaktumEgenskap faktumEgenskap : egenskaper) {
            if (faktaMap.containsKey(faktumEgenskap.getFaktumId())) {
                faktaMap.get(faktumEgenskap.getFaktumId()).medEgenskap(faktumEgenskap);
            }
        }
        return fakta;
    }

    private void slettSoknad(long soknadId, SoknadInnsendingStatus status) {
        logger.debug("Sletter søknad med ID: " + soknadId);
        getJdbcTemplate().update("delete from faktumegenskap where soknad_id = ?", soknadId);
        getJdbcTemplate().update("delete from soknadbrukerdata where soknad_id = ?", soknadId);
        getJdbcTemplate().update("update vedlegg set data = null, storrelse = 0 where soknad_id = ?", soknadId);
        getJdbcTemplate().update("update soknad set status=? where soknad_id = ?", status.name(), soknadId);
    }

    public void slettSoknad(WebSoknad soknad, HendelseType aarsakTilSletting) {
        slettSoknad(soknad.getSoknadId(), convertStatus(aarsakTilSletting));
        hendelseRepository.registrerHendelse(soknad, aarsakTilSletting);
    }

    private SoknadInnsendingStatus convertStatus(HendelseType aarsakTilSletting) {
        switch (aarsakTilSletting) {
            case INNSENDT:
                return SoknadInnsendingStatus.FERDIG;
            case AVBRUTT_AV_BRUKER:
                return SoknadInnsendingStatus.AVBRUTT_AV_BRUKER;
            default:
                return SoknadInnsendingStatus.AVBRUTT_AUTOMATISK;
        }
    }


    public void updateInnsendtDato(String behandlingskjedeId, long innsendtDato) {
        getJdbcTemplate().update("update soknad set INNSENDTDATO=? where BEHANDLINGSKJEDEID = ?", new Timestamp(innsendtDato), behandlingskjedeId);
    }

    public String findAktorIdFromHenvendelseMigration(String behandlingsId) {
        return getJdbcTemplate().queryForObject("select fnr from henvendelsemigration where behandlingsId = ? ", String.class, behandlingsId);
    }

    public String hentSoknadType(Long soknadId) {
        return getJdbcTemplate().queryForObject("select navsoknadid from soknad where soknad_id = ? ", String.class, soknadId);
    }

    public void settDelstegstatus(Long soknadId, DelstegStatus status) {
        getJdbcTemplate()
                .update("update soknad set delstegstatus=? where soknad_id = ?", status.name(), soknadId);
    }

    public void settDelstegstatus(String behandlingsId, DelstegStatus status) {
        getJdbcTemplate()
                .update("update soknad set delstegstatus=? where brukerbehandlingid = ?", status.name(), behandlingsId);
    }

    public void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet) {
        getJdbcTemplate()
                .update("update soknad set journalforendeenhet=? where brukerbehandlingid=?", journalforendeEnhet, behandlingsId);
    }

    private <T> List<T> select(String sql, RowMapper<T> rowMapper, Object... args) {
        return getJdbcTemplate().query(sql, args, rowMapper);
    }
}
