package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata;
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata;
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendingDataMappers.*;

@Service
public class InnsendingService {

    private final Innsending innsending;
    private final Brukernotifikasjon brukernotifikasjon;


    @Autowired
    public InnsendingService(Innsending innsending, Brukernotifikasjon brukernotifikasjon) {
        this.innsending = innsending;
        this.brukernotifikasjon = brukernotifikasjon;
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

        publiserBrukernotifikasjoner(soknad, vedlegg);
    }

    private void publiserBrukernotifikasjoner(WebSoknad soknad, List<Vedlegg> vedlegg) {
        String behandlingskjedeId = soknad.getBehandlingskjedeId();

        brukernotifikasjon.cancelNotification(
                soknad.getBrukerBehandlingId(),
                behandlingskjedeId != null ? behandlingskjedeId : soknad.getBrukerBehandlingId(),
                soknad.erEttersending(),
                soknad.getAktoerId()
        );

        if (behandlingskjedeId != null && idErFraHenvendelse(behandlingskjedeId) && alleVedleggErLastetOpp(vedlegg)) {
            brukernotifikasjon.cancelNotification(behandlingskjedeId, behandlingskjedeId, soknad.erEttersending(), soknad.getAktoerId());
        }
    }

    private boolean alleVedleggErLastetOpp(List<Vedlegg> vedlegg) {
        return vedlegg.stream().noneMatch(v -> v.getInnsendingsvalg().er(Vedlegg.Status.SendesSenere));
    }

    private boolean idErFraHenvendelse(String id) {
        // behandlingsid opprettet av henvendelse består av 9 karakterer, prefix=10 [A-Z,a-z]
        // UUID eller ULID vil være 36 karakterer.
        return "10019To00".length() == id.length();
    }
}
