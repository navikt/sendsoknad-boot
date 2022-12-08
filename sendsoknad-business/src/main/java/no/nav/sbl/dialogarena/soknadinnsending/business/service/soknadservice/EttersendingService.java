package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggHentOgPersistService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.AVBRUTT_AV_BRUKER;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class EttersendingService {
    private static final Logger logger = getLogger(EttersendingService.class);

    private final HenvendelseService henvendelseService;
    private final VedleggHentOgPersistService vedleggService;
    private final FaktaService faktaService;
    private final SoknadRepository lokalDb;
    private final SoknadMetricsService soknadMetricsService;

    @Autowired
    public EttersendingService(HenvendelseService henvendelseService, VedleggHentOgPersistService vedleggService,
                               FaktaService faktaService, @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb,
                               SoknadMetricsService soknadMetricsService) {
        this.henvendelseService = henvendelseService;
        this.vedleggService = vedleggService;
        this.faktaService = faktaService;
        this.lokalDb = lokalDb;
        this.soknadMetricsService = soknadMetricsService;
    }


    public WebSoknad henvendelseMigrering(String behandlingsIdDetEttersendesPaa, String aktorId, DateTime innsendtDatoFromHenvendelse) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdDetEttersendesPaa);
        WSHentSoknadResponse nyesteSoknad = hentNyesteSoknadFraHenvendelse(behandlingskjede);

        List<XMLMetadata> alleVedlegg = ((XMLMetadataListe) nyesteSoknad.getAny()).getMetadata();
        List<XMLMetadata> vedleggBortsettFraKvittering = alleVedlegg.stream().filter(IKKE_KVITTERING).collect(toList());

        DateTime innsendtDato = hentOrginalInnsendtDato(behandlingskjede, nyesteSoknad.getBehandlingsId());
        if (innsendtDato == null) {
            logger.info("{}: Cannot find orginalInnsendtDato from Henvendelse. Using provided date instead: {}",
                    behandlingsIdDetEttersendesPaa, innsendtDatoFromHenvendelse);
            innsendtDato = innsendtDatoFromHenvendelse;
        }

        XMLHovedskjema hovedskjema = finnHovedskjema(vedleggBortsettFraKvittering);
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId(nyesteSoknad.getBehandlingsId())
                .medBehandlingskjedeId(nyesteSoknad.getBehandlingskjedeId())
                .medStatus(FERDIG)
                .medDelstegStatus(DelstegStatus.VEDLEGG_VALIDERT)
                .medUuid(randomUUID().toString())
                .medAktorId(aktorId)
                .medOppretteDato(nyesteSoknad.getOpprettetDato())
                .medskjemaNummer(hovedskjema.getSkjemanummer())
                .medJournalforendeEnhet(hovedskjema.getJournalforendeEnhet());
        soknad.setSistLagret(innsendtDato);
        soknad.setInnsendtDato(innsendtDato);

        lagreSoknadTilLokalDb(innsendtDato, vedleggBortsettFraKvittering, soknad);

        return soknad;
    }

    public String start(String behandlingsIdDetEttersendesPaa, String aktorId) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdDetEttersendesPaa);
        WSHentSoknadResponse nyesteSoknad = hentNyesteSoknadFraHenvendelse(behandlingskjede);

        Optional.ofNullable(nyesteSoknad.getInnsendtDato()).orElseThrow(() -> new SendSoknadException("Kan ikke starte ettersending på en ikke fullfort soknad"));

        String nyBehandlingsId = henvendelseService.startEttersending(nyesteSoknad, aktorId);
        String behandlingskjedeId = Optional.ofNullable(nyesteSoknad.getBehandlingskjedeId()).orElse(nyesteSoknad.getBehandlingsId());
        WebSoknad ettersending = lagreEttersendingTilLokalDb(behandlingsIdDetEttersendesPaa, behandlingskjede, behandlingskjedeId, nyBehandlingsId, aktorId);

        soknadMetricsService.startetSoknad(ettersending.getskjemaNummer(), true);

        return ettersending.getBrukerBehandlingId();
    }

    private WSHentSoknadResponse hentNyesteSoknadFraHenvendelse(List<WSBehandlingskjedeElement> behandlingskjede) {
        List<WSBehandlingskjedeElement> nyesteForstBehandlinger = behandlingskjede.stream()
                .filter(element -> AVBRUTT_AV_BRUKER != SoknadInnsendingStatus.valueOf(element.getStatus()))
                .sorted(NYESTE_FORST)
                .collect(toList());

        return henvendelseService.hentSoknad(nyesteForstBehandlinger.get(0).getBehandlingsId());
    }

    private WebSoknad lagreEttersendingTilLokalDb(String originalBehandlingsId, List<WSBehandlingskjedeElement> behandlingskjede,
                                                  String behandlingskjedeId, String ettersendingsBehandlingId, String aktorId) {
        List<XMLMetadata> alleVedlegg = ((XMLMetadataListe) henvendelseService.hentSoknad(ettersendingsBehandlingId).getAny()).getMetadata();
        List<XMLMetadata> vedleggBortsettFraKvittering = alleVedlegg.stream().filter(IKKE_KVITTERING).collect(toList());

        WebSoknad ettersending = lagSoknad(ettersendingsBehandlingId, behandlingskjedeId, finnHovedskjema(vedleggBortsettFraKvittering), aktorId);

        DateTime originalInnsendtDato = hentOrginalInnsendtDato(behandlingskjede, originalBehandlingsId);
        lagreSoknadTilLokalDb(originalInnsendtDato, vedleggBortsettFraKvittering, ettersending);
        return ettersending;
    }

    private void lagreSoknadTilLokalDb(
            DateTime originalInnsendtDato,
            List<XMLMetadata> vedleggBortsettFraKvittering,
            WebSoknad soknad
    ) {
        Long soknadId = lokalDb.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);

        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato(soknadId, originalInnsendtDato));
        vedleggService.hentVedleggOgPersister(new XMLMetadataListe(vedleggBortsettFraKvittering), soknadId);
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

    private XMLHovedskjema finnHovedskjema(List<XMLMetadata> vedleggBortsettFraKvittering) {

        return vedleggBortsettFraKvittering.stream()
                .filter(xmlMetadata -> xmlMetadata instanceof XMLHovedskjema)
                .map(xmlMetadata -> (XMLHovedskjema) xmlMetadata)
                .findFirst()
                .orElseThrow(() -> new SendSoknadException("Kunne ikke hente opp hovedskjema for søknad"));
    }
}
