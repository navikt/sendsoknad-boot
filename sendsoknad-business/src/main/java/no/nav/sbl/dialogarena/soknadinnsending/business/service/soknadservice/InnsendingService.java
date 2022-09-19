package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadinnsending.brukernotifikasjon.Brukernotifikasjon;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata;
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata;
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.SendesSenere;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendingDataMappers.*;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class InnsendingService {
    private static final Logger logger = getLogger(InnsendingService.class);

    private final Innsending innsending;
    private final Brukernotifikasjon brukernotifikasjon;
    private final SoknadService soknadService;


    @Autowired
    public InnsendingService(Innsending innsending, Brukernotifikasjon brukernotifikasjon, SoknadService soknadService) {
        this.innsending = innsending;
        this.brukernotifikasjon = brukernotifikasjon;
        this.soknadService = soknadService;
    }

    public void sendSoknad(
            WebSoknad soknad,
            List<AlternativRepresentasjon> alternativeRepresentations,
            List<Vedlegg> vedlegg,
            byte[] pdf,
            byte[] fullSoknad,
            String fullSoknadId
    ) {
        Soknadsdata soknadsdata = mapWebSoknadToSoknadsdata(soknad);
        List<Hovedskjemadata> hovedskjemas = mapToHovedskjemadataList(soknad, alternativeRepresentations, pdf, fullSoknad, fullSoknadId);
        List<Vedleggsdata> vedleggdata = mapVedleggToVedleggdataList(soknad.getBrukerBehandlingId(), vedlegg);

        innsending.sendInn(soknadsdata, vedleggdata, hovedskjemas);
        brukernotifikasjon.cancelNotification(soknad.getskjemaNummer(), soknad.getBrukerBehandlingId(), soknad.erEttersending(), soknad.getAktoerId());

        startEttersendingIfNeeded(soknad, vedlegg);
    }

    private void startEttersendingIfNeeded(WebSoknad soknad, List<Vedlegg> vedlegg) {

        vedlegg.stream()
                .filter(v -> v.getData() == null)
                .filter(v -> v.getInnsendingsvalg().erIkke(SendesSenere))
                .forEach(v -> logger.warn("{}: Funnet Vedlegg som er ikke lastet opp med status {}",
                        soknad.getBrukerBehandlingId(), v.getInnsendingsvalg()));

        List<Vedlegg> paakrevdeVedlegg = vedlegg.stream().filter(v -> v.getInnsendingsvalg().er(SendesSenere)).collect(Collectors.toList());
        if (paakrevdeVedlegg.stream().anyMatch(v -> v.getData() == null)) {
            soknadService.startEttersending(soknad.getBehandlingskjedeId(), soknad.getAktoerId());
        } else {
            logger.warn("{}: Vedlegg har status SendesSenere og har data", soknad.getBrukerBehandlingId());
        }
    }
}
