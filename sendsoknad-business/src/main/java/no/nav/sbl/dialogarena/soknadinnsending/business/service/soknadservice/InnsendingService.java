package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
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
    private final EttersendingService ettersendingService;


    @Autowired
    public InnsendingService(Innsending innsending, Brukernotifikasjon brukernotifikasjon, EttersendingService ettersendingService) {
        this.innsending = innsending;
        this.brukernotifikasjon = brukernotifikasjon;
        this.ettersendingService = ettersendingService;
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
        brukernotifikasjon.cancelNotification(soknad.getBrukerBehandlingId(), soknad.getBrukerBehandlingId(), soknad.erEttersending(), soknad.getAktoerId());

        startEttersendingIfNeeded(soknad, vedlegg);
    }

    private void startEttersendingIfNeeded(WebSoknad soknad, List<Vedlegg> vedlegg) {

        vedlegg.stream()
                .filter(v -> v.getData() == null)
                .filter(v -> v.getInnsendingsvalg().erIkke(SendesSenere))
                .forEach(v -> logger.warn("{}: Vedlegg med id {} er ikke lastet opp. Status: {}",
                        soknad.getBrukerBehandlingId(), v.getVedleggId(), v.getInnsendingsvalg()));

        List<Vedlegg> paakrevdeVedlegg = vedlegg.stream().filter(v -> v.getInnsendingsvalg().er(SendesSenere)).collect(Collectors.toList());
        if (paakrevdeVedlegg.stream().anyMatch(v -> v.getData() == null)) {
            ettersendingService.start(soknad.getBehandlingskjedeId(), soknad.getAktoerId());
        } else {
            logger.warn("{}: Vedlegg har status SendesSenere og har data", soknad.getBrukerBehandlingId());
        }
    }
}
