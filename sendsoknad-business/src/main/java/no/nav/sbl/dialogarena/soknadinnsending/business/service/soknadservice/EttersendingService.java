package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggHentOgPersistService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class EttersendingService {
    private static final Logger logger = getLogger(EttersendingService.class);

    private final VedleggHentOgPersistService vedleggService;
    private final FaktaService faktaService;
    private final SoknadRepository lokalDb;
    private final SoknadMetricsService soknadMetricsService;
    private final Brukernotifikasjon brukernotifikasjonService;

    @Autowired
    public EttersendingService(
            VedleggHentOgPersistService vedleggService,
            FaktaService faktaService,
            @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb,
            SoknadMetricsService soknadMetricsService,
            Brukernotifikasjon brukernotifikasjon
    ) {
        this.vedleggService = vedleggService;
        this.faktaService = faktaService;
        this.lokalDb = lokalDb;
        this.soknadMetricsService = soknadMetricsService;
        this.brukernotifikasjonService = brukernotifikasjon;
    }

    @Transactional
    public String start(String behandlingsIdDetEttersendesPaa, String aktorId) {
        return start(behandlingsIdDetEttersendesPaa, aktorId, false);
    }


    @Transactional
    public String start(String behandlingsIdDetEttersendesPaa, String aktorId, Boolean erSystemGenerert) {
        String nyBehandlingsId = UUID.randomUUID().toString();
        // Forutsetter at lokaldatabase inneholder søkers innsendte søknader
        WebSoknad nyesteSoknad = lokalDb.hentNyesteSoknadGittBehandlingskjedeId(behandlingsIdDetEttersendesPaa);
        if (nyesteSoknad == null) {
            throw new SendSoknadException("Kan ikke opprette ettersending på en ikke fullfort soknad");
        }

        List<Vedlegg> vedleggBortsettFraKvittering = nyesteSoknad.getVedlegg().stream()
                .filter(v -> !(SKJEMANUMMER_KVITTERING.equalsIgnoreCase(v.getSkjemaNummer()) || nyesteSoknad.getskjemaNummer().equalsIgnoreCase(v.getSkjemaNummer())))
                .collect(toList());

        WebSoknad ettersendingsSoknad = lagSoknad(nyBehandlingsId, behandlingsIdDetEttersendesPaa,
                nyesteSoknad.getskjemaNummer(), nyesteSoknad.getJournalforendeEnhet(), nyesteSoknad.getAktoerId(),
                vedleggBortsettFraKvittering);

        lagreEttersendingTilLokalDb(ettersendingsSoknad, nyesteSoknad.getInnsendtDato());
        soknadMetricsService.startetSoknad(nyesteSoknad.getskjemaNummer(), true);
        try {
            String tittel = SkjemaOppslagService.getTittel(nyesteSoknad.getskjemaNummer());
            brukernotifikasjonService.newNotification(tittel, nyBehandlingsId, behandlingsIdDetEttersendesPaa, true, aktorId, erSystemGenerert);
        } catch (Exception e) {
            logger.error("{}: Failed to create new Brukernotifikasjon", behandlingsIdDetEttersendesPaa, e);
        }

        return nyBehandlingsId;
    }

    private void lagreEttersendingTilLokalDb(WebSoknad ettersendingsSoknad, DateTime nyesteInnsendtdato) {

        Long soknadId = lokalDb.opprettSoknad(ettersendingsSoknad);
        ettersendingsSoknad.setSoknadId(soknadId);

        // TODO sjekk hva hensikten med setting av innsendt dato er her
        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato(soknadId, nyesteInnsendtdato));
        ettersendingsSoknad.getVedlegg().forEach(v ->
                v.medSoknadId(soknadId)
                        .medVedleggId(null)
                        .medOpprinneligInnsendingsvalg(v.getInnsendingsvalg())
                        .medAntallSider(0)
                        .medStorrelse(0L)
                        .medFillagerReferanse(null)
        );
        vedleggService.persisterVedlegg(ettersendingsSoknad.getBrukerBehandlingId(), ettersendingsSoknad.getVedlegg());
    }

    private Faktum soknadInnsendingsDato(Long soknadId, DateTime innsendtDato) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(innsendtDato.getMillis()))
                .medType(SYSTEMREGISTRERT);
    }

    private WebSoknad lagSoknad(String behandlingsId, String behandlingskjedeId, String skjemanr,
                                String journalforendeEnhet, String aktorId, List<Vedlegg> vedlegg) {
        return WebSoknad.startEttersending(behandlingsId)
                .medUuid(randomUUID().toString())
                .medAktorId(aktorId)
                .medskjemaNummer(skjemanr)
                .medBehandlingskjedeId(behandlingskjedeId)
                .medJournalforendeEnhet(journalforendeEnhet)
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medVedlegg(vedlegg);
    }
}
