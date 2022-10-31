package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggHentOgPersistService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.*;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class EttersendingService {
    private static final Logger logger = getLogger(EttersendingService.class);

    private final HenvendelseService henvendelseService;
    private final VedleggHentOgPersistService vedleggService;
    private final FaktaService faktaService;
    private final SoknadRepository lokalDb;
    private final SoknadMetricsService soknadMetricsService;
    private final Brukernotifikasjon brukernotifikasjonService;
    private final boolean sendDirectlyToSoknadsmottaker;

    @Autowired
    public EttersendingService(
            HenvendelseService henvendelseService,
            VedleggHentOgPersistService vedleggService,
            FaktaService faktaService,
            @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb,
            SoknadMetricsService soknadMetricsService,
            Brukernotifikasjon brukernotifikasjon,
            @Value("${innsending.sendDirectlyToSoknadsmottaker}") String sendDirectlyToSoknadsmottaker
    ) {
        this.henvendelseService = henvendelseService;
        this.vedleggService = vedleggService;
        this.faktaService = faktaService;
        this.lokalDb = lokalDb;
        this.soknadMetricsService = soknadMetricsService;
        this.brukernotifikasjonService = brukernotifikasjon;
        this.sendDirectlyToSoknadsmottaker = "true".equals(sendDirectlyToSoknadsmottaker);
    }



    @Transactional
    public String start(String behandlingsIdDetEttersendesPaa, String aktorId) {
        String nyBehandlingsId = UUID.randomUUID().toString();
        if (SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            // Forutsetter at lokaldatabase inneholder søkers innsendte søknader
            WebSoknad nyesteSoknad = lokalDb.hentNyesteSoknadGittBehandlingskjedeId(behandlingsIdDetEttersendesPaa);
            if (nyesteSoknad == null) {
                throw new SendSoknadException("Kan ikke opprette ettersending på en ikke fullfort soknad");
            }

            //@TODO endre lagringsmethoden å ikke bruke List<WSBehandlingskjedeElement> men å bruke original innsendingsdato
            List<Vedlegg> vedleggBortsettFraKvittering =
                    nyesteSoknad.getVedlegg().stream().filter(it-> !(SKJEMANUMMER_KVITTERING.equalsIgnoreCase(it.getSkjemaNummer()) || nyesteSoknad.getskjemaNummer().equalsIgnoreCase(it.getSkjemaNummer()))).collect(toList());
            WebSoknad ettersendingsSoknad = lagSoknad(nyBehandlingsId, behandlingsIdDetEttersendesPaa,
                    nyesteSoknad.getskjemaNummer(), nyesteSoknad.getJournalforendeEnhet(), nyesteSoknad.getAktoerId(),
                    vedleggBortsettFraKvittering);

            lagreEttersendingTilLokalDb(ettersendingsSoknad, nyesteSoknad.getInnsendtDato());
            soknadMetricsService.startetSoknad(nyesteSoknad.getskjemaNummer(), true);
            try {
                brukernotifikasjonService.newNotification(nyesteSoknad.getskjemaNummer(), nyBehandlingsId, behandlingsIdDetEttersendesPaa, true, aktorId);
            } catch (Throwable t) {
                logger.error("{}: Failed to create new Brukernotifikasjon", behandlingsIdDetEttersendesPaa, t);
            }

        } else {
            List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdDetEttersendesPaa);
            WSHentSoknadResponse nyesteSoknadFraHenvendelse = hentNyesteSoknadFraHenvendelse(behandlingskjede);

            if (nyesteSoknadFraHenvendelse == null) throw new SendSoknadException("Kan ikke starte ettersending på en ikke fullfort soknad");
            nyBehandlingsId = henvendelseService.startEttersending(nyesteSoknadFraHenvendelse, aktorId);
        }

        return nyBehandlingsId;
    }

    private void lagreEttersendingTilLokalDb(WebSoknad ettersendingsSoknad, DateTime nyesteInnsendtdato) {

        Long soknadId = lokalDb.opprettSoknad(ettersendingsSoknad);
        ettersendingsSoknad.setSoknadId(soknadId);

        // TODO sjekk hva hensikten med setting av innsendt dato er her
        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato(soknadId, nyesteInnsendtdato));
        ettersendingsSoknad.getVedlegg().forEach(v-> v.medSoknadId(soknadId).medVedleggId(null).medOpprinneligInnsendingsvalg(v.getInnsendingsvalg()).medStorrelse(0L).medFillagerReferanse(null));
        vedleggService.persisterVedlegg(ettersendingsSoknad.getVedlegg());
    }

    private WSHentSoknadResponse hentNyesteSoknadFraHenvendelse(List<WSBehandlingskjedeElement> behandlingskjede) {
        List<WSBehandlingskjedeElement> nyesteForstBehandlinger = behandlingskjede.stream()
                .filter(element -> FERDIG == SoknadInnsendingStatus.valueOf(element.getStatus()))
                .sorted(NYESTE_FORST)
                .collect(toList());

        if (nyesteForstBehandlinger.isEmpty()) return null;

        return henvendelseService.hentSoknad(nyesteForstBehandlinger.get(0).getBehandlingsId());
    }
    //@TODO vi trenger ikke å ha begge originalBehandlingsId og ettersendingsBehandlingsID. Det bør vare like.
    private WebSoknad lagreEttersendingTilLokalDb(String originalBehandlingsId, List<WSBehandlingskjedeElement> behandlingskjede,
                                                  String behandlingskjedeId, String ettersendingsBehandlingId, String aktorId) {
        List<XMLMetadata> alleVedlegg = ((XMLMetadataListe) henvendelseService.hentSoknad(ettersendingsBehandlingId).getAny()).getMetadata();
        // @TODO endre databasen så at vi har innsendtDato og lagre det
        DateTime innsendtDato = lokalDb.hentEttersendingMedBehandlingskjedeId(ettersendingsBehandlingId).map(t->t.getInnsendtDato()).orElse(null);
        List<XMLMetadata> vedleggBortsettFraKvittering = alleVedlegg.stream()
                .filter(IKKE_KVITTERING )
                .filter(IKKE_HOVEDSKJEMA)
                .peek(v-> logger.info("lagreEttersendingTilLokalDb: "+v))
                .collect(toList());

        WebSoknad ettersending = lagSoknad(ettersendingsBehandlingId, behandlingskjedeId, finnHovedskjema(vedleggBortsettFraKvittering), aktorId);

        Long soknadId = lokalDb.opprettSoknad(ettersending);
        ettersending.setSoknadId(soknadId);

        DateTime originalInnsendtDato = SoknadDataFletter.GCP_ARKIVERING_ENABLED ? innsendtDato : hentOrginalInnsendtDato(behandlingskjede, originalBehandlingsId);
        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato(soknadId, originalInnsendtDato));

        vedleggService.hentVedleggOgPersister(new XMLMetadataListe(vedleggBortsettFraKvittering), soknadId);
        return ettersending;
    }

    private Faktum soknadInnsendingsDato(Long soknadId, DateTime innsendtDato) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(innsendtDato.getMillis()))
                .medType(SYSTEMREGISTRERT);
    }

    private WebSoknad lagSoknad(String behandlingsId, String behandlingskjedeId, XMLHovedskjema hovedskjema, String aktorId) {
        return WebSoknad.startEttersending(behandlingsId)
                .medUuid(randomUUID().toString())
                .medAktorId(aktorId)
                .medskjemaNummer(hovedskjema.getSkjemanummer())
                .medBehandlingskjedeId(behandlingskjedeId)
                .medJournalforendeEnhet(hovedskjema.getJournalforendeEnhet());
    }

    private WebSoknad lagSoknad(String behandlingsId, String behandlingskjedeId, String skjemanr, String journalforendeEnhet, String aktorId, List<Vedlegg> vedlegg) {
        return WebSoknad.startEttersending(behandlingsId)
                .medUuid(randomUUID().toString())
                .medAktorId(aktorId)
                .medskjemaNummer(skjemanr)
                .medBehandlingskjedeId(behandlingskjedeId)
                .medJournalforendeEnhet(journalforendeEnhet)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medVedlegg(vedlegg);
    }

    private XMLHovedskjema finnHovedskjema(List<XMLMetadata> vedleggBortsettFraKvittering) {

        return vedleggBortsettFraKvittering.stream()
                .filter(xmlMetadata -> xmlMetadata instanceof XMLHovedskjema)
                .map(xmlMetadata -> (XMLHovedskjema) xmlMetadata)
                .findFirst()
                .orElseThrow(() -> new SendSoknadException("Kunne ikke hente opp hovedskjema for søknad"));
    }
}
